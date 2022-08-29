const express = require('express');
const router = express.Router();
import collectionController from '../controllers/collections.controler';

// get all NFT contracts, & basic data about them. MAYBE include the first token id as the preview?
router.get('/', collectionController.getAllCollectionData);

router.get('/all/:contract_address', collectionController.getAllTokensInCollection);

router.get('/volume/:contract_address', collectionController.getCollectionVolume);

export default router;