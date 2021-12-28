package com.crafteconomy.blockchain.core.types;

public enum RequestTypes {
    BALANCE("balance"), 
    SUPPLY("amount"),
    FAUCET("");

    public String json_key;

    RequestTypes(String json_key) {
        this.json_key = json_key;
    }
}
