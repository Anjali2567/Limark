import {
  Collection,
  Document,
  Filter,
  FindOptions,
  ObjectId,
  UpdateFilter,
  DeleteResult,
  InsertOneResult,
  UpdateResult,
  OptionalUnlessRequiredId,
} from 'mongodb';
import { getDb } from '../connection.js';
import logger, { trackPerformance } from '../../utils/logger.js';
import { DatabaseError, NotFoundError } from '../../utils/errors.js';

/**
 * Base repository class for common CRUD operations
 */
export abstract class BaseRepository<T extends Document> {
  protected abstract collectionName: string;

  /**
   * Gets the collection instance
   */
  protected getCollection(): Collection<T> {
    try {
      const db = getDb();
      return db.collection<T>(this.collectionName);
    } catch (error) {
      throw new DatabaseError(`Failed to get collection: ${this.collectionName}`, {
        error: error instanceof Error ? error.message : 'Unknown error',
      });
    }
  }

  /**
   * Creates a new document
   */
  public async create(data: OptionalUnlessRequiredId<T>): Promise<T> {
    return trackPerformance(`${this.collectionName}.create`, async () => {
      try {
        const collection = this.getCollection();
        const now = new Date();

        const documentToInsert = {
          ...data,
          createdAt: now,
          updatedAt: now,
        } as OptionalUnlessRequiredId<T>;

        const result: InsertOneResult<T> = await collection.insertOne(documentToInsert);

        logger.info(`Document created in ${this.collectionName}`, {
          id: result.insertedId.toString(),
        });

        return {
          ...documentToInsert,
          _id: result.insertedId,
        } as T;
      } catch (error) {
        logger.error(`Failed to create document in ${this.collectionName}`, {
          error: error instanceof Error ? error.message : 'Unknown error',
        });
        throw new DatabaseError(`Failed to create document in ${this.collectionName}`, {
          error: error instanceof Error ? error.message : 'Unknown error',
        });
      }
    });
  }

  /**
   * Finds a document by ID
   */
  public async findById(id: string | ObjectId): Promise<T | null> {
    return trackPerformance(`${this.collectionName}.findById`, async () => {
      try {
        const collection = this.getCollection();
        const objectId = typeof id === 'string' ? new ObjectId(id) : id;

        const document = await collection.findOne({ _id: objectId } as Filter<T>);

        if (document) {
          logger.debug(`Document found in ${this.collectionName}`, {
            id: objectId.toString(),
          });
        }

        return document as T | null;
      } catch (error) {
        logger.error(`Failed to find document in ${this.collectionName}`, {
          id: id.toString(),
          error: error instanceof Error ? error.message : 'Unknown error',
        });
        throw new DatabaseError(`Failed to find document in ${this.collectionName}`, {
          error: error instanceof Error ? error.message : 'Unknown error',
        });
      }
    });
  }

  /**
   * Finds one document by filter
   */
  public async findOne(filter: Filter<T>): Promise<T | null> {
    return trackPerformance(`${this.collectionName}.findOne`, async () => {
      try {
        const collection = this.getCollection();
        const result = await collection.findOne(filter);
        return result as T | null;
      } catch (error) {
        logger.error(`Failed to find document in ${this.collectionName}`, {
          error: error instanceof Error ? error.message : 'Unknown error',
        });
        throw new DatabaseError(`Failed to find document in ${this.collectionName}`, {
          error: error instanceof Error ? error.message : 'Unknown error',
        });
      }
    });
  }

  /**
   * Finds multiple documents by filter
   */
  public async find(filter: Filter<T> = {}, options?: FindOptions<T>): Promise<T[]> {
    return trackPerformance(`${this.collectionName}.find`, async () => {
      try {
        const collection = this.getCollection();
        const documents = await collection.find(filter, options).toArray();

        logger.debug(`Found ${documents.length} documents in ${this.collectionName}`);

        return documents as T[];
      } catch (error) {
        logger.error(`Failed to find documents in ${this.collectionName}`, {
          error: error instanceof Error ? error.message : 'Unknown error',
        });
        throw new DatabaseError(`Failed to find documents in ${this.collectionName}`, {
          error: error instanceof Error ? error.message : 'Unknown error',
        });
      }
    });
  }

