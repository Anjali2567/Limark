import { defineConfig } from 'vitest/config';
import { resolve } from 'path';

export default defineConfig({
  test: {
    name: 'unit',
    globals: true,
    environment: 'node',

    // Run unit tests in parallel for speed
    pool: 'threads',
    poolOptions: {
      threads: {
        singleThread: false,
      },
    },

    // Unit tests should be fast - no external dependencies
    testTimeout: 5000,
    hookTimeout: 5000,

    include: ['src/**/*.spec.ts', 'src/**/*.unit.test.ts'],
    exclude: [
      'node_modules',
      'dist',
      '**/*.integration.test.ts',
      'test/**/*',
    ],

    env: {
      NODE_ENV: 'test',
      LOG_LEVEL: 'error', // Suppress logs in unit tests
    },

    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      exclude: [
        'node_modules/**',
        'dist/**',
        'test/**',
        '**/*.config.ts',
        '**/*.d.ts',
        '**/types/**',
      ],
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
