package sh.reece.testingvault;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.api.IntegrationAPI;
import com.crafteconomy.blockchain.core.types.ErrorTypes;
import com.crafteconomy.blockchain.transactions.Tx;
import com.crafteconomy.blockchain.utils.Util;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import sh.reece.testingvault.callbacks.Logic;


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

        long balance = api.getBalance(player.getUniqueId());
        if(balance < 0) {
            Util.colorMsg(player, "Error:" + ErrorTypes.of((int) balance));
            return true;
        }
         
        if(balance > 20) {

            // Creates inline transaction
            // Tx tx1 = api.createNewTx(player.getUniqueId(), to_wallet, 10, "Describe what it does here", Logic.purchaseBusinessLicense());

            Tx txinfo = new Tx(); // getTxID() -> auto generated. just a UUID [/wallet pending shows all]
            txinfo.setFromUUID(player.getUniqueId());
            txinfo.setToWallet(to_wallet);
            txinfo.setAmount(10);
            txinfo.setDescription("Describe what it does here");
            txinfo.setFunction(Logic.purchaseBusinessLicense());

            ErrorTypes error = api.submit(txinfo);

            if(error == ErrorTypes.NO_ERROR) {
                Util.colorMsg(sender, "Transaction created successfully " + txinfo.getTxID());
            } else {
                Util.colorMsg(player, "Error: " + error.toString());
            }
        } else {
            Util.colorMsg(player, "You do not have enough money to purchase a business license");
        }

        return true;
    }
    
}
