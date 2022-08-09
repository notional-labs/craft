const express = require('express');
const router = express.Router();
import assetsController from '../controllers/assets.controler';

// TODO: Can probably move -> offerings / NFTs / marketplace or something.
// For now it will stay in here and include collections & getting all assets.

router.get('/get/:contract_address/:token_id', assetsController.getAllTokenData);



export default router;