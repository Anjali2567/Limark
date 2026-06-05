# Intelligence Service MVP - Product Specification

**Version**: 1.0
**Owner**: Intelligence Layer Team
**Status**: Draft for Review
**Last Updated**: 2025-11-11

---

## 1. Executive Summary

### What We're Building
A **Manufacturing Intelligence Database Service** that automatically discovers, enriches, and maintains a shared database of manufacturing companies and their contacts. This service acts as the data backbone for the Leadplus campaign platform, providing high-quality, categorized account and contact data through API endpoints.

### Why Now
The Leadplus platform cannot generate targeted manufacturing campaigns without verified, enriched data. Currently, there's no centralized intelligence layer - manual data entry is slow, error-prone, and doesn't scale. This service solves that by automating data discovery and enrichment.

### Success Criteria
- **Data Coverage**: 10,000+ enriched manufacturing accounts within 90 days of MVP launch
- **Data Quality**: 80%+ of accounts have accurate industry keywords and categories
- **Enrichment Speed**: Website crawl and categorization completes within 5 minutes per account
- **API Reliability**: 99.5% uptime for query endpoints
- **Query Performance**: Search results return within 500ms for 95th percentile

---

## 2. Problem Statement

### Current State
- No centralized database of manufacturing companies and contacts
- Manual data entry required for every campaign
- No way to discover or categorize companies at scale
- Contact persona identification is manual and inconsistent
- Data becomes stale with no refresh mechanism
- Product team has no intelligence layer to query for campaign generation

### Pain Points
1. **For Product Team**: Cannot build campaign generation features without reliable data source
2. **For Future Users**: Will receive irrelevant targeting if underlying data is poor quality
3. **For Operations**: Manual data curation doesn't scale beyond 100s of accounts

### Impact of Not Solving
- Leadplus campaign MVP delayed 6-8 weeks
- Poor campaign targeting leads to low engagement rates
- Manual data maintenance becomes operational bottleneck
- Cannot differentiate from competitors who use generic B2B databases

---

## 3. Goals & Non-Goals

### In Scope (MVP)
✅ Automated website discovery and crawling for company information
✅ AI-powered categorization of companies by industry/keywords/capabilities
✅ Contact persona inference from job titles
✅ Enrichment job tracking and status monitoring
✅ Query APIs for searching/filtering accounts and contacts
✅ Campaign analytics endpoint (match criteria to available data)
✅ Background job processing for enrichment tasks
✅ Data quality scoring and staleness tracking
✅ Basic deduplication logic

### Out of Scope (Post-MVP)
❌ Multi-tenant data isolation (handled by product layer)
❌ Third-party enrichment APIs (Apollo, Clay, Seamless)
❌ Web admin portal (use CLI/logs instead)
❌ CSV/CRM export functionality (product layer responsibility)
❌ LinkedIn profile scraping (legal/compliance review needed)
❌ Real-time enrichment triggers
❌ Advanced ML models for persona classification
❌ Regional enrichment rules (US/EU/APAC)
❌ Company news/signals monitoring

### Key Assumptions
- Product layer handles authentication and tenant access control
- Initial data seeding (first 100-500 companies) provided manually or via existing sources
- OpenAI/Claude API access available for categorization
- Hosting environment supports background job workers
- Legal/compliance approved for website scraping at scale

---

## 4. User Stories & Use Cases

### Primary Users
1. **Leadplus Product/Backend** - Consumes intelligence APIs for campaign generation
2. **Intelligence Service Operators** - Monitor enrichment jobs, trigger manual enrichment
3. **Future: Leadplus End Users** - Indirectly benefit from quality data in campaigns

---

### User Story 1: Discover & Categorize New Company
**As a** Intelligence Service Operator
**I want to** add a new company domain and have it automatically enriched
**So that** the company data is available for campaign targeting within minutes

**Acceptance Criteria**:
- Operator provides company domain (e.g., "fanucamerica.com")
- System crawls website and extracts company information
- AI categorizes company with industry keywords (e.g., "robotics", "automation", "CNC")
- Enrichment completes within 5 minutes
- Enrichment status is trackable via API
- Data quality confidence score is calculated
- Enrichment source is tracked (website_crawler, timestamp)

