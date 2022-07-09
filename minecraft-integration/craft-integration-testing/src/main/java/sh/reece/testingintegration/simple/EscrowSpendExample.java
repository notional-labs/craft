package sh.reece.testingintegration.simple;

import java.util.UUID;

import com.crafteconomy.blockchain.CraftBlockchainPlugin;
import com.crafteconomy.blockchain.api.IntegrationAPI;
import com.crafteconomy.blockchain.escrow.EscrowErrors;
import com.crafteconomy.blockchain.utils.Util;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EscrowSpendExample implements CommandExecutor {

    IntegrationAPI api = CraftBlockchainPlugin.getAPI();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {    
        
        Player player = (Player) sender;

        ItemStack itemToGivePlayer = new ItemStack(Material.DIRT, 5);
        long cost = 1; // 10 craft

        EscrowErrors err = api.escrowCraftSpend(player.getUniqueId(), cost);
        if(err == EscrowErrors.SUCCESS) {
            player.getInventory().addItem(itemToGivePlayer);
            player.sendMessage("You have successfully spent " + cost + " escrow craft.");
            
        } else {
            player.sendMessage("You do not have enough escrow balance to spend " + cost + ".");
            player.sendMessage("You only have " + api.escrowGetCraftBalance(player.getUniqueId()) + "escrow craft.");
        }


        return true;
    }
}