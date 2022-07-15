package sh.reece.testingintegration.simple;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.api.IntegrationAPI;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CraftUSDPrice implements CommandExecutor {

    IntegrationAPI api = CraftBlockchainPlugin.getAPI();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {    
        
        Player player = (Player) sender;

        api.getCraftUSDPrice().thenAccept(price -> {
            player.sendMessage("The current craft USD price is: $" + price);
        });

        return true;
    }
    
}
