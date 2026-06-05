# Intelligence API

## Authentication

API key authentication via `X-API-Key` header.

---

## Endpoints

### Search Accounts

Filter and search accounts by criteria.

**Endpoint**: `GET /api/accounts`

**Query Parameters**:
- `keywords` (string, comma-separated): Filter by keywords
- `categories` (string, comma-separated): Filter by categories (OEM, Tier1, etc.)
- `state` (string): US state code
- `country` (string): Country code
- `industry` (string): Industry/vertical
- `min_employees` (number): Minimum employee count
- `max_employees` (number): Maximum employee count
- `min_revenue` (number): Minimum annual revenue (USD)
- `max_revenue` (number): Maximum annual revenue (USD)
- `has_contacts` (boolean): Only accounts with contacts
- `min_confidence` (number): Minimum confidence score (0-100)
- `sort` (string): Sort field - `name`, `contacts_count`, `confidence_score` (default: `name`)
- `order` (string): Sort order - `asc`, `desc` (default: `asc`)
- `page` (number): Page number (default: 1)
- `limit` (number): Results per page (default: 20, max: 100)

**Example**:
```
GET /api/accounts?keywords=robotics,automation&state=TX&min_employees=50&has_contacts=true&sort=contacts_count&order=desc&page=1&limit=20
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "_id": "507f1f77bcf86cd799439011",
      "name": "FANUC America Corporation",
      "domain": "fanucamerica.com",
      "keywords": ["robotics", "automation", "CNC"],
      "categories": ["OEM"],
      "capabilities": ["ISO9001"],
      "location": {
        "state": "TX",
        "city": "Houston",
        "country": "US"
      },
      "size": {
        "employees": 250,
        "revenue": 50000000
      },
      "industry": "Manufacturing",
      "contacts_count": 15,
      "confidence_score": 85,
      "enrichment_status": "enriched",
      "last_enriched_at": "2025-11-11T10:02:45Z"
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 45,
    "totalPages": 3,
    "hasNext": true,
    "hasPrev": false
  }
}
```

---

### Get Account

Fetch single account by ID.

**Endpoint**: `GET /api/accounts/:id`

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "_id": "507f1f77bcf86cd799439011",
    "name": "FANUC America Corporation",
    "domain": "fanucamerica.com",
    "keywords": ["robotics", "automation", "CNC"],
    "categories": ["OEM"],
    "capabilities": ["ISO9001"],
    "location": {
      "state": "TX",
      "city": "Houston",
      "country": "US"
    },
    "size": {
      "employees": 250,
      "revenue": 50000000
    },
    "industry": "Manufacturing",
    "contacts_count": 15,
    "confidence_score": 85,
    "enrichment_status": "enriched",
    "last_enriched_at": "2025-11-11T10:02:45Z",
    "metadata": {},
    "createdAt": "2025-11-01T08:00:00Z",
    "updatedAt": "2025-11-11T10:02:45Z"
  }
}
```

---

### Search Contacts

Filter and search contacts by criteria.

**Endpoint**: `GET /api/contacts`

**Query Parameters**:
- `accountId` (string): Filter by account
- `persona` (string, comma-separated): Filter by persona type
- `status` (string): Contact status
- `min_confidence` (number): Minimum persona confidence score
- `sort` (string): Sort field - `name`, `title` (default: `name`)
- `order` (string): Sort order - `asc`, `desc` (default: `asc`)
- `page` (number): Page number (default: 1)
- `limit` (number): Results per page (default: 20, max: 100)

**Example**:
```
GET /api/contacts?accountId=507f1f77bcf86cd799439011&persona=procurement,supply_chain&sort=name&page=1&limit=20
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "_id": "507f1f77bcf86cd799439013",
      "accountId": "507f1f77bcf86cd799439011",
      "firstName": "John",
      "lastName": "Doe",
      "email": "jdoe@fanucamerica.com",
      "phone": "+1-555-0100",
      "title": "VP of Supply Chain",
      "department": "Operations",
      "inferred_persona": "supply_chain",
      "persona_confidence": 85,
      "status": "ACTIVE",
      "enrichment_status": "enriched",
      "last_enriched_at": "2025-11-11T10:02:45Z",
      "createdAt": "2025-11-05T14:00:00Z"
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 15,
    "totalPages": 1,
    "hasNext": false,
    "hasPrev": false
  }
}
```

---

### Get Contact

Fetch single contact by ID.

**Endpoint**: `GET /api/contacts/:id`

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "_id": "507f1f77bcf86cd799439013",
    "accountId": "507f1f77bcf86cd799439011",
    "firstName": "John",
    "lastName": "Doe",
    "email": "jdoe@fanucamerica.com",
    "phone": "+1-555-0100",
    "title": "VP of Supply Chain",
    "department": "Operations",
    "inferred_persona": "supply_chain",
    "persona_confidence": 85,
    "linkedin_profile_url": "https://linkedin.com/in/johndoe",
    "status": "ACTIVE",
    "enrichment_status": "enriched",
    "last_enriched_at": "2025-11-11T10:02:45Z",
    "metadata": {},
    "createdAt": "2025-11-05T14:00:00Z",
    "updatedAt": "2025-11-11T10:02:45Z"
  }
}
```

