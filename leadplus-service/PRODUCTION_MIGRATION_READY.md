# 🚀 Production Migration - Ready for Execution

**Status**: ✅ READY TO DEPLOY  
**Date**: 2026-05-06  
**Version**: Phase 2 Tier 1  
**Entities**: 122 (Tenants, Workspaces, Users)

---

## ⚡ Quick Start Commands

```bash
# 1. Configure environment
export MONGODB_HOST=pandora
export MONGODB_PORT=27017
export MONGODB_USER=ramesh
export MONGODB_PASS=3f5c8b9e
export MONGODB_DB=leadplus-prod

export POSTGRES_HOST=pandora
export POSTGRES_PORT=5432
export POSTGRES_USER=ramesh
export POSTGRES_PASS=kj3n4h5g6
export POSTGRES_DB=leadplus_prod

# 2. Run migration
cd /Users/ramesh/Workspace/leadplus/leadplus-service
./full-migration.sh 2>&1 | tee production-migration-$(date +%Y%m%d-%H%M%S).log

# 3. Verify migration
PGPASSWORD=kj3n4h5g6 psql -h pandora -U ramesh -d leadplus_prod << SQL
SELECT 'tenants' as table_name, COUNT(*) FROM tenants UNION ALL
SELECT 'workspaces', COUNT(*) FROM workspaces UNION ALL
SELECT 'users', COUNT(*) FROM users;
SQL

# Expected output:
# tenants    | 22
# workspaces | 49
# users      | 51
```

---

## Pre-Migration Checklist

### Database Verification
- [ ] PostgreSQL 13+ running on pandora:5432
- [ ] Database `leadplus_prod` created
- [ ] Schema deployed (tables exist)
- [ ] Connection credentials verified
- [ ] PostgreSQL backup location ready

### MongoDB Verification
- [ ] MongoDB running on pandora:27017
- [ ] Database `leadplus-prod` accessible
- [ ] Collections have data: tenants (22), workspaces (49), users (51)
- [ ] Backup taken (recommended)

### Application Verification
- [ ] All instances stopped
- [ ] No active database connections
- [ ] PostgreSQL JDBC driver configured
- [ ] Application config ready for PostgreSQL
- [ ] Team notified of maintenance window

---

## Detailed Execution Steps

### Step 1: Pre-Migration Validation (30 mins)

```bash
# Verify MongoDB data
mongosh "mongodb://ramesh:3f5c8b9e@pandora:27017/leadplus-prod?authSource=admin"
# In mongosh shell:
db.tenants.countDocuments()      // Should be 22
db.workspaces.countDocuments()   // Should be 49
db.users.countDocuments()        // Should be 51

# Verify PostgreSQL connectivity
PGPASSWORD=kj3n4h5g6 psql -h pandora -U ramesh -d leadplus_prod -c "SELECT version();"
```

### Step 2: Backup Existing Data (Optional)

```bash
# Backup PostgreSQL
PGPASSWORD=kj3n4h5g6 pg_dump -h pandora -U ramesh leadplus_prod > leadplus_prod_backup_$(date +%Y%m%d-%H%M%S).sql

# Backup MongoDB
mongodump --uri "mongodb://ramesh:3f5c8b9e@pandora:27017/leadplus-prod?authSource=admin" \
  --out mongodump_$(date +%Y%m%d-%H%M%S)

echo "✓ Backups created successfully"
```

### Step 3: Stop Application

```bash
# If running as systemd service:
sudo systemctl stop leadplus-api

# If running with Docker Compose:
docker-compose -f docker-compose.prod.yml down

# Wait for graceful shutdown
sleep 10

# Verify stopped (should fail or return error):
curl -s http://localhost:8080/api/actuator/health || echo "✓ Application stopped"
```

### Step 4: Execute Migration

```bash
#!/bin/bash
set -e

cd /Users/ramesh/Workspace/leadplus/leadplus-service

# Configure for production
export MONGODB_HOST=pandora
export MONGODB_PORT=27017
export MONGODB_USER=ramesh
export MONGODB_PASS=3f5c8b9e
export MONGODB_DB=leadplus-prod

export POSTGRES_HOST=pandora
export POSTGRES_PORT=5432
export POSTGRES_USER=ramesh
export POSTGRES_PASS=kj3n4h5g6
export POSTGRES_DB=leadplus_prod

echo "=========================================="
echo "Starting Production Migration"
echo "=========================================="

# Run migration with logging
./full-migration.sh 2>&1 | tee -a production-migration-$(date +%Y%m%d-%H%M%S).log

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Migration completed successfully"
else
    echo ""
    echo "❌ Migration failed"
    exit 1
fi
```

### Step 5: Post-Migration Verification (20 mins)

```bash
# Verify row counts
echo "Checking migrated data:"
PGPASSWORD=kj3n4h5g6 psql -h pandora -U ramesh -d leadplus_prod << SQL
SELECT 'Entity Count Verification:';
SELECT 'tenants' as entity, COUNT(*) as count FROM tenants UNION ALL
SELECT 'workspaces', COUNT(*) FROM workspaces UNION ALL
SELECT 'users', COUNT(*) FROM users ORDER BY entity;

SELECT '';
SELECT 'Foreign Key Integrity:';
SELECT COUNT(*) as users_with_workspace FROM users WHERE workspace_id IS NOT NULL;
SELECT COUNT(*) as workspaces_with_owner FROM workspaces WHERE workspace_lead_owner_id IS NOT NULL;
SQL

# Verify expected counts:
# tenants:    22
# users:      51
# workspaces: 49
# users_with_workspace: 51 (all have workspace)
# workspaces_with_owner: 49 (all have owner)
```

