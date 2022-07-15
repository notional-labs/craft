package sh.reece.testingintegration;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Logic {
    
    @SuppressWarnings("deprecation")
    public static Consumer<UUID> purchaseBusinessLicense() {
        Consumer<UUID> purchase = (uuid) -> {  

            String name = getNameIfOnline(uuid);                
            // put logic here for business license
            Bukkit.broadcastMessage("[COMPLETE] Business License for: " + name + " == " + uuid.toString() + "\n"); 

        };
        return purchase;
    }
    
    public static BiConsumer<UUID, UUID> trade(UUID from, UUID to, ItemStack p1Item, ItemStack p2Item, String description) {
        BiConsumer<UUID, UUID> trading = (FROM, TO) -> {
            // ~~simulates a trade between two players if they are online 
            // (offline would require data to be saved to database, currently just example)

            Player p1 = Bukkit.getPlayer(FROM);
            Player p2 = Bukkit.getPlayer(TO);

            if (p1 == null || p2 == null) {
                System.out.println("[ERROR] One or more players are offline");
                return;
            }

            p1.sendMessage(description);
            p2.sendMessage(description);

            // swaps items
            p1.getInventory().addItem(p2Item);
            p2.getInventory().addItem(p1Item);

            p1.updateInventory();
            p2.updateInventory();
        };
        return trading;
    }

    @SuppressWarnings("deprecation")
    public static Consumer<UUID> expireLogic() {
        Consumer<UUID> purchase = (uuid) -> {  
            Bukkit.broadcastMessage("[!] EXPIRE: The Transaction Expired before you could sign it, looks like this worked!\n"); 
        };
        return purchase;
    }


    private static String getNameIfOnline(UUID uuid) {
        String playername = "";
        // check if the UUID is online
        Player player = Bukkit.getPlayer(uuid);
        if(player != null) {
            playername = player.getName();
        }
        return playername;
    }

}
