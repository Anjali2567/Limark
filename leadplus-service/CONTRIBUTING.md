# Contributing to leadplus-service

Thank you for contributing! Please read this guide before submitting a pull request.

---

## Table of Contents
- [Getting Started](#getting-started)
- [Code Style](#code-style)
- [Pull Request Process](#pull-request-process)
- [Database Query Checklist](#database-query-checklist)
- [Testing Requirements](#testing-requirements)

---

## Getting Started

1. Clone the repository
2. Ensure you have Java 17+ and Maven installed
3. Copy `application.yml` config and configure your local MongoDB connection
4. Run the application: `./mvnw spring-boot:run`

---

## Code Style

- Follow existing package structure and naming conventions
- Use Lombok annotations (`@Data`, `@Builder`, `@RequiredArgsConstructor`) consistently
- All new services must be annotated with `@Service` and use constructor injection
- DTOs must have `toDto()` and `toEntity()` static factory methods following existing patterns

---

## Pull Request Process

1. Create a feature branch from `main`: `git checkout -b feature/your-feature-name`
2. Make your changes
3. Ensure all tests pass: `./mvnw test`
4. Complete the **Database Query Checklist** below if your PR touches any MongoDB queries
5. Request a code review from at least one team member
6. Squash commits before merging

---

## Database Query Checklist

> ⚠️ **This checklist is mandatory for any PR that adds or modifies MongoDB queries.**
> A missing index on a large collection can cause query scan ratios above 900 and
> bring down the entire service. See the incident: Query Targeting scan ratio 947.1 (2026-04-26).

Before submitting your PR, confirm every item below:

---

### ✅ 1. Query Has `explain()` Analysis

Run `explain("executionStats")` on every new query before merging.

```javascript
db.<collection>.find(<your-query>).explain("executionStats")
```

Paste the relevant section (at minimum `winningPlan` and scan ratio) as a comment in your PR.

---

### ✅ 2. No COLLSCAN on Large Collections

In your `explain()` output, confirm the winning plan does **not** contain `COLLSCAN`:

```json
// ✅ Good
"winningPlan": { "inputStage": { "stage": "IXSCAN" } }

// ❌ Bad — do NOT merge
"winningPlan": { "stage": "COLLSCAN" }
```

A `COLLSCAN` on `lead_contacts`, `lead_companies`, or `campaigns` is a **merge blocker**.

---

### ✅ 3. Scan Ratio < 10

Calculate the scan ratio from your `explain()` output:

```
Scan Ratio = totalDocsExamined / nReturned
```

| Ratio | Status |
|---|---|
| ≤ 10 | ✅ Safe to merge |
| 10–100 | ⚠️ Requires tech lead approval |
| > 100 | ❌ Merge blocked — fix index first |

---

### ✅ 4. Index Documented in Code Comments

Every repository method or `MongoTemplate` query that depends on an index
**must have an `// INDEX REQUIRED` comment directly above it.**

```java
// ✅ Required format:
// INDEX REQUIRED: { active: 1, leadCompanyId: 1 } on lead_contacts
Page<LeadContact> findAllByLeadCompanyIdAndActiveTrue(String companyId, Pageable pageable);
```

```java
/*
 * Queries lead_contacts collection.
 * INDEXES REQUIRED on lead_contacts:
 *   { active: 1, leadCompanyId: 1 }
 *   { active: 1, updatedAt: -1 }
 */
private Page<LeadContact> findContacts(LeadFilterDto req, ...) { ... }
```

---

### ✅ 5. New Index Documented in `docs/MONGODB_INDEXES.md`

If your PR creates a new MongoDB index, you must:

1. Add the index creation command to the correct collection section in
   [`docs/MONGODB_INDEXES.md`](docs/MONGODB_INDEXES.md)
2. Add a row to the index inventory table for that collection
3. Note which code depends on it

---

### ✅ 6. No `$or` Across Multiple Fields with Regex

Do not introduce the following pattern — it prevents index usage:

```java
// ❌ Never do this
Criteria[] fieldCriteria = fields.stream()
    .map(field -> Criteria.where(field).in(regexPatterns))
    .toArray(Criteria[]::new);
return new Criteria().orOperator(fieldCriteria);
```

Use `CriteriaUtils.multiFieldCaseInsensitiveContains()` which safely delegates to
the primary indexed field only.

---

## Testing Requirements

- Unit tests are required for all new service methods
- Repository query changes require a corresponding integration test
- All tests must pass before requesting review: `./mvnw test`

---

## Further Reading

- [MongoDB Index Strategy](docs/MONGODB_INDEXES.md)
- [Query Optimization Guide](docs/QUERY_OPTIMIZATION.md)