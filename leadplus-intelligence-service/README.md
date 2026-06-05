# LeadPlus Intelligence Layer

A clean, deployable backend foundation for the LeadPlus Intelligence Layer, featuring a robust database scaffolding, comprehensive utilities, and automated CI/CD deployment.

## Features

- **Database Layer**: MongoDB with connection pooling, query timing, and comprehensive repository pattern
- **Centralized Logging**: Winston-based logging with rotation, performance metrics, and multiple log levels
- **Error Handling**: Unified error classes with retry logic and graceful degradation
- **Performance Utilities**: Rate limiting, retry mechanisms, and API response standardization
- **Configuration Management**: Environment-based configuration with validation
- **CI/CD Pipeline**: GitHub Actions workflow for automated build and deployment

## Project Structure

```
leadplus-intelligence/
├── src/
│   ├── config/              # Configuration management
│   │   └── index.ts         # Environment config loader
│   ├── db/                  # Database layer
│   │   ├── connection.ts    # MongoDB connection manager
│   │   ├── repositories/    # Repository pattern implementations
│   │   │   ├── base.repository.ts
│   │   │   ├── account.repository.ts
│   │   │   ├── contact.repository.ts
│   │   │   └── source.repository.ts
│   │   ├── schemas/         # Database schema definitions
│   │   │   ├── account.schema.ts
│   │   │   ├── contact.schema.ts
│   │   │   └── source.schema.ts
│   │   └── seed.ts          # Database seeding utility
│   ├── utils/               # Utility functions
│   │   ├── logger.ts        # Centralized logging
│   │   ├── errors.ts        # Error handling & retry logic
│   │   ├── rate-limiter.ts  # Rate limiting utilities
│   │   └── api-response.ts  # API response standardization
│   └── index.ts             # Application bootstrap
├── .github/
│   └── workflows/
│       └── ci-cd.yml        # CI/CD pipeline
├── .env.example             # Environment variables template
├── tsconfig.json            # TypeScript configuration
├── package.json             # Dependencies and scripts
└── README.md                # This file
```

## Prerequisites

- **Node.js**: Version 20.x or higher
- **pnpm**: Package manager (install via `npm install -g pnpm`)
- **MongoDB**: Local instance or MongoDB Atlas connection

## Installation

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd leadplus-intelligence
   ```

2. **Install dependencies**:
   ```bash
   pnpm install
   ```

3. **Set up environment variables**:
   ```bash
   cp .env.example .env
   ```

4. **Configure your `.env` file**:
   ```env
   NODE_ENV=development
   DATABASE_URL=mongodb://localhost:27017
   DATABASE_NAME=leadplus_intelligence
   LOG_LEVEL=debug
   ```

## Running the Application

### Development Mode
Runs the application with hot reload:
```bash
pnpm run dev
```

### Build
Compiles TypeScript to JavaScript:
```bash
pnpm run build
```

### Production Mode
Runs the compiled application:
```bash
pnpm run start
```

### Type Checking
Runs TypeScript type checking without compilation:
```bash
pnpm run typecheck
```

## Database Setup

### Connect to MongoDB

Ensure MongoDB is running locally or configure a remote MongoDB connection in your `.env` file.

**Local MongoDB**:
```bash
# Start MongoDB (macOS with Homebrew)
brew services start mongodb-community

# Or using Docker
docker run -d -p 27017:27017 --name mongodb mongo:latest
```

**MongoDB Atlas**:
Update your `.env` with your connection string:
```env
DATABASE_URL=mongodb+srv://username:password@cluster.mongodb.net/
```

### Seed the Database

Populate the database with sample data:
```bash
pnpm run seed
```

This will:
- Create necessary indexes
- Clear existing data (if any)
- Insert sample accounts, contacts, and sources

## Database Schemas

### Accounts
- Company/organization information
- Types: Enterprise, SMB, Startup, Individual
- Status: Active, Inactive, Prospect, Churned

### Contacts
- Individual contacts associated with accounts
- Roles: Decision Maker, Influencer, Champion, User, Gatekeeper
- Status: Active, Inactive, Bounced, Unsubscribed

### Sources
- Data collection sources
- Types: Web Scraping, API, Upload, Integration, Manual
- Status: Active, Inactive, Pending, Error

## Utilities

### Logger (`src/utils/logger.ts`)
- Winston-based logging with daily rotation
- Log levels: error, warn, info, http, debug
- Performance tracking with `PerformanceTracker`
- Automatic log rotation (14 days for application logs, 30 days for errors)

**Usage**:
```typescript
import logger, { trackPerformance } from './utils/logger.js';

logger.info('Application started');
logger.error('An error occurred', { error: err.message });

// Performance tracking
const result = await trackPerformance('database_query', async () => {
  return await db.query();
});
```

### Error Handling (`src/utils/errors.ts`)
- Unified error classes: `AppError`, `DatabaseError`, `ValidationError`, etc.
- Retry mechanism with exponential backoff
- Global error handlers for uncaught exceptions

**Usage**:
```typescript
import { withRetry, DatabaseError } from './utils/errors.js';

