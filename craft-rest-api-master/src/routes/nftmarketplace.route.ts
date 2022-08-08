const express = require('express');
const router = express.Router();
import nftmarketplaceController from '../controllers/nftmarketplace.controler';

// Routes for nftmarketplace (May move over other NFT things here too. If so rename to NFT)

router.get('/offerings/', nftmarketplaceController.getMarketplaceOfferings);

// TODO: we are moving to query via craft addr & token address instead. Moved to assets.route.ts
// router.get('/offerings/id/:offering_id', nftmarketplaceController.getSingleMarketplaceOffering);


// gets real estate only offerings
router.get('/offerings/realestate', nftmarketplaceController.getMarketplaceRealEstateOfferings);
router.get('/offerings/paintings', nftmarketplaceController.getMarketplacePaintingsOfferings);
router.get('/offerings/featured', nftmarketplaceController.getMarketplaceFeatured);

router.get('/offerings/contract/:parent_contract_address', nftmarketplaceController.getMarketplaceSpecificContractOffering);

// TODO: do we still need this? since we query ALL tokens a user owns anyways
router.get('/offerings/for_sale_by/:craft_address', nftmarketplaceController.getMarketplaceOfferingsFromGivenWallet);

export default router;