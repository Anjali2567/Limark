# LeadPlus Intelligence - Quick Start Guide

## Getting Started in 5 Minutes

### 1. Install Dependencies

```bash
pnpm install
```

### 2. Configure Environment

```bash
cp .env.example .env
```

Edit `.env` and set your MongoDB connection:

```env
DATABASE_URL=mongodb://localhost:27017
DATABASE_NAME=leadplus_intelligence
```

### 3. Start MongoDB

If using Docker:

```bash
docker run -d -p 27017:27017 --name mongodb mongo:latest
```

Or if installed locally (macOS):

```bash
brew services start mongodb-community
```

### 4. Seed the Database

```bash
pnpm run seed
```

### 5. Run the Application

**Development mode** (with hot reload):

```bash
pnpm run dev
```

**Production mode**:

```bash
pnpm run build
pnpm run start
```

## Available Scripts

| Script    | Command              | Description          |
| --------- | -------------------- | -------------------- |
| Dev       | `pnpm run dev`       | Run with hot reload  |
| Build     | `pnpm run build`     | Compile TypeScript   |
| Start     | `pnpm run start`     | Run production build |
| Seed      | `pnpm run seed`      | Seed database        |
| Typecheck | `pnpm run typecheck` | Type checking only   |

## Project Structure

```
src/
├── config/          # Environment configuration
├── db/              # Database layer
│   ├── connection.ts
│   ├── repositories/
│   ├── schemas/
│   └── seed.ts
├── utils/           # Utilities
│   ├── logger.ts
│   ├── errors.ts
│   ├── rate-limiter.ts
│   └── api-response.ts
└── index.ts         # Bootstrap
```

## What's Included

✅ **MongoDB Integration** with connection pooling
✅ **Repository Pattern** for data access
✅ **Centralized Logging** with Winston
✅ **Error Handling** with retry logic
✅ **Rate Limiting** utilities
✅ **Performance Tracking**
✅ **CI/CD Pipeline** (GitHub Actions)
✅ **TypeScript** with strict mode

## Sample Data

After running `pnpm run seed`, you'll have:

- 3 sample accounts (Acme Corp, TechStart Inc, Global Services Ltd)
- 6 sample contacts (2 per account)
- 3 sample sources (LinkedIn Scraper, CRM API, Manual Upload)

## Next Steps

1. **Add business logic** in `src/services/`
2. **Create API endpoints** (if needed)
3. **Add scrapers** in `src/scrapers/`
4. **Set up connectors** in `src/connectors/`

## Troubleshooting

**Can't connect to MongoDB?**

- Ensure MongoDB is running
- Check `DATABASE_URL` in `.env`
- Try: `mongosh` to test connection

**Build errors?**

```bash
rm -rf node_modules pnpm-lock.yaml
pnpm install
pnpm run build
```

**Need help?**
Check `README.md` for detailed documentation.
