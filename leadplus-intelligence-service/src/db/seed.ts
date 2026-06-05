import { connectToDatabase, disconnectFromDatabase } from './connection.js';
import { accountRepository } from './repositories/account.repository.js';
import { contactRepository } from './repositories/contact.repository.js';
import { sourceRepository } from './repositories/source.repository.js';
import {
  AccountType,
  AccountStatus,
  CreateAccountPayload,
} from './schemas/account.schema.js';
import {
  ContactRole,
  ContactStatus,
  CreateContactPayload,
} from './schemas/contact.schema.js';
import {
  SourceType,
  SourceStatus,
  CreateSourcePayload,
} from './schemas/source.schema.js';
import logger from '../utils/logger.js';
import { setupGlobalErrorHandlers } from '../utils/errors.js';

/**
 * Sample accounts data
 */
const sampleAccounts: CreateAccountPayload[] = [
  {
    name: 'Acme Corporation',
    domain: 'acme.com',
    type: AccountType.ENTERPRISE,
    status: AccountStatus.ACTIVE,
    industry: 'Technology',
    size: {
      employees: 5000,
      revenue: 500000000,
    },
    location: {
      country: 'USA',
      state: 'California',
      city: 'San Francisco',
    },
    tags: ['tech', 'enterprise', 'saas'],
  },
  {
    name: 'TechStart Inc',
    domain: 'techstart.io',
    type: AccountType.STARTUP,
    status: AccountStatus.PROSPECT,
    industry: 'Technology',
    size: {
      employees: 50,
      revenue: 5000000,
    },
    location: {
      country: 'USA',
      state: 'New York',
      city: 'New York',
    },
    tags: ['tech', 'startup', 'ai'],
  },
  {
    name: 'Global Services Ltd',
    domain: 'globalservices.com',
    type: AccountType.SMB,
    status: AccountStatus.ACTIVE,
    industry: 'Consulting',
    size: {
      employees: 200,
      revenue: 20000000,
    },
    location: {
      country: 'UK',
      state: 'England',
      city: 'London',
    },
    tags: ['consulting', 'services'],
  },
];

/**
 * Sample sources data
 */
const sampleSources: CreateSourcePayload[] = [
  {
    name: 'LinkedIn Scraper',
    type: SourceType.WEB_SCRAPING,
    status: SourceStatus.ACTIVE,
    description: 'Scrapes company information from LinkedIn',
    config: {
      url: 'https://linkedin.com',
      frequency: '0 0 * * *', // Daily at midnight
      selectors: {
        companyName: '.company-name',
        industry: '.industry',
        size: '.company-size',
      },
      rateLimit: {
        maxRequests: 10,
        windowMs: 60000,
      },
    },
    tags: ['linkedin', 'scraping', 'companies'],
  },
  {
    name: 'CRM API Integration',
    type: SourceType.API,
    status: SourceStatus.ACTIVE,
    description: 'Syncs data from CRM API',
    config: {
      endpoint: 'https://api.crm.example.com/v1/accounts',
      method: 'GET',
      frequency: '0 */6 * * *', // Every 6 hours
    },
    tags: ['crm', 'api', 'integration'],
  },
  {
    name: 'Manual Upload Source',
    type: SourceType.UPLOAD,
    status: SourceStatus.PENDING,
    description: 'Manually uploaded contact lists',
    config: {},
    tags: ['manual', 'upload'],
  },
];

/**
 * Seeds the database with sample data
 */
async function seedDatabase() {
  try {
    logger.info('Starting database seeding...');

    // Connect to database
    await connectToDatabase();

    // Create indexes
    logger.info('Creating indexes...');
    await accountRepository.createIndexes();
    await contactRepository.createIndexes();
    await sourceRepository.createIndexes();
    logger.info('Indexes created successfully');

    // Clear existing data (optional - comment out if you want to keep existing data)
    logger.info('Clearing existing data...');
    const accountCollection = accountRepository['getCollection']();
    const contactCollection = contactRepository['getCollection']();
    const sourceCollection = sourceRepository['getCollection']();

    await accountCollection.deleteMany({});
    await contactCollection.deleteMany({});
    await sourceCollection.deleteMany({});
    logger.info('Existing data cleared');

    // Seed accounts
    logger.info('Seeding accounts...');
    const createdAccounts = [];
    for (const accountData of sampleAccounts) {
      const account = await accountRepository.createAccount(accountData);
      createdAccounts.push(account);
      logger.info(`Created account: ${account.name}`);
    }

    // Seed contacts for each account
    logger.info('Seeding contacts...');
    for (const account of createdAccounts) {
      const sampleContacts: CreateContactPayload[] = [
        {
          accountId: account._id!,
          firstName: 'John',
          lastName: 'Doe',
          email: `john.doe@${account.domain}`,
          phone: '+1-555-0100',
          title: 'CEO',
          role: ContactRole.DECISION_MAKER,
          status: ContactStatus.ACTIVE,
          department: 'Executive',
          linkedin: 'https://linkedin.com/in/johndoe',
          tags: ['executive', 'decision-maker'],
        },
        {
          accountId: account._id!,
          firstName: 'Jane',
          lastName: 'Smith',
          email: `jane.smith@${account.domain}`,
          phone: '+1-555-0101',
          title: 'CTO',
          role: ContactRole.INFLUENCER,
          status: ContactStatus.ACTIVE,
          department: 'Technology',
          linkedin: 'https://linkedin.com/in/janesmith',
          tags: ['technology', 'influencer'],
        },
      ];

      for (const contactData of sampleContacts) {
        const contact = await contactRepository.createContact(contactData);
        logger.info(`Created contact: ${contact.firstName} ${contact.lastName}`);
      }
    }

    // Seed sources
    logger.info('Seeding sources...');
    for (const sourceData of sampleSources) {
      const source = await sourceRepository.createSource(sourceData);
      logger.info(`Created source: ${source.name}`);
    }

    logger.info('Database seeding completed successfully!');
  } catch (error) {
    logger.error('Database seeding failed', {
      error: error instanceof Error ? error.message : 'Unknown error',
      stack: error instanceof Error ? error.stack : undefined,
    });
    process.exit(1);
  } finally {
    await disconnectFromDatabase();
  }
}

// Set up global error handlers
setupGlobalErrorHandlers();

// Run the seeding
seedDatabase();
