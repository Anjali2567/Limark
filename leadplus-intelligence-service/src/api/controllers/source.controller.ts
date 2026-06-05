import { Request, Response, NextFunction } from 'express';
import { sourceRepository } from '../../db/repositories/source.repository.js';
import { CreateSourcePayload, UpdateSourcePayload } from '../../db/schemas/source.schema.js';
import { successResponse, paginatedResponse, normalizePaginationParams } from '../../utils/api-response.js';
import { NotFoundError, ValidationError } from '../../utils/errors.js';
import logger from '../../utils/logger.js';

/**
 * Source Controller
 */
export class SourceController {
  /**
   * Get all sources with pagination
   */
  async getAll(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const { page, limit, skip } = normalizePaginationParams({
        page: req.query.page ? parseInt(req.query.page as string) : undefined,
        limit: req.query.limit ? parseInt(req.query.limit as string) : undefined,
      });

      const sources = await sourceRepository.find({}, { skip, limit });
      const total = await sourceRepository.count();

      res.json(paginatedResponse(sources, page, limit, total));
    } catch (error) {
      next(error);
    }
  }

  /**
   * Get source by ID
   */
  async getById(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const { id } = req.params;
      const source = await sourceRepository.findById(id);

      if (!source) {
        throw new NotFoundError(`Source with ID ${id} not found`);
      }

      res.json(successResponse(source));
    } catch (error) {
      next(error);
    }
  }

  /**
   * Create new source
   */
  async create(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const payload: CreateSourcePayload = req.body;

      if (!payload.name || !payload.type || !payload.status) {
        throw new ValidationError('Missing required fields: name, type, status');
      }

      const source = await sourceRepository.createSource(payload);
      logger.info('Source created', { id: source._id?.toString() });

      res.status(201).json(successResponse(source));
    } catch (error) {
      next(error);
    }
  }

  /**
   * Update source
   */
  async update(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const { id } = req.params;
      const payload: UpdateSourcePayload = req.body;

      const source = await sourceRepository.updateSource(id, payload);
      logger.info('Source updated', { id: source._id?.toString() });

      res.json(successResponse(source));
    } catch (error) {
      next(error);
    }
  }

  /**
   * Delete source
   */
  async delete(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const { id } = req.params;

      await sourceRepository.deleteById(id);
      logger.info('Source deleted', { id });

      res.json(successResponse({ message: 'Source deleted successfully' }));
    } catch (error) {
      next(error);
    }
  }

  /**
   * Search sources by text
   */
  async search(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const { q } = req.query;

      if (!q) {
        throw new ValidationError('Search query parameter "q" is required');
      }

      const sources = await sourceRepository.searchSources(q as string);
      res.json(successResponse(sources));
    } catch (error) {
      next(error);
    }
  }

  /**
   * Get sources by type
   */
  async getByType(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const { type } = req.params;
      const sources = await sourceRepository.findByType(type);

      res.json(successResponse(sources));
    } catch (error) {
      next(error);
    }
  }

  /**
   * Update source run stats
   */
  async updateRunStats(req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const { id } = req.params;
      const { success, recordCount, error } = req.body;

      if (typeof success !== 'boolean') {
        throw new ValidationError('Field "success" must be a boolean');
      }

      const source = await sourceRepository.updateRunStats(
        id,
        success,
        recordCount || 0,
        error
      );

      logger.info('Source run stats updated', { id: source._id?.toString() });

      res.json(successResponse(source));
    } catch (error) {
      next(error);
    }
  }
}

export default new SourceController();
