package com.crafteconomy.blockchain;

import com.crafteconomy.blockchain.api.IntegrationAPI;
import com.crafteconomy.blockchain.commands.escrow.EscrowCMD;
import com.crafteconomy.blockchain.commands.escrow.subcommands.EscrowBalance;
import com.crafteconomy.blockchain.commands.escrow.subcommands.EscrowDeposit;
import com.crafteconomy.blockchain.commands.escrow.subcommands.EscrowHelp;
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
import com.crafteconomy.blockchain.commands.wallet.subcommands.debugging.WalletFakeSign;
import com.crafteconomy.blockchain.commands.wallet.subcommands.debugging.WalletGenerateFakeTx;
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
// TODO: Fix redis key listener not closing correctly / unexpect quit on server shutdown. This is kinda fixed but eh

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

    private String webappLink = null;

    public static boolean ENABLED_FAUCET = false;


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

        webappLink = getConfig().getString("SIGNING_WEBAPP_LINK");

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

        // Escrow Commands
        EscrowCMD escrowCMD = new EscrowCMD();
        getCommand("escrow").setExecutor(escrowCMD);
        getCommand("escrow").setTabCompleter(escrowCMD);
        // register sub commands
        escrowCMD.registerCommand("help", new EscrowHelp());
        escrowCMD.registerCommand(new String[] {"b", "bal", "balance"}, new EscrowBalance());
        escrowCMD.registerCommand(new String[] {"d", "dep", "deposit"}, new EscrowDeposit());
        escrowCMD.registerCommand(new String[] {"r", "red", "redeem"}, new EscrowRedeem());
        // arg[0] commands which will tab complete
        escrowCMD.addTabComplete(new String[] {"balance","deposit","redeem"});


        getServer().getPluginManager().registerEvents(new JoinLeave(), this);  
        getServer().getPluginManager().registerEvents(new SignedTxCheckListner(), this);


        // We dont want to crash main server thread. Running sync crashes main server thread
        keyListener = new RedisKeyListener(); 
        jedisPubSubClient = redisDB.getRedisConnection();  
        redisPubSubTask = Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
            @Override
            public void run() {      
                Util.logSevere("Starting Redis PubSub Client");                          
                // Webapp sends this request after the Tx has been signed
                jedisPubSubClient.psubscribe(keyListener, "__key*__:signed_*");                
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

    public String getWebappLink() {
        return webappLink;
    }

    public String getTokenDenom(boolean smallerValue) {
        if(smallerValue) {
            return "uosmo";
        }
        return "osmo";
    }
}
