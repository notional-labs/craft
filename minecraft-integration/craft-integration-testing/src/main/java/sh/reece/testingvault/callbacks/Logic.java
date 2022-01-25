package sh.reece.testingvault.callbacks;

import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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

    // TODO: Not done yet
    // @SuppressWarnings("deprecation")
    // public static BiConsumer<UUID, UUID> trade() {
    //     BiConsumer<UUID, UUID> trading = (FROM, TO) -> {
    //         Bukkit.broadcastMessage("Trading from: " + FROM + " to " + TO + "\n");    
    //     };
    //     return trading;
    // }


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
