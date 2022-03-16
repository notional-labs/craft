package com.crafteconomy.blockchain.core.request;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.crafteconomy.blockchain.core.types.RequestTypes;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import org.bukkit.Bukkit;

public class Caches {

    /*
     * Holds account balances & total supply queries for X seconds to reduce load
     */

    // ADDRESS, BALANCE
    private static final Cache<String, Long> BalanceCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .build();

    // DENOMINATION, SUPPLY
    private static final Cache<String, Long> TokenSupply = CacheBuilder.newBuilder()
            .maximumSize(10) // Change this if NFT's become individual tokens
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();

    // UUID, ESCROW_BALANCE
    private static final Cache<UUID, Long> EscrowBalance = CacheBuilder.newBuilder()
            .maximumSize((long) (Bukkit.getMaxPlayers() * 2)) // room for staff to be cached too
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();


    public static Object getIfPresent(RequestTypes type, String key) {
        Object value = null;
        switch (type) {
            case BALANCE -> value = BalanceCache.getIfPresent(key);
            case SUPPLY -> value = TokenSupply.getIfPresent(key);
            case ESCROW -> value = EscrowBalance.getIfPresent(UUID.fromString(key));
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        }
        return value;
    }

    public static Object getIfPresent(RequestTypes type, UUID uuid) {
        Object value = null;
        switch (type) {
            case ESCROW -> value = EscrowBalance.getIfPresent(uuid);
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        }
        return value;
    }

    public static void put(RequestTypes type, String key, long value) {
        switch (type) {
            case BALANCE -> BalanceCache.put(key, value);
            case SUPPLY -> TokenSupply.put(key, value);
            case ESCROW -> EscrowBalance.put(UUID.fromString(key), value);
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        }
    }

    public static void put(RequestTypes type, UUID uuid, long value) {
        switch (type) {
            case ESCROW -> EscrowBalance.put(uuid, value);
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        }
    }

    public static void invalidate(RequestTypes type, UUID uuid) {
        switch (type) {
            case ESCROW -> EscrowBalance.invalidate(uuid);
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        }
    }

}
