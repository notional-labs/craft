const express = require('express');
const router = express.Router();
import collectionController from '../controllers/collections.controler';

// When we get this, should we also scan marketplace for tokens beind sold? since these are token ids, just held in another contract.
router.get('/', collectionController.getAllCollectionData);

router.get('/all/:contract_address', collectionController.getAllTokensInCollection);

router.get('/stats/:contract_address', collectionController.getCollectionStats);

router.get('/recently_sold', collectionController.getRecentlySold);

export default router;