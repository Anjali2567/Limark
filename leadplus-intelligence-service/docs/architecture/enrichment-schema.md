# Enrichment Schema Design

## Overview

Extends existing Account/Contact schemas with enrichment tracking, confidence scoring, and audit trails. All enrichment metadata stored inline; full audit history in separate collection.

---

## Schema Extensions

### Account Schema Updates

```typescript
interface Account {
  // Existing fields preserved...

  // Intelligence fields
  keywords: string[]              // ["robotics", "CNC machining", "automation"]
  categories: string[]            // ["OEM", "Tier1", "Manufacturing"]
  capabilities?: string[]         // ["ISO9001", "ITAR", "5-axis CNC"]

  // Enrichment tracking
  enrichment_status: EnrichmentStatus
  last_enriched_at?: Date
  confidence_score?: number       // 0-100

  // Source tracking (last update only)
  last_enrichment_source?: EnrichmentSourceType
}

enum EnrichmentStatus {
  PENDING = 'pending',
  IN_PROGRESS = 'in_progress',
  ENRICHED = 'enriched',
  STALE = 'stale',              // > 7 days old
  FAILED = 'failed'
}

enum EnrichmentSourceType {
  WEBSITE_CRAWLER = 'website_crawler',
  APOLLO = 'apollo',
  CLAY = 'clay',
  SEAMLESS = 'seamless',
  LINKEDIN = 'linkedin',
  MANUAL = 'manual'
}
```

### Contact Schema Updates

```typescript
interface Contact {
  // Existing fields preserved...

  // Persona intelligence
  inferred_persona?: ContactPersona
  persona_confidence?: number     // 0-100
  linkedin_profile_url?: string

  // Enrichment tracking
  enrichment_status: EnrichmentStatus
  last_enriched_at?: Date
  last_enrichment_source?: EnrichmentSourceType
}

enum ContactPersona {
  PROCUREMENT = 'procurement',
  SUPPLY_CHAIN = 'supply_chain',
  OPERATIONS = 'operations',
  PRODUCT_DESIGN = 'product_design',
  ENGINEERING = 'engineering',
  QUALITY = 'quality',
  EXECUTIVE = 'executive',
  SALES = 'sales',
  UNKNOWN = 'unknown'
}
```

### EnrichmentJob Collection (New)

```typescript
interface EnrichmentJob {
  _id: ObjectId
  job_type: EnrichmentJobType
  target_type: 'account' | 'contact'
  target_id: ObjectId

  status: JobStatus
  priority: number                // Higher = process first, default 0

  // Execution tracking
  started_at?: Date
  completed_at?: Date
  attempts: number                // Retry counter
  error?: string

  // Results
  result?: {
    keywords_added?: string[]
    categories_added?: string[]
    persona_inferred?: ContactPersona
    confidence_score?: number
  }

  // Metadata
  triggered_by: 'api' | 'event' | 'cron' | 'cli'
  created_at: Date
  updated_at: Date
}

enum EnrichmentJobType {
  ACCOUNT_WEBSITE_CRAWL = 'account_website_crawl',
  CONTACT_PERSONA_INFERENCE = 'contact_persona_inference',
  BATCH_REFRESH = 'batch_refresh',
  VALIDATION = 'validation'
}

enum JobStatus {
  QUEUED = 'queued',
  IN_PROGRESS = 'in_progress',
  COMPLETED = 'completed',
  FAILED = 'failed'
}
```

### EnrichmentEvent Collection (New - Audit Trail)

```typescript
interface EnrichmentEvent {
  _id: ObjectId
  target_type: 'account' | 'contact'
  target_id: ObjectId

  source: EnrichmentSourceType
  job_id?: ObjectId               // Link to EnrichmentJob if applicable

  fields_updated: string[]        // ['keywords', 'categories']
  values_before: Record<string, unknown>
  values_after: Record<string, unknown>

  confidence_score?: number
  timestamp: Date
}
```

---

## Indexes

### Account Collection
```typescript
// Existing indexes preserved
{ domain: 1 }                     // unique, sparse
{ name: 1 }                       // unique

// New indexes
{ keywords: 1 }                   // Array index for filtering
{ categories: 1 }                 // Array index
{ enrichment_status: 1 }
{ last_enriched_at: 1 }           // For staleness queries
{ confidence_score: 1 }
{ 'location.state': 1, keywords: 1 }  // Compound for campaign queries
```

### Contact Collection
```typescript
// Existing indexes preserved
{ email: 1 }                      // unique
{ accountId: 1 }

// New indexes
{ inferred_persona: 1 }
{ accountId: 1, inferred_persona: 1 }  // Compound
{ enrichment_status: 1 }
```

