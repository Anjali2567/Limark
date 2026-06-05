import dotenv from 'dotenv';
import { ConfigurationError } from '../utils/errors.js';

// Load environment variables from .env file
dotenv.config();

/**
 * Database configuration
 */
export interface DatabaseConfig {
  url: string;
  name: string;
  maxPoolSize: number;
  minPoolSize: number;
  connectTimeoutMs: number;
  socketTimeoutMs: number;
}

/**
 * Logging configuration
 */
export interface LoggingConfig {
  level: string;
  dir: string;
}

/**
 * Rate limiting configuration
 */
export interface RateLimitConfig {
  maxRequests: number;
  windowMs: number;
}

/**
 * External API configuration
 */
export interface ExternalApiConfig {
  baseUrl?: string;
  apiKey?: string;
  timeout: number;
  retryAttempts: number;
}

/**
 * Application configuration
 */
export interface AppConfig {
  env: string;
  port: number;
  database: DatabaseConfig;
  logging: LoggingConfig;
  rateLimit: RateLimitConfig;
  externalApis: {
    [key: string]: ExternalApiConfig;
  };
}

/**
 * Validates required environment variables
 */
const validateEnvVar = (name: string, value: string | undefined): string => {
  if (!value || value.trim() === '') {
    throw new ConfigurationError(`Missing required environment variable: ${name}`);
  }
  return value;
};

/**
 * Gets environment variable with default value
 */
const getEnvVar = (name: string, defaultValue: string): string => {
  return process.env[name] || defaultValue;
};

/**
 * Gets numeric environment variable with default value
 */
const getNumericEnvVar = (name: string, defaultValue: number): number => {
  const value = process.env[name];
  if (!value) {
    return defaultValue;
  }
  const parsed = parseInt(value, 10);
  if (isNaN(parsed)) {
    throw new ConfigurationError(`Invalid numeric value for ${name}: ${value}`);
  }
  return parsed;
};

/**
 * Loads and validates application configuration
 */
const loadConfig = (): AppConfig => {
  const env = getEnvVar('NODE_ENV', 'development');

  // Database configuration
  const databaseUrl = validateEnvVar('DATABASE_URL', process.env.DATABASE_URL);
  const databaseName = getEnvVar('DATABASE_NAME', 'leadplus_intelligence');

  // Logging configuration
  const logLevel = getEnvVar('LOG_LEVEL', env === 'production' ? 'info' : 'debug');
  const logDir = getEnvVar('LOG_DIR', 'logs');

  // Rate limiting configuration
  const rateLimitMaxRequests = getNumericEnvVar('RATE_LIMIT_MAX_REQUESTS', 100);
  const rateLimitWindowMs = getNumericEnvVar('RATE_LIMIT_WINDOW_MS', 60000);

  // Application port
  const port = getNumericEnvVar('PORT', 3000);

  const config: AppConfig = {
    env,
    port,
    database: {
      url: databaseUrl,
      name: databaseName,
      maxPoolSize: getNumericEnvVar('DB_MAX_POOL_SIZE', 10),
      minPoolSize: getNumericEnvVar('DB_MIN_POOL_SIZE', 2),
      connectTimeoutMs: getNumericEnvVar('DB_CONNECT_TIMEOUT_MS', 10000),
      socketTimeoutMs: getNumericEnvVar('DB_SOCKET_TIMEOUT_MS', 45000),
    },
    logging: {
      level: logLevel,
      dir: logDir,
    },
    rateLimit: {
      maxRequests: rateLimitMaxRequests,
      windowMs: rateLimitWindowMs,
    },
    externalApis: {
      // Example external API configuration
      default: {
        baseUrl: process.env.EXTERNAL_API_BASE_URL,
        apiKey: process.env.EXTERNAL_API_KEY,
        timeout: getNumericEnvVar('EXTERNAL_API_TIMEOUT', 30000),
        retryAttempts: getNumericEnvVar('EXTERNAL_API_RETRY_ATTEMPTS', 3),
      },
    },
  };

  return config;
};

/**
 * Export the configuration
 */
let config: AppConfig;

try {
  config = loadConfig();
} catch (error) {
  console.error('Failed to load configuration:', error);
  process.exit(1);
}

export default config;

/**
 * Helper function to check if running in production
 */
export const isProduction = (): boolean => config.env === 'production';

/**
 * Helper function to check if running in development
 */
export const isDevelopment = (): boolean => config.env === 'development';

/**
 * Helper function to check if running in test
 */
export const isTest = (): boolean => config.env === 'test';
