package sh.reece.testingintegration.simple;

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


public class MyExampleTransaction implements CommandExecutor {

    IntegrationAPI api = CraftBlockchainPlugin.getAPI();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {    
        
        if(sender instanceof ConsoleCommandSender) {
            Util.colorMsg(sender, "Only players can use this command");
            return true;
        }

        Player player = (Player) sender;


        String to_wallet = api.getWallet(player.getUniqueId());
        if(to_wallet == null) {
            Util.colorMsg(player, "You do not have a wallet set");
            return true;
        }

        float craft_balance = api.getCraftBalance(player.getUniqueId());
        if(craft_balance < 0) {
            Util.colorMsg(player, "Error:" + ErrorTypes.of((int) craft_balance));
            return true;
        }
         
        if(craft_balance > 10) {

            // Creates inline transaction
            // Tx tx1 = api.createNewTx(player.getUniqueId(), to_wallet, 10, "Describe what it does here", Logic.purchaseBusinessLicense());
            Tx txinfo = new Tx(); // getTxID() -> auto generated. just a UUID [/wallet pending shows all]
            txinfo.setFromUUID(player.getUniqueId());
            txinfo.setToWallet(to_wallet);
            txinfo.setCraftAmount(10);
            txinfo.setDescription("Describe what it does here");
            txinfo.setFunction(Logic.purchaseBusinessLicense());
            
            txinfo.setRedisMinuteTTL(15);            

            txinfo.setIncludeTxClickable(false);
            txinfo.setSendDescMessage(false);
            txinfo.setSendWebappLink(false);

            ErrorTypes error = txinfo.submit();

            if(error == ErrorTypes.SUCCESS) {
                // Util.colorMsg(sender,  + txinfo.getTxID());
                api.sendTxIDClickable(sender, txinfo.getTxID().toString(), "\n&eMisc Transaction created successfully\n%value%");
            } else {
                Util.colorMsg(player, "Error: " + error.toString());
            }
        } else {
            Util.colorMsg(player, "You do not have 10 craft to purchase a business license");
        }

        return true;
    }
    
}
