const express = require('express');
const router = express.Router();
import realestateController from '../controllers/realestate.controler';

// Routes for real estate
router.get('/:id', realestateController.getInformation);
router.get('/owned/:wallet', realestateController.getPlayersOwnedNFTs);

export default router;