package com.crafteconomy.blockchain.core.types;

public enum FaucetTypes {
    // https://github.com/cosmos/cosmos-sdk/blob/main/types/errors/errors.go
    SUCCESS,
    NO_WALLET,
    ENDPOINT_TIMEOUT, // failure, API may be down?. Do we want to ever return this or just have integration handle it
    NO_RESPONSE,
    API_DOWN,
    NOT_ENOUGH_FUNDS_IN_SERVER_WALLET, // if the servers wallet runs out of craft, this fires
    FAILURE;
}
