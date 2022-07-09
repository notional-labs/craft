const express = require('express');
const router = express.Router();
import transactionsController from '../controllers/transactions.controller';

router.get('/:uuid', transactionsController.getTransactionData);
router.get('/all/:wallet', transactionsController.getAllTransactionsData);
router.post('/sign/:uuid/:tenderminthash', transactionsController.signTransaction);
router.delete('/:uuid', transactionsController.removeTransaction);

export default router;