**User Flow**:
```
1. Operator → POST /api/accounts {name: "FANUC America", domain: "fanucamerica.com"}
2. System → Creates account record with status "pending"
3. System → Auto-triggers enrichment job
4. Background Worker → Crawls website
5. Background Worker → Extracts content, sends to AI for categorization
6. Background Worker → Updates account with keywords: ["robotics", "automation", "CNC machining"]
7. Operator → GET /api/accounts/{id} → sees enriched data
```

---

### User Story 2: Infer Contact Persona from Title
**As a** Intelligence Service Operator
**I want** contact personas automatically inferred from job titles
**So that** campaigns can target the right decision-makers

**Acceptance Criteria**:
- Contact job title is analyzed (e.g., "Director of Procurement")
- System infers persona (e.g., "procurement") with confidence score
- Persona is stored and queryable
- Inference method is tracked (title_match, ai_classification)
- Multiple personas can be assigned if title is ambiguous

**User Flow**:
```
1. Operator → POST /api/contacts {firstName: "John", lastName: "Doe", title: "VP of Supply Chain"}
2. System → Creates contact record
3. System → Auto-triggers persona inference
4. Background Worker → Analyzes title "VP of Supply Chain"
5. Background Worker → Infers persona: "supply_chain" with 85% confidence
6. System → Updates contact record
7. Product Layer → Queries contacts with persona filter
```

---

### User Story 3: Search for Campaign Targets
**As the** Leadplus Product Backend
**I want to** query accounts matching specific criteria
**So that** I can generate targeted campaign lists

**Acceptance Criteria**:
- Can filter accounts by keywords (e.g., "robotics", "food equipment")
- Can filter by location (state, country)
- Can filter by company size, industry
- Results include pagination
- Results include data quality indicators (confidence score, last enriched date)
- Query responds within 500ms for 95th percentile

**Example API Request**:
```json
GET /api/intelligence/accounts/search?
  keywords=robotics,automation
  &location.state=TX
  &size.employees_min=50
  &page=1
  &limit=20
```

---

### User Story 4: Match Campaign Criteria
**As the** Leadplus Product Backend
**I want to** preview how many accounts/contacts match campaign criteria
**So that** users can validate targeting before launching campaigns

**Acceptance Criteria**:
- Accepts campaign criteria (keywords, location, personas)
- Returns count of matching accounts and contacts
- Returns preview of first 10 matches
- Includes breakdown by persona distribution
- Response includes pagination info for full results

**Example API Request**:
```json
POST /api/intelligence/campaigns/match
{
  "criteria": {
    "keywords": ["robotics", "manufacturing"],
    "location": {"state": "TX", "country": "US"},
    "personas": ["procurement", "supply_chain"]
  }
}

Response:
{
  "matched_accounts": 45,
  "matched_contacts": 127,
  "persona_breakdown": {
    "procurement": 68,
    "supply_chain": 59
  },
  "accounts_preview": [...],
  "contacts_preview": [...]
}
```

---

### User Story 5: Monitor Enrichment Status
**As an** Intelligence Service Operator
**I want to** track enrichment job progress and failures
**So that** I can troubleshoot issues and ensure data quality

**Acceptance Criteria**:
- Can query enrichment job status by job ID
- Can view failed enrichment jobs with error details
- Can see queue depth and processing rate
- Can retry failed jobs
- Logs include detailed error messages for debugging

**User Flow**:
```
1. Operator → POST /api/enrichment/trigger {target_type: "account", target_id: "xxx"}
2. System → Returns {job_id: "enrich_123", status: "queued"}
3. Operator → GET /api/enrichment/jobs/enrich_123
4. System → Returns {status: "in_progress", progress: 60%, started_at: ...}
5. Wait...
6. Operator → GET /api/enrichment/jobs/enrich_123
7. System → Returns {status: "completed", result: {...}, completed_at: ...}
```

---

