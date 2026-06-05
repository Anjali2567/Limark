import { defineConfig } from 'vitest/config';
import { resolve } from 'path';

export default defineConfig({
  test: {
    name: 'integration',
    globals: true,
    environment: 'node',
    setupFiles: ['./test/setup.ts'],

    // Run integration tests sequentially to avoid database conflicts
    pool: 'forks',
    poolOptions: {
      forks: {
        singleFork: true,
      },
    },

    // Integration tests can take longer (MongoDB operations)
    testTimeout: 30000,
    hookTimeout: 30000,

    include: [
      'test/**/*.test.ts',
      'src/**/*.integration.test.ts',
    ],
    exclude: [
      'node_modules',
      'dist',
      '**/*.spec.ts',
      '**/*.unit.test.ts',
    ],

    env: {
      NODE_ENV: 'test',
      DATABASE_URL:
        'mongodb+srv://dany:wZfF7NzxkthBtf5I@dev-db.a5y84yr.mongodb.net/leadplus-intelligence-test?retryWrites=true&w=majority',
      DATABASE_NAME: 'leadplus-intelligence-test',
      LOG_LEVEL: 'error', // Reduce log noise
      PORT: '3001',
      EXTERNAL_API_KEY: 'test-api-key-12345',
    },
  },

  resolve: {
    alias: {
      '@': resolve(__dirname, './src'),
      '@config': resolve(__dirname, './src/config'),
      '@utils': resolve(__dirname, './src/utils'),
      '@db': resolve(__dirname, './src/db'),
      '@repositories': resolve(__dirname, './src/db/repositories'),
      '@api': resolve(__dirname, './src/api'),
      '@middleware': resolve(__dirname, './src/api/middleware'),
      '@routes': resolve(__dirname, './src/api/routes'),
    },
  },
});
