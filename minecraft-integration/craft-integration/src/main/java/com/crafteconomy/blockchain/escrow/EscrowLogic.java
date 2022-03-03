package com.crafteconomy.blockchain.escrow;

import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class EscrowLogic {

    private static EscrowManager escrow = EscrowManager.getInstance();   

    /**
     * On successful signing, this function will be called to update the balance of the player
     * @param player_uuid
     * @param amount
     * @return Consumer<UUID>
     */
    public static Consumer<UUID> depositEscrow(UUID player_uuid, long amount) {        
        Consumer<UUID> deposit = (uuid) -> {  
            escrow.changeBalance(player_uuid, +amount);

            Player player = Bukkit.getPlayer(player_uuid);
            if(player != null) {
                player.sendMessage("You have deposited " + amount + "CRAFT into your escrow account.");
            }        
        };
        return deposit;
    }    
}
