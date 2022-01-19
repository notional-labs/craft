package sh.reece.testingintegration.simple;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.api.IntegrationAPI;
import com.crafteconomy.blockchain.utils.Util;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;


public class Balance implements CommandExecutor {

    IntegrationAPI api = CraftBlockchainPlugin.getAPI();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {    
        
        if(sender instanceof ConsoleCommandSender) {
            Util.colorMsg(sender, "Only players can use this command");
            return true;
        }

        Player player = (Player) sender;

        long balance = api.getBalance(player.getUniqueId());
        if(balance >= 0) {
            Util.colorMsg(player, "Your balance is: " + balance);
        } else {
            Util.colorMsg(player, "An error occured while fetching your balance");
            // ErrorTypes.values() && ErrorTypes.NETWORK_ERROR.code
        }

        return true;
    }
    
}
