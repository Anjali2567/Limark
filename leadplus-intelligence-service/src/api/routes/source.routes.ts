import { Router } from 'express';
import type { Router as ExpressRouter } from 'express';
import sourceController from '../controllers/source.controller.js';
import { authenticateApiKey } from '../middleware/auth.js';

const router: ExpressRouter = Router();

// Apply authentication middleware to all routes
router.use(authenticateApiKey);

// GET /api/sources - Get all sources (with pagination)
router.get('/', sourceController.getAll.bind(sourceController));

// GET /api/sources/search?q=query - Search sources
router.get('/search', sourceController.search.bind(sourceController));

// GET /api/sources/type/:type - Get sources by type
router.get('/type/:type', sourceController.getByType.bind(sourceController));

// GET /api/sources/:id - Get source by ID
router.get('/:id', sourceController.getById.bind(sourceController));

// POST /api/sources - Create new source
router.post('/', sourceController.create.bind(sourceController));

// PUT /api/sources/:id - Update source
router.put('/:id', sourceController.update.bind(sourceController));

// PUT /api/sources/:id/run-stats - Update source run statistics
router.put('/:id/run-stats', sourceController.updateRunStats.bind(sourceController));

// DELETE /api/sources/:id - Delete source
router.delete('/:id', sourceController.delete.bind(sourceController));

export default router;
