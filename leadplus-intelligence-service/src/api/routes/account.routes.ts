import { Router } from 'express';
import type { Router as ExpressRouter } from 'express';
import accountController from '../controllers/account.controller.js';
import { authenticateApiKey } from '../middleware/auth.js';

const router: ExpressRouter = Router();

// Apply authentication middleware to all routes
router.use(authenticateApiKey);

// GET /api/accounts - Get all accounts (with pagination)
router.get('/', accountController.getAll.bind(accountController));

// GET /api/accounts/search?q=query - Search accounts
router.get('/search', accountController.search.bind(accountController));

// GET /api/accounts/type/:type - Get accounts by type
router.get('/type/:type', accountController.getByType.bind(accountController));

// GET /api/accounts/status/:status - Get accounts by status
router.get('/status/:status', accountController.getByStatus.bind(accountController));

// GET /api/accounts/:id - Get account by ID
router.get('/:id', accountController.getById.bind(accountController));

// POST /api/accounts - Create new account
router.post('/', accountController.create.bind(accountController));

// PUT /api/accounts/:id - Update account
router.put('/:id', accountController.update.bind(accountController));

// DELETE /api/accounts/:id - Delete account
router.delete('/:id', accountController.delete.bind(accountController));

export default router;
