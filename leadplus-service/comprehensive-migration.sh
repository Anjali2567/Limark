#!/bin/bash

###############################################################################
# COMPREHENSIVE MONGODB → POSTGRESQL MIGRATION SCRIPT
# All 61 Collections (~300K+ entities)
# 
# Usage:
#   Option 1 - With individual PostgreSQL variables:
#     export PG_HOST="pandora"
#     export PG_PORT="5432"
#     export PG_DB="leadplus_staging"
#     export PG_USER="ramesh"
#     export PG_PASSWORD="password"
#     ./comprehensive-migration.sh
#
#   Option 2 - With PostgreSQL URI (recommended):
#     export PG_URI="postgresql://ramesh:password@pandora:5432/leadplus_staging"
#     ./comprehensive-migration.sh
#
#   Option 3 - With MongoDB URI:
#     export MONGO_URI="mongodb://localhost:27017/leadplus"
#     ./comprehensive-migration.sh
#
# Prerequisites:
# - MongoDB Atlas connection configured
# - PostgreSQL database created and schema loaded
# - Both services running
# - Maintenance window scheduled
###############################################################################

set -e

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
MONGO_URI=${MONGO_URI:-"mongodb://localhost:27017/leadplus"}

# Support both PG_URI and individual PG_* variables
if [ -n "$PG_URI" ]; then
    # Parse PG_URI: postgresql://user:password@host:port/database
    # Format: postgresql://[user[:password]@][host][:port][/database][?options]
    PG_USER=$(echo "$PG_URI" | grep -oP '(?<=://).*?(?=:)' || echo "ramesh")
    PG_PASSWORD=$(echo "$PG_URI" | grep -oP '(?<=:).*?(?=@)' || echo "")
    PG_HOST=$(echo "$PG_URI" | grep -oP '(?<=@).*?(?=:)' | head -1 || echo "pandora")
    PG_PORT=$(echo "$PG_URI" | grep -oP '(?<=:)[0-9]+(?=/)' || echo "5432")
    PG_DB=$(echo "$PG_URI" | grep -oP '(?<=/)[^?]*' || echo "leadplus_staging")
else
    # Use individual variables (with defaults)
    PG_HOST=${PG_HOST:-"pandora"}
    PG_PORT=${PG_PORT:-"5432"}
    PG_DB=${PG_DB:-"leadplus_staging"}
    PG_USER=${PG_USER:-"ramesh"}
    PG_PASSWORD=${PG_PASSWORD:-""}
fi

# Logging
LOG_FILE="migration-$(date +%Y%m%d-%H%M%S).log"

echo -e "${BLUE}═══════════════════════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  COMPREHENSIVE MONGODB → POSTGRESQL MIGRATION${NC}"
echo -e "${BLUE}  All 61 Collections (~300K+ entities)${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════════════════════${NC}"
echo ""

# Verify prerequisites
echo -e "${BLUE}📋 Verifying prerequisites...${NC}"

if ! command -v mongosh &> /dev/null; then
    echo -e "${RED}❌ mongosh not found. Please install MongoDB Shell${NC}"
    exit 1
fi

if ! command -v psql &> /dev/null; then
    echo -e "${RED}❌ psql not found. Please install PostgreSQL client${NC}"
    exit 1
fi

echo -e "${GREEN}✅ All prerequisites met${NC}"
echo ""

# Test MongoDB connection
echo -e "${BLUE}🔍 Testing MongoDB connection...${NC}"
if mongosh "$MONGO_URI" --eval "db.runCommand('ping')" > /dev/null 2>&1; then
    echo -e "${GREEN}✅ MongoDB connected${NC}"
else
    echo -e "${RED}❌ Cannot connect to MongoDB at $MONGO_URI${NC}"
    exit 1
fi

# Test PostgreSQL connection
echo -e "${BLUE}🔍 Testing PostgreSQL connection...${NC}"
export PGPASSWORD="$PG_PASSWORD"
if psql -h "$PG_HOST" -U "$PG_USER" -d "$PG_DB" -c "SELECT 1" > /dev/null 2>&1; then
    echo -e "${GREEN}✅ PostgreSQL connected${NC}"
else
    echo -e "${RED}❌ Cannot connect to PostgreSQL at $PG_HOST:$PG_PORT/$PG_DB${NC}"
    exit 1
fi

echo ""
echo -e "${YELLOW}⚠️  IMPORTANT: This will migrate ALL 61 collections (~300K+ entities)${NC}"
echo -e "${YELLOW}    Ensure maintenance window is scheduled and backups are taken${NC}"
echo ""

# Ask for confirmation
read -p "Type 'yes' to proceed with migration: " confirm
if [ "$confirm" != "yes" ]; then
    echo "Migration cancelled."
    exit 0
fi

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  STARTING MIGRATION (see $LOG_FILE for details)${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════════════════════${NC}"
echo ""

START_TIME=$(date +%s)

# Run the Spring Boot migration test
echo -e "${BLUE}🚀 Executing comprehensive migration...${NC}"
./gradlew test --tests "ComprehensiveFullMigrationTest" --info 2>&1 | tee -a "$LOG_FILE"

END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}✅ MIGRATION COMPLETE${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "Duration: $(( DURATION / 60 )) minutes $(( DURATION % 60 )) seconds"
echo -e "Log file: $LOG_FILE"
echo ""
echo -e "${YELLOW}📋 POST-MIGRATION CHECKLIST:${NC}"
echo "  [ ] Verify all 61 collections migrated"
echo "  [ ] Check data integrity (row counts, checksums)"
echo "  [ ] Run smoke tests"
echo "  [ ] Verify application startup on PostgreSQL"
echo "  [ ] Test core business flows"
echo "  [ ] Test vector search functionality"
echo "  [ ] Monitor performance and logs"
echo ""
echo -e "${BLUE}See PRODUCTION_MIGRATION_READY.md for detailed verification steps${NC}"
