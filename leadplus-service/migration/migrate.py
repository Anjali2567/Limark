#!/usr/bin/env python3
"""
Fixed comprehensive Python-based migration for all MongoDB collections to PostgreSQL.
Uses correct field mappings based on actual PostgreSQL schema.
Supports batch inserts for 1000+ records/sec performance.
Properly handles camelCase to snake_case field mapping and type conversions.
"""

import os
import sys
import json
import time
import re
import hashlib
from datetime import datetime
from pymongo import MongoClient
import psycopg2
from psycopg2.extras import execute_batch, execute_values
from urllib.parse import urlparse

# Fix Unicode output on Windows
if sys.stdout.encoding and sys.stdout.encoding.lower() != 'utf-8':
    sys.stdout.reconfigure(encoding='utf-8', errors='replace')
    sys.stderr.reconfigure(encoding='utf-8', errors='replace')



def truncate_string(value, max_length=65535):
    """Truncate string values to max_length to prevent PostgreSQL varchar overflow"""
    if value is None:
        return None
    if not isinstance(value, str):
        value = str(value)
    value = value.replace('\x00', '')  # PostgreSQL rejects NUL bytes in string literals
    if len(value) > max_length:
        return value[:max_length]
    return value


def normalize_text(value, max_length=65535):
    """Normalize scalar/object values into safe varchar content."""
    if value is None:
        return None
    if isinstance(value, (dict, list)):
        value = json.dumps(value, default=str)
    elif not isinstance(value, str):
        value = str(value)
    return truncate_string(value, max_length)


def mongo_id_to_long_string(value):
    """
    Convert Mongo-style ObjectId strings to deterministic positive 64-bit integer strings.
    Keeps existing numeric values unchanged.
    """
    if value is None:
        return None
    s = str(value)
    if s.isdigit():
        return s
    if re.fullmatch(r"[0-9a-fA-F]{24}", s):
        digest = hashlib.blake2b(s.encode(), digest_size=8).digest()
        num = int.from_bytes(digest, byteorder="big") & 0x7FFFFFFFFFFFFFFF
        return str(num if num != 0 else 1)
    return s


def to_user_id_long(value):
    """Convert createdBy/updatedBy to a Long (int) suitable for a bigint column.

    Handles three cases:
      - Numeric string / int  → returned as int
      - 24-char hex ObjectId  → deterministically hashed to a positive int64
      - Anything else (e.g. legacy email/username) → None (cannot map reliably)
    """
    if value is None:
        return None
    s = str(value)
    if s.isdigit():
        return int(s)
    if re.fullmatch(r"[0-9a-fA-F]{24}", s):
        digest = hashlib.blake2b(s.encode(), digest_size=8).digest()
        num = int.from_bytes(digest, byteorder="big") & 0x7FFFFFFFFFFFFFFF
        return num if num != 0 else 1
    return None


def remap_questionnaire_ids(questionnaire_json, id_map):
    """Remap question IDs within questionnaire answers.
    
    Takes questionnaire JSON like:
      [{"questionId": "mongo_id", "answer": "text"}, ...]
    And remaps questionId values using the id_map (mongo_id -> postgres_id).
    Returns the updated JSON string or None if input is None/invalid.
    """
    if not questionnaire_json:
        return None
    
    try:
        if isinstance(questionnaire_json, str):
            data = json.loads(questionnaire_json)
        else:
            data = questionnaire_json
        
        if not isinstance(data, list):
            return None
        
        # Remap each answer's questionId
        for answer in data:
            if isinstance(answer, dict) and 'questionId' in answer:
                old_id = str(answer['questionId'])
                mapped_id = id_map.get(old_id)
                if mapped_id is not None:
                    answer['questionId'] = int(mapped_id)
        
        return json.dumps(data, default=str)
    except (json.JSONDecodeError, TypeError, ValueError):
        return None


def to_postgres_array(mongo_list, convert_object_ids=False, max_element_length=65535):
    """Convert MongoDB list to PostgreSQL array format {val1,val2,val3}

    Args:
        mongo_list: List from MongoDB or None
        max_element_length: Truncate each element to this length (default 255 for varchar(255)[])

    Returns:
        PostgreSQL array string (e.g., '{val1,val2,val3}') or None
    """
    if not mongo_list:
        return None

    if not isinstance(mongo_list, list):
        return None

    if len(mongo_list) == 0:
        return None

    # Convert each item to string and escape special characters
    items = []
    for item in mongo_list:
        if item is not None:
            raw_item = mongo_id_to_long_string(item) if convert_object_ids else item
            str_item = str(raw_item).replace('\x00', '')  # PostgreSQL rejects NUL bytes
            if max_element_length and len(str_item) > max_element_length:
                str_item = str_item[:max_element_length]
            # Escape double quotes and backslashes
            str_item = str_item.replace('\\', '\\\\').replace('"', '\\"')
            items.append(f'"{str_item}"')
    
    if not items:
        return None
    
    return '{' + ','.join(items) + '}'


