#!/bin/bash
set -e

echo "═══════════════════════════════════════════════════════════════════════════════"
echo "LeadPlus MongoDB to PostgreSQL Production Migration"
echo "═══════════════════════════════════════════════════════════════════════════════"
echo ""
echo "Source: MongoDB (leadplus-prod)"
echo "Target: PostgreSQL (leadplus_staging)"
echo ""

# Build the JAR if not present
if [ ! -f "build/libs/leadplus-service.jar" ]; then
    echo "Building application..."
    ./gradlew build -x test --no-daemon
fi

echo "Running production migration..."
echo ""

# Run the production migration test
./gradlew test --tests "ai.leadplus.staging.DirectProductionMigrationTest.migrateProductionData" --no-daemon -i

echo ""
echo "═══════════════════════════════════════════════════════════════════════════════"
echo "✅ Migration script completed"
echo "═══════════════════════════════════════════════════════════════════════════════"
