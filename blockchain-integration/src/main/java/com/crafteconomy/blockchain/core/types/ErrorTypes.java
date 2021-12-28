package com.crafteconomy.blockchain.core.types;

public enum ErrorTypes {
    NONE(0),
    NO_WALLET(-1),
    NOT_ENOUGH_TO_SEND(-2),
    NODE_DOWN(-5),
    NETWORK_ERROR(-6);

    public int error_code;

    ErrorTypes(int error_code) {
        this.error_code = error_code;
    }
}
