import { Request, Response, NextFunction } from 'express';
import { errorResponse } from '../../utils/api-response.js';
import logger from '../../utils/logger.js';

/**
 * API Key authentication middleware
 */
export const authenticateApiKey = (
  req: Request,
  res: Response,
  next: NextFunction
): void => {
  // Get API key from header
  const apiKey = req.headers['x-api-key'] as string;

  if (!apiKey) {
    logger.warn('API request without API key', {
      path: req.path,
      method: req.method,
      ip: req.ip,
    });

    res.status(401).json(
      errorResponse('API key is required. Please provide X-API-Key header.')
    );
    return;
  }

  // Check if API key is valid (from environment)
  const validApiKey = process.env.EXTERNAL_API_KEY;

  if (!validApiKey || apiKey !== validApiKey) {
    logger.warn('Invalid API key attempt', {
      path: req.path,
      method: req.method,
      ip: req.ip,
      apiKey: apiKey.substring(0, 8) + '...', // Log only first 8 chars for security
    });

    res.status(403).json(errorResponse('Invalid API key'));
    return;
  }

  // API key is valid, proceed
  logger.debug('API key authenticated', {
    path: req.path,
    method: req.method,
  });

  next();
};
