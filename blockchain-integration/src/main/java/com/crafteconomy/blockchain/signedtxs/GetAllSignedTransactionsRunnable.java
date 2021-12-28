package com.crafteconomy.blockchain.signedtxs;

import java.util.List;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.storage.RedisDB;
import com.crafteconomy.blockchain.utils.Util;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class GetAllSignedTransactionsRunnable extends BukkitRunnable {

	RedisDB redisDB = CraftBlockchainPlugin.getInstance().getRedis();  

	// This way we do not have to init same object every time
	SignedTransactionEvent event = new SignedTransactionEvent(null, null);         

	@Override
	public void run() {
			
		// signed_TxID_METADATA 
		List<String> keys = CraftBlockchainPlugin.getInstance().getRedis().getSignedTransactions("signed_*");

		Util.logSevere("[DEBUG] BukkitRunnable run for SignedTXEvent\n");
		Util.log("[DEBUG] Jedis Active: " + redisDB.getPool().getNumActive());
		Util.log("[DEBUG] Jedis Idle: " + redisDB.getPool().getNumIdle());

		for(String key : keys){
			String[] TxInfo = key.split("_");

			// [0] = signed_
			String TxHash = TxInfo[1];
			String metaData = TxInfo[2];

			if(TxHash == null || metaData == null) { return;  }

			event.setTx(TxHash);
			event.setMetaData(metaData);
			Bukkit.getServer().getPluginManager().callEvent(event);
		}
	}
    
}