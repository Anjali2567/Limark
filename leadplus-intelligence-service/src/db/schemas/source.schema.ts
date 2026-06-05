import { ObjectId } from 'mongodb';

/**
 * Source type enumeration
 */
export enum SourceType {
  WEB_SCRAPING = 'web_scraping',
  API = 'api',
  UPLOAD = 'upload',
  INTEGRATION = 'integration',
  MANUAL = 'manual',
}

/**
 * Source status enumeration
 */
export enum SourceStatus {
  ACTIVE = 'active',
  INACTIVE = 'inactive',
  PENDING = 'pending',
  ERROR = 'error',
}

/**
 * Scraper configuration
 */
export interface ScraperConfig {
  url?: string;
  frequency?: string; // cron expression
  selectors?: Record<string, string>;
  headers?: Record<string, string>;
  rateLimit?: {
    maxRequests: number;
    windowMs: number;
  };
}

/**
 * API configuration
 */
export interface ApiConfig {
  endpoint?: string;
  method?: string;
  headers?: Record<string, string>;
  auth?: {
    type: string;
    credentials: Record<string, string>;
  };
  frequency?: string; // cron expression
}

/**
 * Source interface
 */
export interface Source {
  _id?: ObjectId;
  name: string;
  type: SourceType;
  status: SourceStatus;
  description?: string;
  config?: ScraperConfig | ApiConfig | Record<string, unknown>;
  lastRunAt?: Date;
  lastRunStatus?: 'success' | 'failure' | 'partial';
  lastRunError?: string;
  stats?: {
    totalRuns: number;
    successfulRuns: number;
    failedRuns: number;
    totalRecordsCollected: number;
    lastRecordCount?: number;
  };
  tags?: string[];
  metadata?: Record<string, unknown>;
  createdAt: Date;
  updatedAt: Date;
  createdBy?: string;
  updatedBy?: string;
}

/**
 * Source creation payload (without auto-generated fields)
 */
export type CreateSourcePayload = Omit<
  Source,
  '_id' | 'createdAt' | 'updatedAt' | 'lastRunAt' | 'lastRunStatus' | 'lastRunError' | 'stats'
>;

/**
 * Source update payload (partial)
 */
export type UpdateSourcePayload = Partial<Omit<Source, '_id' | 'createdAt'>>;

/**
 * Source collection name
 */
export const SOURCE_COLLECTION = 'sources';

/**
 * Source indexes
 */
export const sourceIndexes = [
  { key: { name: 1 }, unique: true },
  { key: { type: 1 } },
  { key: { status: 1 } },
  { key: { tags: 1 } },
  { key: { lastRunAt: -1 } },
  { key: { createdAt: -1 } },
  { key: { updatedAt: -1 } },
  // Compound index for type and status
  { key: { type: 1, status: 1 } },
  // Text search index
  { key: { name: 'text', description: 'text' } },
];
