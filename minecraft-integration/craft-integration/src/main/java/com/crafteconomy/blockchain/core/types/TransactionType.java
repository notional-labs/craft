package com.crafteconomy.blockchain.core.types;

// Used when making a transaction, used ONLY for the webapp for sorting.
public enum TransactionType {
    DEFAULT, // base transaction type unless otherwise set
    TRADE,
    LIQUIDITY_POOL,
    COMPANY,
    LAND_CLAIM,
    AUTHENTICATION,
    REAL_ESTATE;
}