### User Story 6: Maintain Data Freshness
**As the** Intelligence Service System
**I want to** automatically refresh stale account data
**So that** campaign targeting uses current information

**Acceptance Criteria**:
- Accounts not enriched in 7+ days marked as "stale"
- Weekly batch job re-enriches stale accounts
- Failed enrichments retry with exponential backoff
- Enrichment history preserved (not overwritten)
- Operators can trigger manual refresh for specific accounts

**Background Job Logic**:
```
Weekly Cron Job:
1. Query accounts where last_enriched_at < 7 days ago
2. Enqueue enrichment jobs for each account
3. Process queue with rate limiting (respect crawling limits)
4. Update last_enriched_at on success
5. Mark as "failed" with error message on repeated failures
6. Send alert if failure rate > 20%
```

---

## 5. Features & Capabilities

### Feature 1: Website Crawler & Content Extraction
**Description**: Automated web scraping service that visits company websites and extracts relevant information.

**Capabilities**:
- Crawl multiple pages (homepage, about, products, services)
- Extract company name, description, product/service listings
- Handle JavaScript-heavy sites (require headless browser)
- Respect robots.txt and crawl delays
- Handle timeouts and errors gracefully
- Store raw HTML/content for future re-analysis

**Success Metrics**:
- 90%+ crawl success rate
- Average crawl time < 3 minutes per domain
- < 5% false positives (extracting wrong company info)

---

### Feature 2: AI-Powered Categorization
**Description**: Uses LLM APIs to analyze website content and extract industry keywords, capabilities, and categories.

**Capabilities**:
- Send extracted content to AI with structured prompt
- Extract industry keywords (e.g., "robotics", "CNC", "injection molding")
- Identify capabilities (e.g., "ISO9001", "ITAR certified", "5-axis machining")
- Categorize by supplier tier (OEM, Tier 1, Tier 2) when possible
- Assign confidence score based on content quality
- Handle multi-industry companies (assign multiple keywords)

**AI Prompt Example**:
```
Analyze this manufacturing company website and extract:
1. Primary industry keywords (3-5 terms)
2. Manufacturing capabilities (certifications, processes, equipment)
3. Supplier tier if identifiable (OEM, Tier 1, Tier 2, Tier 3)
4. Product categories

Website content: [extracted text]

Return JSON format with confidence scores.
```

**Success Metrics**:
- 80%+ keyword accuracy (validated by manual review)
- 70%+ capability extraction accuracy
- Average AI processing time < 30 seconds per company

---

### Feature 3: Contact Persona Inference
**Description**: Analyzes contact job titles to infer functional role/persona for campaign targeting.

**Capabilities**:
- Match job title against persona keyword dictionary
- Assign primary persona (procurement, supply chain, operations, etc.)
- Calculate confidence score (high for exact matches, low for ambiguous titles)
- Handle variations and synonyms (e.g., "buyer" = procurement)
- Support multiple persona assignment for hybrid roles

**Persona Categories**:
- Procurement
- Supply Chain
- Operations
- Product Design/Engineering
- Quality
- Executive
- Sales
- Unknown

**Success Metrics**:
- 75%+ persona accuracy (validated by manual review)
- < 10% "unknown" classification rate
- Processing time < 1 second per contact

---

### Feature 4: Enrichment Job Queue
**Description**: Background job processing system for asynchronous enrichment tasks.

**Capabilities**:
- Queue enrichment jobs (account crawl, contact inference, batch refresh)
- Process jobs asynchronously with worker threads/processes
- Track job status (queued, in_progress, completed, failed)
- Retry failed jobs with exponential backoff
- Priority queue (manual triggers > event-driven > batch jobs)
- Rate limiting to respect crawling limits
- Dead letter queue for repeatedly failed jobs

**Job Types**:
- `account_website_crawl` - Crawl and categorize company website
- `contact_persona_inference` - Infer persona from title
- `batch_refresh` - Re-enrich stale accounts
- `validation_job` - Validate email/domain freshness

**Success Metrics**:
- Job processing latency < 10 minutes (95th percentile)
- Failed job retry success rate > 60%
- Queue never grows beyond 1000 pending jobs

