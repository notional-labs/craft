package com.crafteconomy.blockchain;

import com.crafteconomy.blockchain.api.IntegrationAPI;
import com.crafteconomy.blockchain.commands.WalletCMD;
import com.crafteconomy.blockchain.commands.subcommands.WalletBalance;
import com.crafteconomy.blockchain.commands.subcommands.WalletFaucet;
import com.crafteconomy.blockchain.commands.subcommands.WalletHelp;
import com.crafteconomy.blockchain.commands.subcommands.WalletMyPendingTxs;
import com.crafteconomy.blockchain.commands.subcommands.WalletOutputPendingTxs;
import com.crafteconomy.blockchain.commands.subcommands.WalletSend;
import com.crafteconomy.blockchain.commands.subcommands.WalletSet;
import com.crafteconomy.blockchain.commands.subcommands.WalletSupply;
import com.crafteconomy.blockchain.commands.subcommands.WalletWebapp;
import com.crafteconomy.blockchain.commands.subcommands.debugging.WalletFakeSign;
import com.crafteconomy.blockchain.commands.subcommands.debugging.WalletGenerateFakeTx;
import com.crafteconomy.blockchain.listeners.JoinLeave;
import com.crafteconomy.blockchain.storage.MongoDB;
import com.crafteconomy.blockchain.storage.RedisManager;
import com.crafteconomy.blockchain.transactions.PendingTransactions;
import com.crafteconomy.blockchain.transactions.events.RedisKeyListener;
import com.crafteconomy.blockchain.transactions.events.SignedTxCheckListner;
import com.crafteconomy.blockchain.utils.Util;
import com.crafteconomy.blockchain.wallets.WalletManager;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import redis.clients.jedis.Jedis;

// CraftBlockchainPlugin.java Task:
// +whitelist http://ENDPOINT:4500/ to only our machines ip [since only DOA needs it for Quest and Such]. BE SUPER CAREFUL
// TODO: Fix redis key listener not closing correctly / unexpect quit on server shutdown

// ********* IMPORTANT *********
// Ensure redis-cli -> `CONFIG SET notify-keyspace-events K$` (KEA also works)
// notify-keyspace-events = "KEA" in /etc/redis/redis.conf

public class CraftBlockchainPlugin extends JavaPlugin {

    private static CraftBlockchainPlugin instance;

    private static RedisManager redisDB;

    private static MongoDB mongoDB;

    public static long MAX_FAUCET_AMOUNT = 10_000;

    private BukkitTask redisPubSubTask = null;
    private Jedis jedisPubSubClient = null;
    private RedisKeyListener keyListener = null;


    @Override
    public void onEnable() {
        instance = this;

        getConfig().options().copyDefaults(true);
        saveConfig();


        redisDB = new RedisManager(
            getConfig().getString("Redis.host"), 
            getConfig().getInt("Redis.port"),
            getConfig().getString("Redis.password")
        );

        mongoDB = new MongoDB(
            getConfig().getString("MongoDB.host"), 
            getConfig().getInt("MongoDB.port"), 
            getConfig().getString("MongoDB.database"), 
            getConfig().getString("MongoDB.username"),
            getConfig().getString("MongoDB.password")
        );

        if(getTokenFaucet() == null || getApiEndpoint() == null) {
            getLogger().severe("Faucet token OR API endpoints not set in config.yml, disabling plugin");
            getPluginLoader().disablePlugin(this);
            return;
        }

        WalletCMD cmd = new WalletCMD();
        getCommand("wallet").setExecutor(cmd);
        getCommand("wallet").setTabCompleter(cmd);

        cmd.registerCommand("help", new WalletHelp());
        cmd.registerCommand(new String[] {"b", "bal", "balance"}, new WalletBalance());
        cmd.registerCommand(new String[] {"set", "setwallet"}, new WalletSet());
        cmd.registerCommand(new String[] {"supply"}, new WalletSupply());
        cmd.registerCommand(new String[] {"faucet", "deposit"}, new WalletFaucet());
        cmd.registerCommand(new String[] {"pay", "send"}, new WalletSend());
        cmd.registerCommand(new String[] {"webapp"}, new WalletWebapp());

        // debug commands
        cmd.registerCommand(new String[] {"faketx"}, new WalletGenerateFakeTx());
        cmd.registerCommand(new String[] {"fakesign"}, new WalletFakeSign());
        cmd.registerCommand(new String[] {"allpending", "allkeys"}, new WalletOutputPendingTxs());
        cmd.registerCommand(new String[] {"mypending", "pending", "mykeys", "keys"}, new WalletMyPendingTxs());

        // arg[0] commands which will tab complete
        cmd.addTabComplete(new String[] {"balance","setwallet","supply","send","pending","webapp"});


        getServer().getPluginManager().registerEvents(new JoinLeave(), this);  
        getServer().getPluginManager().registerEvents(new SignedTxCheckListner(), this);


        // We dont want to crash main server thread. Running sync crashes main server thread        
        keyListener = new RedisKeyListener();    
        redisPubSubTask = Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
            @Override
            public void run() {      
                Util.logSevere("Starting Redis PubSub Client");  
                jedisPubSubClient = redisDB.getRedisConnection();        
                // Webapp sends this request after the Tx has been signed
                jedisPubSubClient.psubscribe(keyListener, "__key*__:signed_*");                
            }
        });
        
        // set players wallets back to memory from database
        Bukkit.getOnlinePlayers().forEach(player -> WalletManager.getInstance().cacheWalletOnJoin(player.getUniqueId()));        
    }

    @Override
    public void onDisable() {
        // TODO:
        // PendingTransactions.clearUncompletedTransactionsFromRedis();

        keyListener.unsubscribe();
        redisPubSubTask.cancel();

        redisDB.closePool(); 
        // jedisPubSubClient.close();              
    }

    public RedisManager getRedis(){
        return redisDB;
    }

    public MongoDB getMongo(){
        return mongoDB;
    }

    public static CraftBlockchainPlugin getInstance() {
        return instance;
    }

    public static IntegrationAPI getAPI() {
        return IntegrationAPI.getInstance();
    }

    public String getTokenFaucet() {        
        return getConfig().getString("TOKEN_FAUCET_ENDPOINT"); // :4500
    }

    public String getApiEndpoint() {
        // BlockchainAPI - :1317
        return getConfig().getString("API_ENDPOINT");
    }

    public String getWalletPrefix() {
        // TODO: Update -> ex. osmo or craft, make lowercase
        return "osmo";
    }
    public int getWalletLength() {
        // TODO: craft is 43, osmo is 43
        return 43;
    }

    public String getTokenDenom(boolean smallerValue) {
        if(smallerValue) {
            return "uosmo";
        }
        return "osmo";
    }
}
