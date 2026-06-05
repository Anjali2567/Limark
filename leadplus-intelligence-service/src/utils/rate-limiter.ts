import logger from './logger.js';
import { RateLimitError } from './errors.js';

/**
 * Rate limiter configuration
 */
export interface RateLimiterConfig {
  maxRequests: number; // Maximum number of requests
  windowMs: number; // Time window in milliseconds
  identifier?: string; // Optional identifier for logging
}

/**
 * Token bucket rate limiter implementation
 */
export class RateLimiter {
  private tokens: number;
  private lastRefill: number;
  private readonly maxTokens: number;
  private readonly refillRate: number; // tokens per millisecond
  private readonly identifier: string;

  constructor(config: RateLimiterConfig) {
    this.maxTokens = config.maxRequests;
    this.tokens = this.maxTokens;
    this.lastRefill = Date.now();
    this.refillRate = config.maxRequests / config.windowMs;
    this.identifier = config.identifier || 'default';
  }

  /**
   * Refills tokens based on elapsed time
   */
  private refill(): void {
    const now = Date.now();
    const timePassed = now - this.lastRefill;
    const tokensToAdd = timePassed * this.refillRate;

    this.tokens = Math.min(this.maxTokens, this.tokens + tokensToAdd);
    this.lastRefill = now;
  }

  /**
   * Attempts to consume a token
   * @returns true if token was consumed, false otherwise
   */
  public tryConsume(): boolean {
    this.refill();

    if (this.tokens >= 1) {
      this.tokens -= 1;
      logger.debug(`Rate limiter consumed token`, {
        identifier: this.identifier,
        tokensRemaining: Math.floor(this.tokens),
      });
      return true;
    }

    logger.warn(`Rate limit exceeded`, {
      identifier: this.identifier,
    });
    return false;
  }

  /**
   * Waits until a token is available and consumes it
   */
  public async consume(): Promise<void> {
    this.refill();

    if (this.tokens >= 1) {
      this.tokens -= 1;
      return;
    }

    // Calculate wait time until next token is available
    const waitMs = Math.ceil((1 - this.tokens) / this.refillRate);

    logger.debug(`Rate limit reached, waiting ${waitMs}ms`, {
      identifier: this.identifier,
    });

    await new Promise((resolve) => setTimeout(resolve, waitMs));

    // Refill and consume after waiting
    this.refill();
    this.tokens -= 1;
  }

  /**
   * Gets the current number of available tokens
   */
  public getAvailableTokens(): number {
    this.refill();
    return Math.floor(this.tokens);
  }

  /**
   * Resets the rate limiter
   */
  public reset(): void {
    this.tokens = this.maxTokens;
    this.lastRefill = Date.now();
    logger.debug(`Rate limiter reset`, {
      identifier: this.identifier,
    });
  }
}

/**
 * Rate limiter registry for managing multiple rate limiters
 */
export class RateLimiterRegistry {
  private limiters: Map<string, RateLimiter> = new Map();

  /**
   * Gets or creates a rate limiter for a given key
   */
  public getLimiter(key: string, config: RateLimiterConfig): RateLimiter {
    if (!this.limiters.has(key)) {
      this.limiters.set(
        key,
        new RateLimiter({
          ...config,
          identifier: key,
        })
      );
    }

    return this.limiters.get(key)!;
  }

  /**
   * Removes a rate limiter
   */
  public removeLimiter(key: string): void {
    this.limiters.delete(key);
  }

  /**
   * Clears all rate limiters
   */
  public clear(): void {
    this.limiters.clear();
  }
}

/**
 * Global rate limiter registry instance
 */
export const rateLimiterRegistry = new RateLimiterRegistry();

/**
 * Decorator for rate-limited functions
 */
export function rateLimit(config: RateLimiterConfig) {
  const limiter = new RateLimiter(config);

  return function <T extends unknown[], R>(
    _target: unknown,
    propertyKey: string,
    descriptor: TypedPropertyDescriptor<(...args: T) => Promise<R>>
  ) {
    const originalMethod = descriptor.value!;

    descriptor.value = async function (...args: T): Promise<R> {
      if (!limiter.tryConsume()) {
        throw new RateLimitError(`Rate limit exceeded for ${propertyKey}`, {
          method: propertyKey,
          maxRequests: config.maxRequests,
          windowMs: config.windowMs,
        });
      }

      return originalMethod.apply(this, args);
    };

    return descriptor;
  };
}

/**
 * Higher-order function for rate-limited operations
 */
export function withRateLimit<T extends unknown[], R>(
  fn: (...args: T) => Promise<R>,
  config: RateLimiterConfig
): (...args: T) => Promise<R> {
  const limiter = new RateLimiter(config);

  return async (...args: T): Promise<R> => {
    await limiter.consume(); // Wait if necessary
    return fn(...args);
  };
}
