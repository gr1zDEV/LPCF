package com.ezinnovations.ezchat.listeners;

import com.ezinnovations.ezchat.service.DeathMessageService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public final class DeathMessageListener implements Listener {

    private final DeathMessageService deathMessageService;

    public DeathMessageListener(final DeathMessageService deathMessageService) {
        this.deathMessageService = deathMessageService;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDeath(final PlayerDeathEvent event) {
        deathMessageService.handleDeathMessage(event);
    }
}
