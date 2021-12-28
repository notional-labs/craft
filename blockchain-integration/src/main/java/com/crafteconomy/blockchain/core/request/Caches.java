package com.crafteconomy.blockchain.core.request;

import java.util.concurrent.TimeUnit;

import com.crafteconomy.blockchain.core.types.RequestTypes;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class Caches {

    /*
     * Holds account balances & total supply queries for X seconds to reduce load 
     */

    // ADDRESS, BALANCE
   private static final Cache<String, Long> BalanceCache = CacheBuilder.newBuilder()
       .maximumSize(1000)
       .expireAfterWrite(10, TimeUnit.SECONDS)
       .build();

   // DENOMINATION, SUPPLY
   private static final Cache<String, Long> TokenSupply = CacheBuilder.newBuilder()
       .maximumSize(10) // Change this if NFT's become individual tokens
       .expireAfterWrite(60, TimeUnit.SECONDS)
       .build();


   public static Object getIfPresent(RequestTypes type, String key) {

       Object value = null;
       switch (type) {
           case BALANCE    ->  value = BalanceCache.getIfPresent(key);
           case SUPPLY     ->  value = TokenSupply.getIfPresent(key);
           default -> throw new IllegalArgumentException("Unexpected value: " + type);
       }
       return value;
   }

   public static void put(RequestTypes type, String key, long value) {

       switch (type) {
           case BALANCE    ->  BalanceCache.put(key, value);
           case SUPPLY     ->  TokenSupply.put(key, value);
           default -> throw new IllegalArgumentException("Unexpected value: " + type);
       }
       
   }

}
