const express = require('express');
const router = express.Router();
import nftController from '../controllers/nft.controler';

// Routes for standalone nft contracts (middleware)
router.get('/get_contract_addresses', nftController.getContractAddresses);

router.get('/owned/:addr721_address/:wallet', nftController.getPlayersOwnedNFTs);

router.get('/data/:addr721_address/:token_id', nftController.getDataFromTokenID);

router.get('/info/:addr721_address', nftController.getContractInformation);

router.get('/get_all_nfts/:craft_address/:chain?', nftController.getAllUserNFTs);
router.get('/get_all_nfts_including_offerings/:craft_address/:chain?', nftController.getAllUserNFTsIncludingOfferings);
router.post('/sync_all_to_db/:craft_address', nftController.syncOtherPlatformNFTs);

export default router;