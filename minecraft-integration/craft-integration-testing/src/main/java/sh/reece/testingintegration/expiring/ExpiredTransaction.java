package sh.reece.testingintegration.expiring;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.api.IntegrationAPI;
import com.crafteconomy.blockchain.core.types.ErrorTypes;
import com.crafteconomy.blockchain.transactions.Tx;
import com.crafteconomy.blockchain.utils.Util;

import sh.reece.testingintegration.Logic;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * This class is used to show how when a transaction expires, you can run code for it.
 * 
 * This is useful if 2 players make a trade, the items are held by the server while 1 goes & signs.
 * If they do not sign, after the expired time: 
 *  - return the items back to the players inventories.
 *  - If the server restarts before a player has a chance to sign, this fixes that issue as well :)
 * 
 * By default the TTL is defined by Integration at 15 minutes, so you can specify this or leave it as default
 */

public class ExpiredTransaction implements CommandExecutor {

    IntegrationAPI api = CraftBlockchainPlugin.getAPI();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {    
        Player player = (Player) sender;

        String to_wallet = api.getWallet(player.getUniqueId());

        Tx txinfo = new Tx(); // getTxID() -> auto generated. just a UUID [/wallet pending shows all]
        txinfo.setFromUUID(player.getUniqueId());
        txinfo.setToWallet(to_wallet);
        txinfo.setCraftAmount(0);
        txinfo.setDescription("This transaction is not meant to be signed");
        txinfo.setFunction(null);
        
        txinfo.setRedisMinuteTTL(1); // This runs after 1 minute from redis
        txinfo.setConsumerOnExpire(Logic.expireLogic());
        // txinfo.setBiConsumerOnExpire(biConsumerOnExpireHere); // if needed

        txinfo.setIncludeTxClickable(false);
        txinfo.setSendDescMessage(false);
        txinfo.setSendWebappLink(false);

        ErrorTypes error = txinfo.submit();
        if(error == ErrorTypes.SUCCESS) {
            // Util.colorMsg(sender,  + txinfo.getTxID());
            api.sendTxIDClickable(sender, txinfo.getTxID().toString(), "\n&eExpire Transaction created successfully\n%value%");
            sender.sendMessage("Wait 1 minute & the Tx will expire, then the code will run for Logic.expireLogic()");
        } else {
            Util.colorMsg(player, "Error: " + error.toString());
        }
        

        return true;
    }
    
}
