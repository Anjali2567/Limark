import { describe, it, expect, beforeAll, afterAll } from 'vitest';
import request from 'supertest';
import { createApp } from '../src/api/server';
import { accountRepository } from '../src/db/repositories/account.repository';
import type { Express } from 'express';

describe('Accounts API', () => {
  let app: Express;
  let createdAccountId: string;
  const API_KEY = process.env.EXTERNAL_API_KEY || 'test-api-key-12345';

  beforeAll(() => {
    app = createApp();
  });

  afterAll(async () => {
    // Cleanup: delete test accounts
    if (createdAccountId) {
      await accountRepository.deleteById(createdAccountId);
    }
  });

  describe('POST /api/accounts', () => {
    it('should create a new account', async () => {
      const newAccount = {
        name: 'Test Company',
        domain: 'test-company-' + Date.now() + '.com',
        type: 'ENTERPRISE',
        status: 'ACTIVE',
      };

      const response = await request(app)
        .post('/api/accounts')
        .set('X-API-Key', API_KEY)
        .send(newAccount)
        .expect(201);

      expect(response.body.success).toBe(true);
      expect(response.body.data).toHaveProperty('_id');
      expect(response.body.data.name).toBe(newAccount.name);
      expect(response.body.data.domain).toBe(newAccount.domain);

      createdAccountId = response.body.data._id;
    });
  });

  describe('GET /api/accounts/:id', () => {
    it('should get account by id', async () => {
      const response = await request(app)
        .get(`/api/accounts/${createdAccountId}`)
        .set('X-API-Key', API_KEY)
        .expect(200);

      expect(response.body.success).toBe(true);
      expect(response.body.data._id).toBe(createdAccountId);
      expect(response.body.data.name).toBe('Test Company');
    });

    it('should return 404 for non-existent account', async () => {
      const response = await request(app)
        .get('/api/accounts/507f1f77bcf86cd799439011')
        .set('X-API-Key', API_KEY)
        .expect(404);

      expect(response.body.success).toBe(false);
    });
  });

  describe('GET /api/accounts', () => {
    it('should list accounts with pagination', async () => {
      const response = await request(app)
        .get('/api/accounts')
        .set('X-API-Key', API_KEY)
        .query({ page: 1, limit: 10 })
        .expect(200);

      expect(response.body.success).toBe(true);
      expect(response.body.data).toBeInstanceOf(Array);
      expect(response.body.metadata.pagination).toHaveProperty('page', 1);
      expect(response.body.metadata.pagination).toHaveProperty('limit', 10);
      expect(response.body.metadata.pagination).toHaveProperty('total');
    });
  });
});