### Step 6: Deploy Updated Application

```bash
# Update application-prod.yml to use PostgreSQL:
# spring:
#   datasource:
#     url: jdbc:postgresql://pandora:5432/leadplus_prod
#     username: ramesh
#     password: kj3n4h5g6
#   jpa:
#     database-platform: org.hibernate.dialect.PostgreSQLDialect
#   data:
#     mongodb:
#       enabled: false

# Build application
./gradlew clean build -x test

# Deploy:
# Option 1 - Systemd:
sudo systemctl start leadplus-api

# Option 2 - Docker:
docker-compose -f docker-compose.prod.yml up -d

# Option 3 - Direct Java:
java -jar build/libs/leadplus-service.jar --spring.profiles.active=prod
```

### Step 7: Health Check & Validation (20 mins)

```bash
# Wait for application startup
sleep 30

# Check application health
echo "Application Health:"
curl -s http://localhost:8080/api/actuator/health | jq .

# Expected: {"status":"UP"}

# Run smoke tests
echo "Smoke Tests:"
curl -s http://localhost:8080/api/v1/tenants | jq '.data | length'    # Should be 22
curl -s http://localhost:8080/api/v1/workspaces | jq '.data | length' # Should be 49
curl -s http://localhost:8080/api/v1/users | jq '.data | length'      # Should be 51

# Check logs
echo ""
echo "Error Check:"
tail -50 /var/log/leadplus/app.log | grep -E "ERROR|Exception" || echo "✓ No errors found"
```

---

## Monitoring (48 hours post-deployment)

```bash
# Monitor application logs
tail -f /var/log/leadplus/app.log | grep -E "ERROR|WARN"

# Monitor database
PGPASSWORD=kj3n4h5g6 psql -h pandora -U ramesh -d leadplus_prod << SQL
SELECT query, calls, mean_time FROM pg_stat_statements 
  WHERE mean_time > 100 ORDER BY mean_time DESC LIMIT 10;
SQL

# Monitor metrics
curl -s http://localhost:8080/api/actuator/metrics | jq '.names[]'
```

---

## Rollback Procedure

### If Migration Failed

```bash
# Restore PostgreSQL from backup
PGPASSWORD=kj3n4h5g6 psql -h pandora -U ramesh -d leadplus_prod < leadplus_prod_backup_*.sql
```

### If Application Issues Post-Deployment

```bash
# Option 1: Stop app and revert to MongoDB
sudo systemctl stop leadplus-api
# Edit application-prod.yml to use MongoDB URI
sudo systemctl start leadplus-api

# Option 2: Restore MongoDB
mongorestore --uri "mongodb://ramesh:3f5c8b9e@pandora:27017/leadplus-prod?authSource=admin" \
  --drop mongodump_*/

# Option 3: Keep both databases in sync for 24 hours
# Application dual-writes to MongoDB + PostgreSQL
# Read from PostgreSQL with MongoDB fallback
# Monitor for issues
# After 24 hours: disable MongoDB writes if stable
```

---

## Success Criteria

✅ All checks must pass:

- [ ] PostgreSQL contains 122 entities (22+49+51)
- [ ] MongoDB counts match PostgreSQL counts
- [ ] All foreign keys intact (users.workspace_id != NULL)
- [ ] No constraint violations in logs
- [ ] Application starts without errors
- [ ] Health endpoint returns `{"status":"UP"}`
- [ ] API endpoints return correct entity counts
- [ ] No ERROR or WARN logs
- [ ] Response time < 200ms (p95)
- [ ] User operations working (login, workspace create, user add)

---

## Post-Deployment Timeline

### Immediately (Day 0)
- Monitor logs continuously
- Watch error rate
- Run smoke tests every 30 mins

### First 24 hours (Day 0-1)
- Monitor application performance
- Verify all business operations working
- Check batch jobs execute successfully
- Document any issues

### 48 hours (Day 1-2)
- Verify data consistency
- Check third-party integrations
- Ensure no data corruption
- Prepare to decommission MongoDB (optional)

### After 72 hours
- If fully stable: can decommission MongoDB
- Update documentation
- Close migration ticket

---

## Rollback Approval

Rollback needed if:
- [ ] Migration failed (row count mismatch)
- [ ] Application fails to start
- [ ] Database constraint violations
- [ ] API endpoints return 500 errors
- [ ] Business logic broken
- [ ] Data loss detected

Rollback steps:
1. Stop application immediately
2. Restore PostgreSQL from backup
3. Revert application config to MongoDB
4. Restart application
5. Verify MongoDB data intact
6. Investigate root cause
7. Fix issues
8. Schedule retry

---

## Contact

**On-Call Engineer**: [DevOps Team]  
**Database Administrator**: [DBA Team]  
**Application Owner**: [Product Team]

If issues: Contact on-call immediately with:
- Error message
- Time it occurred
- Steps to reproduce
- Current status (failed/degraded/working)

---

**Version**: 1.0  
**Last Updated**: 2026-05-06 09:56 IST  
**Status**: ✅ Ready for Production Deployment

**Approval**: Can proceed pending:
1. ✅ Staging migration verified (122/122)
2. ⏳ Production database schema deployed
3. ⏳ Maintenance window scheduled with stakeholders
4. ⏳ Production backup confirmed
5. ⏳ Application configuration updated

