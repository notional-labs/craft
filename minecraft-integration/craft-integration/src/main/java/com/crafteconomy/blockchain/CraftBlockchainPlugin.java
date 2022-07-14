package com.crafteconomy.blockchain;

import com.crafteconomy.blockchain.api.IntegrationAPI;
import com.crafteconomy.blockchain.commands.escrow.EscrowCMD;
import com.crafteconomy.blockchain.commands.escrow.subcommands.EscrowBalance;
import com.crafteconomy.blockchain.commands.escrow.subcommands.EscrowDeposit;
import com.crafteconomy.blockchain.commands.escrow.subcommands.EscrowHelp;
import com.crafteconomy.blockchain.commands.escrow.subcommands.EscrowPay;
import com.crafteconomy.blockchain.commands.escrow.subcommands.EscrowRedeem;
import com.crafteconomy.blockchain.commands.wallet.WalletCMD;
import com.crafteconomy.blockchain.commands.wallet.subcommands.WalletBalance;
import com.crafteconomy.blockchain.commands.wallet.subcommands.WalletFaucet;
import com.crafteconomy.blockchain.commands.wallet.subcommands.WalletHelp;
import com.crafteconomy.blockchain.commands.wallet.subcommands.WalletMyPendingTxs;
import com.crafteconomy.blockchain.commands.wallet.subcommands.WalletOutputPendingTxs;
import com.crafteconomy.blockchain.commands.wallet.subcommands.WalletSend;
import com.crafteconomy.blockchain.commands.wallet.subcommands.WalletSet;
import com.crafteconomy.blockchain.commands.wallet.subcommands.WalletSupply;
import com.crafteconomy.blockchain.commands.wallet.subcommands.WalletWebapp;
import com.crafteconomy.blockchain.commands.wallet.subcommands.debugging.CraftTokenPrice;
import com.crafteconomy.blockchain.commands.wallet.subcommands.debugging.WalletFakeSign;
import com.crafteconomy.blockchain.commands.wallet.subcommands.debugging.WalletGenerateFakeTx;
import com.crafteconomy.blockchain.listeners.JoinLeave;
import com.crafteconomy.blockchain.storage.MongoDB;
import com.crafteconomy.blockchain.storage.RedisManager;
import com.crafteconomy.blockchain.transactions.PendingTransactions;
import com.crafteconomy.blockchain.transactions.listeners.ExpiredTransactionListener;
import com.crafteconomy.blockchain.transactions.listeners.RedisKeyListener;
import com.crafteconomy.blockchain.transactions.listeners.SignedTxCheckListner;
import com.crafteconomy.blockchain.utils.Util;
import com.crafteconomy.blockchain.wallets.WalletManager;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import redis.clients.jedis.Jedis;

// CraftBlockchainPlugin.java Task:
// +whitelist http://ENDPOINT:4500/ to only our machines ip [since only DOA needs it for Quest and Such]. BE SUPER CAREFUL

// ********* IMPORTANT *********
// Ensure redis-cli -> `CONFIG SET notify-keyspace-events K$` (KEA also works)
// notify-keyspace-events = "KEA" in /etc/redis/redis.conf

public class CraftBlockchainPlugin extends JavaPlugin {

    private static CraftBlockchainPlugin instance;

    private static RedisManager redisDB;

    private static MongoDB mongoDB;

    public static String ADMIN_PERM = "crafteconomy.admin";

    private Double TAX_RATE;

    private String SERVER_WALLET = null;

    private BukkitTask redisPubSubTask = null;
    private Jedis jedisPubSubClient = null;
    private RedisKeyListener keyListener = null;

    private static String webappLink = null;
    private static String TX_QUERY_ENDPOINT = null;

    public static boolean ENABLED_FAUCET = false;

    private static Integer REDIS_MINUTE_TTL = 30;   
    private static Boolean DEV_MODE = false;