---

### Feature 5: Data Quality Tracking
**Description**: Monitors and scores data quality to ensure reliable campaign targeting.

**Capabilities**:
- Track enrichment status per record (pending, enriched, stale, failed)
- Calculate confidence scores (0-100) based on:
  - Data completeness (all fields populated)
  - Source reliability (website crawl vs. manual entry)
  - Freshness (recently enriched = higher score)
- Track enrichment sources (which system updated which fields)
- Identify duplicate accounts/contacts
- Flag stale data (not refreshed in 7+ days)

**Quality Metrics Exposed**:
- % of accounts enriched
- % of accounts with high confidence (>70)
- % of contacts with persona assigned
- Average data freshness (days since last enrichment)
- Top failing domains (crawl errors)

**Success Metrics**:
- 80%+ of accounts have confidence score >70
- < 10% stale data at any given time
- < 5% duplicate records

---

### Feature 6: Query & Search APIs
**Description**: REST APIs for product layer to search and filter enriched data.

**Capabilities**:
- Search accounts by keywords, location, industry, size
- Search contacts by persona, department, account
- Full-text search on company names, descriptions
- Compound filters (AND/OR logic)
- Pagination (page-based and cursor-based)
- Sorting by relevance, confidence, freshness
- Return enrichment metadata (confidence, last_enriched_at, sources)

**API Endpoints**:
```
GET /api/intelligence/accounts/search
GET /api/intelligence/contacts/search
POST /api/intelligence/campaigns/match
GET /api/intelligence/stats
GET /api/intelligence/quality/metrics
```

**Success Metrics**:
- Query response time < 500ms (95th percentile)
- Support 100+ concurrent API requests
- 99.5% API uptime

---

### Feature 7: Enrichment Triggers
**Description**: Multiple ways to trigger enrichment jobs based on context.

**MVP Triggers**:
1. **Manual/API Trigger** (Phase 1)
   - Operator calls API to enrich specific account/contact
   - Used for immediate needs or troubleshooting

2. **Event-Driven Trigger** (Phase 2)
   - New account created → auto-enqueue website crawl
   - New contact created → auto-enqueue persona inference
   - Used for just-in-time enrichment

3. **Batch/Scheduled Trigger** (Phase 3)
   - Weekly cron job: refresh stale accounts
   - Daily cron job: process pending enrichment queue
   - Used for proactive data maintenance

**Out of Scope for MVP**:
- Real-time on-demand triggers (enrich during query)

---

### Feature 8: Deduplication Logic
**Description**: Identifies and handles duplicate records to maintain data integrity.

**Capabilities**:
- Detect duplicate accounts by domain (exact match)
- Detect duplicate contacts by email (exact match)
- Flag duplicates for manual review
- Basic merge logic (keep record with higher confidence score)
- Preserve enrichment history from both duplicates

**Deduplication Rules**:
- Accounts: Same domain = duplicate
- Contacts: Same email = duplicate
- On conflict: Keep record with most recent enrichment + higher confidence

**Success Metrics**:
- < 2% duplicate accounts in database
- < 3% duplicate contacts in database
- Duplicate detection runs weekly

---

## 6. Technical Requirements

### Performance
- API query response time: < 500ms (95th percentile)
- Website crawl time: < 5 minutes per domain
- Persona inference time: < 1 second per contact
- Background job processing: < 10 minutes queue latency
- Database query time: < 100ms for filtered searches

### Scalability
- Support 100,000+ accounts in database
- Support 500,000+ contacts in database
- Handle 100+ concurrent API requests
- Process 1,000+ enrichment jobs per day

### Reliability
- API uptime: 99.5%
- Data durability: 99.99% (MongoDB with replication)
- Failed job retry: 3 attempts with exponential backoff
- Graceful degradation: return cached data if enrichment fails

### Security
- API authentication via API keys
- Rate limiting: 100 requests/minute per API key
- No sensitive data in logs (PII, API keys)
- Respect robots.txt and crawl politeness