// Retry with default config
const result = await withRetry(async () => {
  return await someAsyncOperation();
});

// Throw custom errors
throw new DatabaseError('Connection failed', { host: 'localhost' });
```

### Rate Limiting (`src/utils/rate-limiter.ts`)
- Token bucket implementation
- Configurable limits per operation
- Registry for managing multiple limiters

**Usage**:
```typescript
import { RateLimiter, withRateLimit } from './utils/rate-limiter.js';

const limiter = new RateLimiter({
  maxRequests: 10,
  windowMs: 60000, // 10 requests per minute
});

await limiter.consume(); // Waits if limit exceeded

// Or wrap a function
const rateLimitedFn = withRateLimit(myFunction, {
  maxRequests: 5,
  windowMs: 1000,
});
```

### API Response (`src/utils/api-response.ts`)
- Standardized response format
- Pagination support
- Error formatting

**Usage**:
```typescript
import { successResponse, errorResponse, paginatedResponse } from './utils/api-response.js';

return successResponse({ message: 'Success!' });
return errorResponse(new Error('Failed'));
return paginatedResponse(data, page, limit, total);
```

## Repository Pattern

All database operations use the repository pattern for consistency:

```typescript
import { accountRepository } from './db/repositories/account.repository.js';

// Create
const account = await accountRepository.createAccount({
  name: 'Acme Corp',
  domain: 'acme.com',
  type: AccountType.ENTERPRISE,
  status: AccountStatus.ACTIVE,
});

// Read
const account = await accountRepository.findById(id);
const accounts = await accountRepository.findByType('enterprise');

// Update
const updated = await accountRepository.updateAccount(id, {
  status: AccountStatus.INACTIVE,
});

// Delete
await accountRepository.deleteById(id);
```

## CI/CD Pipeline

The project includes a GitHub Actions workflow (`.github/workflows/ci-cd.yml`) that:

1. **Build & Test** (on push/PR to `main` or `develop`):
   - Checks out code
   - Sets up Node.js and pnpm
   - Installs dependencies
   - Runs type checking
   - Builds the project
   - Uploads build artifacts

2. **Deploy** (on push to `main` or `develop`):
   - Downloads build artifacts
   - Sets environment variables
   - Triggers deployment (configure based on your infrastructure)

### GitHub Secrets

Configure these secrets in your GitHub repository:

- `DEPLOY_HOST`: Deployment server hostname
- `DEPLOY_USER`: SSH username
- `DEPLOY_SSH_KEY`: SSH private key
- Or configure for your specific deployment platform (Heroku, AWS, etc.)

## Environment Variables

All configuration is managed through environment variables. See `.env.example` for the complete list:

| Variable | Description | Default |
|----------|-------------|---------|
| `NODE_ENV` | Environment (development/production) | `development` |
| `PORT` | Application port | `3000` |
| `DATABASE_URL` | MongoDB connection URL | `mongodb://localhost:27017` |
| `DATABASE_NAME` | Database name | `leadplus_intelligence` |
| `LOG_LEVEL` | Logging level | `debug` |
| `RATE_LIMIT_MAX_REQUESTS` | Max requests per window | `100` |
| `RATE_LIMIT_WINDOW_MS` | Rate limit window (ms) | `60000` |

## Development

### Adding New Collections

1. **Create a schema** in `src/db/schemas/`:
   ```typescript
   export interface MyEntity {
     _id?: ObjectId;
     name: string;
     // ... other fields
   }
   ```

2. **Create a repository** in `src/db/repositories/`:
   ```typescript
   export class MyEntityRepository extends BaseRepository<MyEntity> {
     protected collectionName = 'my_entities';
     // ... custom methods
   }
   ```

3. **Initialize indexes** in `src/index.ts`

### Adding New Utilities

Create utility files in `src/utils/` and export functions for reuse across the application.

## Performance Monitoring

The application includes built-in performance tracking:

- Database query timing
- Operation duration logging
- Connection pool metrics
- Log rotation for long-term analysis

Check logs in the `logs/` directory for detailed performance data.

## Troubleshooting

### Database Connection Issues

```bash
# Check MongoDB is running
pnpm run dev

# If connection fails, verify:
# 1. MongoDB is running
# 2. DATABASE_URL in .env is correct
# 3. Network connectivity (for remote databases)
```

### Type Errors

```bash
# Run type checking
pnpm run typecheck

# Fix errors before building
pnpm run build
```

### Dependency Issues

```bash
# Clear node_modules and reinstall
rm -rf node_modules pnpm-lock.yaml
pnpm install
```

## Contributing

1. Create a feature branch
2. Make your changes
3. Run type checking and build
4. Commit and push
5. Create a pull request

## License

ISC

## Support

For issues or questions, please open an issue in the GitHub repository.
