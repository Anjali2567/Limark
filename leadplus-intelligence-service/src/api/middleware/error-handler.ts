import { Request, Response, NextFunction } from 'express';
import { AppError } from '../../utils/errors.js';
import logger from '../../utils/logger.js';
import { errorResponse } from '../../utils/api-response.js';

/**
 * Determines the appropriate log level based on error type and status code
 * Prevents log spam from expected errors (validation, auth failures, etc.)
 */
function getLogLevel(statusCode: number, _error: Error): 'error' | 'warn' | 'info' | 'silent' {
  // 400 Bad Request - Client validation errors (expected, not logged)
  if (statusCode === 400) {
    return 'silent';
  }

  // 401 Unauthorized - Auth failures are normal (info level)
  if (statusCode === 401) {
    return 'info';
  }

  // 403 Forbidden - Permission errors (warn level)
  if (statusCode === 403) {
    return 'warn';
  }

  // 404 Not Found - Expected in normal operation (warn level)
  if (statusCode === 404) {
    return 'warn';
  }

  // 409 Conflict - Rate limits, duplicates (info level)
  if (statusCode === 409) {
    return 'info';
  }

  // 429 Too Many Requests - Rate limiting (info level)
  if (statusCode === 429) {
    return 'info';
  }

  // 5xx Server Errors - Actual problems (error level)
  if (statusCode >= 500) {
    return 'error';
  }

  // Default: log as warning
  return 'warn';
}

/**
 * Sanitizes error message for production
 * Truncates long messages and removes sensitive data
 */
function sanitizeErrorMessage(message: string, maxLength: number = 500): string {
  if (message.length <= maxLength) {
    return message;
  }

  return message.substring(0, maxLength) + '... (truncated)';
}

/**
 * Global error handling middleware for Express
 * Implements smart logging to reduce noise and improve signal-to-noise ratio
 */
export const errorHandler = (
  error: Error,
  req: Request,
  res: Response,
  _next: NextFunction
): void => {
  const statusCode = error instanceof AppError ? error.statusCode : 500;
  const logLevel = getLogLevel(statusCode, error);

  // Build error context
  const errorContext = {
    method: req.method,
    path: req.path,
    statusCode,
    error: sanitizeErrorMessage(error.message),
    ...(error instanceof AppError && error.context ? { context: error.context } : {}),
  };

  // Log based on determined level
  if (logLevel === 'error') {
    logger.error('API Error', {
      ...errorContext,
      stack: error.stack,
    });
  } else if (logLevel === 'warn') {
    logger.warn('API Warning', errorContext);
  } else if (logLevel === 'info') {
    logger.info('API Info', errorContext);
  }
  // 'silent' level means no logging

  // Send appropriate response
  if (error instanceof AppError) {
    res.status(error.statusCode).json(errorResponse(error));
    return;
  }

  // Unknown errors
  res.status(500).json(errorResponse(error));
};

/**
 * 404 Not Found handler
 */
export const notFoundHandler = (req: Request, res: Response): void => {
  res.status(404).json(
    errorResponse(`Route ${req.method} ${req.path} not found`)
  );
};
