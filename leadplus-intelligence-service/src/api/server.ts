import express, { Express } from 'express';
import cors from 'cors';
import config from '../config/index.js';
import logger from '../utils/logger.js';
import { errorHandler, notFoundHandler } from './middleware/error-handler.js';
import { requestLogger } from './middleware/request-logger.js';
import accountRoutes from './routes/account.routes.js';
import contactRoutes from './routes/contact.routes.js';
import sourceRoutes from './routes/source.routes.js';

/**
 * Creates and configures the Express application
 */
export const createApp = (): Express => {
  const app = express();

  // Middleware
  app.use(cors());
  app.use(express.json());
  app.use(express.urlencoded({ extended: true }));
  app.use(requestLogger);

  // Health check endpoint
  app.get('/health', (_req, res) => {
    res.json({
      success: true,
      data: {
        status: 'healthy',
        timestamp: new Date().toISOString(),
        environment: config.env,
      },
    });
  });

  // API routes
  app.use('/api/accounts', accountRoutes);
  app.use('/api/contacts', contactRoutes);
  app.use('/api/sources', sourceRoutes);

  // Error handling
  app.use(notFoundHandler);
  app.use(errorHandler);

  return app;
};

/**
 * Starts the Express server
 */
export const startServer = (app: Express): void => {
  app.listen(config.port, () => {
    logger.info(`Server started on port ${config.port}`, {
      port: config.port,
      environment: config.env,
    });
  });
};
