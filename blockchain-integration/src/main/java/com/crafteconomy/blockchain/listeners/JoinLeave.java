package com.crafteconomy.blockchain.listeners;

import com.crafteconomy.blockchain.wallets.WalletManager;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinLeave implements Listener {
    
    private WalletManager walletManager;

    public JoinLeave() {        
        walletManager = WalletManager.getInstance();   
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        walletManager.cacheWalletOnJoin(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeave(PlayerQuitEvent e) {
        walletManager.removeFromCache(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeave(PlayerKickEvent e) {
        walletManager.removeFromCache(e.getPlayer().getUniqueId());
    }

}
