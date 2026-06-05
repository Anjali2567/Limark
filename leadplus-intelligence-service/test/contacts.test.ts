import { describe, it, expect, beforeAll, afterAll } from 'vitest';
import request from 'supertest';
import { createApp } from '../src/api/server';
import { accountRepository } from '../src/db/repositories/account.repository';
import { contactRepository } from '../src/db/repositories/contact.repository';
import type { Express } from 'express';

describe('Contacts API', () => {
  let app: Express;
  let testAccountId: string;
  let createdContactId: string;
  const API_KEY = process.env.EXTERNAL_API_KEY || 'test-api-key-12345';

  beforeAll(async () => {
    app = createApp();

    // Create test account first
    const account = await accountRepository.create({
      name: 'Test Company for Contacts',
      domain: 'test-contacts-' + Date.now() + '.com',
      type: 'ENTERPRISE',
      status: 'ACTIVE',
    });
    testAccountId = account._id!.toString();
  });

  afterAll(async () => {
    // Cleanup
    if (createdContactId) {
      await contactRepository.deleteById(createdContactId);
    }
    if (testAccountId) {
      await accountRepository.deleteById(testAccountId);
    }
  });

  describe('POST /api/contacts', () => {
    it('should create a new contact', async () => {
      const newContact = {
        accountId: testAccountId,
        firstName: 'John',
        lastName: 'Doe',
        email: 'john.doe.' + Date.now() + '@test.com',
        title: 'VP of Supply Chain',
        status: 'ACTIVE',
      };

      const response = await request(app)
        .post('/api/contacts')
        .set('X-API-Key', API_KEY)
        .send(newContact)
        .expect(201);

      expect(response.body.success).toBe(true);
      expect(response.body.data).toHaveProperty('_id');
      expect(response.body.data.firstName).toBe(newContact.firstName);
      expect(response.body.data.email).toBe(newContact.email);

      createdContactId = response.body.data._id;
    });
  });

  describe('GET /api/contacts/:id', () => {
    it('should get contact by id', async () => {
      const response = await request(app)
        .get(`/api/contacts/${createdContactId}`)
        .set('X-API-Key', API_KEY)
        .expect(200);

      expect(response.body.success).toBe(true);
      expect(response.body.data._id).toBe(createdContactId);
      expect(response.body.data.firstName).toBe('John');
      expect(response.body.data.lastName).toBe('Doe');
    });
  });

  describe('GET /api/contacts', () => {
    it('should list contacts with pagination', async () => {
      const response = await request(app)
        .get('/api/contacts')
        .set('X-API-Key', API_KEY)
        .query({ page: 1, limit: 10 })
        .expect(200);

      expect(response.body.success).toBe(true);
      expect(response.body.data).toBeInstanceOf(Array);
      expect(response.body.metadata.pagination).toHaveProperty('page');
      expect(response.body.metadata.pagination).toHaveProperty('total');
    });

    it('should filter contacts by accountId', async () => {
      const response = await request(app)
        .get('/api/contacts')
        .set('X-API-Key', API_KEY)
        .query({ accountId: testAccountId })
        .expect(200);

      expect(response.body.success).toBe(true);
      expect(response.body.data).toBeInstanceOf(Array);
      expect(response.body.data.length).toBeGreaterThan(0);
      expect(response.body.data[0].accountId).toBe(testAccountId);
    });
  });
});
