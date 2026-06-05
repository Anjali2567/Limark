# Enrichment Pipeline Architecture

## System Overview

```
Trigger → Queue → Worker → Crawler/AI → Storage → Audit
   ↓         ↓       ↓          ↓           ↓        ↓
  API    MongoDB   Worker   Playwright   Account  Event Log
  Event            Process    + AI API
  Cron
```

---

## Components

### 1. Enrichment Service
**Location**: `src/services/enrichment/`

**Responsibilities**:
- Orchestrates enrichment workflow
- Validates inputs
- Enqueues jobs
- Coordinates crawler + AI services
- Updates records with results

**Key Methods**:
```typescript
enrichAccount(accountId: ObjectId, source: 'api' | 'event' | 'cron'): Promise<EnrichmentJob>
enrichContact(contactId: ObjectId): Promise<EnrichmentJob>
processJob(job: EnrichmentJob): Promise<void>
```

---

### 2. Website Crawler
**Location**: `src/services/scraper/website-crawler.ts`

**Proven Tech**: Playwright (stealth mode from marketing project)

**Flow**:
```
1. Fetch robots.txt, respect crawl delays
2. Launch headless browser (stealth mode)
3. Navigate to homepage
4. Extract initial content
5. Follow links (about, products, services) - max 2 levels
6. Extract ~9K words of clean text
7. Return structured content
```

**Output**:
```typescript
interface CrawlResult {
  domain: string
  pages_crawled: number
  content: {
    homepage: string
    about?: string
    products?: string
  }
  metadata: {
    title: string
    description?: string
    duration_ms: number
  }
  success: boolean
  error?: string
}
```

---

### 3. AI Categorization Service
**Location**: `src/services/ai/categorization.ts`

**Provider**: Claude 3.5 Sonnet (or Gemini Flash for cost optimization)

**Prompt Strategy**:
```typescript
const CATEGORIZATION_PROMPT = `
Analyze this manufacturing company website and extract:

1. Industry Keywords (3-5 terms): Specific technologies, processes, materials
2. Categories: OEM, Tier1, Tier2, Tier3, or Distributor
3. Capabilities: Certifications (ISO9001, ITAR, AS9100), processes, equipment

Website Content:
{content}

Return JSON:
{
  "keywords": ["term1", "term2"],
  "categories": ["OEM"],
  "capabilities": ["ISO9001", "5-axis CNC"],
  "confidence": 85
}
`;
```

**Cost Optimization** (from marketing project):
- Use Gemini Flash for simple extraction
- Reserve Claude for complex reasoning
- Cache frequent prompts
- Batch process when possible

---

### 4. Persona Inference Engine
**Location**: `src/services/persona/persona-inference.ts`

**Strategy**: Keyword matching with fuzzy logic

```typescript
const PERSONA_KEYWORDS = {
  procurement: ['procurement', 'buyer', 'purchasing', 'sourcing', 'supply manager'],
  supply_chain: ['supply chain', 'logistics', 'inventory', 'planner', 'SCM'],
  operations: ['operations', 'production', 'plant', 'manufacturing', 'factory'],
  product_design: ['design', 'engineer', 'R&D', 'development', 'CAD', 'product'],
  executive: ['CEO', 'president', 'VP', 'director', 'C-level', 'head of'],
  quality: ['quality', 'QA', 'QC', 'inspection', 'assurance'],
  engineering: ['engineer', 'technical', 'systems', 'mechanical', 'electrical']
};

function inferPersona(title: string): { persona: ContactPersona, confidence: number } {
  const normalized = title.toLowerCase();

  for (const [persona, keywords] of Object.entries(PERSONA_KEYWORDS)) {
    for (const keyword of keywords) {
      if (normalized.includes(keyword)) {
        return {
          persona: persona as ContactPersona,
          confidence: calculateConfidence(title, keyword)
        };
      }
    }
  }

  return { persona: 'unknown', confidence: 0 };
}
```

---

### 5. Job Queue System
**Location**: `src/queue/`

**MVP Implementation**: MongoDB-based queue (upgrade to BullMQ later if needed)

**Queue Logic**:
```typescript
// Worker polling
async function processQueue() {
  while (true) {
    const job = await jobRepository.getNextJob();  // Priority ordering
    if (!job) {
      await sleep(5000);  // Poll every 5s
      continue;
    }

    await jobRepository.updateStatus(job._id, 'in_progress');

    try {
      await enrichmentService.processJob(job);
      await jobRepository.updateStatus(job._id, 'completed');
    } catch (error) {
      await handleJobFailure(job, error);
    }
  }
}

async function handleJobFailure(job: EnrichmentJob, error: Error) {
  if (job.attempts >= 3) {
    await jobRepository.markFailed(job._id, error.message);
    logger.error('Job failed after 3 attempts', { job, error });
  } else {
    // Exponential backoff
    const delay = Math.pow(2, job.attempts) * 1000;
    await sleep(delay);
    await jobRepository.retry(job._id);
  }
}
```

