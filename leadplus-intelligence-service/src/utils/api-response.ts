import { AppError } from './errors.js';

/**
 * Standard API response structure
 */
export interface ApiResponse<T = unknown> {
  success: boolean;
  data?: T;
  error?: ErrorResponse;
  metadata?: ResponseMetadata;
}

/**
 * Error response structure
 */
export interface ErrorResponse {
  message: string;
  code?: string;
  statusCode?: number;
  details?: Record<string, unknown>;
}

/**
 * Response metadata
 */
export interface ResponseMetadata {
  timestamp: string;
  requestId?: string;
  duration?: number;
  pagination?: PaginationMetadata;
}

/**
 * Pagination metadata
 */
export interface PaginationMetadata {
  page: number;
  limit: number;
  total: number;
  totalPages: number;
  hasNext: boolean;
  hasPrev: boolean;
}

/**
 * API Response utility class
 */
export class ApiResponseUtil {
  /**
   * Creates a successful response
   */
  public static success<T>(
    data: T,
    metadata?: Partial<ResponseMetadata>
  ): ApiResponse<T> {
    return {
      success: true,
      data,
      metadata: {
        timestamp: new Date().toISOString(),
        ...metadata,
      },
    };
  }

  /**
   * Creates an error response
   */
  public static error(
    error: Error | AppError | string,
    metadata?: Partial<ResponseMetadata>
  ): ApiResponse {
    const errorResponse: ErrorResponse = ApiResponseUtil.formatError(error);

    return {
      success: false,
      error: errorResponse,
      metadata: {
        timestamp: new Date().toISOString(),
        ...metadata,
      },
    };
  }

  /**
   * Creates a paginated response
   */
  public static paginated<T>(
    data: T[],
    page: number,
    limit: number,
    total: number,
    metadata?: Partial<ResponseMetadata>
  ): ApiResponse<T[]> {
    const totalPages = Math.ceil(total / limit);

    const paginationMetadata: PaginationMetadata = {
      page,
      limit,
      total,
      totalPages,
      hasNext: page < totalPages,
      hasPrev: page > 1,
    };

    return {
      success: true,
      data,
      metadata: {
        timestamp: new Date().toISOString(),
        pagination: paginationMetadata,
        ...metadata,
      },
    };
  }

  /**
   * Formats an error into a standard error response
   */
  private static formatError(error: Error | AppError | string): ErrorResponse {
    if (typeof error === 'string') {
      return {
        message: error,
        code: 'UNKNOWN_ERROR',
        statusCode: 500,
      };
    }

    if (error instanceof AppError) {
      return {
        message: error.message,
        code: error.constructor.name,
        statusCode: error.statusCode,
        details: error.context,
      };
    }

    return {
      message: error.message || 'An unexpected error occurred',
      code: 'INTERNAL_ERROR',
      statusCode: 500,
      details: {
        name: error.name,
      },
    };
  }
}

/**
 * Helper functions for common response patterns
 */

export const successResponse = <T>(data: T, metadata?: Partial<ResponseMetadata>) =>
  ApiResponseUtil.success(data, metadata);

export const errorResponse = (
  error: Error | AppError | string,
  metadata?: Partial<ResponseMetadata>
) => ApiResponseUtil.error(error, metadata);

export const paginatedResponse = <T>(
  data: T[],
  page: number,
  limit: number,
  total: number,
  metadata?: Partial<ResponseMetadata>
) => ApiResponseUtil.paginated(data, page, limit, total, metadata);

/**
 * Pagination helper
 */
export interface PaginationParams {
  page?: number;
  limit?: number;
}

export const DEFAULT_PAGE = 1;
export const DEFAULT_LIMIT = 20;
export const MAX_LIMIT = 100;

export const normalizePaginationParams = (
  params: PaginationParams
): { page: number; limit: number; skip: number } => {
  const page = Math.max(1, params.page || DEFAULT_PAGE);
  const limit = Math.min(
    MAX_LIMIT,
    Math.max(1, params.limit || DEFAULT_LIMIT)
  );
  const skip = (page - 1) * limit;

  return { page, limit, skip };
};
