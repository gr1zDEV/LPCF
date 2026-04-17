package com.ezinnovations.ezchat.listeners;

import com.ezinnovations.ezchat.service.JoinLeaveService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class JoinLeaveMessageListener implements Listener {

    private final JoinLeaveService joinLeaveService;

    public JoinLeaveMessageListener(final JoinLeaveService joinLeaveService) {
        this.joinLeaveService = joinLeaveService;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        joinLeaveService.handleJoinMessage(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        joinLeaveService.handleLeaveMessage(event);
    }
}
