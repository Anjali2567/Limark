# Query Optimization Guide — leadplus-service

## Table of Contents
- [Core Principles](#core-principles)
- [When to Use Indexes](#when-to-use-indexes)
- [Query Design Best Practices](#query-design-best-practices)
- [Anti-Patterns to Avoid](#anti-patterns-to-avoid)
- [Reading explain() Output](#reading-explain-output)
- [Performance Benchmarks](#performance-benchmarks)

---

## Core Principles

1. **Every query on a large collection must have an index.** If `lead_contacts` or `lead_companies`
   has more than 10,000 documents and your query has no index, you are scanning the entire collection.

2. **Design indexes alongside queries, not after.** Before writing a new repository method or
   `MongoTemplate` query, think: "What index does this need?"

3. **Scan ratio must be < 10.** This means: for every 10 documents MongoDB examines, at least
   1 must be returned. A ratio of 947 (as seen pre-optimization) means 946 documents were
   wasted work.

---

## When to Use Indexes

| Situation | Use Index? | Notes |
|---|---|---|
| Query on collection > 10,000 docs | ✅ Yes | Always |
| Filter by `active: true` + another field | ✅ Yes | Compound index with `active` first |
| Sort by a field on a large result set | ✅ Yes | Include sort field in index |
| Range query (`$gte`, `$lte`) | ✅ Yes | Put range field last in compound index (ESR rule) |
| Query by `_id` only | ❌ Not needed | Default `_id` index handles this |
| Query on a collection < 1,000 docs | ❌ Optional | Full scan is fast on tiny collections |
| `$or` across multiple different fields | ⚠️ Avoid | Hard to index — redesign the query |
| Regex without `^` anchor | ⚠️ Avoid | Regex can't use indexes unless left-anchored |

---

## Query Design Best Practices

### 1. Always Put `active: true` First in Compound Indexes

```java
// ✅ Good — active filters out ~50% of documents immediately
criteriaList.add(Criteria.where("active").is(true));
criteriaList.add(Criteria.where("industry").in(industries));

// Index: { active: 1, industry: 1 }
```

### 2. Avoid `$or` Across Different Fields

`$or` across different fields forces MongoDB to evaluate each branch separately.
It often cannot use a single index and may fall back to COLLSCAN.

```java
// ❌ Bad — $or across 2 fields, can't use a single index
return new Criteria().orOperator(
    Criteria.where("name").in(patterns),
    Criteria.where("domain").in(patterns)
);

// ✅ Good — use the primary indexed field only
return Criteria.where("name").in(patterns);
// INDEX REQUIRED: { active: 1, name: 1 }
```

### 3. Left-Anchor Regex Patterns When Possible

```java
// ❌ Bad — middle match, cannot use index
Pattern.compile(Pattern.quote(value), Pattern.CASE_INSENSITIVE)
// Becomes: /acme/i  — MongoDB must scan every document

// ✅ Better — left-anchored, CAN use index
Pattern.compile("^" + Pattern.quote(value) + "$", Pattern.CASE_INSENSITIVE)
// Becomes: /^acme$/i  — MongoDB uses the index prefix
```

### 4. Use Field Projection on Large Result Sets

When you only need a few fields, tell MongoDB to only fetch those fields.
This reduces the data transferred from disk to memory significantly.

```java
// ❌ Bad — fetches all fields even though we only need _id
Query query = new Query(criteria);
mongoTemplate.find(query, LeadCompany.class);

// ✅ Good — only fetch _id (used in CompanyLeadSearchService)
Query query = new Query(criteria);
query.fields().include("_id");  // fetches ONLY the _id field
mongoTemplate.find(query, LeadCompany.class);
```

### 5. Document Index Dependencies in Code

Every repository method and MongoTemplate query that depends on an index
**must have an `// INDEX REQUIRED` comment**.

```java
// INDEX REQUIRED: { leadCompanyId: 1, active: 1, email: 1 } on lead_contacts
@Query("{ 'leadCompanyId': ?0, 'email': { $exists: true, $ne: null, $ne: '' }, 'active': true }")
Page<LeadContact> findValidContacts(String companyId, Pageable pageable);
```

### 6. Limit Large Dynamic Queries

When running dynamic filter queries that could match thousands of company IDs,
always set a hard limit.

```java
// ✅ Good — prevents runaway queries
Query query = new Query(combinedCriteria).limit(MAX_COMPANY_IDS); // MAX = 20,000
```

---

## Anti-Patterns to Avoid

### ❌ Anti-Pattern 1: `$or` with Regex Across Multiple Fields

```java
// This is the pattern that caused scan ratio 947 in leadplus-service
Criteria[] fieldCriteria = fields.stream()
    .map(field -> Criteria.where(field).in(patterns))  // regex on each field
    .toArray(Criteria[]::new);
return new Criteria().orOperator(fieldCriteria);        // $or = no index
```

**Fix:** Use the primary indexed field only. See `CriteriaUtils.multiFieldCaseInsensitiveContains`.

---

### ❌ Anti-Pattern 2: Count Query Without Index

```java
// ❌ Bad — counts ALL documents matching active=true (full scan if no index)
long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), LeadContact.class);
```

Make sure the criteria used in count queries are also covered by an index.

---

### ❌ Anti-Pattern 3: `findDistinct` Without Index

```java
// In CompanyLeadSearchService — this query needs an index on lead_contacts
mongoTemplate.findDistinct(q, "leadCompanyId", "lead_contacts", String.class);
// INDEX REQUIRED: { active: 1, leadCompanyId: 1, email: 1 }
```

---

## Reading `explain()` Output

Run this to analyze any query:
```javascript
db.<collection>.find(<query>).explain("executionStats")
```

### Key Fields to Look At

```
executionStats.nReturned          — documents returned to caller
executionStats.totalDocsExamined  — documents read from disk
executionStats.totalKeysExamined  — index entries scanned
executionStats.executionTimeMillis — total query time in ms

winningPlan.stage                 — FETCH (normal) or COLLSCAN (bad)
winningPlan.inputStage.stage      — IXSCAN (good) or COLLSCAN (bad)
winningPlan.inputStage.indexName  — which index was used
```

### How to Calculate Scan Ratio

```
Scan Ratio = totalDocsExamined / nReturned

Ratio = 1        ✅ Perfect  — 1 doc examined per result
Ratio = 1–10     ✅ Good     — acceptable
Ratio = 10–100   ⚠️ Warning  — consider adding/improving indexes
Ratio > 100      ❌ Critical — index missing or wrong query design
Ratio = 947      🔴 ALERT    — what leadplus had before optimization
```

### Example: Good Output (IXSCAN)

```json
"winningPlan": {
    "stage": "FETCH",
    "inputStage": {
        "stage": "IXSCAN",
        "indexName": "active_1_updatedAt_-1",
        "indexBounds": {
            "active": ["[true, true]"],
            "updatedAt": ["[MaxDate, 2025-01-01)"]
        }
    }
}
```
✅ `IXSCAN` — index is being used. Scan ratio = 1.0.

### Example: Bad Output (COLLSCAN)

```json
"winningPlan": {
    "stage": "COLLSCAN",
    "filter": { "active": { "$eq": true } }
}
```
❌ `COLLSCAN` — no index. MongoDB reads every document. Fix: create an index.

### Understanding `rejectedPlans`

MongoDB's query optimizer evaluates all available indexes and picks the best one.
`rejectedPlans` shows what it considered but didn't choose — this is normal and healthy.
If `rejectedPlans` is empty, it means only one viable plan existed.

---

## Performance Benchmarks

These are the target metrics for this project:

| Metric | Target | Pre-optimization | Post-optimization |
|---|---|---|---|
| Query Targeting Scan Ratio | < 10 | 947.1 🔴 | ~1.0 ✅ |
| Average query time | < 50ms | 200–500ms | 10–50ms |
| CPU usage during queries | Normal | Very High | Normal |
| COLLSCAN on large collections | 0 | Multiple | 0 |