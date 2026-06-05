import logger from './logger.js';

/**
 * Base error class for all application errors
 */
export class AppError extends Error {
  public readonly statusCode: number;
  public readonly isOperational: boolean;
  public readonly context?: Record<string, unknown>;

  constructor(
    message: string,
    statusCode: number = 500,
    isOperational: boolean = true,
    context?: Record<string, unknown>
  ) {
    super(message);
    this.statusCode = statusCode;
    this.isOperational = isOperational;
    this.context = context;

    // Maintains proper stack trace for where our error was thrown
    Error.captureStackTrace(this, this.constructor);

    // Set the prototype explicitly
    Object.setPrototypeOf(this, AppError.prototype);
  }
}

/**
 * Database-specific errors
 */
export class DatabaseError extends AppError {
  constructor(message: string, context?: Record<string, unknown>) {
    super(message, 500, true, { type: 'DatabaseError', ...context });
    Object.setPrototypeOf(this, DatabaseError.prototype);
  }
}

/**
 * Validation errors
 */
export class ValidationError extends AppError {
  constructor(message: string, context?: Record<string, unknown>) {
    super(message, 400, true, { type: 'ValidationError', ...context });
    Object.setPrototypeOf(this, ValidationError.prototype);
  }
}

/**
 * Not found errors
 */
export class NotFoundError extends AppError {
  constructor(message: string, context?: Record<string, unknown>) {
    super(message, 404, true, { type: 'NotFoundError', ...context });
    Object.setPrototypeOf(this, NotFoundError.prototype);
  }
}

/**
 * API/External service errors
 */
export class ExternalServiceError extends AppError {
  constructor(message: string, context?: Record<string, unknown>) {
    super(message, 502, true, { type: 'ExternalServiceError', ...context });
    Object.setPrototypeOf(this, ExternalServiceError.prototype);
  }
}

/**
 * Rate limit errors
 */
export class RateLimitError extends AppError {
  constructor(message: string, context?: Record<string, unknown>) {
    super(message, 429, true, { type: 'RateLimitError', ...context });
    Object.setPrototypeOf(this, RateLimitError.prototype);
  }
}

/**
 * Timeout errors
 */
export class TimeoutError extends AppError {
  constructor(message: string, context?: Record<string, unknown>) {
    super(message, 408, true, { type: 'TimeoutError', ...context });
    Object.setPrototypeOf(this, TimeoutError.prototype);
  }
}

/**
 * Configuration errors
 */
export class ConfigurationError extends AppError {
  constructor(message: string, context?: Record<string, unknown>) {
    super(message, 500, false, { type: 'ConfigurationError', ...context });
    Object.setPrototypeOf(this, ConfigurationError.prototype);
  }
}

/**
 * Global error handler
 */
export class ErrorHandler {
  /**
   * Handles errors and determines if the application should exit
   */
  public static handle(error: Error): void {
    if (error instanceof AppError) {
      ErrorHandler.handleAppError(error);
    } else {
      ErrorHandler.handleUnknownError(error);
    }
  }

  /**
   * Handles known application errors
   */
  private static handleAppError(error: AppError): void {
    logger.error(error.message, {
      statusCode: error.statusCode,
      isOperational: error.isOperational,
      context: error.context,
      stack: error.stack,
    });

    // If the error is not operational, exit the process
    if (!error.isOperational) {
      ErrorHandler.exitProcess();
    }
  }

  /**
   * Handles unknown errors
   */
  private static handleUnknownError(error: Error): void {
    logger.error('Unknown error occurred', {
      message: error.message,
      stack: error.stack,
    });

    // Unknown errors are considered non-operational
    ErrorHandler.exitProcess();
  }

  /**
   * Gracefully exits the process
   */
  private static exitProcess(): void {
    logger.error('Non-operational error detected. Exiting process...');
    process.exit(1);
  }
}

/**
 * Retry configuration
 */
export interface RetryConfig {
  maxAttempts: number;
  delayMs: number;
  backoffMultiplier?: number;
  maxDelayMs?: number;
  retryableErrors?: (typeof AppError)[];
}

/**
 * Default retry configuration
 */
const DEFAULT_RETRY_CONFIG: Omit<RetryConfig, 'retryableErrors'> & { retryableErrors?: (new (...args: unknown[]) => Error)[] } = {
  maxAttempts: 3,
  delayMs: 1000,
  backoffMultiplier: 2,
  maxDelayMs: 10000,
};

/**
 * Retry wrapper for operations
 */
export async function withRetry<T>(
  operation: () => Promise<T>,
  config: Partial<RetryConfig> = {}
): Promise<T> {
  const finalConfig = { ...DEFAULT_RETRY_CONFIG, ...config };
  let lastError: Error;
  let currentDelay = finalConfig.delayMs;

  for (let attempt = 1; attempt <= finalConfig.maxAttempts; attempt++) {
    try {
      logger.debug(`Attempt ${attempt}/${finalConfig.maxAttempts}`);
      return await operation();
    } catch (error) {
      lastError = error as Error;

      // Check if the error is retryable
      const retryable = isRetryable(error as Error, finalConfig.retryableErrors as (typeof AppError)[] | undefined);

      if (!retryable || attempt === finalConfig.maxAttempts) {
        logger.error(`Operation failed after ${attempt} attempts`, {
          error: lastError.message,
          isRetryable: retryable,
        });
        throw lastError;
      }

      // Log the retry attempt
      logger.warn(`Operation failed, retrying in ${currentDelay}ms`, {
        attempt,
        error: lastError.message,
      });

      // Wait before retrying
      await new Promise((resolve) => setTimeout(resolve, currentDelay));

      // Calculate next delay with exponential backoff
      currentDelay = Math.min(
        currentDelay * (finalConfig.backoffMultiplier || 2),
        finalConfig.maxDelayMs || 10000
      );
    }
  }

  throw lastError!;
}

/**
 * Helper to determine if an error is retryable
 */
function isRetryable(
  error: Error,
  retryableErrors?: (typeof AppError)[]
): boolean {
  if (!retryableErrors || retryableErrors.length === 0) {
    return true; // Retry all errors if no specific errors are configured
  }

  return retryableErrors.some((ErrorClass) => error instanceof ErrorClass);
}

/**
 * Async error wrapper for catching errors in async functions
 */
export const asyncErrorWrapper = <T extends unknown[], R>(
  fn: (...args: T) => Promise<R>
) => {
  return async (...args: T): Promise<R> => {
    try {
      return await fn(...args);
    } catch (error) {
      ErrorHandler.handle(error as Error);
      throw error;
    }
  };
};

/**
 * Process-level error handlers
 */
export const setupGlobalErrorHandlers = (): void => {
  // Handle uncaught exceptions
  process.on('uncaughtException', (error: Error) => {
    logger.error('Uncaught Exception', {
      message: error.message,
      stack: error.stack,
    });
    ErrorHandler.handle(error);
  });

  // Handle unhandled promise rejections
  process.on('unhandledRejection', (reason: unknown) => {
    const error = reason instanceof Error ? reason : new Error(String(reason));
    logger.error('Unhandled Rejection', {
      message: error.message,
      stack: error.stack,
    });
    ErrorHandler.handle(error);
  });

  // Handle process termination signals
  process.on('SIGTERM', () => {
    logger.info('SIGTERM signal received: closing application gracefully');
    process.exit(0);
  });

  process.on('SIGINT', () => {
    logger.info('SIGINT signal received: closing application gracefully');
    process.exit(0);
  });
};
