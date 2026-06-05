import { Request, Response, NextFunction } from 'express';
import { contactRepository } from '../../db/repositories/contact.repository.js';
import { CreateContactPayload, UpdateContactPayload } from '../../db/schemas/contact.schema.js';
import { successResponse, paginatedResponse, normalizePaginationParams } from '../../utils/api-response.js';
import { NotFoundError, ValidationError } from '../../utils/errors.js';
import logger from '../../utils/logger.js';

/**
 * Contact Controller
 */
export class ContactController {
  /**
   * Get all contacts with pagination
   */
  async getAll(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const { page, limit, skip } = normalizePaginationParams({
        page: req.query.page ? parseInt(req.query.page as string) : undefined,
        limit: req.query.limit ? parseInt(req.query.limit as string) : undefined,
      });

      const contacts = await contactRepository.find({}, { skip, limit });
      const total = await contactRepository.count();

      res.json(paginatedResponse(contacts, page, limit, total));
    } catch (error) {
      next(error);
    }
  }

  /**
   * Get contact by ID
   */
  async getById(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const { id } = req.params;
      const contact = await contactRepository.findById(id);

      if (!contact) {
        throw new NotFoundError(`Contact with ID ${id} not found`);
      }

      res.json(successResponse(contact));
    } catch (error) {
      next(error);
    }
  }

  /**
   * Create new contact
   */
  async create(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const payload: CreateContactPayload = req.body;

      if (!payload.accountId || !payload.firstName || !payload.lastName || !payload.email || !payload.status) {
        throw new ValidationError('Missing required fields: accountId, firstName, lastName, email, status');
      }

      const contact = await contactRepository.createContact(payload);
      logger.info('Contact created', { id: contact._id?.toString() });

      res.status(201).json(successResponse(contact));
    } catch (error) {
      next(error);
    }
  }

  /**
   * Update contact
   */
  async update(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const { id } = req.params;
      const payload: UpdateContactPayload = req.body;

      const contact = await contactRepository.updateContact(id, payload);
      logger.info('Contact updated', { id: contact._id?.toString() });

      res.json(successResponse(contact));
    } catch (error) {
      next(error);
    }
  }

  /**
   * Delete contact
   */
  async delete(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const { id } = req.params;

      await contactRepository.deleteById(id);
      logger.info('Contact deleted', { id });

      res.json(successResponse({ message: 'Contact deleted successfully' }));
    } catch (error) {
      next(error);
    }
  }

  /**
   * Get contacts by account ID
   */
  async getByAccountId(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const { accountId } = req.params;
      const contacts = await contactRepository.findByAccountId(accountId);

      res.json(successResponse(contacts));
    } catch (error) {
      next(error);
    }
  }

  /**
   * Search contacts by text
   */
  async search(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const { q } = req.query;

      if (!q) {
        throw new ValidationError('Search query parameter "q" is required');
      }

      const contacts = await contactRepository.searchContacts(q as string);
      res.json(successResponse(contacts));
    } catch (error) {
      next(error);
    }
  }

  /**
   * Get contacts by status
   */
  async getByStatus(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const { status } = req.params;
      const contacts = await contactRepository.findByStatus(status);

      res.json(successResponse(contacts));
    } catch (error) {
      next(error);
    }
  }
}

export default new ContactController();
