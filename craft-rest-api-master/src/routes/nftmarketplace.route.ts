const express = require('express');
const router = express.Router();
import nftmarketplaceController from '../controllers/nftmarketplace.controler';

// Routes for nftmarketplace (May move over other NFT things here too. If so rename to NFT)

router.get('/offerings/', nftmarketplaceController.getMarketplaceOfferings);
router.get('/offerings/id/:id', nftmarketplaceController.getSingleMarketplaceOffering);
// gets real estate only offerings
router.get('/offerings/realestate', nftmarketplaceController.getMarketplaceRealEstateOfferings);
router.get('/offerings/paintings', nftmarketplaceController.getMarketplacePaintingsOfferings);
router.get('/offerings/featured', nftmarketplaceController.getMarketplaceFeatured);
router.get('/offerings/contract/:parent_contract_address', nftmarketplaceController.getMarketplaceSpecificContractOffering);

export default router;