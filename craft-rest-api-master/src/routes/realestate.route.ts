const express = require('express');
const router = express.Router();
import realestateController from '../controllers/realestate.controler';

// Routes for real estate

router.get('/:id', realestateController.getInformation);

// token_id = the token id of the NFT in the ADDR721
router.get('/property_data_from_nft/:token_id', realestateController.getPropertyByTokenFromNFT); 

router.get('/owned/:wallet', realestateController.getPlayersOwnedNFTs);

// src/main/java/com/crafteconomy/realestate/property/PropertyState.java
router.get('/get_state/:state', realestateController.getPropertyState); // OWNED, FOR_SALE, UNAVAILABLE, FOR_RENT, RENTED. Found in getInformation as well



export default router;