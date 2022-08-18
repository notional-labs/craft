const express = require('express');
const router = express.Router();
import transactionsController from '../controllers/transactions.controller';

router.get('/:uuid', transactionsController.getTransactionData);
router.get('/all/:wallet', transactionsController.getAllTransactionsData);
router.post('/sign/:uuid/:tenderminthash', transactionsController.signTransaction);
router.delete('/delete/:uuid', transactionsController.removeTransaction);

// router.get('/confirm/:to_address/:ucraft_amt/:description/:tendermint_hash', transactionsController.confirmTransactionDetailsMatch); // done in game

export default router;
