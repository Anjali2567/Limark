import { Router } from 'express';
import type { Router as ExpressRouter } from 'express';
import contactController from '../controllers/contact.controller.js';
import { authenticateApiKey } from '../middleware/auth.js';

const router: ExpressRouter = Router();

// Apply authentication middleware to all routes
router.use(authenticateApiKey);

// GET /api/contacts - Get all contacts (with pagination)
router.get('/', contactController.getAll.bind(contactController));

// GET /api/contacts/search?q=query - Search contacts
router.get('/search', contactController.search.bind(contactController));

// GET /api/contacts/account/:accountId - Get contacts by account ID
router.get('/account/:accountId', contactController.getByAccountId.bind(contactController));

// GET /api/contacts/status/:status - Get contacts by status
router.get('/status/:status', contactController.getByStatus.bind(contactController));

// GET /api/contacts/:id - Get contact by ID
router.get('/:id', contactController.getById.bind(contactController));

// POST /api/contacts - Create new contact
router.post('/', contactController.create.bind(contactController));

// PUT /api/contacts/:id - Update contact
router.put('/:id', contactController.update.bind(contactController));

// DELETE /api/contacts/:id - Delete contact
router.delete('/:id', contactController.delete.bind(contactController));

export default router;
