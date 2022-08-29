const express = require('express');
const router = express.Router();
import connectionsController from '../controllers/connections.controller';

// Routes for connections
router.post('/link', connectionsController.createConnectionLink);

export default router;
