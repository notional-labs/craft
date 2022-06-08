const express = require('express');
const router = express.Router();
import statisticsController from '../controllers/statistics.controller';

// Routes for staistics
router.get('/players', statisticsController.getPlayers);
router.get('/players/active', statisticsController.getPlayersActive);
router.get('/players/new', statisticsController.getPlayersNew);
router.get('/playtime', statisticsController.getPlaytime);
router.get('/:id', statisticsController.getLatestPlayer);

export default router;