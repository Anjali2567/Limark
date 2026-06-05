import { defineConfig, mergeConfig } from 'vitest/config';
import unitConfig from './vitest.unit.config.js';
import integrationConfig from './vitest.integration.config.js';

/**
 * Default Vitest configuration
 * This runs both unit and integration tests together
 * For faster feedback, use:
 * - pnpm test:unit (fast, parallel)
 * - pnpm test:integration (slower, sequential, requires MongoDB)
 */
export default defineConfig({
  test: {
    globals: true,
    environment: 'node',
    setupFiles: ['./test/setup.ts'],

    // Include all test types
    include: ['**/*.test.ts', '**/*.spec.ts'],
    exclude: ['node_modules', 'dist'],

    testTimeout: 30000,
    hookTimeout: 30000,

    env: {
      NODE_ENV: 'test',
      // Local test database (docker-compose.test.yml)
      DATABASE_URL: 'mongodb://localhost:27018',
      DATABASE_NAME: 'leadplus_intelligence_test',
      LOG_LEVEL: 'error',
      PORT: '3001',
      EXTERNAL_API_KEY: 'test-api-key-12345',
    },
  },
});
