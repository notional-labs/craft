package com.crafteconomy.blockchain.core.types;

/**
 * Used to store blockchainapi request string for request
 */
public enum RequestTypes {
    BALANCE("balance"), 
    SUPPLY("amount"),
    CRAFT_PRICE("craft_usd"),
    ESCROW(""), // escrow balance in game
    ACCOUNT(""); // useful for account sequence when making transactions
    // FAUCET("");

    public String json_key;

    RequestTypes(String json_key) {
        this.json_key = json_key;
    }
}
