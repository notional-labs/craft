const express = require('express');
const router = express.Router();
import realestateController from '../controllers/realestate.controler';

// Routes for real estate

// Returns data for a property from the MongoDB instance
router.get('/:id', realestateController.getInformation);

// Returns a dict of the data stored in the CosmWasm contract (decoded base64)
router.get('/nft_metadata/:token_id', realestateController.getPropertyByTokenFromNFT); 

// src/main/java/com/crafteconomy/realestate/property/PropertyState.java
// OWNED, FOR_SALE, UNAVAILABLE, FOR_RENT, RENTED. Found in getInformation as well
router.get('/get_states/:state', realestateController.getPropertyState);

// Gets a list of dicts which also returns the NFT metadata for a user
router.get('/owned/:wallet', realestateController.getPlayersOwnedNFTs);

// Gets all owned property UUIDs -> [ "dbcd78cb-326e-4842-982b-9252f9ca25a7", "84561fc8-9450-4a70-9fa2-7d43227ee98f", ...]
router.get('/owned_uuids/:wallet', realestateController.getPlayersOwnedUUIDsList); 


export default router;