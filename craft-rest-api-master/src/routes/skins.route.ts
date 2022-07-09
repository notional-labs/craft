const express = require('express');
const router = express.Router();
import skinControler from '../controllers/skins.controler';


router.get('/owned_skin_list/:wallet', skinControler.getPlayersOwnedNFTs);

// cached value of the token's id {value, signature, url}
router.get('/value/:token_id', skinControler.getSkinValuesFromNFT);


export default router;