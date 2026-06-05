import { BaseRepository } from './base.repository.js';
import {
  Source,
  SOURCE_COLLECTION,
  sourceIndexes,
  CreateSourcePayload,
  UpdateSourcePayload,
} from '../schemas/source.schema.js';
import { Filter, ObjectId, IndexSpecification } from 'mongodb';
import logger from '../../utils/logger.js';

/**
 * Source repository
 */
export class SourceRepository extends BaseRepository<Source> {
  protected collectionName = SOURCE_COLLECTION;

  /**
   * Creates indexes for the source collection
   */
  public async createIndexes(): Promise<void> {
    try {
      const collection = this.getCollection();

      for (const indexSpec of sourceIndexes) {
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
   * Creates a new source
   */
  public async createSource(payload: CreateSourcePayload): Promise<Source> {
    const sourceData = {
      ...payload,
      stats: {
        totalRuns: 0,
        successfulRuns: 0,
        failedRuns: 0,
        totalRecordsCollected: 0,
      },
    };
    return this.create(sourceData as Source);
  }

  /**
   * Updates a source
   */
  public async updateSource(
    id: string | ObjectId,
    payload: UpdateSourcePayload
  ): Promise<Source> {
    return this.updateById(id, payload);
  }

  /**
   * Finds sources by type
   */
  public async findByType(type: string): Promise<Source[]> {
    return this.find({ type } as Filter<Source>);
  }

  /**
   * Finds sources by status
   */
  public async findByStatus(status: string): Promise<Source[]> {
    return this.find({ status } as Filter<Source>);
  }

  /**
   * Finds a source by name
   */
  public async findByName(name: string): Promise<Source | null> {
    return this.findOne({ name } as Filter<Source>);
  }

  /**
   * Finds sources by type and status
   */
  public async findByTypeAndStatus(type: string, status: string): Promise<Source[]> {
    return this.find({ type, status } as Filter<Source>);
  }

  /**
   * Searches sources by text
   */
  public async searchSources(searchText: string): Promise<Source[]> {
    return this.find({ $text: { $search: searchText } } as Filter<Source>);
  }

  /**
   * Finds sources by tags
   */
  public async findByTags(tags: string[]): Promise<Source[]> {
    return this.find({ tags: { $in: tags } } as Filter<Source>);
  }

  /**
   * Updates source run statistics
   */
  public async updateRunStats(
    id: string | ObjectId,
    success: boolean,
    recordCount: number = 0,
    error?: string
  ): Promise<Source> {
    const source = await this.findById(id);
    if (!source) {
      throw new Error('Source not found');
    }

    const stats = source.stats || {
      totalRuns: 0,
      successfulRuns: 0,
      failedRuns: 0,
      totalRecordsCollected: 0,
    };

    const update = {
      lastRunAt: new Date(),
      lastRunStatus: success ? 'success' : 'failure',
      lastRunError: error,
      stats: {
        totalRuns: stats.totalRuns + 1,
        successfulRuns: success ? stats.successfulRuns + 1 : stats.successfulRuns,
        failedRuns: success ? stats.failedRuns : stats.failedRuns + 1,
        totalRecordsCollected: stats.totalRecordsCollected + recordCount,
        lastRecordCount: recordCount,
      },
    };

    return this.updateById(id, update as UpdateSourcePayload);
  }
}

// Export singleton instance
export const sourceRepository = new SourceRepository();
