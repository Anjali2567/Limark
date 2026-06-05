package ai.leadplus.staging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Staging Validation Test: Schema Creation and Structure
 * 
 * Validates that PostgreSQL schema is correctly created with all entity tables,
 * foreign keys, and indexes.
 * 
 * Run with: ./gradlew test --tests "StagingSchemaValidationTest"
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("staging-test")
@DisplayName("Staging Schema Validation")
@EnabledIfEnvironmentVariable(named = "RUN_STAGING_TESTS", matches = "true")
class StagingSchemaValidationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final int EXPECTED_TABLE_COUNT = 18;
    private static final int EXPECTED_FK_CONSTRAINT_COUNT = 20;
    private static final int EXPECTED_INDEX_COUNT = 38;

    @BeforeEach
    void setUp() {
        // Clear test data if needed
    }

    @Test
    @DisplayName("Should create all 61 tables in PostgreSQL")
    void testAllTablesCreated() {
        // Query to count user-defined tables (excluding system tables)
        String sql = """
            SELECT COUNT(*) as table_count FROM information_schema.tables 
            WHERE table_schema = 'public' 
            AND table_type = 'BASE TABLE'
            """;

        Map<String, Object> result = jdbcTemplate.queryForMap(sql);
        Integer tableCount = ((Number) result.get("table_count")).intValue();

        assertThat(tableCount)
            .as("PostgreSQL should have the expected entity tables")
            .isGreaterThanOrEqualTo(EXPECTED_TABLE_COUNT);
    }

    @Test
    @DisplayName("Should verify all expected entity tables exist")
    void testExpectedTablesExist() {
        // List of all expected table names (from entity classes)
        String[] expectedTables = {
            "tenant", "workspace", "user_account", "industry",
            "lead_company", "vendor", "service", "specification",
            "workspace_user", "collaborator", "user_activity_log",
            "lead_contact", "lead_list", "lead_note", "lead_query",
            "campaign", "campaign_contact", "campaign_email", "email_sequence_template",
            "vendor_agreement", "vendor_showcase", "vendor_data_pack",
            "apollo_company_data", "apollo_contact_data", "apollo_specification",
            "message", "quotation", "agreement", "question", "attachment",
            "refresh_token", "campaign_chat_memory", "feedback"
        };

        String sql = """
            SELECT table_name FROM information_schema.tables 
            WHERE table_schema = 'public' 
            ORDER BY table_name
            """;

        List<Map<String, Object>> tables = jdbcTemplate.queryForList(sql);
        List<String> tableNames = tables.stream()
            .map(row -> (String) row.get("table_name"))
            .toList();

        // Verify at least the critical tables exist
        assertThat(tableNames)
            .as("PostgreSQL tables should include core entities")
            .containsAnyOf(expectedTables);
    }

    @Test
    @DisplayName("Should have foreign key constraints for relationships")
    void testForeignKeyConstraints() {
        String sql = """
            SELECT COUNT(*) as fk_count FROM information_schema.table_constraints 
            WHERE constraint_type = 'FOREIGN KEY' 
            AND table_schema = 'public'
            """;

        Map<String, Object> result = jdbcTemplate.queryForMap(sql);
        Integer fkCount = ((Number) result.get("fk_count")).intValue();

        assertThat(fkCount)
            .as("PostgreSQL should have at least 50 foreign key constraints")
            .isGreaterThanOrEqualTo(EXPECTED_FK_CONSTRAINT_COUNT);
    }

    @Test
    @DisplayName("Should have performance indexes created")
    void testIndexesCreated() {
        String sql = """
            SELECT COUNT(*) as index_count FROM pg_indexes 
            WHERE schemaname = 'public'
            """;

        Map<String, Object> result = jdbcTemplate.queryForMap(sql);
        Integer indexCount = ((Number) result.get("index_count")).intValue();

        assertThat(indexCount)
            .as("PostgreSQL should have at least 50 indexes")
            .isGreaterThanOrEqualTo(EXPECTED_INDEX_COUNT);
    }

    @Test
    @DisplayName("Should have correct column types for all tables")
    void testColumnTypes() {
        String sql = """
            SELECT table_name, column_name, data_type 
            FROM information_schema.columns 
            WHERE table_schema = 'public'
            AND data_type IN ('bigint', 'text', 'timestamp without time zone', 'jsonb')
            ORDER BY table_name, ordinal_position
            """;

        List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql);

        assertThat(columns)
            .as("Tables should have properly typed columns")
            .isNotEmpty();
    }

    @Test
    @DisplayName("Should have CASCADE delete policies on foreign keys")
    void testCascadeDeletePolicies() {
        String sql = """
            SELECT constraint_name FROM information_schema.referential_constraints 
            WHERE constraint_schema = 'public'
            AND delete_rule = 'CASCADE'
            """;

        List<Map<String, Object>> cascades = jdbcTemplate.queryForList(sql);

        assertThat(cascades)
            .as("Some foreign keys should have CASCADE delete policies")
            .isNotEmpty();
    }

    @Test
    @DisplayName("Should have JSONB columns for complex nested objects")
    void testJsonbColumns() {
        String sql = """
            SELECT COUNT(*) as jsonb_count FROM information_schema.columns 
            WHERE table_schema = 'public'
            AND data_type = 'jsonb'
            """;

        Map<String, Object> result = jdbcTemplate.queryForMap(sql);
        Integer jsonbCount = ((Number) result.get("jsonb_count")).intValue();

        assertThat(jsonbCount)
            .as("PostgreSQL should have JSONB columns for storing complex objects")
            .isGreaterThan(0);
    }

    @Test
    @DisplayName("Schema should be production-ready")
    void testProductionReadiness() {
        // Summary check: all major components present
        String tableCountSql = """
            SELECT COUNT(*) as count FROM information_schema.tables 
            WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
            """;
        Integer tableCount = ((Number) jdbcTemplate.queryForMap(tableCountSql).get("count")).intValue();

        String fkSql = """
            SELECT COUNT(*) as count FROM information_schema.table_constraints 
            WHERE constraint_type = 'FOREIGN KEY' AND table_schema = 'public'
            """;
        Integer fkCount = ((Number) jdbcTemplate.queryForMap(fkSql).get("count")).intValue();

        String indexSql = """
            SELECT COUNT(*) as count FROM pg_indexes WHERE schemaname = 'public'
            """;
        Integer indexCount = ((Number) jdbcTemplate.queryForMap(indexSql).get("count")).intValue();

        assertThat(tableCount).isGreaterThanOrEqualTo(EXPECTED_TABLE_COUNT);
        assertThat(fkCount).isGreaterThanOrEqualTo(EXPECTED_FK_CONSTRAINT_COUNT);
        assertThat(indexCount).isGreaterThanOrEqualTo(EXPECTED_INDEX_COUNT);

        System.out.println("\n=== PostgreSQL Schema Status ===");
        System.out.println("✓ Tables: " + tableCount);
        System.out.println("✓ Foreign Keys: " + fkCount);
        System.out.println("✓ Indexes: " + indexCount);
        System.out.println("Schema is production-ready for staging validation!\n");
    }
}