---

### Analytics Preview

Get aggregated stats for search criteria without fetching full results.

**Endpoint**: `POST /api/analytics`

**Request Body**:
```json
{
  "keywords": ["robotics", "automation"],
  "categories": ["OEM"],
  "location": {
    "state": "TX",
    "country": "US"
  },
  "industry": "Manufacturing",
  "size": {
    "min_employees": 50,
    "max_employees": 500
  },
  "personas": ["procurement", "supply_chain"],
  "min_confidence": 70
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "total_accounts": 45,
    "total_contacts": 127,
    "breakdown": {
      "by_persona": {
        "procurement": 68,
        "supply_chain": 59
      },
      "by_industry": {
        "Manufacturing": 45
      },
      "by_country": {
        "US": 45
      },
      "by_state": {
        "TX": 45
      },
      "by_category": {
        "OEM": 32,
        "Tier1": 13
      }
    },
    "avg_confidence": 78,
    "avg_contacts_per_account": 2.8
  }
}
```

---

## Error Responses

### 400 Bad Request
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid query parameter",
    "details": {
      "field": "min_employees",
      "issue": "Must be a positive number"
    }
  }
}
```

### 401 Unauthorized
```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Invalid or missing API key"
  }
}
```

### 404 Not Found
```json
{
  "success": false,
  "error": {
    "code": "NOT_FOUND",
    "message": "Account not found"
  }
}
```

### 429 Too Many Requests
```json
{
  "success": false,
  "error": {
    "code": "RATE_LIMIT_EXCEEDED",
    "message": "Rate limit exceeded: 100 requests per minute",
    "retry_after_seconds": 45
  }
}
```

---

## Rate Limits

- 100 requests per minute per API key
- Search endpoints: Max 100 results per page
- Analytics endpoint: No restrictions on criteria complexity

---

## Field Reference

### Account Fields

| Field | Type | Description |
|-------|------|-------------|
| `_id` | ObjectId | Unique identifier |
| `name` | string | Company name |
| `domain` | string | Primary domain |
| `keywords` | string[] | Industry keywords |
| `categories` | string[] | Supplier categories (OEM, Tier1, etc.) |
| `capabilities` | string[] | Certifications, processes |
| `location.state` | string | US state code |
| `location.city` | string | City name |
| `location.country` | string | Country code |
| `size.employees` | number | Employee count |
| `size.revenue` | number | Annual revenue (USD) |
| `industry` | string | Primary industry |
| `contacts_count` | number | Number of associated contacts |
| `confidence_score` | number | Data quality score (0-100) |
| `enrichment_status` | string | pending, enriched, stale, failed |
| `last_enriched_at` | Date | Last enrichment timestamp |

### Contact Fields

| Field | Type | Description |
|-------|------|-------------|
| `_id` | ObjectId | Unique identifier |
| `accountId` | ObjectId | Associated account |
| `firstName` | string | First name |
| `lastName` | string | Last name |
| `email` | string | Email address |
| `phone` | string | Phone number |
| `title` | string | Job title |
| `department` | string | Department |
| `inferred_persona` | string | Functional role |
| `persona_confidence` | number | Inference confidence (0-100) |
| `linkedin_profile_url` | string | LinkedIn URL |
| `status` | string | ACTIVE, INACTIVE, BOUNCED, etc. |
| `enrichment_status` | string | pending, enriched, stale, failed |

### Persona Types

- `procurement`
- `supply_chain`
- `operations`
- `product_design`
- `engineering`
- `quality`
- `executive`
- `sales`
- `unknown`
