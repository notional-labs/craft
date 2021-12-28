package com.crafteconomy.blockchain;

import com.crafteconomy.blockchain.commands.WalletCMD;
import com.crafteconomy.blockchain.commands.subcommands.WalletBalance;
import com.crafteconomy.blockchain.commands.subcommands.WalletFaucet;
import com.crafteconomy.blockchain.commands.subcommands.WalletHelp;
import com.crafteconomy.blockchain.commands.subcommands.WalletSend;
import com.crafteconomy.blockchain.commands.subcommands.WalletSet;
import com.crafteconomy.blockchain.commands.subcommands.WalletSupply;
import com.crafteconomy.blockchain.listeners.JoinLeave;
import com.crafteconomy.blockchain.signedtxs.GetAllSignedTransactionsRunnable;
import com.crafteconomy.blockchain.storage.MongoDB;
import com.crafteconomy.blockchain.storage.RedisDB;
import com.crafteconomy.blockchain.wallets.WalletManager;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

// TODO:
// - Add way to remove TxID from Redis Queue once signed & action completed in game (API)
// - Change WalletSend to HTTP API query instead of craftd binary [https://github.com/notional-labs/craft/issues/8]
// - Read database json information from CraftBukkitCore location for redis & mongo

public class CraftBlockchainPlugin extends JavaPlugin {

    private static WalletManager walletManager;

    private static MongoDB mongoDB;

    private static RedisDB redisDB;

    private static CraftBlockchainPlugin instance;

    @Override
    public void onEnable() {
        instance = this;

        redisDB = new RedisDB("localhost", 6379);
        mongoDB = new MongoDB("localhost", 27017, "crafteconomy");

        walletManager = new WalletManager();

        WalletCMD cmd = new WalletCMD();
        getCommand("wallet").setExecutor(cmd);
        cmd.registerCommand("help", new WalletHelp());
        cmd.registerCommand(new String[] {"b", "bal", "balance"}, new WalletBalance());
        cmd.registerCommand(new String[] {"set", "add", "addwallet", "setwallet"}, new WalletSet());
        cmd.registerCommand(new String[] {"token", "stake", "supply"}, new WalletSupply());
        cmd.registerCommand(new String[] {"faucet", "deposit"}, new WalletFaucet());
        cmd.registerCommand(new String[] {"pay", "send", "tx"}, new WalletSend());

        getServer().getPluginManager().registerEvents(new JoinLeave(), this);  
        // getServer().getPluginManager().registerEvents(new SignedTxCheckListner(), this);

        // Load players wallets in during reloads
        Bukkit.getOnlinePlayers().forEach(player -> walletManager.cacheWalletOnJoin(player.getUniqueId()));
        
         // Start runnable to check for signed transactions 
         int seconds = 5;
         new GetAllSignedTransactionsRunnable().runTaskTimer(this, 20L, seconds * 20L);
    }

    @Override
    public void onDisable() {
        redisDB.getPool().close();
    }

    public RedisDB getRedis(){
        return redisDB;
    }

    public MongoDB getMongo(){
        return mongoDB;
    }

    public static String getCraftBinary() {
        return "craftd"; // or absolute path
    }

    public static CraftBlockchainPlugin getInstance() {
        return instance;
    }

    public static boolean debugStatus() {
        return true;
    }
    
}
