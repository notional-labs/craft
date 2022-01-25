package com.crafteconomy.blockchain.core.types;

import java.util.HashMap;
import java.util.Map;

public enum ErrorTypes {
    NO_ERROR(0),
    NO_WALLET(-1),
    NOT_ENOUGH_TO_SEND(-2),
    NODE_DOWN(-5), // no BlockchainAPI (1317)
    NETWORK_ERROR(-6),
    NO_TOKENS_FOR_WALLET(-7), // When wallet address is not on chain 
    JSON_PARSE_TRANSACTION(-19);

    public int code;

    ErrorTypes(int code) {
        this.code = code;
    }


    private static final Map<Integer, ErrorTypes> map = new HashMap<>(values().length, 1);
    static {
        for (ErrorTypes error : values()) map.put(error.code, error);
    }
    public static ErrorTypes of(int code) {
        ErrorTypes result = map.get(code);
        if (result == null) {
          throw new IllegalArgumentException("Invalid category name: " + code);
        }
        return result;
      }

}
