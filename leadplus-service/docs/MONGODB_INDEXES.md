# MongoDB Index Strategy — leadplus-service

## Table of Contents
- [Overview](#overview)
- [Naming Conventions](#naming-conventions)
- [Compound Index Design Patterns](#compound-index-design-patterns)
- [Current Index Inventory](#current-index-inventory)
- [Index Maintenance Procedures](#index-maintenance-procedures)

---

## Overview

This document defines the indexing strategy for all MongoDB collections in `leadplus-service`.
Every index listed here was created to support a specific query pattern. **Do not drop an index
without first checking which code depends on it** (search for `INDEX REQUIRED` comments in the codebase).

> **Rule of thumb:** If a query runs on a collection with more than 10,000 documents and does not
> have an index, it will cause a full collection scan (COLLSCAN) and degrade performance severely.

---

## Naming Conventions

MongoDB auto-names indexes using the pattern `field_direction`. We follow this convention and
do not override index names unless there is a conflict.

| Pattern | Example | Meaning |
|---|---|---|
| `field_1` | `active_1` | Ascending on `active` |
| `field_-1` | `updatedAt_-1` | Descending on `updatedAt` |
| `field1_1_field2_1` | `active_1_leadCompanyId_1` | Compound ascending |
| `field1_1_field2_-1` | `active_1_updatedAt_-1` | Compound mixed direction |

---

## Compound Index Design Patterns

### The ESR Rule
When designing compound indexes, follow the **ESR Rule** — field order matters:

```
E — Equality fields first    (e.g. active: true)
S — Sort fields second       (e.g. updatedAt: -1)
R — Range fields last        (e.g. revenueUsdAmount: { $gte: x })
```

**Example:**
```javascript
// Query: { active: true, revenueUsdAmount: { $gte: 1000000 } } sorted by updatedAt
// Correct index order following ESR:
db.lead_companies.createIndex({ active: 1, updatedAt: -1, revenueUsdAmount: 1 })
```

### Why `active` Is Always First
Almost every query in this codebase filters by `active: true`. Putting `active` first in every
compound index means MongoDB can immediately discard ~50% of documents (inactive records)
before evaluating the second field.

### Prefix Rule
MongoDB can use a compound index for queries that match a **left-side prefix** of the index fields.

```javascript
// Index: { active: 1, leadCompanyId: 1, email: 1 }
// This index covers ALL of these queries:
{ active: true }                                      // ✅ prefix match
{ active: true, leadCompanyId: "abc" }               // ✅ prefix match
{ active: true, leadCompanyId: "abc", email: "x" }   // ✅ full match

// But NOT:
{ leadCompanyId: "abc" }                             // ❌ skips first field
{ active: true, email: "x" }                         // ❌ skips middle field
```

---

## Current Index Inventory

### `lead_contacts` Collection

| Index Name | Fields | Used By |
|---|---|---|
| `_id_` | `{ _id: 1 }` | Default — all findById queries |
| `active_1_leadCompanyId_1` | `{ active: 1, leadCompanyId: 1 }` | `findAllByLeadCompanyIdAndActiveTrue`, `countContactsByCompanyIds` |
| `leadCompanyId_1_active_1_email_1` | `{ leadCompanyId: 1, active: 1, email: 1 }` | `findValidContacts` |
| `active_1_updatedAt_-1` | `{ active: 1, updatedAt: -1 }` | `findAllByUpdatedAtAfterAndActiveTrue` |

**Creation commands:**
```javascript
db.lead_contacts.createIndex({ active: 1, leadCompanyId: 1 }, { background: true })
db.lead_contacts.createIndex({ leadCompanyId: 1, active: 1, email: 1 }, { background: true })
db.lead_contacts.createIndex({ active: 1, updatedAt: -1 }, { background: true })
```

---

### `lead_companies` Collection

| Index Name | Fields | Used By |
|---|---|---|
| `_id_` | `{ _id: 1 }` | Default — all findById queries |
| `active_1_name_1` | `{ active: 1, name: 1 }` | Company name search in `CompanyLeadSearchService` |
| `active_1_domain_1` | `{ active: 1, domain: 1 }` | Domain filter |
| `active_1_segment_1` | `{ active: 1, segment: 1 }` | Segment filter |
| `active_1_industry_1` | `{ active: 1, industry: 1 }` | Industry filter in `ContactLeadSearchService`, `CompanyLeadSearchService` |
| `active_1_employeeRange_1` | `{ active: 1, employeeRange: 1 }` | Employee range filter |
| `active_1_revenueUsdAmount_1` | `{ active: 1, revenueUsdAmount: 1 }` | Revenue range filter |

**Creation commands:**
```javascript
db.lead_companies.createIndex({ active: 1, name: 1 }, { background: true })
db.lead_companies.createIndex({ active: 1, domain: 1 }, { background: true })
db.lead_companies.createIndex({ active: 1, segment: 1 }, { background: true })
db.lead_companies.createIndex({ active: 1, industry: 1 }, { background: true })
db.lead_companies.createIndex({ active: 1, employeeRange: 1 }, { background: true })
db.lead_companies.createIndex({ active: 1, revenueUsdAmount: 1 }, { background: true })
```

---

### `campaigns` Collection

| Index Name | Fields | Used By |
|---|---|---|
| `_id_` | `{ _id: 1 }` | Default — all findById queries |
| `tenantId_1_status_1` | `{ tenantId: 1, status: 1 }` | Tenant campaign listing |
| `workspaceId_1` | `{ workspaceId: 1 }` | Workspace campaign listing |
| `sendingMailboxId_1_status_1` | `{ sendingMailboxId: 1, status: 1 }` | Mailbox campaign queries |

**Creation commands:**
```javascript
db.campaigns.createIndex({ tenantId: 1, status: 1 }, { background: true })
db.campaigns.createIndex({ workspaceId: 1 }, { background: true })
db.campaigns.createIndex({ sendingMailboxId: 1, status: 1 }, { background: true })
```

---

## Index Maintenance Procedures

### Adding a New Index

1. Write the query first and identify the fields it filters/sorts on
2. Follow the ESR rule to determine field order
3. Add `{ background: true }` to avoid locking the collection
4. Add an `// INDEX REQUIRED: { ... }` comment to the repository method
5. Document the new index in this file under the relevant collection section
6. Run `explain("executionStats")` to confirm `IXSCAN` is used

```javascript
// Template
db.<collection>.createIndex({ <fields> }, { background: true })
```

### Verifying an Index Is Being Used

```javascript
db.<collection>.find(<your-query>).explain("executionStats")

// In the output, look for:
// "winningPlan.inputStage.stage": "IXSCAN"  ← index is used ✅
// "winningPlan.stage": "COLLSCAN"            ← no index used ❌
```

### Checking All Indexes on a Collection

```javascript
db.lead_contacts.getIndexes()
db.lead_companies.getIndexes()
db.campaigns.getIndexes()
```

### Monitoring Index Usage in Atlas

1. Go to MongoDB Atlas → your cluster
2. Navigate to **Performance Advisor** tab
3. Check **Query Targeting** metric — should be **< 10**
4. Any value above 10 indicates a missing or unused index

### Dropping an Index (Use With Caution)

Before dropping any index:
1. Search the codebase for `INDEX REQUIRED` comments referencing that index
2. Confirm no active queries depend on it
3. Run `db.collection.dropIndex("index_name")` during low-traffic hours

```javascript
// Never drop without checking first
db.lead_contacts.dropIndex("active_1_leadCompanyId_1")  // ⚠️ check code first
```
