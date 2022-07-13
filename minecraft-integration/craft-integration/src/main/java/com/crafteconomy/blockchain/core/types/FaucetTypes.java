package com.crafteconomy.blockchain.core.types;

public enum FaucetTypes {
    SUCCESS,
    NO_WALLET,
    ENDPOINT_TIMEOUT, // failure, API may be down?. Do we want to ever return this or just have integration handle it
    FAILURE;
}
