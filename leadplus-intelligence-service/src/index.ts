import { connectToDatabase, disconnectFromDatabase } from './db/connection.js';
import { setupGlobalErrorHandlers } from './utils/errors.js';
import logger from './utils/logger.js';
import config from './config/index.js';
import { accountRepository } from './db/repositories/account.repository.js';
import { contactRepository } from './db/repositories/contact.repository.js';
import { sourceRepository } from './db/repositories/source.repository.js';
import { createApp, startServer } from './api/server.js';

/**
 * Initializes database indexes
 */
async function initializeIndexes(): Promise<void> {
  try {
    logger.info('Initializing database indexes...');

    await accountRepository.createIndexes();
    await contactRepository.createIndexes();
    await sourceRepository.createIndexes();

    logger.info('Database indexes initialized successfully');
  } catch (error) {
    logger.error('Failed to initialize database indexes', {
      error: error instanceof Error ? error.message : 'Unknown error',
    });
    throw error;
  }
}

/**
 * Displays application startup information
 */
function displayStartupInfo(): void {
  logger.info('='.repeat(60));
  logger.info('LeadPlus Intelligence Layer - Starting');
  logger.info('='.repeat(60));
  logger.info('Configuration:', {
    environment: config.env,
    port: config.port,
    database: config.database.name,
    logLevel: config.logging.level,
  });
  logger.info('='.repeat(60));
}

/**
 * Main application bootstrap function
 */
async function bootstrap(): Promise<void> {
  try {
    // Display startup information
    displayStartupInfo();

    // Connect to database
    logger.info('Connecting to database...');
    await connectToDatabase();
    logger.info('Database connection established');

    // Initialize indexes
    await initializeIndexes();

    // Display database statistics
    const stats = await accountRepository.count();
    const contactStats = await contactRepository.count();
    const sourceStats = await sourceRepository.count();

    logger.info('Database statistics:', {
      accounts: stats,
      contacts: contactStats,
      sources: sourceStats,
    });

    // Create and start Express server
    const app = createApp();
    startServer(app);

    logger.info('Application ready!');
    logger.info('='.repeat(60));
  } catch (error) {
    logger.error('Application bootstrap failed', {
      error: error instanceof Error ? error.message : 'Unknown error',
      stack: error instanceof Error ? error.stack : undefined,
    });
    throw error;
  }
}

/**
 * Graceful shutdown handler
 */
async function gracefulShutdown(signal: string): Promise<void> {
  logger.info(`${signal} signal received: closing application gracefully`);

  try {
    // Disconnect from database
    await disconnectFromDatabase();
    logger.info('Database connection closed');

    logger.info('Application shutdown complete');
    process.exit(0);
  } catch (error) {
    logger.error('Error during graceful shutdown', {
      error: error instanceof Error ? error.message : 'Unknown error',
    });
    process.exit(1);
  }
}

/**
 * Set up signal handlers for graceful shutdown
 */
function setupSignalHandlers(): void {
  process.on('SIGTERM', () => gracefulShutdown('SIGTERM'));
  process.on('SIGINT', () => gracefulShutdown('SIGINT'));
}

// Set up global error handlers
setupGlobalErrorHandlers();

// Set up signal handlers
setupSignalHandlers();

// Start the application
bootstrap().catch((error) => {
  logger.error('Fatal error during application startup', {
    error: error instanceof Error ? error.message : 'Unknown error',
    stack: error instanceof Error ? error.stack : undefined,
  });
  process.exit(1);
});
