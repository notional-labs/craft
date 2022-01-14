package com.crafteconomy.blockchain.storage;

import java.time.Duration;
import java.util.UUID;

import com.crafteconomy.blockchain.utils.Util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

// 	Util.log("[DEBUG] Jedis Active: " + redisDB.getPool().getNumActive());
// 	Util.log("[DEBUG] Jedis Idle: " + redisDB.getPool().getNumIdle());

public class RedisManager {

    private static JedisPool pool;
    
    private static JedisPoolConfig config;

    private static RedisManager instance;

    public RedisManager(String host, int port, String username, String password) {
        instance = this;
        config = new JedisPoolConfig();  

        // TODO: Up "setMaxTotal" in the future when live (Too low = crash)        
        config.setMaxTotal(500);
        config.setMaxIdle(150);
        config.setMinIdle(50);
  
        // needed for redis pubsub
        config.setMaxWait(Duration.ZERO);
        
        if(username.length() > 0 && password.length() > 0) {
            pool = new JedisPool(config, host, port, 2000, username, password);
        } else {
            pool = new JedisPool(config, host, port, 2000);
        }
    }    

    public void debugging() {
        System.out.println();
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

   
    public void submitTxForSigning(String FROM_ADDRESS, UUID TxID, String JSON_Output, int TimeToLiveMinutes) {
        String TxLabel = "tx_" + FROM_ADDRESS + "_" + TxID.toString();

        try (Jedis jedis = getRedisConnection()) {
            jedis.setex(TxLabel, TimeToLiveMinutes*60, JSON_Output);
            Util.log("Tx JSON Saved to redis as " + TxLabel + ", "+ JSON_Output + "\n");                        
        } catch (Exception e) {
            Util.logSevere("[RedisManager.java] Error saving Tx JSON to redis: " + e.getMessage());          
        } 
    }

    public void closePool() {
        pool.destroy();
    }

    public static RedisManager getInstance() {
        return instance;
    }
}