    @Override
    public void onEnable() {
        instance = this;

        getConfig().options().copyDefaults(true);
        saveConfig();

        redisDB = new RedisManager(getConfig().getString("Redis.uri"));
        mongoDB = new MongoDB(getConfig().getString("MongoDB.uri"), getConfig().getString("MongoDB.database"));
        // redisDB = new RedisManager("redis://:PASSWORD@IP:6379");
        // mongoDB = new MongoDB("mongodb://USER:PASS@IP:PORT/?authSource=AUTHDB", "crafteconomy");

        System.out.println(redisDB.getRedisConnection().ping());
        System.out.println("" + mongoDB.getDatabase().getCollection("connections").countDocuments());


        SERVER_WALLET = getConfig().getString("SERVER_WALLET_ADDRESS");

        webappLink = getConfig().getString("SIGNING_WEBAPP_LINK");
        TX_QUERY_ENDPOINT = getConfig().getString("TX_QUERY_ENDPOINT");

        TAX_RATE = getConfig().getDouble("TAX_RATE");
        if(TAX_RATE == null) TAX_RATE = 0.0;

        REDIS_MINUTE_TTL = getConfig().getInt("TAX_RATE");
        if(REDIS_MINUTE_TTL == null) REDIS_MINUTE_TTL = 30;

        DEV_MODE = getConfig().getBoolean("DEV_MODE");
        if(DEV_MODE == null) DEV_MODE = false;

        if(DEV_MODE) {
            // async runnable every 4 minutes
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
                @Override
                public void run() {
                    Util.coloredBroadcast("&c&l[!] REMINDER, INTEGRATION DEV MODE ENABLED");
                }
            }, 0, 20*60*4);
        }


        if(getApiEndpoint() == null) {
            getLogger().severe("API REST (lcd) endpoint not set in config.yml, disabling plugin");
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

        cmd.registerCommand(new String[] {"craft", "craftprice"}, new CraftTokenPrice());

        // arg[0] commands which will tab complete
        cmd.addTabComplete(new String[] {"balance","setwallet","supply","send","pending","webapp"});

        // Escrow Commands
        EscrowCMD escrowCMD = new EscrowCMD();
        getCommand("escrow").setExecutor(escrowCMD);
        getCommand("escrow").setTabCompleter(escrowCMD);
        // register sub commands
        escrowCMD.registerCommand("help", new EscrowHelp());
        escrowCMD.registerCommand(new String[] {"b", "bal", "balance"}, new EscrowBalance());
        escrowCMD.registerCommand(new String[] {"d", "dep", "deposit"}, new EscrowDeposit());
        escrowCMD.registerCommand(new String[] {"r", "red", "redeem"}, new EscrowRedeem());
        escrowCMD.registerCommand(new String[] {"p", "pay", "payment"}, new EscrowPay());
        // arg[0] commands which will tab complete
        escrowCMD.addTabComplete(new String[] {"balance","deposit","redeem","pay"});


        getServer().getPluginManager().registerEvents(new JoinLeave(), this);  
        getServer().getPluginManager().registerEvents(new SignedTxCheckListner(), this);
        getServer().getPluginManager().registerEvents(new ExpiredTransactionListener(), this);


        // We dont want to crash main server thread. Running sync crashes main server thread
        keyListener = new RedisKeyListener(); 
        jedisPubSubClient = redisDB.getRedisConnection();  
        redisPubSubTask = Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
            @Override
            public void run() {      
                Util.logSevere("Starting Redis PubSub Client");                          
                // Webapp sends this request after the Tx has been signed
                // jedisPubSubClient.psubscribe(keyListener, "__key*__:signed_*"); 
                // jedisPubSubClient.psubscribe(keyListener, "__keyevent@*__:expire*"); // gets expired keys from redis (after Tx is removed), so we can remove from pending
                jedisPubSubClient.psubscribe(keyListener, "*");  // __keyevent@*__:expire* 
            }
        });
        
        // set players wallets back to memory from database
        Bukkit.getOnlinePlayers().forEach(player -> WalletManager.getInstance().cacheWalletOnJoin(player.getUniqueId()));        
    }

    @Override
    public void onDisable() {
        // TODO: some reason, this still crashes main server thread sometimes locally
        keyListener.unsubscribe();
        redisPubSubTask.cancel();
        
        // TODO This breaks getting resources from the redis pool on reload
        // Bukkit.getScheduler().cancelTasks(this);

        PendingTransactions.clearUncompletedTransactionsFromRedis();
        redisDB.closePool();
        mongoDB.disconnect(); 
        // jedisPubSubClient.close();          
        
        Bukkit.getScheduler().cancelTasks(this);
    }

    public static int getRedisMinuteTTL() {
        return REDIS_MINUTE_TTL;
    }
    public static boolean getIfInDevMode() {
        return DEV_MODE;
    }

    public static String getTxQueryEndpoint() {
        // https://api.cosmos.network/cosmos/tx/v1beta1/txs/{TENDERMINT_HASH}
        return TX_QUERY_ENDPOINT;
    }

    public RedisManager getRedis() {
        return redisDB;
    }

    public MongoDB getMongo() {
        return mongoDB;
    }

    public static CraftBlockchainPlugin getInstance() {
        return instance;
    }

    public static IntegrationAPI getAPI() {
        return IntegrationAPI.getInstance();
    }

    public String getSecret() {        
        return getConfig().getString("DAO_ESCROW_ENDPOINT_SECRET"); // random string of secret characters for rest api
    }

    public String getApiEndpoint() {
        // BlockchainAPI - :1317
        return getConfig().getString("API_ENDPOINT");
    }

    public String getWalletPrefix() {        
        return "craft"; 
    }
    public int getWalletLength() {    
        return 39 + getWalletPrefix().length();
    }

    public String getWebappLink() {
        return webappLink;
    }

    public Double getTaxRate() {
        return TAX_RATE;
    }

    
    public String getServersWalletAddress() {
        return SERVER_WALLET;
    }

    // TODO: Remove?
    public String getTokenDenom(boolean smallerValue) {
        if(smallerValue) {
            return "ucraft";
        }
        return "craft";
    }
}
