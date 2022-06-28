const express = require('express');
const router = express.Router();
import nftmarketplaceController from '../controllers/nftmarketplace.controler';

// Routes for nftmarketplace (May move over other NFT things here too. If so rename to NFT)
router.get('/offerings', nftmarketplaceController.getMarketplaceOfferings);
router.get('/get_contract_addresses', nftmarketplaceController.getContractAddresses);

export default router;