### Compliance
- GDPR-aware data storage (contact data can be deleted on request)
- Respect robots.txt and website Terms of Service
- No scraping of blocked/private content
- Email validation respects anti-spam rules

---

## 7. Dependencies

### External Dependencies
- **AI Provider** (OpenAI or Claude API)
  - Purpose: Website content categorization
  - Risk: API cost, rate limits, outages
  - Mitigation: Fallback to keyword extraction if AI unavailable

- **Web Scraping Library** (Playwright/Puppeteer)
  - Purpose: Headless browser for crawling
  - Risk: Maintenance, breaking changes
  - Mitigation: Containerized deployment, version pinning

- **Job Queue System** (BullMQ + Redis or MongoDB-based)
  - Purpose: Background job processing
  - Risk: Redis dependency, queue management complexity
  - Mitigation: Health checks, queue monitoring

### Internal Dependencies
- **Leadplus Product Backend** (Consumer)
  - Dependency: Stable query APIs
  - Integration: API contract must be maintained
  - Communication: API versioning strategy

- **Deployment Infrastructure** (AWS ECS)
  - Dependency: Container orchestration, environment variables
  - Risk: Deployment failures, downtime
  - Mitigation: CI/CD pipeline, health checks

### Data Dependencies
- **Initial Data Seeding**
  - Need 100-500 company domains to start
  - Manual entry or CSV import
  - Required before MVP launch

---

## 8. Success Metrics & KPIs

### Data Metrics (30 days post-MVP)
- **Target**: 10,000+ enriched accounts
- **Target**: 50,000+ contacts with persona assigned
- **Target**: 80%+ accounts with keywords/categories
- **Target**: 70%+ contacts with persona (not "unknown")

### Quality Metrics
- **Target**: 80%+ keyword accuracy (manual validation sample)
- **Target**: 75%+ persona accuracy (manual validation sample)
- **Target**: < 10% stale data (older than 7 days)
- **Target**: Average confidence score > 70

### Performance Metrics
- **Target**: API p95 latency < 500ms
- **Target**: Crawl success rate > 90%
- **Target**: Job processing latency < 10 minutes

### Operational Metrics
- **Target**: 99.5% API uptime
- **Target**: < 5% failed enrichment jobs (non-retriable)
- **Target**: Zero data breaches or security incidents

### Business Metrics (Post-Launch)
- **Target**: Product team launches campaign generation feature using intelligence APIs
- **Target**: 5+ beta customers use campaigns powered by intelligence data
- **Target**: Campaign targeting accuracy > 70% (validated by user feedback)

---

## 9. Risks & Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Website crawling blocked by anti-bot measures | High | High | Use residential proxies, rotate user agents, respect rate limits |
| AI categorization inaccurate for niche industries | Medium | High | Manual review workflow, confidence scoring, allow manual overrides |
| Enrichment costs too high (AI API usage) | Medium | Medium | Batch processing, cache results, optimize prompts |
| Job queue grows unbounded (slow processing) | Medium | High | Worker auto-scaling, queue monitoring alerts, priority levels |
| Legal issues with website scraping | Low | High | Legal review, respect robots.txt, scrape only public data |
| Data becomes stale quickly | Medium | Medium | Weekly batch refresh, prioritize high-value accounts |
| API becomes bottleneck for product | Low | High | Caching layer, query optimization, horizontal scaling |

---

## 10. MVP Milestones & Timeline

### Phase 0: Foundation (Week 1)
**Goal**: Prepare data models and infrastructure

**Deliverables**:
- Extended data schemas for enrichment tracking
- Database migrations
- EnrichmentJob collection created
- Repository methods updated

**Success Criteria**:
- All existing tests pass
- New enrichment fields queryable

---

### Phase 1: Website Crawler (Weeks 2-3)
**Goal**: Build core crawling and categorization capability

**Deliverables**:
- Website crawler service (handles HTML extraction)
- AI categorization service (sends content to LLM)
- Account enrichment orchestrator
- API endpoint: `POST /api/enrichment/account/:id/crawl`
- CLI command for manual enrichment

