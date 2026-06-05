import { BaseRepository } from './base.repository.js';
import {
  Account,
  ACCOUNT_COLLECTION,
  accountIndexes,
  CreateAccountPayload,
  UpdateAccountPayload,
} from '../schemas/account.schema.js';
import { Filter, ObjectId, IndexSpecification } from 'mongodb';
import logger from '../../utils/logger.js';

/**
 * Account repository
 */
export class AccountRepository extends BaseRepository<Account> {
  protected collectionName = ACCOUNT_COLLECTION;

  /**
   * Creates indexes for the account collection
   */
  public async createIndexes(): Promise<void> {
    try {
      const collection = this.getCollection();

      for (const indexSpec of accountIndexes) {
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
   * Creates a new account
   */
  public async createAccount(payload: CreateAccountPayload): Promise<Account> {
    return this.create(payload as Account);
  }

  /**
   * Updates an account
   */
  public async updateAccount(
    id: string | ObjectId,
    payload: UpdateAccountPayload
  ): Promise<Account> {
    return this.updateById(id, payload);
  }

  /**
   * Finds accounts by type
   */
  public async findByType(type: string): Promise<Account[]> {
    return this.find({ type } as Filter<Account>);
  }

  /**
   * Finds accounts by status
   */
  public async findByStatus(status: string): Promise<Account[]> {
    return this.find({ status } as Filter<Account>);
  }

  /**
   * Finds an account by domain
   */
  public async findByDomain(domain: string): Promise<Account | null> {
    return this.findOne({ domain } as Filter<Account>);
  }

  /**
   * Finds an account by name
   */
  public async findByName(name: string): Promise<Account | null> {
    return this.findOne({ name } as Filter<Account>);
  }

  /**
   * Searches accounts by text
   */
  public async searchAccounts(searchText: string): Promise<Account[]> {
    return this.find({ $text: { $search: searchText } } as Filter<Account>);
  }

  /**
   * Finds accounts by tags
   */
  public async findByTags(tags: string[]): Promise<Account[]> {
    return this.find({ tags: { $in: tags } } as Filter<Account>);
  }
}

// Export singleton instance
export const accountRepository = new AccountRepository();
