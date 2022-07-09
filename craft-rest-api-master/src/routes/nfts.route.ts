const express = require('express');
const router = express.Router();
import nftController from '../controllers/nft.controler';

// Routes for standalone nft contracts (middleware)
router.get('/get_contract_addresses', nftController.getContractAddresses);

router.get('/owned/:addr721_address/:wallet', nftController.getPlayersOwnedNFTs);

router.get('/data/:addr721_address/:token_id', nftController.getDataFromTokenID);


export default router;