**Priority Queue**:
- Priority 100: Manual API triggers (immediate)
- Priority 50: Event-driven (new account/contact)
- Priority 10: Batch refresh jobs
- Priority 0: Low-priority background tasks

---

### 6. Enrichment Worker
**Location**: `src/workers/enrichment-worker.ts`

**Deployment**: Same ECS container initially, separate service later if needed

**Process Flow**:
```
1. Poll queue for next job (priority order)
2. Lock job (status = in_progress)
3. Route to appropriate handler:
   - account_website_crawl → Crawler + AI
   - contact_persona_inference → Persona engine
   - batch_refresh → Multiple sub-jobs
4. Update target entity with results
5. Calculate confidence score
6. Record enrichment event (audit)
7. Mark job completed
8. Handle errors with retry logic
```

---

## Data Flow Diagrams

### Account Enrichment Flow
```
POST /api/accounts {domain: "example.com"}
  ↓
AccountRepository.create() → enrichment_status: 'pending'
  ↓
Trigger event: 'account.created'
  ↓
EnrichmentService.enrichAccount()
  ↓
EnrichmentJob.enqueue(type: 'account_website_crawl')
  ↓
Worker.processJob()
  ↓
  ├→ Crawler.scrape(domain)
  ↓    ↓
  │  CrawlResult {content, metadata}
  ↓    ↓
  ├→ AI.categorize(content)
  ↓    ↓
  │  {keywords, categories, capabilities, confidence}
  ↓    ↓
AccountRepository.updateEnrichmentData()
  ↓
EnrichmentEventRepository.recordEvent()
  ↓
Job.status = 'completed'
```

### Contact Persona Inference Flow
```
POST /api/contacts {title: "VP of Supply Chain"}
  ↓
ContactRepository.create()
  ↓
Trigger event: 'contact.created'
  ↓
EnrichmentService.enrichContact()
  ↓
EnrichmentJob.enqueue(type: 'contact_persona_inference')
  ↓
Worker.processJob()
  ↓
PersonaInference.infer(title)
  ↓
{persona: 'supply_chain', confidence: 85}
  ↓
ContactRepository.updatePersona()
  ↓
EnrichmentEventRepository.recordEvent()
  ↓
Job.status = 'completed'
```

---

## Trigger Mechanisms

### 1. API Trigger (Manual)
```typescript
POST /api/enrichment/trigger
{
  "target_type": "account",
  "target_id": "507f1f77bcf86cd799439011",
  "enrichment_type": "website_crawler"
}

Response: { job_id: "...", status: "queued" }
```

### 2. Event-Driven Trigger
```typescript
// In AccountService.create()
async createAccount(data: CreateAccountDto): Promise<Account> {
  const account = await accountRepository.create(data);

  // Auto-trigger enrichment
  await enrichmentService.enrichAccount(account._id, 'event');

  return account;
}
```

### 3. Batch/Scheduled Trigger
```typescript
// Cron job (runs weekly)
async function refreshStaleAccounts() {
  const staleAccounts = await accountRepository.findStale(7);  // > 7 days

  for (const account of staleAccounts) {
    await enrichmentService.enrichAccount(account._id, 'cron');
  }
}
```

---

## Error Handling

### Retry Strategy
- Max 3 attempts per job
- Exponential backoff: 1s, 2s, 4s
- Failed jobs after 3 attempts → dead letter queue
- Alert if failure rate > 20%

### Common Failures
| Error | Mitigation |
|-------|-----------|
| Website blocking/403 | Rotate user agents, add delay, skip domain |
| AI API rate limit | Queue jobs, add backoff, use cheaper model |
| Timeout (slow sites) | 30s timeout, partial results acceptable |
| Invalid/empty content | Mark as 'failed', require manual review |
| Database connection | Retry with backoff, circuit breaker pattern |

---

## Performance Targets

| Metric | Target |
|--------|--------|
| Crawl time per domain | < 3 minutes |
| AI categorization | < 30 seconds |
| Persona inference | < 1 second |
| Job queue latency | < 10 minutes (p95) |
| Queue throughput | 100+ jobs/hour |
| Worker CPU usage | < 70% average |

---

## Monitoring

### Key Metrics
- Queue depth (alert if > 500)
- Job success/failure rates
- Average processing time per job type
- Confidence score distribution
- Stale data percentage

### Logging
```typescript
logger.info('Enrichment job started', {
  job_id,
  job_type,
  target_type,
  target_id
});

logger.info('Crawl completed', {
  domain,
  pages_crawled,
  duration_ms
});

logger.info('AI categorization completed', {
  keywords_count,
  confidence_score,
  cost_cents
});

logger.error('Enrichment job failed', {
  job_id,
  error,
  attempts,
  will_retry
});
```

---

## Scalability Considerations

### Vertical Scaling (MVP)
- Single worker process
- In-memory job processing
- MongoDB connection pooling

### Horizontal Scaling (Future)
- Multiple worker containers
- Redis-based queue (BullMQ)
- Distributed crawling pool
- AI API rate limit sharing
