const express = require('express');
const router = express.Router();
import daoControler from '../controllers/dao.controler';

// Routes for standalone nft contracts (middleware)
router.get('/get_all', daoControler.getAll);
router.get('/supply', daoControler.getSupply)

router.get('/usd_value', daoControler.getUSDValueOfDAO)
router.get('/assets', daoControler.getTotalAssets)

router.get('/exp_price', daoControler.getEXPPrice)
router.get('/escrow_account_info', daoControler.getServerEscrowWallet)

router.post('/make_payment', daoControler.makePaymentToPlayer)
export default router;