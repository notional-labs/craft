const express = require('express');
const router = express.Router();
import nftmarketplaceController from '../controllers/nftmarketplace.controler';

// Routes for nftmarketplace (May move over other NFT things here too. If so rename to NFT)

router.get('/offerings', nftmarketplaceController.getMarketplaceOfferings);
// gets real estate only offerings
router.get('/offerings/skins', nftmarketplaceController.getMarketplaceSkinsOfferings);
router.get('/offerings/realestate', nftmarketplaceController.getMarketplaceRealEstateOfferings);
router.get('/offerings/contract/:parent_contract_address', nftmarketplaceController.getMarketplaceSpecificContractOffering);

export default router;