import { BaseRepository } from './base.repository.js';
import {
  Contact,
  CONTACT_COLLECTION,
  contactIndexes,
  CreateContactPayload,
  UpdateContactPayload,
} from '../schemas/contact.schema.js';
import { Filter, ObjectId, IndexSpecification } from 'mongodb';
import logger from '../../utils/logger.js';

/**
 * Contact repository
 */
export class ContactRepository extends BaseRepository<Contact> {
  protected collectionName = CONTACT_COLLECTION;

  /**
   * Creates indexes for the contact collection
   */
  public async createIndexes(): Promise<void> {
    try {
      const collection = this.getCollection();

      for (const indexSpec of contactIndexes) {
        const options: { unique?: boolean; sparse?: boolean } = {};
        if ('unique' in indexSpec && indexSpec.unique) options.unique = true;
        if ('sparse' in indexSpec && indexSpec.sparse) options.sparse = true;
        await collection.createIndex(indexSpec.key as unknown as IndexSpecification, options);
      }

      logger.info(`Indexes created for ${this.collectionName}`);
    } catch (error) {
      logger.error(`Failed to create indexes for ${this.collectionName}`, {
        error: error instanceof Error ? error.message : 'Unknown error',
      });
      throw error;
    }
  }

  /**
   * Creates a new contact
   */
  public async createContact(payload: CreateContactPayload): Promise<Contact> {
    return this.create(payload as Contact);
  }

  /**
   * Updates a contact
   */
  public async updateContact(
    id: string | ObjectId,
    payload: UpdateContactPayload
  ): Promise<Contact> {
    return this.updateById(id, payload);
  }

  /**
   * Finds contacts by account ID
   */
  public async findByAccountId(accountId: string | ObjectId): Promise<Contact[]> {
    const objectId = typeof accountId === 'string' ? new ObjectId(accountId) : accountId;
    return this.find({ accountId: objectId } as Filter<Contact>);
  }

  /**
   * Finds a contact by email
   */
  public async findByEmail(email: string): Promise<Contact | null> {
    return this.findOne({ email } as Filter<Contact>);
  }

  /**
   * Finds contacts by status
   */
  public async findByStatus(status: string): Promise<Contact[]> {
    return this.find({ status } as Filter<Contact>);
  }

  /**
   * Finds contacts by role
   */
  public async findByRole(role: string): Promise<Contact[]> {
    return this.find({ role } as Filter<Contact>);
  }

  /**
   * Finds contacts by account and status
   */
  public async findByAccountAndStatus(
    accountId: string | ObjectId,
    status: string
  ): Promise<Contact[]> {
    const objectId = typeof accountId === 'string' ? new ObjectId(accountId) : accountId;
    return this.find({ accountId: objectId, status } as Filter<Contact>);
  }

  /**
   * Searches contacts by text
   */
  public async searchContacts(searchText: string): Promise<Contact[]> {
    return this.find({ $text: { $search: searchText } } as Filter<Contact>);
  }

  /**
   * Finds contacts by tags
   */
  public async findByTags(tags: string[]): Promise<Contact[]> {
    return this.find({ tags: { $in: tags } } as Filter<Contact>);
  }

  /**
   * Finds contacts by department
   */
  public async findByDepartment(department: string): Promise<Contact[]> {
    return this.find({ department } as Filter<Contact>);
  }
}

// Export singleton instance
export const contactRepository = new ContactRepository();
