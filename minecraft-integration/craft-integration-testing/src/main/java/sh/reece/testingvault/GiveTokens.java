package sh.reece.testingvault;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.api.IntegrationAPI;
import com.crafteconomy.blockchain.utils.Util;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class GiveTokens implements CommandExecutor {

    IntegrationAPI api = CraftBlockchainPlugin.getAPI();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {    
        
        if(sender instanceof Player) {
            Util.colorMsg(sender, "Only console can use this command");
            // security feature, we will whitelist the TokenFaucet (:4500) to our servers only
            return true;
        }

        if(args.length != 2) {
            Util.colorMsg(sender, "Usage: /tokensapi <player> <amount>");
            return true;
        }

        // Player player = (Player) sender;

        // SINCE CONSOLE CAN ONLY SEND COMMANDS TO PLAYERS, YOU MUST PASS THE SENDER THROUGH TO ENSURE

        Player player = Bukkit.getPlayer(args[0]);

        String value = api.deposit(sender, player.getUniqueId(), Long.valueOf(args[1]));

        if(value != null) {
            Util.colorMsg(sender, "Deposited " + args[1] + " to " + player.getName() + " wallet");
            Util.colorMsg(player, "Console just gave you " + args[1] + " tokens");

        } else {
            // Console only
            Util.logSevere("Error: This command can only be used by console. (("+args[0]+", ))");
        }

        return true;
    }
    
}
