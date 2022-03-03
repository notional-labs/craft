package com.crafteconomy.blockchain.escrow;

public enum EscrowErrors {

    NOT_ENOUGH_ESCROW_BALANCE("You do not have enough escrow balance to redeem this amount."),
    NOT_ENOUGH_CRAFT_FUNDS("You do not have enough CRAFT balance to redeem this amount."),
    NO_WALLET("You do not have a wallet."),
    FAUCET_DEPOSIT_ERROR("The Faucet deposit failed."),
    SUCCESS("success");

    private final String message;

    private EscrowErrors(String message) {
       this.message = message;
    }
 
    public String getMessage() {
       return message;
    }
}
