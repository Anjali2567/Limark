import { MongoClient, Db, MongoClientOptions } from 'mongodb';
import config from '../config/index.js';
import logger, { PerformanceTracker } from '../utils/logger.js';
import { DatabaseError } from '../utils/errors.js';

/**
 * Database connection manager
 */
class DatabaseConnection {
  private client: MongoClient | null = null;
  private db: Db | null = null;
  private isConnecting: boolean = false;
  private connectionPromise: Promise<void> | null = null;

  /**
   * Gets the MongoDB client options
   */
  private getClientOptions(): MongoClientOptions {
    return {
      maxPoolSize: config.database.maxPoolSize,
      minPoolSize: config.database.minPoolSize,
      connectTimeoutMS: config.database.connectTimeoutMs,
      socketTimeoutMS: config.database.socketTimeoutMs,
      retryWrites: true,
      retryReads: true,
      compressors: ['zlib'],
    };
  }

  /**
   * Connects to the database
   */
  public async connect(): Promise<void> {
    // If already connected, return immediately
    if (this.client && this.db) {
      logger.debug('Database already connected');
      return;
    }

    // If currently connecting, wait for that connection to complete
    if (this.isConnecting && this.connectionPromise) {
      logger.debug('Connection in progress, waiting...');
      return this.connectionPromise;
    }

    // Start new connection
    this.isConnecting = true;
    this.connectionPromise = this._connect();

    try {
      await this.connectionPromise;
    } finally {
      this.isConnecting = false;
      this.connectionPromise = null;
    }
  }

  /**
   * Internal connection logic
   */
  private async _connect(): Promise<void> {
    const tracker = new PerformanceTracker('database_connection');

    try {
      logger.info('Connecting to MongoDB...', {
        database: config.database.name,
        maxPoolSize: config.database.maxPoolSize,
      });

      const options = this.getClientOptions();
      this.client = new MongoClient(config.database.url, options);

      await this.client.connect();
      this.db = this.client.db(config.database.name);

      // Test the connection
      await this.db.admin().ping();

      tracker.end(true, {
        database: config.database.name,
      });

      logger.info('Successfully connected to MongoDB', {
        database: config.database.name,
      });

      // Set up connection event handlers
      this.setupEventHandlers();
    } catch (error) {
      tracker.end(false, {
        error: error instanceof Error ? error.message : 'Unknown error',
      });

      logger.error('Failed to connect to MongoDB', {
        error: error instanceof Error ? error.message : 'Unknown error',
        database: config.database.name,
      });

      // Clean up on failure
      if (this.client) {
        await this.client.close();
        this.client = null;
      }
      this.db = null;

      throw new DatabaseError('Failed to connect to database', {
        error: error instanceof Error ? error.message : 'Unknown error',
      });
    }
  }

  /**
   * Sets up event handlers for the MongoDB client
   */
  private setupEventHandlers(): void {
    if (!this.client) return;

    this.client.on('connectionPoolCreated', () => {
      logger.debug('Connection pool created');
    });

    this.client.on('connectionPoolClosed', () => {
      logger.debug('Connection pool closed');
    });

    this.client.on('connectionCreated', () => {
      logger.debug('New connection created');
    });

    this.client.on('connectionClosed', () => {
      logger.debug('Connection closed');
    });

    this.client.on('error', (error) => {
      logger.error('MongoDB client error', {
        error: error.message,
        stack: error.stack,
      });
    });
  }

  /**
   * Gets the database instance
   */
  public getDb(): Db {
    if (!this.db) {
      throw new DatabaseError('Database not connected. Call connect() first.');
    }
    return this.db;
  }

  /**
   * Gets the MongoDB client
   */
  public getClient(): MongoClient {
    if (!this.client) {
      throw new DatabaseError('Database client not initialized. Call connect() first.');
    }
    return this.client;
  }

  /**
   * Checks if connected to the database
   */
  public isConnected(): boolean {
    return this.client !== null && this.db !== null;
  }

  /**
   * Disconnects from the database
   */
  public async disconnect(): Promise<void> {
    if (!this.client) {
      logger.debug('Database already disconnected');
      return;
    }

    try {
      logger.info('Disconnecting from MongoDB...');
      await this.client.close();
      this.client = null;
      this.db = null;
      logger.info('Successfully disconnected from MongoDB');
    } catch (error) {
      logger.error('Error disconnecting from MongoDB', {
        error: error instanceof Error ? error.message : 'Unknown error',
      });
      throw new DatabaseError('Failed to disconnect from database', {
        error: error instanceof Error ? error.message : 'Unknown error',
      });
    }
  }

  /**
   * Executes a database operation with connection guarantee
   */
  public async withConnection<T>(
    operation: (db: Db) => Promise<T>
  ): Promise<T> {
    await this.connect();
    const db = this.getDb();
    return operation(db);
  }

  /**
   * Gets database statistics
   */
  public async getStats(): Promise<unknown> {
    return this.withConnection(async (db) => {
      const stats = await db.stats();
      return {
        database: stats.db,
        collections: stats.collections,
        dataSize: stats.dataSize,
        storageSize: stats.storageSize,
        indexes: stats.indexes,
        indexSize: stats.indexSize,
      };
    });
  }
}

/**
 * Singleton instance of the database connection
 */
export const dbConnection = new DatabaseConnection();

/**
 * Helper function to get the database instance
 */
export const getDb = (): Db => dbConnection.getDb();

/**
 * Helper function to connect to the database
 */
export const connectToDatabase = async (): Promise<void> => {
  await dbConnection.connect();
};

/**
 * Helper function to disconnect from the database
 */
export const disconnectFromDatabase = async (): Promise<void> => {
  await dbConnection.disconnect();
};