  /**
   * Finds all documents
   */
  public async findAll(options?: FindOptions<T>): Promise<T[]> {
    return this.find({}, options);
  }

  /**
   * Updates a document by ID
   */
  public async updateById(
    id: string | ObjectId,
    update: UpdateFilter<T> | Partial<T>
  ): Promise<T> {
    return trackPerformance(`${this.collectionName}.updateById`, async () => {
      try {
        const collection = this.getCollection();
        const objectId = typeof id === 'string' ? new ObjectId(id) : id;

        const updateDoc =
          '$set' in update
            ? update
            : {
                $set: {
                  ...update,
                  updatedAt: new Date(),
                },
              };

        const result: UpdateResult = await collection.updateOne(
          { _id: objectId } as Filter<T>,
          updateDoc as UpdateFilter<T>
        );

        if (result.matchedCount === 0) {
          throw new NotFoundError(`Document not found in ${this.collectionName}`, {
            id: objectId.toString(),
          });
        }

        logger.info(`Document updated in ${this.collectionName}`, {
          id: objectId.toString(),
          modifiedCount: result.modifiedCount,
        });

        const updated = await this.findById(objectId);
        if (!updated) {
          throw new DatabaseError(`Failed to retrieve updated document from ${this.collectionName}`);
        }

        return updated;
      } catch (error) {
        if (error instanceof NotFoundError) {
          throw error;
        }
        logger.error(`Failed to update document in ${this.collectionName}`, {
          id: id.toString(),
          error: error instanceof Error ? error.message : 'Unknown error',
        });
        throw new DatabaseError(`Failed to update document in ${this.collectionName}`, {
          error: error instanceof Error ? error.message : 'Unknown error',
        });
      }
    });
  }

  /**
   * Deletes a document by ID
   */
  public async deleteById(id: string | ObjectId): Promise<boolean> {
    return trackPerformance(`${this.collectionName}.deleteById`, async () => {
      try {
        const collection = this.getCollection();
        const objectId = typeof id === 'string' ? new ObjectId(id) : id;

        const result: DeleteResult = await collection.deleteOne({ _id: objectId } as Filter<T>);

        if (result.deletedCount === 0) {
          throw new NotFoundError(`Document not found in ${this.collectionName}`, {
            id: objectId.toString(),
          });
        }

        logger.info(`Document deleted from ${this.collectionName}`, {
          id: objectId.toString(),
        });

        return true;
      } catch (error) {
        if (error instanceof NotFoundError) {
          throw error;
        }
        logger.error(`Failed to delete document from ${this.collectionName}`, {
          id: id.toString(),
          error: error instanceof Error ? error.message : 'Unknown error',
        });
        throw new DatabaseError(`Failed to delete document from ${this.collectionName}`, {
          error: error instanceof Error ? error.message : 'Unknown error',
        });
      }
    });
  }

  /**
   * Counts documents matching filter
   */
  public async count(filter: Filter<T> = {}): Promise<number> {
    return trackPerformance(`${this.collectionName}.count`, async () => {
      try {
        const collection = this.getCollection();
        return await collection.countDocuments(filter);
      } catch (error) {
        logger.error(`Failed to count documents in ${this.collectionName}`, {
          error: error instanceof Error ? error.message : 'Unknown error',
        });
        throw new DatabaseError(`Failed to count documents in ${this.collectionName}`, {
          error: error instanceof Error ? error.message : 'Unknown error',
        });
      }
    });
  }

  /**
   * Checks if a document exists
   */
  public async exists(filter: Filter<T>): Promise<boolean> {
    const count = await this.count(filter);
    return count > 0;
  }

  /**
   * Creates indexes for the collection
   */
  public abstract createIndexes(): Promise<void>;
}
