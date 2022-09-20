package com.crafteconomy.blockchain.core.types;

// Used when making a transaction, used ONLY for the webapp for sorting.
public enum TransactionType {
    DEFAULT, // base transaction type unless otherwise set
    TRADE,
    COMPANY,
    LAND_CLAIM,
    REAL_ESTATE,

    ESCROW_DEPOSIT, // tax free transactions in BlockchainRequests.java
    ESCROW_WITHDRAW,
    LIQUIDITY_POOL, // swaps
    AUTHENTICATION;
}