### EnrichmentJob Collection
```typescript
{ status: 1, priority: -1, created_at: 1 }  // Queue processing order
{ target_type: 1, target_id: 1 }            // Find jobs for entity
{ job_type: 1, status: 1 }                  // Job type filtering
{ created_at: 1 }                           // Cleanup old jobs
```

### EnrichmentEvent Collection
```typescript
{ target_type: 1, target_id: 1, timestamp: -1 }  // Audit history
{ job_id: 1 }                                    // Link to job
{ source: 1, timestamp: -1 }                     // Source tracking
```

---

## Migration Strategy

### Phase 1: Add New Fields (Non-breaking)
```typescript
// Add fields with optional/default values
db.accounts.updateMany({}, {
  $set: {
    keywords: [],
    categories: [],
    enrichment_status: 'pending',
    confidence_score: null
  }
});

db.contacts.updateMany({}, {
  $set: {
    enrichment_status: 'pending',
    persona_confidence: null
  }
});
```

### Phase 2: Create New Collections
```typescript
db.createCollection('enrichment_jobs');
db.createCollection('enrichment_events');
```

### Phase 3: Build Indexes
```typescript
// Run index creation commands
// Non-blocking background builds
```

### Phase 4: Backfill Existing Data (Optional)
```typescript
// Trigger enrichment jobs for existing accounts
// Run in batches to avoid overwhelming system
```

---

## Confidence Score Calculation

```typescript
function calculateConfidence(account: Account): number {
  const completeness = calculateCompleteness(account);  // 0-100
  const freshness = calculateFreshness(account);        // 0-100
  const sourceReliability = getSourceReliability(account.last_enrichment_source);

  return (completeness * 0.4) + (freshness * 0.3) + (sourceReliability * 0.3);
}

function calculateCompleteness(account: Account): number {
  const criticalFields = ['name', 'domain', 'keywords', 'categories'];
  const populated = criticalFields.filter(f => account[f] && account[f].length > 0);
  return (populated.length / criticalFields.length) * 100;
}

function calculateFreshness(account: Account): number {
  if (!account.last_enriched_at) return 0;
  const daysOld = daysSince(account.last_enriched_at);
  if (daysOld < 7) return 100;
  if (daysOld > 90) return 0;
  return 100 - ((daysOld - 7) / 83 * 100);  // Linear decay 7-90 days
}

const SOURCE_RELIABILITY: Record<EnrichmentSourceType, number> = {
  manual: 100,
  apollo: 90,
  clay: 90,
  seamless: 85,
  website_crawler: 80,
  linkedin: 85
};
```

---

## Data Quality Rules

### Staleness
- Accounts/contacts not enriched in 7+ days marked as `stale`
- Weekly batch job re-enriches stale records
- Critical records (high-value accounts) refreshed more frequently

### Deduplication
- Accounts: Same domain = duplicate (merge on higher confidence)
- Contacts: Same email = duplicate (merge on higher confidence)
- Run dedup check before creating new records

### Validation
- Domain accessibility check (DNS + HTTP 200)
- Email format validation (regex + MX record check)
- Keywords normalized (lowercase, trimmed)
- Categories validated against taxonomy list

---

## Repository Methods

### AccountRepository Extensions
```typescript
findByKeywords(keywords: string[]): Promise<Account[]>
findByCategories(categories: string[]): Promise<Account[]>
findStale(olderThanDays: number): Promise<Account[]>
updateEnrichmentData(id: ObjectId, data: EnrichmentData): Promise<Account>
```

### ContactRepository Extensions
```typescript
findByPersona(persona: ContactPersona): Promise<Contact[]>
findByAccountAndPersona(accountId: ObjectId, persona: ContactPersona): Promise<Contact[]>
updatePersona(id: ObjectId, persona: ContactPersona, confidence: number): Promise<Contact>
```

### EnrichmentJobRepository (New)
```typescript
enqueue(jobData: CreateEnrichmentJobDto): Promise<EnrichmentJob>
getNextJob(): Promise<EnrichmentJob | null>  // Priority queue logic
updateStatus(jobId: ObjectId, status: JobStatus): Promise<void>
markFailed(jobId: ObjectId, error: string): Promise<void>
```

### EnrichmentEventRepository (New)
```typescript
recordEvent(eventData: CreateEnrichmentEventDto): Promise<EnrichmentEvent>
getHistory(targetType: string, targetId: ObjectId): Promise<EnrichmentEvent[]>
```
