package com.crafteconomy.blockchain.storage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

// TODO: redis.clients.jedis.exceptions.JedisException: Could not return the broken resource to the pool
// on close - Caused by: java.lang.IllegalStateException: Invalidated object not currently part of this pool

public class RedisDB {

    private static JedisPool pool;
    
    private static JedisPoolConfig config;

    private static RedisDB instance;

    public RedisDB(String host, int port) {
        instance = this;

        int maxConnections = 2;

        config = new JedisPoolConfig();        
        config.setMaxTotal(maxConnections);
        config.setMaxIdle(maxConnections);
        config.setMinIdle(maxConnections);
        
        pool = new JedisPool(config, host, port);
    }    

    public JedisPool getPool() {
        return pool;
    }

    public Jedis getRedisConnection() {   
        // be sure to getPool().returnResource(jedis); after use
        return pool.getResource();
    }   

    private List<String> keysList = new ArrayList<>();

    // signed_TX-ID_METADATA
    public List<String> getSignedTransactions(String key) {        
        Jedis jedis = null;
        try {
            jedis = getRedisConnection();
            Set<String> redisKeys = jedis.keys(key);
            Iterator<String> it = redisKeys.iterator();
            
            keysList.clear();    
                        
            while (it.hasNext()) {
                String data = it.next();
                keysList.add(data);
            }                                                    
        } catch(Exception e){

        } finally {
            getPool().returnResource(jedis);
        }
        return keysList;
    }

    public void close(){
        pool.close();
    }

    public static RedisDB getInstance() {
        return instance;
    }
}