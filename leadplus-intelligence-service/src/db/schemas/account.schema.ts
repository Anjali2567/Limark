import { ObjectId } from 'mongodb';

/**
 * Account type enumeration
 */
export enum AccountType {
  ENTERPRISE = 'enterprise',
  SMB = 'smb',
  STARTUP = 'startup',
  INDIVIDUAL = 'individual',
}

/**
 * Account status enumeration
 */
export enum AccountStatus {
  ACTIVE = 'active',
  INACTIVE = 'inactive',
  PROSPECT = 'prospect',
  CHURNED = 'churned',
}

/**
 * Account interface
 */
export interface Account {
  _id?: ObjectId;
  name: string;
  domain?: string;
  type: AccountType;
  status: AccountStatus;
  industry?: string;
  size?: {
    employees?: number;
    revenue?: number;
  };
  location?: {
    country?: string;
    state?: string;
    city?: string;
    address?: string;
  };
  tags?: string[];
  metadata?: Record<string, unknown>;
  createdAt: Date;
  updatedAt: Date;
  createdBy?: string;
  updatedBy?: string;
}

/**
 * Account creation payload (without auto-generated fields)
 */
export type CreateAccountPayload = Omit<Account, '_id' | 'createdAt' | 'updatedAt'>;

/**
 * Account update payload (partial)
 */
export type UpdateAccountPayload = Partial<Omit<Account, '_id' | 'createdAt'>>;

/**
 * Account collection name
 */
export const ACCOUNT_COLLECTION = 'accounts';

/**
 * Account indexes
 */
export const accountIndexes = [
  { key: { name: 1 }, unique: true },
  { key: { domain: 1 }, unique: true, sparse: true },
  { key: { type: 1 } },
  { key: { status: 1 } },
  { key: { industry: 1 } },
  { key: { tags: 1 } },
  { key: { createdAt: -1 } },
  { key: { updatedAt: -1 } },
  // Text search index
  { key: { name: 'text', industry: 'text', 'location.city': 'text' } },
];