**Success Criteria**:
- Successfully crawl and categorize 10 sample companies
- 80%+ keyword accuracy on validation set
- < 5 minute average crawl time

---

### Phase 2: Persona Inference (Week 4)
**Goal**: Enable contact persona classification

**Deliverables**:
- Persona inference engine (title keyword matching)
- Contact enrichment service
- API endpoint: `POST /api/enrichment/contact/:id/infer-persona`
- CLI command for bulk contact enrichment

**Success Criteria**:
- Accurately classify 75%+ of test contacts
- < 15% "unknown" classification rate
- < 1 second processing time per contact

---

### Phase 3: Background Jobs (Week 5)
**Goal**: Enable asynchronous and scheduled enrichment

**Deliverables**:
- Job queue infrastructure (BullMQ or MongoDB-based)
- Enrichment workers (process queued jobs)
- Event-driven triggers (new account/contact → enqueue job)
- Batch job scheduler (weekly stale data refresh)
- Job status API: `GET /api/enrichment/jobs/:id`

**Success Criteria**:
- Process 100+ enrichment jobs per hour
- Queue latency < 10 minutes
- Failed job retry success rate > 50%

---

### Phase 4: Query APIs (Week 6)
**Goal**: Enable product layer to query enriched data

**Deliverables**:
- Search APIs for accounts and contacts
- Campaign match analytics endpoint
- Data quality metrics API
- API documentation (Swagger/OpenAPI)

**Success Criteria**:
- Query response time < 500ms (95th percentile)
- Successfully power campaign generation in product layer
- Handle 100+ concurrent requests

---

### Phase 5: Data Quality (Week 7)
**Goal**: Ensure data freshness and accuracy

**Deliverables**:
- Validation jobs (email/domain checks)
- Deduplication logic
- Confidence scoring algorithm
- Quality metrics dashboard API

**Success Criteria**:
- < 10% stale data at any time
- < 5% duplicate records
- 80%+ accounts with confidence > 70

---

### Phase 6: Production Readiness (Week 8)
**Goal**: Deploy to production with monitoring

**Deliverables**:
- Complete API documentation
- Production deployment (ECS + workers)
- Monitoring dashboards (queue health, API metrics)
- Load testing results
- Security audit complete

**Success Criteria**:
- 99.5% API uptime
- All tests passing in CI/CD
- Product team integrated and using APIs
- Ready for beta customer launch

---

## 11. Open Questions

### Technical Decisions Needed
1. **Queue System**: BullMQ (Redis-based) or MongoDB-based queue?
2. **AI Provider**: OpenAI GPT-4 or Claude Sonnet for categorization?
3. **Deployment**: Separate ECS service for workers or same service?
4. **Caching**: Add Redis caching layer for query APIs?

### Product Decisions Needed
1. **LinkedIn Scraping**: Include in MVP or defer pending legal review?
2. **Manual Override**: Allow operators to manually edit AI-generated keywords?
3. **Confidence Threshold**: What confidence score qualifies as "high quality"?
4. **Refresh Frequency**: Weekly batch refresh sufficient or need daily?

### Compliance Questions
1. **Scraping Policy**: Legal review completed for website crawling at scale?
2. **GDPR**: Data retention policy for contact information?
3. **Anti-Spam**: Email validation approach compliant with CAN-SPAM?

---

## 12. Appendix

### Glossary
- **Enrichment**: Process of adding intelligence to account/contact records
- **Confidence Score**: 0-100 rating indicating data quality/reliability
- **Enrichment Source**: System that contributed data (crawler, API, manual)
- **Persona**: Functional role classification (procurement, supply chain, etc.)
- **Stale Data**: Records not refreshed in 7+ days
- **Campaign Match**: API that previews targeting coverage for campaign criteria

### Related Documents
- PRD: Leadplus.ai Manufacturing Campaign Agent MVP
- Architecture Diagram: [TBD]
- API Contract: [TBD]
- Data Privacy Policy: [TBD]

---

**Approval Sign-off**:
- [ ] Product Lead
- [ ] Engineering Lead
- [ ] Operations Lead
- [ ] Legal/Compliance