class ComprehensiveMigrator:
    def __init__(self, mongo_uri, pg_uri):
        self.mongo_uri = mongo_uri
        self.pg_uri = pg_uri
        self.mongo_client = None
        self.mongo_db = None
        self.pg_conn = None
        self.pg_cursor = None
        self.stats = {
            'start_time': None,
            'end_time': None,
            'total_records': 0,
            'total_errors': 0,
            'tables_migrated': {}
        }
        self.table_errors = {}
        self.id_maps = {}
        self.bigint_columns = {}

    @staticmethod
    def enum_to_ordinal(value, enum_order):
        """Map enum string values to ordinal integers expected by smallint enum columns."""
        if value is None:
            return None
        if isinstance(value, int):
            return value
        try:
            return enum_order.index(str(value).upper())
        except ValueError:
            return None

    def connect(self):
        """Connect to MongoDB and PostgreSQL"""
        print("\n🔗 Connecting to databases...")
        try:
            self.mongo_client = MongoClient(self.mongo_uri)
            # Extract database name from URI if present
            uri_parsed = urlparse(self.mongo_uri)
            db_name = uri_parsed.path.lstrip('/').split('?')[0] or 'leadplus'
            self.mongo_db = self.mongo_client[db_name]
            self.mongo_db.command('ping')
            print("  ✓ MongoDB connected")
            
            self.pg_conn = psycopg2.connect(self.pg_uri)
            self.pg_cursor = self.pg_conn.cursor()
            self.load_bigint_columns()
            print("  ✓ PostgreSQL connected")
        except Exception as e:
            print(f"  ✗ Connection failed: {e}")
            raise

    def disconnect(self):
        """Close database connections"""
        if self.pg_conn:
            self.pg_conn.close()
        if self.mongo_client:
            self.mongo_client.close()
        print("  ✓ Connections closed")

    def register_id_map(self, pg_table, source_id, generated_id):
        if source_id is None or generated_id is None:
            return
        self.id_maps.setdefault(pg_table, {})[str(source_id)] = str(generated_id)

    def map_id(self, pg_table, value):
        if value is None:
            return None
        source_id = str(value)
        mapped = self.id_maps.get(pg_table, {}).get(source_id)
        if mapped is not None:
            return mapped
        return None

    def load_bigint_columns(self):
        """Cache bigint columns per table from PostgreSQL schema."""
        self.pg_cursor.execute("""
            SELECT table_name, column_name
            FROM information_schema.columns
            WHERE table_schema = 'public' AND data_type = 'bigint'
        """)
        rows = self.pg_cursor.fetchall()
        bigint_columns = {}
        for table_name, column_name in rows:
            bigint_columns.setdefault(table_name, set()).add(column_name)
        self.bigint_columns = bigint_columns

    @staticmethod
    def coerce_bigint_value(value):
        """Convert Mongo/ObjectId-like values into PostgreSQL bigint-compatible integers."""
        if value is None:
            return None
        if isinstance(value, bool):
            return int(value)
        if isinstance(value, int):
            return value
        if isinstance(value, float):
            return int(value)

        as_str = str(value).strip()
        if not as_str:
            return None
        if re.fullmatch(r"-?\d+", as_str):
            try:
                return int(as_str)
            except ValueError:
                return None

        converted = mongo_id_to_long_string(as_str)
        if converted is None:
            return None
        if re.fullmatch(r"-?\d+", str(converted)):
            try:
                return int(converted)
            except ValueError:
                return None
        return None

    def coerce_row_bigint_columns(self, pg_table, row):
        """Ensure bigint columns receive bigint-compatible values."""
        bigint_cols = self.bigint_columns.get(pg_table)
        if not bigint_cols:
            return row
        coerced = dict(row)
        for col in bigint_cols:
            if col in coerced:
                coerced[col] = self.coerce_bigint_value(coerced[col])
        return coerced

    def map_id_array(self, pg_table, values):
        if not values:
            return None
        mapped = [self.map_id(pg_table, value) for value in values if value is not None]
        return to_postgres_array(mapped) if mapped else None

    def remap_questionnaire_question_ids(self, questionnaire_json):
        """Remap question IDs within questionnaire using the question id_map."""
        if not questionnaire_json or 'question' not in self.id_maps:
            return questionnaire_json
        return remap_questionnaire_ids(questionnaire_json, self.id_maps['question'])

    def remap_row_references(self, pg_table, row):
        """Remap known foreign key-like fields to generated PostgreSQL ids."""
        remap_rules = {
            'tenant': {
                'owner_id': 'tenant_user',
            },
            'workspace': {
                'tenant_id': 'tenant',
                'owner_id': 'tenant_user',
            },
            'tenant_user': {
                'workspace_id': 'workspace',
                'tenant_id': 'tenant',
            },
            'vendor': {
                'tenant_id': 'tenant',
                'user_id': 'tenant_user',
                'industry_ids': ('industry', True),
                'service_ids': ('service', True),
                'specification_ids': ('specification', True),
            },
            'workspace_user': {
                'user_id': 'tenant_user',
                'workspace_id': 'workspace',
                'tenant_id': 'tenant',
            },
            'mailbox': {
                'workspace_id': 'workspace',
                'user_id': 'tenant_user',
            },
            'tenant_company': {
                'tenant_id': 'tenant',
            },
            'lead_contact': {
                'lead_company_id': 'lead_company',
                'owner_id': 'tenant_user',
            },
            'lead_note': {
                'workspace_id': 'workspace',
                'tenant_id': 'tenant',
            },
            'lead_list': {
                'workspace_id': 'workspace',
                'tenant_id': 'tenant',
            },
            'tenant_contact': {
                'tenant_id': 'tenant',
                'company_id': 'lead_company',
            },
            'campaign': {
                'workspace_id': 'workspace',
                'tenant_id': 'tenant',
            },
            'campaign_contact': {
                'campaign_id': 'campaign',
                'contact_id': 'lead_contact',
            },
            'campaign_email': {
                'campaign_id': 'campaign',
            },
            'tenant_announcement': {
                'tenant_id': 'tenant',
            },
            'tenant_announcement_contact': {
                'announcement_id': 'tenant_announcement',
            },
            'tenant_lead_filter': {
                'tenant_id': 'tenant',
            },
            'user_activity_log': {
                'tenant_id': 'tenant',
                'workspace_id': 'workspace',
                'user_id': 'tenant_user',
            },
            'vendor_agreement': {
                'vendor_id': 'vendor',
            },
            'vendor_data_pack': {
                'vendor_id': 'vendor',
                'tenant_id': 'tenant',
            },
            'vendor_showcase': {
                'vendor_id': 'vendor',
                'tenant_id': 'tenant',
            },
            'question': {
                'question_section_id': 'question_section',
                'industry_ids': ('industry', True),
            },
            'apollo_company_data': {
                'lead_company_id': 'lead_company',
                'specification_id': 'apollo_specification',
            },
            'apollo_contact_data': {
                'lead_contact_id': 'lead_contact',
                'specification_id': 'apollo_specification',
            },
            'attachment_library': {
                'workspace_id': 'workspace',
            },
            'collaborator': {
                'user_id': 'tenant_user',
            },
            'contact_email': {
                'tenant_id': 'tenant',
                'workspace_id': 'workspace',
                'campaign_id': 'campaign',
                'contact_id': 'lead_contact',
            },
            'contact_outreach_status': {
                'contact_id': 'lead_contact',
                'tenant_id': 'tenant',
                'current_campaign_ids': ('campaign', True),
            },
            'email_sequence_template': {
                'tenant_id': 'tenant',
            },
            'feedback': {
                'tenant_id': 'tenant',
                'workspace_id': 'workspace',
                'user_id': 'tenant_user',
            },
            'industry_service_mapping': {
                'industry_id': 'industry',
                'service_id': 'service',
            },
            'service': {
                'service_category_id': 'service_category',
            },
            'specification': {
                'specification_category_id': 'specification_category',
            },
            'service_specification': {
                'service_id': 'service',
                'specification_id': 'specification',
            },
            'lead_company_event': {
                'lead_company_id': 'lead_company',
            },
            'lead_contact_event': {
                'tenant_id': 'tenant',
                'workspace_id': 'workspace',
                'contact_id': 'lead_contact',
            },
            'lead_contact_normalized_title': {
                'lead_contact_id': 'lead_contact',
            },
            'lead_data_pack': {
                'industry_ids': ('industry', True),
            },
            'lead_company_job': {
                'lead_company_id': 'lead_company',
            },
            'campaign_chat_memory': {
                'campaign_id': 'campaign',
                'tenant_id': 'tenant',
                'workspace_id': 'workspace',
            },
            'refresh_token': {
                'user_id': 'tenant_user',
            },
            'request_for_proposal': {
                'user_id': 'tenant_user',
            },
            'request_for_quote': {
                'user_id': 'tenant_user',
            },
            'message': {
                'user_id': 'tenant_user',
                'tenant_id': 'tenant',
                'workspace_id': 'workspace',
                'campaign_id': 'campaign',
            },
        }

        rules = remap_rules.get(pg_table, {})
        remapped = {}
        for col, val in row.items():
            rule = rules.get(col)
            if rule is None:
                if pg_table == 'lead_list' and col == 'source_ids' and isinstance(val, list):
                    lead_type = str(row.get('type') or '').upper()
                    target_table = 'lead_company' if lead_type == 'LEAD_COMPANY' else 'lead_contact'
                    remapped[col] = self.map_id_array(target_table, val)
                    continue
                if pg_table == 'lead_note' and col == 'source_id':
                    lead_type = str(row.get('type') or '').upper()
                    target_table = 'lead_company' if lead_type == 'LEAD_COMPANY' else 'lead_contact'
                    remapped[col] = self.map_id(target_table, val)
                    continue
                remapped[col] = val
                continue
            if isinstance(rule, tuple) and rule[1] is True:
                remapped[col] = self.map_id_array(rule[0], val)
            else:
                # Try to map the id; if mapping not yet available preserve the
                # original source id so deferred update methods can resolve it
                mapped_val = self.map_id(rule, val)
                remapped[col] = mapped_val if mapped_val is not None else (str(val) if val is not None else None)

        # Remap audit columns to actual user IDs (applies to every table).
        # The raw MongoDB ObjectId string stored in created_by/updated_by is
        # looked up against the tenant_user id_map built when users are migrated.
        # Returns None when the user hasn't been migrated yet (handled by
        # update_deferred_audit_ids) or the original value was an email/username.
        for audit_col in ('created_by', 'updated_by'):
            if audit_col in remapped and remapped[audit_col] is not None:
                remapped[audit_col] = self.map_id('tenant_user', remapped[audit_col])

        return remapped

    def update_deferred_owner_ids(self):
        """Resolve tenant/workspace owner ids after users have been migrated."""
        if 'tenant_user' not in self.id_maps:
            return

        # Tenant: re-query MongoDB to map original ownerId → pg tenant_user id.
        # Reading owner_id back from PG won't work because the bigint coercion
        # hashes the raw ObjectId, making the id_map lookup fail.
        if 'tenant' in self.id_maps:
            updated = 0
            for doc in self.mongo_db['tenants'].find(
                {'ownerId': {'$exists': True, '$ne': None}},
                {'_id': 1, 'ownerId': 1}
            ):
                pg_tenant_id = self.map_id('tenant', str(doc.get('_id')))
                if pg_tenant_id is None:
                    continue
                pg_owner_id = self.map_id('tenant_user', str(doc.get('ownerId')))
                if pg_owner_id is not None:
                    self.pg_cursor.execute(
                        "UPDATE tenant SET owner_id = %s WHERE id = %s",
                        (pg_owner_id, pg_tenant_id)
                    )
                    updated += 1
            self.pg_conn.commit()
            if updated:
                print(f"  ✓ tenant: resolved {updated} owner_id references from MongoDB")

            # Fallback for tenants still missing owner_id: match by user email domain.
            # Mirrors how TenantService derives the owner from the user's email domain.
            self.pg_cursor.execute("""
                UPDATE tenant t
                SET owner_id = (
                    SELECT u.id FROM tenant_user u
                    WHERE u.tenant_id = t.id
                      AND LOWER(SPLIT_PART(u.email, '@', 2)) = LOWER(t.domain)
                    ORDER BY u.id
                    LIMIT 1
                )
                WHERE t.owner_id IS NULL AND t.domain IS NOT NULL
            """)
            fallback_updated = self.pg_cursor.rowcount
            self.pg_conn.commit()
            if fallback_updated:
                print(f"  ✓ tenant: domain-matched owner_id for {fallback_updated} tenants")

        # Workspace: re-query MongoDB to map original ownerId → pg tenant_user id.
        # Workspaces are migrated before users, so owner_id initially contains the hashed MongoDB ID.
        # After users are migrated, we need to remap to the actual tenant_user IDs based on email domain.
        if 'workspace' in self.id_maps and 'tenant_user' in self.id_maps:
            updated = 0
            for doc in self.mongo_db['workspaces'].find(
                {'ownerId': {'$exists': True, '$ne': None}},
                {'_id': 1, 'ownerId': 1, 'tenantId': 1}
            ):
                mongo_workspace_id = str(doc.get('_id'))
                mongo_tenant_id = str(doc.get('tenantId'))
                
                pg_workspace_id = self.map_id('workspace', mongo_workspace_id)
                pg_tenant_id = self.map_id('tenant', mongo_tenant_id)
                
                if pg_workspace_id is None or pg_tenant_id is None:
                    continue
                
                # Get tenant domain
                self.pg_cursor.execute("SELECT domain FROM tenant WHERE id = %s", (pg_tenant_id,))
                tenant_row = self.pg_cursor.fetchone()
                
                if not tenant_row or not tenant_row[0]:
                    continue
                
                tenant_domain = tenant_row[0]
                
                # Find user whose email domain matches tenant domain
                self.pg_cursor.execute("""
                    SELECT u.id FROM tenant_user u
                    WHERE u.tenant_id = %s
                      AND LOWER(SPLIT_PART(u.email, '@', 2)) = LOWER(%s)
                    ORDER BY u.id
                    LIMIT 1
                """, (pg_tenant_id, tenant_domain))
                
                user_row = self.pg_cursor.fetchone()
                
                if user_row:
                    pg_owner_id = user_row[0]
                    self.pg_cursor.execute(
                        "UPDATE workspace SET owner_id = %s WHERE id = %s",
                        (pg_owner_id, pg_workspace_id)
                    )
                    updated += 1
            
            self.pg_conn.commit()
            if updated:
                print(f"  ✓ workspace: resolved {updated} owner_id references from MongoDB")

    def update_deferred_audit_ids(self):
        """Fix created_by/updated_by on tables migrated before tenant_user.

        workspace is migrated in TIER 1 before users, so its audit columns
        receive NULL during the initial pass (map_id returns None when
        id_maps['tenant_user'] is not yet populated).  Re-query MongoDB and
        apply the correct Postgres user IDs now that the user map is ready.
        """
        if 'tenant_user' not in self.id_maps or 'workspace' not in self.id_maps:
            return

        updated = 0
        for doc in self.mongo_db['workspaces'].find(
            {'$or': [{'createdBy': {'$exists': True}}, {'updatedBy': {'$exists': True}}]},
            {'_id': 1, 'createdBy': 1, 'updatedBy': 1}
        ):
            src_id = str(doc.get('_id'))
            pg_ws_id = self.map_id('workspace', src_id)
            if pg_ws_id is None:
                continue

            created_by = self.map_id('tenant_user', str(doc['createdBy'])) if doc.get('createdBy') else None
            updated_by = self.map_id('tenant_user', str(doc['updatedBy'])) if doc.get('updatedBy') else None

            if created_by is not None or updated_by is not None:
                self.pg_cursor.execute(
                    "UPDATE workspace SET created_by = COALESCE(%s, created_by), "
                    "updated_by = COALESCE(%s, updated_by) WHERE id = %s",
                    (created_by, updated_by, pg_ws_id)
                )
                updated += 1

        self.pg_conn.commit()
        if updated:
            print(f"  ✓ workspace: back-filled audit ids for {updated} rows")

    def update_vendor_questionnaires_with_remapped_questions(self):
        """Update vendor questionnaires with remapped question IDs after questions are migrated.

        Vendors are migrated before questions (TIER 2 vs TIER 7), so questionnaires
        initially contain old MongoDB ObjectIds for questionIds. After questions are
        migrated and id_maps['question'] is populated, this method remaps them.
        """
        if 'question' not in self.id_maps or 'vendor' not in self.id_maps:
            return

        print("\n  ↻ Post-processing: Remapping question IDs in vendor questionnaires...")

        updated = 0
        try:
            # Get all vendors with questionnaire data
            self.pg_cursor.execute("SELECT id, questionnaire FROM vendor WHERE questionnaire IS NOT NULL")
            rows = self.pg_cursor.fetchall()

            for vendor_id, questionnaire_json in rows:
                if not questionnaire_json:
                    continue

                # Remap the question IDs using the id_maps
                remapped = remap_questionnaire_ids(questionnaire_json, self.id_maps['question'])

                if remapped and remapped != questionnaire_json:
                    self.pg_cursor.execute(
                        "UPDATE vendor SET questionnaire = %s WHERE id = %s",
                        (remapped, vendor_id)
                    )
                    updated += 1

            self.pg_conn.commit()
            if updated:
                print(f"  ✓ vendor: remapped question IDs in {updated} questionnaires")
        except Exception as e:
            self.pg_conn.rollback()
            print(f"  ⚠ Error remapping vendor questionnaires: {str(e)}")

    def migrate_generic_table(self, mongo_collection, pg_table, field_mapper=None):
        """Generic migration with custom field mapping"""
        collection = self.mongo_db[mongo_collection]
        count = collection.count_documents({})

        if count == 0:
            print(f"  ⊘ {mongo_collection}: 0 documents (skipped)")
            return 0

        print(f"  ↻ Processing {mongo_collection}: {count:,} documents")

        batch_size = 5000
        total_inserted = 0
        start_time = time.time()

        try:
            for skip in range(0, count, batch_size):
                batch = list(collection.find({}).skip(skip).limit(batch_size))
                if not batch:
                    break

                rows = []
                source_ids = []
                for doc in batch:
                    try:
                        if field_mapper:
                            row = field_mapper(doc)
                        else:
                            # Simple 1:1 mapping
                            row = {
                                **{k: v for k, v in doc.items() if k != '_id'}
                            }
                        row = self.remap_row_references(pg_table, row)
                        row.pop('id', None)
                        row = self.coerce_row_bigint_columns(pg_table, row)
                        rows.append(row)
                        source_ids.append(str(doc.get('_id')) if doc.get('_id') is not None else None)
                    except Exception as e:
                        self.stats['total_errors'] += 1
                        if pg_table not in self.table_errors:
                            self.table_errors[pg_table] = f"row mapping failed: {str(e)}"
                            print(f"    ⚠ {pg_table} row mapping failed: {str(e)[:200]}")
                        continue

                if rows:
                    # Build SQL dynamically
                    columns = list(rows[0].keys())
                    sql = f"INSERT INTO {pg_table} ({', '.join(columns)}) VALUES %s RETURNING id"

                    try:
                        values = [tuple(row.get(col) for col in columns) for row in rows]
                        returned = execute_values(self.pg_cursor, sql, values, page_size=1000, fetch=True)
                        self.pg_conn.commit()
                        total_inserted += len(rows)
                        if returned:
                            for source_id, returned_row in zip(source_ids, returned):
                                generated_id = returned_row[0] if isinstance(returned_row, (tuple, list)) else returned_row
                                self.register_id_map(pg_table, source_id, generated_id)
                    except Exception as e:
                        self.pg_conn.rollback()
                        self.stats['total_errors'] += len(rows)
                        if pg_table not in self.table_errors:
                            self.table_errors[pg_table] = str(e)
                            print(f"    ⚠ {pg_table} batch insert failed: {str(e)[:200]}")
            
            elapsed = time.time() - start_time
            speed = total_inserted / elapsed if elapsed > 0 else 0
            print(f"  ✓ {mongo_collection}: {total_inserted:,} rows migrated in {elapsed:.1f}s ({speed:.0f} rec/sec)")
            
            self.stats['tables_migrated'][mongo_collection] = total_inserted
            self.stats['total_records'] += total_inserted
            return total_inserted
            
        except Exception as e:
            print(f"  ✗ {mongo_collection}: ERROR: {str(e)[:80]}")
            self.stats['total_errors'] += count
            return 0

    def migrate_all(self):
        """Execute full migration with correct field mappings"""
        self.stats['start_time'] = time.time()
        
        print("\n╔══════════════════════════════════════════════════════════════╗")
        print("║  COMPREHENSIVE MIGRATION: All Collections (Fixed Python)     ║")
        print("║  Fast Batch Inserts - ~1000+ records/sec                     ║")
        print("╚══════════════════════════════════════════════════════════════╝")

        # TIER 1: Core
        print("\n📦 TIER 1: Core Entities")
        self.migrate_generic_table('tenants', 'tenant',
            lambda d: {
                'id': str(d.get('_id')),
                'name': d.get('name'),
                'domain': d.get('domain'),
                'owner_id': d.get('ownerId'),
                'modules': ('{' + ','.join(str(m) for m in d.get('modules')) + '}') if d.get('modules') else None,
                'zoho_user_id': d.get('zohoUserId') or d.get('zoho_user_id'),
                'zoho_email': d.get('zohoEmail') or d.get('zoho_email'),
                'zoho_refresh_token': d.get('zohoRefreshToken') or d.get('zoho_refresh_token'),
                'zoho_connected_at': d.get('zohoConnectedAt') or d.get('zoho_connected_at'),
                'hubspot_user_id': d.get('hubspotUserId') or d.get('hubspot_user_id'),
                'hubspot_email': d.get('hubspotEmail') or d.get('hubspot_email'),
                'hubspot_refresh_token': d.get('hubspotRefreshToken') or d.get('hubspot_refresh_token'),
                'hubspot_connected_at': d.get('hubspotConnectedAt') or d.get('hubspot_connected_at'),
                'announcement_from_email': normalize_text(d.get('announcementFromEmail') or d.get('announcement_from_email')),
                'announcement_sender_name': normalize_text(d.get('announcementSenderName') or d.get('announcement_sender_name')),
                'announcement_type': normalize_text(d.get('announcementType') or d.get('announcement_type')),
                'announcement_meta_data': truncate_string(json.dumps(v, default=str) if (v := (d.get('announcementMetaData') or d.get('announcement_meta_data'))) is not None else None),
                'cc_recipients': truncate_string(json.dumps(v, default=str) if (v := (d.get('ccRecipients') or d.get('cc_recipients'))) is not None else None),
                'bcc_recipients': truncate_string(json.dumps(v, default=str) if (v := (d.get('bccRecipients') or d.get('bcc_recipients'))) is not None else None),
                'created_at': d.get('createdAt'),
                'updated_at': d.get('updatedAt')
            }
        )

        self.migrate_generic_table('workspaces', 'workspace',
            lambda d: {
                'id': str(d.get('_id')),
                'name': truncate_string(d.get('name')),
                'tenant_id': d.get('tenantId'),
                'owner_id': d.get('ownerId'),
                'daily_send_limit': d.get('dailySendLimit', 30),
                'created_at': d.get('createdAt'),
                'updated_at': d.get('updatedAt'),
                'created_by': d.get('createdBy') or d.get('created_by'),
                'updated_by': d.get('updatedBy') or d.get('updated_by'),
                'cc_recipients': normalize_text(d.get('ccRecipients') or d.get('cc_recipients')),
                'bcc_recipients': normalize_text(d.get('bccRecipients') or d.get('bcc_recipients'))
            }
        )

        # Special handling for 'user' table (reserved keyword)
        self.migrate_generic_table('users', 'tenant_user',
            lambda d: {
                'id': str(d.get('_id')),
                'email': d.get('email'),
                'name': d.get('name'),
                'password': d.get('password'),
                'phone_number': d.get('phoneNumber'),
                'company': d.get('company'),
                'active': d.get('active', False),
                'draft': d.get('draft', False),
                'email_verified': d.get('emailVerified', False),
                'status': d.get('status'),
                'workspace_id': d.get('workspaceId'),
                'tenant_id': d.get('tenantId'),
                'roles': to_postgres_array(d.get('roles')),
                'identity_providers': json.dumps(d.get('identityProviders'), default=str) if d.get('identityProviders') else None,
                'email_verification_token': d.get('emailVerificationToken'),
                'verification_token': d.get('verificationToken'),
                'created_at': d.get('createdAt'),
                'updated_at': d.get('updatedAt'),
                'last_login_at': d.get('lastLoginAt')
            }
        )
        self.update_deferred_owner_ids()
        self.update_deferred_audit_ids()

        self.migrate_generic_table('industries', 'industry',
            lambda d: {
                'id': str(d.get('_id')),
                'name': d.get('name'),
                'slug': normalize_text(d.get('slug')),
                'description': normalize_text(d.get('description')),
                'image': normalize_text(d.get('image')),
                'active': d.get('active', True),
                'disabled': d.get('disabled', False),
                'segments': to_postgres_array(d.get('segments')),
                'created_at': d.get('createdAt'),
                'updated_at': d.get('updatedAt'),
                'created_by': d.get('createdBy') or d.get('created_by'),
                'updated_by': d.get('updatedBy') or d.get('updated_by')
            }
        )

        # TIER 2: Reference Data
        print("\n📦 TIER 2: Reference Data")
        self.migrate_generic_table('lead_companies', 'lead_company',
            lambda d: {
                'id': str(d.get('_id')),
                'name': truncate_string(d.get('name')),
                'domain': truncate_string(d.get('domain')),
                'industry': truncate_string(d.get('industry')),
                'active': d.get('active', True),
                'is_target_account': d.get('isTargetAccount', False),
                'score': d.get('score', 0),
                'employee_count': truncate_string(d.get('employeeCount')),
                'employee_range': truncate_string(d.get('employeeRange')),
                'revenue_usd': d.get('revenueUsd') or d.get('revenue_usd'),
                'revenue_usd_amount': d.get('revenueUsdAmount') or d.get('revenue_usd_amount'),
                'hq_city': truncate_string(d.get('hqCity')),
                'hq_state': truncate_string(d.get('hqState')),
                'hq_country': truncate_string(d.get('hqCountry')),
                'postal_code': truncate_string(d.get('postalCode')),
                'phone_number': truncate_string(d.get('phoneNumber')),
                'website_url': truncate_string(d.get('websiteUrl')),
                'linkedin_url': truncate_string(d.get('linkedinUrl')),
                'twitter_url': truncate_string(d.get('twitterUrl')),
                'facebook_url': truncate_string(d.get('facebookUrl')),
                'logo_url': truncate_string(d.get('logoUrl')),
                'publicly_traded_symbol': truncate_string(d.get('publiclyTradedSymbol')),
                'account_summary': truncate_string(d.get('accountSummary')),
                'icp_tag': truncate_string(d.get('icpTag')),
                'region': truncate_string(d.get('region')),
                'territory': truncate_string(d.get('territory')),
                'segment': truncate_string(d.get('segment')),
                'source': truncate_string(d.get('source')),
                'lead_company_status': truncate_string(d.get('leadCompanyStatus') or d.get('lead_company_status')),
                'salesperson_id': truncate_string(d.get('salespersonId')),
                'salesperson_name': truncate_string(d.get('salespersonName')),
                'salesperson_assign_at': d.get('salespersonAssignAt') or d.get('salesperson_assign_at'),
                'zoho_account_id': truncate_string(d.get('zohoAccountId') or d.get('zoho_account_id')),
                'keywords': to_postgres_array(d.get('keywords')),
                'technologies': to_postgres_array(d.get('technologies')),
                'scraped_technologies': to_postgres_array(d.get('scrapedTechnologies')),
                'scraped_services': to_postgres_array(d.get('scrapedServices')),
                'scraped_tools': to_postgres_array(d.get('scrapedTools')),
                'naics_codes': to_postgres_array(d.get('naicsCodes')),
                'sic_codes': to_postgres_array(d.get('sicCodes')),
                'created_at': d.get('createdAt'),
                'updated_at': d.get('updatedAt')
            }
        )

        self.migrate_generic_table('lead_company_events', 'lead_company_event',
            lambda d: {
                'lead_company_id': d.get('leadCompanyId') or d.get('lead_company_id'),
                'type': truncate_string(d.get('type')),
                'title': truncate_string(d.get('title')),
                'summary': truncate_string(d.get('summary')),
                'url': truncate_string(d.get('url')),
                'source': truncate_string(d.get('source')),
                'sentiment': d.get('sentiment'),
                'published_at': d.get('publishedAt') or d.get('published_at'),
                'detected_at': d.get('detectedAt') or d.get('detected_at'),
                'unique_hash': truncate_string(d.get('uniqueHash') or d.get('unique_hash')),
                'active': d.get('active', True),
            }
        )

        self.migrate_generic_table('lead_company_jobs', 'lead_company_job',
            lambda d: {
                'lead_company_id': d.get('leadCompanyId') or d.get('lead_company_id'),
                'type': truncate_string(d.get('type'), 255),
                'title': truncate_string(d.get('title'), 255),
                'skills': to_postgres_array(d.get('skills'), max_element_length=255),
                'job_url': normalize_text(d.get('jobUrl') or d.get('job_url')),
                'apply_url': normalize_text(d.get('applyUrl') or d.get('apply_url')),
                'benefits': to_postgres_array(d.get('benefits'), max_element_length=255),
                'location': truncate_string(d.get('location'), 255),
                'department': truncate_string(d.get('department'), 255),
                'posted_date': d.get('postedDate') or d.get('posted_date'),
                'description': normalize_text(d.get('description')),
                'requirements': to_postgres_array(d.get('requirements'), max_element_length=255),
                'technologies': to_postgres_array(d.get('technologies'), max_element_length=255),
                'tools': to_postgres_array(d.get('tools'), max_element_length=255),
                'services': to_postgres_array(d.get('services'), max_element_length=255),
                'active': d.get('active', True),
                'created_at': d.get('createdAt') or d.get('created_at'),
                'updated_at': d.get('updatedAt') or d.get('updated_at'),
            }
        )

        self.migrate_generic_table('vendors', 'vendor',
            lambda d: {
                'id': str(d.get('_id')),
                'active': d.get('active', True),
                'created_at': d.get('createdAt'),
                'updated_at': d.get('updatedAt'),
                'address': normalize_text(d.get('address')),
                'annual_revenue': normalize_text(d.get('annualRevenue')),
                'business_hours': normalize_text(d.get('businessHours')),
                'company_name': normalize_text(d.get('companyName')),
                'company_size': normalize_text(d.get('companySize')),
                'description': normalize_text(d.get('description')),
                'fax_number': normalize_text(d.get('faxNumber')),
                'logo': normalize_text(d.get('logo')),
                'phone_number': normalize_text(d.get('phoneNumber')),
                'review_comment': normalize_text(d.get('reviewComment')),
                'tagline': normalize_text(d.get('tagline')),
                'tenant_id': d.get('tenantId'),
                'user_id': d.get('userId'),
                'vendor_verification_status': normalize_text(d.get('vendorVerificationStatus')),
                'website': normalize_text(d.get('website')),
                'year_established': normalize_text(d.get('yearEstablished')),
                'questionnaire': normalize_text(d.get('questionnaire')),
                'certifications': to_postgres_array(d.get('certifications')),
                'industry_ids': d.get('industryIds'),
                'regions_covered': to_postgres_array(d.get('regionsCovered')),
                'service_ids': d.get('serviceIds'),
                'specification_ids': d.get('specificationIds')
            }
        )

        self.migrate_generic_table('vendor_agreements', 'vendor_agreement',
            lambda d: {
                'vendor_id': truncate_string(d.get('vendorId') or d.get('vendor_id')),
                'agreement_type': truncate_string(d.get('agreementType') or d.get('agreement_type')),
                'version': d.get('version') or 0,
                'agreement_text': truncate_string(d.get('agreementText') or d.get('agreement_text')),
                'otp': json.dumps(v, default=str) if (v := d.get('otp')) is not None else None,
                'verified': d.get('verified', False),
                'signed': d.get('signed', False),
                'signed_by': truncate_string(d.get('signedBy') or d.get('signed_by')),
                'name': truncate_string(d.get('name')),
                'title': truncate_string(d.get('title')),
                'signed_at': d.get('signedAt') or d.get('signed_at'),
                'created_at': d.get('createdAt') or d.get('created_at'),
                'updated_at': d.get('updatedAt') or d.get('updated_at'),
            }
        )

        self.migrate_generic_table('vendor_data_packs', 'vendor_data_pack',
            lambda d: {
                'vendor_id': truncate_string(d.get('vendorId') or d.get('vendor_id')),
                'tenant_id': truncate_string(d.get('tenantId') or d.get('tenant_id')),
                'lead_data_pack_id': truncate_string(d.get('leadDataPackId') or d.get('lead_data_pack_id')),
                'active': d.get('active', True),
                'assigned_by': truncate_string(d.get('assignedBy') or d.get('assigned_by')),
                'created_by': d.get('createdBy') or d.get('created_by'),
                'created_at': d.get('createdAt') or d.get('created_at'),
                'updated_by': d.get('updatedBy') or d.get('updated_by'),
                'updated_at': d.get('updatedAt') or d.get('updated_at'),
            }
        )

        self.migrate_generic_table('vendor_showcases', 'vendor_showcase',
            lambda d: {
                'vendor_id': truncate_string(d.get('vendorId') or d.get('vendor_id')),
                'tenant_id': truncate_string(d.get('tenantId') or d.get('tenant_id')),
                'project_name': truncate_string(d.get('projectName') or d.get('project_name')),
                'client_name': truncate_string(d.get('clientName') or d.get('client_name')),
                'description': truncate_string(d.get('description')),
                'service_ids': to_postgres_array(d.get('serviceIds') or d.get('service_ids')),
                'duration': truncate_string(d.get('duration')),
                'results_and_outcomes': truncate_string(d.get('resultsAndOutcomes') or d.get('results_and_outcomes')),
                'active': d.get('active', True),
                'created_by': d.get('createdBy') or d.get('created_by'),
                'created_at': d.get('createdAt') or d.get('created_at'),
                'updated_by': d.get('updatedBy') or d.get('updated_by'),
                'updated_at': d.get('updatedAt') or d.get('updated_at'),
            }
        )

        self.migrate_generic_table('service_categories', 'service_category',
            lambda d: {
                'name': truncate_string(d.get('name')),
                'active': d.get('active', True),
                'created_by': d.get('createdBy') or d.get('created_by'),
                'created_at': d.get('createdAt') or d.get('created_at'),
                'updated_by': d.get('updatedBy') or d.get('updated_by'),
                'updated_at': d.get('updatedAt') or d.get('updated_at'),
            }
        )

        self.migrate_generic_table('specification_categories', 'specification_category',
            lambda d: {
                'name': truncate_string(d.get('name')),
                'type': truncate_string(d.get('type')),
                'active': d.get('active', True),
                'created_by': d.get('createdBy') or d.get('created_by'),
                'created_at': d.get('createdAt') or d.get('created_at'),
                'updated_by': d.get('updatedBy') or d.get('updated_by'),
                'updated_at': d.get('updatedAt') or d.get('updated_at'),
            }
        )

        self.migrate_generic_table('services', 'service',
            lambda d: {
                'id': str(d.get('_id')),
                'name': d.get('name'),
                'slug': normalize_text(d.get('slug')),
                'service_category_id': normalize_text(d.get('serviceCategoryId') or d.get('service_category_id')),
                'disabled': d.get('disabled', False),
                'active': d.get('active', True),
                'created_by': d.get('createdBy') or d.get('created_by'),
                'updated_by': d.get('updatedBy') or d.get('updated_by'),
                'created_at': d.get('createdAt'),
                'updated_at': d.get('updatedAt')
            }
        )

        self.migrate_generic_table('specifications', 'specification',
            lambda d: {
                'id': str(d.get('_id')),
                'name': d.get('name'),
                'type': normalize_text(d.get('type')),
                'icon': normalize_text(d.get('icon')),
                'specification_category_id': normalize_text(d.get('specificationCategoryId') or d.get('specification_category_id')),
                'disabled': d.get('disabled', False),
                'active': d.get('active', True),
                'created_by': d.get('createdBy') or d.get('created_by'),
                'updated_by': d.get('updatedBy') or d.get('updated_by'),
                'created_at': d.get('createdAt'),
                'updated_at': d.get('updatedAt')
            }
        )

        self.migrate_generic_table('service_specifications', 'service_specification',
            lambda d: {
                'service_id': truncate_string(d.get('serviceId') or d.get('service_id')),
                'specification_id': truncate_string(d.get('specificationId') or d.get('specification_id')),
            }
        )

        self.migrate_generic_table('industry_service_mappings', 'industry_service_mapping',
            lambda d: {
                'industry_id': d.get('industryId') or d.get('industry_id'),
                'service_id': d.get('serviceId') or d.get('service_id'),
            }
        )

        # TIER 3: Workspace Related
        print("\n📦 TIER 3: Workspace-Related")
        self.migrate_generic_table('workspace_users', 'workspace_user',
            lambda d: {
                'id': str(d.get('_id')),
                'user_id': d.get('userId'),
                'workspace_id': d.get('workspaceId'),
                'tenant_id': d.get('tenantId'),
                'workspace_user_role': d.get('workspaceUserRole'),
                'workspace_user_status': d.get('workspaceUserStatus'),
                'active': d.get('active', True),
                'invitation_token': d.get('invitationToken'),
                'created_at': d.get('createdAt'),
                'updated_at': d.get('updatedAt'),
                'created_by': d.get('createdBy') or d.get('created_by'),
                'updated_by': d.get('updatedBy') or d.get('updated_by')
            }
        )

        self.migrate_generic_table('mailboxes', 'mailbox',
            lambda d: {
                'id': str(d.get('_id')),
                'workspace_id': d.get('workspaceId'),
                'user_id': d.get('userId'),
                'email_address': d.get('emailAddress') or d.get('name'),
                'emails_sent_today': d.get('emailsSentToday', 0),
                'token_expired': d.get('tokenExpired', False),
                'active': d.get('active', True),
                'type': d.get('type'),
                'meta_data': normalize_text(d.get('metaData')),
                'created_at': d.get('createdAt'),
                'updated_at': d.get('updatedAt')
            }
        )
        
        self.migrate_generic_table('tenant_companies', 'tenant_company',
            lambda d: {
                'id': str(d.get('_id')),
                'tenant_id': d.get('tenantId'),
                'source_type': normalize_text(d.get('sourceType') or d.get('source_type')),
                'source_id': normalize_text(d.get('sourceId') or d.get('source_id')),
                'name': d.get('name'),
                'created_at': d.get('createdAt'),
                'updated_at': d.get('updatedAt')
            }
        )

        self.migrate_generic_table('quotations', 'quotation',
            lambda d: {
                'id': str(d.get('_id')),
                'source_id': truncate_string(d.get('sourceId') or d.get('source_id')),
                'tenant_id': truncate_string(d.get('tenantId') or d.get('tenant_id')),
                'workspace_id': truncate_string(d.get('workspaceId') or d.get('workspace_id')),
                'title': truncate_string(d.get('title')),
                'description': truncate_string(d.get('description')),
                'budget': truncate_string(d.get('budget')),
                'deadline': d.get('deadline'),
                'items': json.dumps(d.get('items'), default=str) if d.get('items') else None,
                'payment_terms': truncate_string(d.get('paymentTerms') or d.get('payment_terms')),
                'deliverables': to_postgres_array(d.get('deliverables')),
                'status': truncate_string(d.get('status')),
                'source_type': truncate_string(d.get('sourceType') or d.get('source_type')),
                'vendor_id': truncate_string(d.get('vendorId') or d.get('vendor_id')),
                'active': d.get('active', True),
                'created_by': d.get('createdBy') or d.get('created_by'),
                'created_at': d.get('createdAt') or d.get('created_at'),
                'updated_by': d.get('updatedBy') or d.get('updated_by'),
                'updated_at': d.get('updatedAt') or d.get('updated_at'),
            }
        )

        # TIER 4: Lead/Contact Data
        print("\n📦 TIER 4: Contact/Lead Data")
        self.migrate_generic_table('lead_contacts', 'lead_contact',
            lambda d: {
                'id': str(d.get('_id')),
                'active': d.get('active', True),
                'apollo_enriched': d.get('apolloEnriched', False),
                'do_not_contact': d.get('doNotContact', False),
                'persona_score': d.get('personaScore'),
                'created_at': d.get('createdAt'),
                'updated_at': d.get('updatedAt'),
                'apollo_id': truncate_string(d.get('apolloId')),
                'consent_status': truncate_string(d.get('consentStatus')),
                'data_source': truncate_string(d.get('dataSource')),
                'department': truncate_string(d.get('department')),
                'email': truncate_string(d.get('email')),
                'email_status': truncate_string(d.get('emailStatus')),
                'first_name': truncate_string(d.get('firstName')),
                'first_name_normalized': truncate_string(d.get('firstNameNormalized')),
                'full_name': truncate_string(d.get('fullName')),
                'last_name': truncate_string(d.get('lastName')),
                'last_name_normalized': truncate_string(d.get('lastNameNormalized')),
                'lead_company_id': d.get('leadCompanyId') or d.get('lead_company_id'),
                'linkedin_url': truncate_string(d.get('linkedinUrl')),
                'title': truncate_string(d.get('title')),
                'seniority': truncate_string(d.get('seniority')),
                'phonee164': truncate_string(d.get('phoneE164') or d.get('phonee164')),
                'location_city': truncate_string(d.get('locationCity')),
                'location_state': truncate_string(d.get('locationState')),
                'location_country': truncate_string(d.get('locationCountry')),
                'location_zip': truncate_string(d.get('locationZip')),
                'owner_id': truncate_string(d.get('ownerId')),
                'source': truncate_string(d.get('source')),
                'notes': truncate_string(d.get('notes')),
                'persona_match': truncate_string(d.get('personaMatch')),
                'normalized_title_tokens': to_postgres_array(d.get('normalizedTitleTokens'))
            }
        )
        
        self.migrate_generic_table('lead_contact_events', 'lead_contact_event',
            lambda d: {
                'tenant_id': d.get('tenantId') or d.get('tenant_id'),
                'workspace_id': d.get('workspaceId') or d.get('workspace_id'),
                'contact_id': d.get('contactId') or d.get('contact_id'),
                'category': truncate_string(d.get('category')),
                'type': truncate_string(d.get('type')),
                'description': truncate_string(d.get('description')),
                'source_id': truncate_string(d.get('sourceId') or d.get('source_id')),
                'source_type': truncate_string(d.get('sourceType') or d.get('source_type')),
                'event_by': truncate_string(d.get('eventBy') or d.get('event_by')),
                'event_at': d.get('eventAt') or d.get('event_at'),
                'active': d.get('active', True),
                'created_at': d.get('createdAt') or d.get('created_at'),
            }
        )

        self.migrate_generic_table('lead_contact_normalized_titles', 'lead_contact_normalized_title',
            lambda d: {
                'lead_contact_id': d.get('leadContactId') or d.get('lead_contact_id'),
                'original_title': truncate_string(d.get('originalTitle') or d.get('original_title')),
                'canonical_title': truncate_string(d.get('canonicalTitle') or d.get('canonical_title')),
                'seniority': truncate_string(d.get('seniority')),
                'normalized_titles': to_postgres_array(d.get('normalizedTitles') or d.get('normalized_titles')),
                'keywords': to_postgres_array(d.get('keywords')),
                'title_abbreviations': normalize_text(d.get('titleAbbreviations') or d.get('title_abbreviations')),
                'created_at': d.get('createdAt') or d.get('created_at'),
            }
        )

        self.migrate_generic_table('lead_data_packs', 'lead_data_pack',
            lambda d: {
                'name': truncate_string(d.get('name')),
                'slug': truncate_string(d.get('slug')),
                'industry_ids': d.get('industryIds') or d.get('industry_ids'),
                'active': d.get('active', True),
                'created_by': d.get('createdBy') or d.get('created_by'),
                'created_at': d.get('createdAt') or d.get('created_at'),
                'updated_by': d.get('updatedBy') or d.get('updated_by'),
                'updated_at': d.get('updatedAt') or d.get('updated_at'),
            }
        )

        self.migrate_generic_table('lead_notes', 'lead_note',
            lambda d: {
                'id': str(d.get('_id')),
                'workspace_id': d.get('workspaceId'),
                'tenant_id': d.get('tenantId'),
                'source_id': d.get('sourceId') or d.get('lead_id'),
                'note': d.get('note'),
                'type': d.get('type') or d.get('leadType'),
                'active': d.get('active', True),
                'created_by': d.get('createdBy') or d.get('created_by'),
                'updated_by': d.get('updatedBy') or d.get('updated_by'),
                'created_at': d.get('createdAt'),
                'updated_at': d.get('updatedAt')
            }
        )
        
        self.migrate_generic_table('lead_lists', 'lead_list',
            lambda d: {
                'id': str(d.get('_id')),
                'workspace_id': d.get('workspaceId'),
                'tenant_id': d.get('tenantId'),
                'name': d.get('name'),
                'type': d.get('type'),
                'active': d.get('active', True),
                'source_ids': d.get('sourceIds'),
                'created_at': d.get('createdAt'),
                'updated_at': d.get('updatedAt'),
                'created_by': d.get('createdBy') or d.get('created_by'),
                'updated_by': d.get('updatedBy') or d.get('updated_by')
            }
        )
        
        self.migrate_generic_table('lead_queries', 'lead_query',
            lambda d: {
                'id': str(d.get('_id')),
                'type': truncate_string(d.get('type')),
                'value': truncate_string(d.get('value')),
                'created_at': d.get('createdAt') or d.get('created_at'),
            }
        )

        self.migrate_generic_table('lead_search_histories', 'lead_search_history',
            lambda d: {
                'id': str(d.get('_id')),
                'user_id': truncate_string(d.get('userId') or d.get('user_id')),
                'title': truncate_string(d.get('title')),
                'result_count': d.get('resultCount') or d.get('result_count'),
                'type': truncate_string(d.get('type')),
                'cities': to_postgres_array(d.get('cities')),
                'states': to_postgres_array(d.get('states')),
                'countries': to_postgres_array(d.get('countries')),
                'contact_names': to_postgres_array(d.get('contactNames') or d.get('contact_names')),
                'company_names': to_postgres_array(d.get('companyNames') or d.get('company_names')),
                'company_cities': to_postgres_array(d.get('companyCities') or d.get('company_cities')),
                'company_states': to_postgres_array(d.get('companyStates') or d.get('company_states')),
                'company_countries': to_postgres_array(d.get('companyCountries') or d.get('company_countries')),
                'regions': to_postgres_array(d.get('regions')),
                'keywords': to_postgres_array(d.get('keywords')),
                'industries': to_postgres_array(d.get('industries')),
                'employee_ranges': to_postgres_array(d.get('employeeRanges') or d.get('employee_ranges')),
                'revenue_ranges': to_postgres_array(d.get('revenueRanges') or d.get('revenue_ranges')),
                'technologies': to_postgres_array(d.get('technologies')),
                'titles': to_postgres_array(d.get('titles')),
                'seniority': to_postgres_array(d.get('seniority')),
                'departments': to_postgres_array(d.get('departments')),
                'postal_codes': to_postgres_array(d.get('postalCodes') or d.get('postal_codes')),
                'sic_codes': to_postgres_array(d.get('sicCodes') or d.get('sic_codes')),
                'naics_codes': to_postgres_array(d.get('naicsCodes') or d.get('naics_codes')),
                'created_at': d.get('createdAt') or d.get('created_at'),
                'updated_at': d.get('updatedAt') or d.get('updated_at'),
            }
        )

        self.migrate_generic_table('tenant_contacts', 'tenant_contact',
            lambda d: {
                'id': str(d.get('_id')),
                'tenant_id': d.get('tenantId'),
                'email': d.get('email') or d.get('contact_email'),
                'first_name': d.get('firstName'),
                'last_name': d.get('lastName'),
                'source_id': d.get('sourceId'),
                'source_type': d.get('sourceType'),
                'company_id': d.get('companyId'),
                'created_at': d.get('createdAt'),
                'updated_at': d.get('updatedAt')
            }
        )

        # TIER 5: Campaign Data
        print("\n📦 TIER 5: Campaign Data")
        self.migrate_generic_table('campaigns', 'campaign',
            lambda d: {
                'id': str(d.get('_id')),
                'workspace_id': d.get('workspaceId'),
                'tenant_id': d.get('tenantId'),
                'name': truncate_string(d.get('name')),
                'industry': truncate_string(d.get('industry')),
                'template_id': truncate_string(d.get('templateId')),
                'sending_mailbox_id': truncate_string(d.get('sendingMailboxId')),
                'approved_by': truncate_string(d.get('approvedBy')),
                'targeting_criteria': json.dumps(v, default=str) if (v := d.get('targetingCriteria')) is not None else None,
                'scheduled_start': d.get('scheduledStart'),
                'status': truncate_string(d.get('status')),
                'cc_recipients': json.dumps(v, default=str) if (v := (d.get('ccRecipients') or d.get('cc_recipients'))) is not None else None,
                'bcc_recipients': json.dumps(v, default=str) if (v := (d.get('bccRecipients') or d.get('bcc_recipients'))) is not None else None,
                'sending_window': json.dumps(v, default=str) if (v := d.get('sendingWindow')) is not None else None,
                'launched_at': d.get('launchedAt'),
                'created_at': d.get('createdAt'),
                'created_by': d.get('createdBy') or d.get('created_by'),
                'updated_at': d.get('updatedAt'),
                'updated_by': d.get('updatedBy') or d.get('updated_by')
            }
        )
        
        self.migrate_generic_table('campaign_contacts', 'campaign_contact',
            lambda d: {
                'id': str(d.get('_id')),
                'campaign_id': d.get('campaignId'),
                'contact_id': d.get('contactId'),
                'current_step': d.get('currentStep', 0),
                'participating': d.get('participating', True),
                'reply_received': d.get('replyReceived', False),
                'status': truncate_string(d.get('status')),
                'email_data': normalize_text(d.get('emailData')),
                'last_sent_at': d.get('lastSentAt'),
                'next_send_at': d.get('nextSendAt'),
                'updated_at': d.get('updatedAt')
            }
        )
        
        self.migrate_generic_table('campaign_emails', 'campaign_email',
            lambda d: {
                'id': str(d.get('_id')),
                'campaign_id': d.get('campaignId'),
                'body_template': truncate_string(d.get('bodyTemplate')),
                'delay_days': d.get('delayDays', 0),
                'status': truncate_string(d.get('status')),
                'step_number': d.get('stepNumber', 1),
                'subject': truncate_string(d.get('subject')),
                'updated_at': d.get('updatedAt'),
                'updated_by': d.get('updatedBy') or d.get('updated_by'),
                'attachment_ids': to_postgres_array(d.get('attachmentIds'), convert_object_ids=True)
            }
        )

        # TIER 6: Announcements
        print("\n📦 TIER 6: Announcements")
        self.migrate_generic_table('tenant_announcements', 'tenant_announcement',
            lambda d: {
                'id': str(d.get('_id')),
                'active': d.get('active', True),
                'attachment_ids': to_postgres_array(d.get('attachmentIds')),
                'body': truncate_string(d.get('body')),
                'created_at': d.get('createdAt'),
                'created_by': d.get('createdBy') or d.get('created_by'),
                'launched_at': d.get('launchedAt'),
                'name': truncate_string(d.get('name')),
                'status': truncate_string(d.get('status')),
                'subject': truncate_string(d.get('subject')),
                'tenant_id': d.get('tenantId'),
                'cc_recipients': truncate_string(json.dumps(v, default=str) if (v := (d.get('ccRecipients') or d.get('cc_recipients'))) is not None else None),
                'bcc_recipients': truncate_string(json.dumps(v, default=str) if (v := (d.get('bccRecipients') or d.get('bcc_recipients'))) is not None else None),
                'updated_at': d.get('updatedAt'),
                'updated_by': d.get('updatedBy') or d.get('updated_by')
            }
        )

        self.migrate_generic_table('tenant_lead_filters', 'tenant_lead_filter',
            lambda d: {
                'tenant_id': truncate_string(d.get('tenantId') or d.get('tenant_id')),
                'type': truncate_string(d.get('type')),
                'cities': to_postgres_array(d.get('cities')),
                'states': to_postgres_array(d.get('states')),
                'countries': to_postgres_array(d.get('countries')),
                'company_names': to_postgres_array(d.get('companyNames') or d.get('company_names')),
                'company_cities': to_postgres_array(d.get('companyCities') or d.get('company_cities')),
                'company_states': to_postgres_array(d.get('companyStates') or d.get('company_states')),
                'company_countries': to_postgres_array(d.get('companyCountries') or d.get('company_countries')),
                'regions': to_postgres_array(d.get('regions')),
                'keywords': to_postgres_array(d.get('keywords')),
                'industries': to_postgres_array(d.get('industries')),
                'employee_ranges': to_postgres_array(d.get('employeeRanges') or d.get('employee_ranges')),
                'revenue_ranges': to_postgres_array(d.get('revenueRanges') or d.get('revenue_ranges')),
                'technologies': to_postgres_array(d.get('technologies')),
                'titles': to_postgres_array(d.get('titles')),
                'seniority': to_postgres_array(d.get('seniority')),
                'departments': to_postgres_array(d.get('departments')),
                'postal_codes': to_postgres_array(d.get('postalCodes') or d.get('postal_codes')),
                'sic_codes': to_postgres_array(d.get('sicCodes') or d.get('sic_codes')),
                'naics_codes': to_postgres_array(d.get('naicsCodes') or d.get('naics_codes')),
                'active': d.get('active', True),
                'created_by': d.get('createdBy') or d.get('created_by'),
                'created_at': d.get('createdAt') or d.get('created_at'),
                'updated_by': d.get('updatedBy') or d.get('updated_by'),
                'updated_at': d.get('updatedAt') or d.get('updated_at'),
            }
        )

        self.migrate_generic_table('tenant_announcement_contacts', 'tenant_announcement_contact',
            lambda d: {
                'id': str(d.get('_id')),
                'announcement_id': d.get('announcementId'),
                'source_id': d.get('contactId') or d.get('sourceId'),
                'source_type': d.get('sourceType'),
                'email': d.get('email'),
                'first_name': d.get('firstName'),
                'status': d.get('status'),
                'created_at': d.get('createdAt'),
                'created_by': d.get('createdBy') or d.get('created_by'),
                'sent_at': d.get('sentAt')
            }
        )

        self.migrate_generic_table('apollo_specification', 'apollo_specification',
            lambda d: {
                'id': str(d.get('_id')),
                'person_title_enabled': d.get('personTitleEnabled', False),
                'person_titles': normalize_text(d.get('personTitles')),
                'person_seniority_enabled': d.get('personSeniorityEnabled', False),
                'person_seniorities': normalize_text(d.get('personSeniorities')),
                'created_at': d.get('createdAt'),
                'created_by': d.get('createdBy') or d.get('created_by')
            }
        )

        # TIER 7: Questions
        print("\n📦 TIER 7: Questions & Other")
        self.migrate_generic_table('question_sections', 'question_section',
            lambda d: {
                'id': str(d.get('_id')),
                'name': truncate_string(d.get('name')),
                'position': d.get('position'),
                'active': d.get('active', True),
                'created_by': d.get('createdBy') or d.get('created_by'),
                'created_at': d.get('createdAt') or d.get('created_at'),
                'updated_by': d.get('updatedBy') or d.get('updated_by'),
                'updated_at': d.get('updatedAt') or d.get('updated_at'),
            }
        )

        self.migrate_generic_table('questions', 'question',
            lambda d: {
                'id': str(d.get('_id')),
                'label': truncate_string(d.get('label') or d.get('questionText') or d.get('title')),
                'type': d.get('type'),
                'question_section_id': d.get('questionSectionId') or d.get('question_section_id'),
                'industry_ids': d.get('industryIds'),
                'options': to_postgres_array(d.get('options')),
                'position': d.get('position'),
                'active': d.get('active', True),
                'created_by': d.get('createdBy') or d.get('created_by'),
                'updated_by': d.get('updatedBy') or d.get('updated_by'),
                'created_at': d.get('createdAt'),
                'updated_at': d.get('updatedAt')
            }
        )

        # Post-process: Remap question IDs in vendor questionnaires
        self.update_vendor_questionnaires_with_remapped_questions()

        # TIER 8: Apollo Enrichment Data
        print("\n📦 TIER 8: Apollo Enrichment Data")
        self.migrate_generic_table('apollo_company_data', 'apollo_company_data',
            lambda d: {
                'lead_company_id': d.get('leadCompanyId') or d.get('lead_company_id'),
                'type': truncate_string(d.get('type')),
                'data': normalize_text(d.get('data')),
                'specification_id': d.get('specificationId') or d.get('specification_id'),
                'fetched_at': d.get('fetchedAt') or d.get('fetched_at'),
            }
        )

        self.migrate_generic_table('apollo_contact_data', 'apollo_contact_data',
            lambda d: {
                'lead_contact_id': d.get('leadContactId') or d.get('lead_contact_id'),
                'type': truncate_string(d.get('type')),
                'data': normalize_text(d.get('data')),
                'specification_id': d.get('specificationId') or d.get('specification_id'),
                'fetched_at': d.get('fetchedAt') or d.get('fetched_at'),
            }
        )

        self.migrate_generic_table('campaign_chat_memory', 'campaign_chat_memory',
            lambda d: {
                'campaign_id': d.get('campaignId') or d.get('campaign_id'),
                'name': truncate_string(d.get('name')),
                'tenant_id': d.get('tenantId') or d.get('tenant_id'),
                'workspace_id': d.get('workspaceId') or d.get('workspace_id'),
                'industry': truncate_string(d.get('industry')),
                'targeting_criteria': json.dumps(v, default=str) if (v := (d.get('targetingCriteria') or d.get('targeting_criteria'))) is not None else None,
                'lead_filter': json.dumps(v, default=str) if (v := (d.get('leadFilter') or d.get('lead_filter'))) is not None else None,
                'created_by': d.get('createdBy') or d.get('created_by'),
                'created_at': d.get('createdAt') or d.get('created_at'),
                'updated_by': d.get('updatedBy') or d.get('updated_by'),
                'updated_at': d.get('updatedAt') or d.get('updated_at'),
                'last_search_at': d.get('lastSearchAt') or d.get('last_search_at'),
            }
        )

        self.migrate_generic_table('contact_outreach_statuses', 'contact_outreach_status',
            lambda d: {
                'contact_id': d.get('contactId') or d.get('contact_id'),
                'tenant_id': d.get('tenantId') or d.get('tenant_id'),
                'current_campaign_ids': d.get('currentCampaignIds') or d.get('current_campaign_ids'),
                'last_email_at': d.get('lastEmailAt') or d.get('last_email_at'),
                'status': truncate_string(d.get('status')),
                'sequence_completed_at': d.get('sequenceCompletedAt') or d.get('sequence_completed_at'),
                'unsubscribe_token': truncate_string(d.get('unsubscribeToken') or d.get('unsubscribe_token')),
                'updated_at': d.get('updatedAt') or d.get('updated_at'),
            }
        )

        self.migrate_generic_table('contact_emails', 'contact_email',
            lambda d: {
                'tenant_id': d.get('tenantId') or d.get('tenant_id'),
                'workspace_id': d.get('workspaceId') or d.get('workspace_id'),
                'campaign_id': d.get('campaignId') or d.get('campaign_id'),
                'contact_id': d.get('contactId') or d.get('contact_id'),
                'message_id': truncate_string(d.get('messageId') or d.get('message_id')),
                'conversation_id': truncate_string(d.get('conversationId') or d.get('conversation_id')),
                'subject': truncate_string(d.get('subject')),
                'body': normalize_text(d.get('body')),
                'to_recipients': normalize_text(d.get('toRecipients') or d.get('to_recipients')),
                'cc_recipients': normalize_text(d.get('ccRecipients') or d.get('cc_recipients')),
                'bcc_recipients': normalize_text(d.get('bccRecipients') or d.get('bcc_recipients')),
                'attachment_ids': to_postgres_array(d.get('attachmentIds') or d.get('attachment_ids'), convert_object_ids=True),
                'platform': truncate_string(d.get('platform')),
                'type': truncate_string(d.get('type')),
                'created_by': d.get('createdBy') or d.get('created_by'),
                'created_at': d.get('createdAt') or d.get('created_at'),
            }
        )

        self.migrate_generic_table('collaborators', 'collaborator',
            lambda d: {
                'source_id': truncate_string(d.get('sourceId') or d.get('source_id')),
                'source_type': truncate_string(d.get('sourceType') or d.get('source_type')),
                'user_id': d.get('userId') or d.get('user_id'),
                'role': truncate_string(d.get('role')),
                'active': d.get('active', True),
                'created_by': d.get('createdBy') or d.get('created_by'),
                'created_at': d.get('createdAt') or d.get('created_at'),
                'updated_by': d.get('updatedBy') or d.get('updated_by'),
                'updated_at': d.get('updatedAt') or d.get('updated_at'),
            }
        )

        self.migrate_generic_table('facts', 'fact',
            lambda d: {
                'fact': truncate_string(d.get('fact')),
                'created_at': d.get('createdAt') or d.get('created_at'),
                'updated_at': d.get('updatedAt') or d.get('updated_at'),
            }
        )

        self.migrate_generic_table('feedbacks', 'feedback',
            lambda d: {
                'tenant_id': d.get('tenantId') or d.get('tenant_id'),
                'workspace_id': d.get('workspaceId') or d.get('workspace_id'),
                'user_id': d.get('userId') or d.get('user_id'),
                'username': truncate_string(d.get('username')),
                'company_name': truncate_string(d.get('companyName') or d.get('company_name')),
                'type': truncate_string(d.get('type')),
                'message': truncate_string(d.get('message')),
                'rating': d.get('rating'),
                'status': truncate_string(d.get('status')),
                'feedback_date': d.get('feedbackDate') or d.get('feedback_date'),
                'created_by': d.get('createdBy') or d.get('created_by'),
                'created_at': d.get('createdAt') or d.get('created_at'),
                'updated_by': d.get('updatedBy') or d.get('updated_by'),
                'updated_at': d.get('updatedAt') or d.get('updated_at'),
            }
        )

        self.migrate_generic_table('email_images', 'email_image',
            lambda d: {
                'source_id': truncate_string(d.get('sourceId') or d.get('source_id')),
                'source_type': truncate_string(d.get('sourceType') or d.get('source_type')),
                'resource_url': truncate_string(d.get('resourceUrl') or d.get('resource_url')),
                'created_by': d.get('createdBy') or d.get('created_by'),
                'created_at': d.get('createdAt') or d.get('created_at'),
            }
        )

        self.migrate_generic_table('email_sequence_templates', 'email_sequence_template',
            lambda d: {
                'tenant_id': d.get('tenantId') or d.get('tenant_id'),
                'name': truncate_string(d.get('name')),
                'description': truncate_string(d.get('description')),
                'steps': normalize_text(d.get('steps')),
                'created_by': d.get('createdBy') or d.get('created_by'),
                'created_at': d.get('createdAt') or d.get('created_at'),
                'updated_at': d.get('updatedAt') or d.get('updated_at'),
            }
        )

        self.migrate_generic_table('sequence_templates', 'sequence_template',
            lambda d: {
                'name': truncate_string(d.get('name')),
                'step_count': d.get('stepCount') or d.get('step_count') or 0,
                'default_delays': to_postgres_array(d.get('defaultDelays') or d.get('default_delays')),
                'purpose': truncate_string(d.get('purpose')),
            }
        )

        # TIER 9: Attachments
        print("\n📦 TIER 9: Attachments")
        self.migrate_generic_table('attachment_libraries', 'attachment_library',
            lambda d: {
                'workspace_id': d.get('workspaceId') or d.get('workspace_id'),
                'filename': truncate_string(d.get('filename') or d.get('fileName')),
                'file_url': truncate_string(d.get('fileUrl') or d.get('file_url')),
                'file_type': truncate_string(d.get('fileType') or d.get('file_type')),
                'size_bytes': d.get('sizeBytes') or d.get('size_bytes') or 0,
                'updated_at': d.get('updatedAt') or d.get('updated_at'),
                'updated_by': d.get('updatedBy') or d.get('updated_by'),
            }
        )

        self.migrate_generic_table('attachments', 'attachment',
            lambda d: {
                'source_id': truncate_string(d.get('sourceId') or d.get('source_id')),
                'source_type': truncate_string(d.get('sourceType') or d.get('source_type')),
                'file_name': truncate_string(d.get('fileName') or d.get('file_name')),
                'file_type': truncate_string(d.get('fileType') or d.get('file_type')),
                'file_url': truncate_string(d.get('fileUrl') or d.get('file_url')),
                'size_bytes': d.get('sizeBytes') or d.get('size_bytes') or 0,
                'active': d.get('active', True),
                'created_by': d.get('createdBy') or d.get('created_by'),
                'created_at': d.get('createdAt') or d.get('created_at'),
                'updated_by': d.get('updatedBy') or d.get('updated_by'),
                'updated_at': d.get('updatedAt') or d.get('updated_at'),
            }
        )

        self.migrate_generic_table('scrape_jobs', 'scrape_job',
            lambda d: {
                'id': str(d.get('_id')),
                'company_id': truncate_string(d.get('companyId') or d.get('company_id')),
                'scraper_job_id': truncate_string(d.get('scraperJobId') or d.get('scraper_job_id')),
                'source_type': truncate_string(d.get('sourceType') or d.get('source_type')),
                'source_id': truncate_string(d.get('sourceId') or d.get('source_id')),
                'status': truncate_string(d.get('status')),
                'scheduled_at': d.get('scheduledAt') or d.get('scheduled_at'),
                'completed_at': d.get('completedAt') or d.get('completed_at'),
                'created_at': d.get('createdAt') or d.get('created_at'),
                'updated_at': d.get('updatedAt') or d.get('updated_at'),
            }
        )

        self.migrate_generic_table('requestForProposals', 'request_for_proposal',
            lambda d: {
                'id': str(d.get('_id')),
                'user_id': truncate_string(d.get('userId') or d.get('user_id')),
                'title': truncate_string(d.get('title')),
                'service_ids': to_postgres_array(d.get('serviceIds') or d.get('service_ids')),
                'quantity': d.get('quantity') or 0,
                'budget': truncate_string(d.get('budget')),
                'timeline': truncate_string(d.get('timeline')),
                'description': truncate_string(d.get('description')),
                'status': truncate_string(d.get('status')),
                'specification_ids': to_postgres_array(d.get('specificationIds') or d.get('specification_ids')),
                'active': d.get('active', True),
                'created_by': d.get('createdBy') or d.get('created_by'),
                'created_at': d.get('createdAt') or d.get('created_at'),
                'updated_by': d.get('updatedBy') or d.get('updated_by'),
                'updated_at': d.get('updatedAt') or d.get('updated_at'),
            }
        )

        self.migrate_generic_table('requestForQuotes', 'request_for_quote',
            lambda d: {
                'id': str(d.get('_id')),
                'user_id': truncate_string(d.get('userId') or d.get('user_id')),
                'title': truncate_string(d.get('title')),
                'service_ids': to_postgres_array(d.get('serviceIds') or d.get('service_ids')),
                'vendor_ids': to_postgres_array(d.get('vendorIds') or d.get('vendor_ids')),
                'quantity': d.get('quantity') or 0,
                'budget': truncate_string(d.get('budget')),
                'deadline': d.get('deadline'),
                'description': truncate_string(d.get('description')),
                'status': truncate_string(d.get('status')),
                'active': d.get('active', True),
                'created_by': d.get('createdBy') or d.get('created_by'),
                'created_at': d.get('createdAt') or d.get('created_at'),
                'updated_by': d.get('updatedBy') or d.get('updated_by'),
                'updated_at': d.get('updatedAt') or d.get('updated_at'),
            }
        )

        self.migrate_generic_table('timezone_mappings', 'timezone_mapping',
            lambda d: {
                'location_type': truncate_string(d.get('locationType') or d.get('location_type')),
                'location_key': truncate_string(d.get('locationKey') or d.get('location_key')),
                'timezone': truncate_string(d.get('timezone')),
                'active': d.get('active', True),
                'created_at': d.get('createdAt') or d.get('created_at'),
                'updated_at': d.get('updatedAt') or d.get('updated_at'),
            }
        )

        self.migrate_generic_table('user_activity_logs', 'user_activity_log',
            lambda d: {
                'tenant_id': truncate_string(d.get('tenantId') or d.get('tenant_id')),
                'workspace_id': truncate_string(d.get('workspaceId') or d.get('workspace_id')),
                'user_id': truncate_string(d.get('userId') or d.get('user_id')),
                'username': truncate_string(d.get('username')),
                'message': truncate_string(d.get('message')),
                'log_level': truncate_string(d.get('logLevel') or d.get('log_level')),
                'type': truncate_string(d.get('type')),
                'created_by': d.get('createdBy') or d.get('created_by'),
                'created_at': d.get('createdAt') or d.get('created_at'),
            }
        )

        self.migrate_generic_table('refresh_tokens', 'refresh_token',
            lambda d: {
                'id': str(d.get('_id')),
                'user_id': truncate_string(d.get('userId') or d.get('user_id')),
                'token': truncate_string(d.get('token')),
                'refresh_count': d.get('refreshCount') or d.get('refresh_count') or 0,
                'issued_at': d.get('issuedAt') or d.get('issued_at'),
                'expires_at': d.get('expiresAt') or d.get('expires_at'),
                'last_used_at': d.get('lastUsedAt') or d.get('last_used_at'),
                'revoked': d.get('revoked', False),
            }
        )

        self.migrate_generic_table('prompt_specifications', 'prompt_specification',
            lambda d: {
                'id': str(d.get('_id')),
                'type': truncate_string(d.get('type')),
                'prompt_template': truncate_string(d.get('promptTemplate') or d.get('prompt_template')),
                'created_by': d.get('createdBy') or d.get('created_by'),
                'created_at': d.get('createdAt') or d.get('created_at'),
            }
        )

        self.migrate_generic_table('messages', 'message',
            lambda d: {
                'id': str(d.get('_id')),
                'tenant_id': truncate_string(d.get('tenantId') or d.get('tenant_id')),
                'workspace_id': truncate_string(d.get('workspaceId') or d.get('workspace_id')),
                'conversation_id': truncate_string(d.get('conversationId') or d.get('conversation_id')),
                'user_id': truncate_string(d.get('userId') or d.get('user_id')),
                'type': truncate_string(d.get('type')),
                'request': truncate_string(d.get('request')),
                'response': truncate_string(d.get('response')),
                'campaign_id': truncate_string(d.get('campaignId') or d.get('campaign_id')),
                'targeting_criteria': json.dumps(v, default=str) if (v := (d.get('targetingCriteria') or d.get('targeting_criteria'))) is not None else None,
                'search_performed': d.get('searchPerformed') or d.get('search_performed') or False,
                'created_at': d.get('createdAt') or d.get('created_at'),
            }
        )

        self.stats['end_time'] = time.time()

        # Print summary
        elapsed = self.stats['end_time'] - self.stats['start_time']
        speed = self.stats['total_records'] / elapsed if elapsed > 0 else 0
        
        print("\n" + "="*70)
        print("📊 MIGRATION COMPLETE")
        print("="*70)
        print(f"Collections Migrated: {len(self.stats['tables_migrated'])}")
        print(f"Total Records:        {self.stats['total_records']:,}")
        print(f"Total Errors:         {self.stats['total_errors']}")
        print(f"Total Time:           {elapsed:.1f} seconds ({elapsed/60:.2f} minutes)")
        print(f"Throughput:           {speed:.0f} records/sec")
        print("="*70)
        
        if self.stats['tables_migrated']:
            print("\nTables Migrated:")
            for table, count in sorted(self.stats['tables_migrated'].items()):
                print(f"  • {table}: {count:,} rows")
        if self.table_errors:
            print("\nTable Errors (first error per table):")
            for table, err in sorted(self.table_errors.items()):
                print(f"  • {table}: {err[:200]}")
        
        print("="*70)
        print("\n✅ Migration completed successfully!")


def main():
    mongo_uri = os.getenv('MONGO_URI')
    pg_uri = os.getenv('PG_URI')
    
    if not mongo_uri or not pg_uri:
        print("Usage: export MONGO_URI='...' PG_URI='...'")
        print("  python3 migration/migrate.py")
        sys.exit(1)
    
    try:
        migrator = ComprehensiveMigrator(mongo_uri, pg_uri)
        migrator.connect()
        migrator.migrate_all()
        migrator.disconnect()
    except Exception as e:
        print(f"\n❌ Migration failed: {e}")
        sys.exit(1)


if __name__ == '__main__':
    main()
