import { Request, Response, NextFunction } from 'express';
import { accountRepository } from '../../db/repositories/account.repository.js';
import { CreateAccountPayload, UpdateAccountPayload } from '../../db/schemas/account.schema.js';
import { successResponse, paginatedResponse, normalizePaginationParams } from '../../utils/api-response.js';
import { NotFoundError, ValidationError } from '../../utils/errors.js';
import logger from '../../utils/logger.js';

/**
 * Account Controller
 */
export class AccountController {
  /**
   * Get all accounts with pagination
   */
  async getAll(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const { page, limit, skip } = normalizePaginationParams({
        page: req.query.page ? parseInt(req.query.page as string) : undefined,
        limit: req.query.limit ? parseInt(req.query.limit as string) : undefined,
      });

      const accounts = await accountRepository.find({}, { skip, limit });
      const total = await accountRepository.count();

      res.json(paginatedResponse(accounts, page, limit, total));
    } catch (error) {
      next(error);
    }
  }

  /**
   * Get account by ID
   */
  async getById(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const { id } = req.params;
      const account = await accountRepository.findById(id);

      if (!account) {
        throw new NotFoundError(`Account with ID ${id} not found`);
      }

      res.json(successResponse(account));
    } catch (error) {
      next(error);
    }
  }

  /**
   * Create new account
   */
  async create(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const payload: CreateAccountPayload = req.body;

      if (!payload.name || !payload.type || !payload.status) {
        throw new ValidationError('Missing required fields: name, type, status');
      }

      const account = await accountRepository.createAccount(payload);
      logger.info('Account created', { id: account._id?.toString() });

      res.status(201).json(successResponse(account));
    } catch (error) {
      next(error);
    }
  }

  /**
   * Update account
   */
  async update(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const { id } = req.params;
      const payload: UpdateAccountPayload = req.body;

      const account = await accountRepository.updateAccount(id, payload);
      logger.info('Account updated', { id: account._id?.toString() });

      res.json(successResponse(account));
    } catch (error) {
      next(error);
    }
  }

  /**
   * Delete account
   */
  async delete(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const { id } = req.params;

      await accountRepository.deleteById(id);
      logger.info('Account deleted', { id });

      res.json(successResponse({ message: 'Account deleted successfully' }));
    } catch (error) {
      next(error);
    }
  }

  /**
   * Search accounts by text
   */
  async search(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const { q } = req.query;

      if (!q) {
        throw new ValidationError('Search query parameter "q" is required');
      }

      const accounts = await accountRepository.searchAccounts(q as string);
      res.json(successResponse(accounts));
    } catch (error) {
      next(error);
    }
  }

  /**
   * Get accounts by type
   */
  async getByType(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const { type } = req.params;
      const accounts = await accountRepository.findByType(type);

      res.json(successResponse(accounts));
    } catch (error) {
      next(error);
    }
  }

  /**
   * Get accounts by status
   */
  async getByStatus(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const { status } = req.params;
      const accounts = await accountRepository.findByStatus(status);

      res.json(successResponse(accounts));
    } catch (error) {
      next(error);
    }
  }
}

export default new AccountController();
