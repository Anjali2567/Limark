import { ObjectId } from 'mongodb';

/**
 * Contact role enumeration
 */
export enum ContactRole {
  DECISION_MAKER = 'decision_maker',
  INFLUENCER = 'influencer',
  CHAMPION = 'champion',
  USER = 'user',
  GATEKEEPER = 'gatekeeper',
}

/**
 * Contact status enumeration
 */
export enum ContactStatus {
  ACTIVE = 'active',
  INACTIVE = 'inactive',
  BOUNCED = 'bounced',
  UNSUBSCRIBED = 'unsubscribed',
}

/**
 * Contact interface
 */
export interface Contact {
  _id?: ObjectId;
  accountId: ObjectId;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  title?: string;
  role?: ContactRole;
  status: ContactStatus;
  department?: string;
  linkedin?: string;
  twitter?: string;
  location?: {
    country?: string;
    state?: string;
    city?: string;
  };
  tags?: string[];
  metadata?: Record<string, unknown>;
  createdAt: Date;
  updatedAt: Date;
  createdBy?: string;
  updatedBy?: string;
}

/**
 * Contact creation payload (without auto-generated fields)
 */
export type CreateContactPayload = Omit<Contact, '_id' | 'createdAt' | 'updatedAt'>;

/**
 * Contact update payload (partial)
 */
export type UpdateContactPayload = Partial<Omit<Contact, '_id' | 'createdAt'>>;

/**
 * Contact collection name
 */
export const CONTACT_COLLECTION = 'contacts';

/**
 * Contact indexes
 */
export const contactIndexes = [
  { key: { email: 1 }, unique: true },
  { key: { accountId: 1 } },
  { key: { firstName: 1 } },
  { key: { lastName: 1 } },
  { key: { status: 1 } },
  { key: { role: 1 } },
  { key: { department: 1 } },
  { key: { tags: 1 } },
  { key: { createdAt: -1 } },
  { key: { updatedAt: -1 } },
  // Compound index for account and status
  { key: { accountId: 1, status: 1 } },
  // Text search index
  { key: { firstName: 'text', lastName: 'text', email: 'text', title: 'text' } },
];
