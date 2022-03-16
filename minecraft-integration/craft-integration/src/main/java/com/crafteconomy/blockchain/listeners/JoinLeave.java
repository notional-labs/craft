package com.crafteconomy.blockchain.listeners;

import java.util.UUID;

import com.crafteconomy.blockchain.core.request.Caches;
import com.crafteconomy.blockchain.core.types.RequestTypes;
import com.crafteconomy.blockchain.escrow.EscrowManager;
import com.crafteconomy.blockchain.wallets.WalletManager;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinLeave implements Listener {
    
    private WalletManager walletManager = WalletManager.getInstance();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        walletManager.cacheWalletOnJoin(uuid);
        EscrowManager.getInstance().loadCachedPlayer(uuid);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeave(PlayerQuitEvent e) {
        unloadPlayer(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onKick(PlayerKickEvent e) {
        unloadPlayer(e.getPlayer().getUniqueId());
    }

    private void unloadPlayer(UUID uuid) {
        walletManager.removeFromCache(uuid);
        EscrowManager.getInstance().unloadCachedPlayer(uuid);
    }

}
