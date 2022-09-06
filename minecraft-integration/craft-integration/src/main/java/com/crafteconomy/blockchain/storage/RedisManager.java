package com.crafteconomy.blockchain.storage;

import java.net.URI;
import java.time.Duration;
import java.util.UUID;
import java.util.logging.Level;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.utils.Util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.InvalidURIException;
import redis.clients.jedis.util.JedisURIHelper;

// 	CraftBlockchainPlugin.log("[DEBUG] Jedis Active: " + redisDB.getPool().getNumActive());
// 	CraftBlockchainPlugin.log("[DEBUG] Jedis Idle: " + redisDB.getPool().getNumIdle());

// ********* IMPORTANT *********
// Ensure redis-cli -> `CONFIG SET notify-keyspace-events K$` (KEA also works)
// notify-keyspace-events = "KEA" in /etc/redis/redis.conf

public class RedisManager {

    private static JedisPool pool;
    
    private static JedisPoolConfig config;

    private static RedisManager instance;

    public RedisManager(String uri) {
        instance = this;
        config = new JedisPoolConfig();  

        // TODO: Up "setMaxTotal" in the future when live (Too low = crash)        
        config.setMaxTotal(500);
        config.setMaxIdle(150);
        config.setMinIdle(50);
  
        // needed for redis pubsub
        config.setMaxWait(Duration.ZERO);
        
        // if(password.length() > 0) {
        //     pool = new JedisPool(config, host, port, 0, password, Protocol.DEFAULT_DATABASE);
        // } else {
        //     pool = new JedisPool(config, host, port, 0);
        // }
        
        // CraftBlockchainPlugin.log(uri);

        URI redisURI = URI.create(uri);
        if (!JedisURIHelper.isValid(redisURI)) {
            throw new InvalidURIException(String.format("Cannot open Redis connection due invalid URI. %s", uri.toString()));
        }
        pool = new JedisPool(config, redisURI);
    }

    public JedisPool getPool() {
        return pool;
    }

    public Jedis getRedisConnection() { 
        return pool.getResource();
    }

    public void returnRedisConnection(Jedis jedis) {
        // check if jedis is null
        if(jedis != null) {
            jedis.close();
            
            if(jedis.isBroken()) {
                pool.returnBrokenResource(jedis);
            } else {
                pool.returnResource(jedis);
                
            }          
        }
    }

    public void removePendingTxsOnStartupIfServerNameMatches(final String server_name) {
        // loop through all keys in redis, get their values. Check if server_name.equalsIgnroeCase(servername)
        // if so, remove the key
        try (Jedis jedis = RedisManager.getInstance().getRedisConnection()) {
            for (String key : jedis.keys("*")) {
                if (key.startsWith("tx_")) {
                    String value = jedis.get(key);
                    if (value != null) {                        
                        if(value.contains(CraftBlockchainPlugin.SERVER_NAME)) {
                            CraftBlockchainPlugin.log("Deleting key since it is a straggler from last shutdown:" + value);                            
                            jedis.unlink(key);
                        }                        
                    }
                }
            }
        } catch (Exception e) {
            CraftBlockchainPlugin.log("[RedisManager.java] Failed to clear stale transactions from last shutdown.");
        }
    }

   
    public void submitTxForSigning(String FROM_ADDRESS, UUID TxID, String JSON_Output, int TimeToLiveMinutes) {
        String TxLabel = "tx_" + FROM_ADDRESS + "_" + TxID.toString();

        try (Jedis jedis = getRedisConnection()) {

            // If we deside we dont want expring txs, it will just set it (good for debugging)
            if(TimeToLiveMinutes <= 0) {
                jedis.set(TxLabel, JSON_Output);
            } else {
                jedis.setex(TxLabel, TimeToLiveMinutes*60, JSON_Output);
            }
            // CraftBlockchainPlugin.log("Tx JSON Saved to redis as " + TxLabel + ", "+ JSON_Output + "\n");                        
        } catch (Exception e) {
            CraftBlockchainPlugin.log("[RedisManager.java] Error saving Tx JSON to redis: " + e.getMessage(), Level.SEVERE);          
        } 
    }

    public void closePool() {
        pool.destroy();
    }

    public static RedisManager getInstance() {
        return instance;
    }
}