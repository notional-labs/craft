package sh.reece.testingvault;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.api.IntegrationAPI;
import com.crafteconomy.blockchain.utils.Util;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;


public class Wallet implements CommandExecutor {

    IntegrationAPI api = CraftBlockchainPlugin.getAPI();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {    
        
        if(sender instanceof ConsoleCommandSender) {
            Util.colorMsg(sender, "Only players can use this command");
            return true;
        }

        Player player = (Player) sender;

        String wallet = api.getWallet(player.getUniqueId());
        if(wallet != null) {
            Util.colorMsg(sender, "Your wallet is: " + wallet);
        } else {
            Util.colorMsg(sender, "You do not have a wallet set");
        }

        return true;
    }
    
}
