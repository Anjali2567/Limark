-- Standardize PostgreSQL table names to singular convention
-- This ensures consistency across all tables in the schema

-- Step 1: Rename plural tables to singular
ALTER TABLE IF EXISTS users RENAME TO "user";
ALTER TABLE IF EXISTS workspaces RENAME TO workspace;
ALTER TABLE IF EXISTS tenants RENAME TO tenant;
ALTER TABLE IF EXISTS industries RENAME TO industry;
ALTER TABLE IF EXISTS lead_companies RENAME TO lead_company;
ALTER TABLE IF EXISTS lead_notes RENAME TO lead_note;
ALTER TABLE IF EXISTS lead_lists RENAME TO lead_list;
ALTER TABLE IF EXISTS workspace_users RENAME TO workspace_user;
ALTER TABLE IF EXISTS user_roles RENAME TO user_role;
ALTER TABLE IF EXISTS user_activity_log RENAME TO user_activity_log;
ALTER TABLE IF EXISTS refresh_tokens RENAME TO refresh_token;

-- Step 2: Update foreign key constraints and indexes
-- These will be handled automatically by PostgreSQL

-- Verify the changes
SELECT tablename FROM pg_tables 
WHERE schemaname = 'public' 
AND tablename IN ('user', 'workspace', 'tenant', 'industry', 'lead_company',
                   'lead_note', 'lead_list', 'workspace_user', 'user_role', 
                   'user_activity_log', 'refresh_token')
ORDER BY tablename;
