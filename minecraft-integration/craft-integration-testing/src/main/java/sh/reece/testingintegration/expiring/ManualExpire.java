package sh.reece.testingintegration.expiring;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.api.IntegrationAPI;
import com.crafteconomy.blockchain.core.types.ErrorTypes;
import com.crafteconomy.blockchain.transactions.Tx;
import com.crafteconomy.blockchain.utils.Util;

import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ManualExpire implements CommandExecutor {

    IntegrationAPI api = CraftBlockchainPlugin.getAPI();

    Tx txinfo;

    // test-manual-expire
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {    

        // if len of args == 0
        if(args.length == 0) {
            sender.sendMessage("USAGE: /test-manual-expire <create/expire>");
            return true;
        }

        Player player = (Player) sender;

        if(args[0].toLowerCase().equalsIgnoreCase("create")) {
            // This is a normal tx which we will manually expire later on command run
            txinfo = new Tx(); // getTxID() -> auto generated. just a UUID [/wallet pending shows all]
            txinfo.setFromUUID(player.getUniqueId());
            txinfo.setToWallet(api.getWallet(player.getUniqueId()));
            txinfo.setCraftAmount(0);
            txinfo.setDescription("This transaction will be manually expired");
            txinfo.setFunction(null);                    
            txinfo.setConsumerOnExpire(manualExpire());            
    
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

        // first the msg - Transaction was successfully expired runs
        // then after 1 second, the expires logic runs so that the 'player did not sign logic' runs & reverts what ever is needed.
        } else if(args[0].toLowerCase().equalsIgnoreCase("expire")) {
            boolean wasSuccessful = api.expireTransaction(txinfo.getTxID());
            // in production you wouldn't have to put this here so long as your 
            // manualExpire function messages the player what happened
            if(wasSuccessful) {
                sender.sendMessage("msg - Transaction was successfully expired");
            } else {
                sender.sendMessage("Transaction was not expired");
            }
        }
        return true;
    }

    @SuppressWarnings("deprecation")
    public static Consumer<UUID> manualExpire() {
        Consumer<UUID> purchase = (uuid) -> {  
            Bukkit.broadcastMessage("[!] The transaction expired (manually)!\n"); 
        };
        return purchase;
    }
    
}
