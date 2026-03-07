package com.ezinnovations.ezchat.managers;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MessageManager {

    private final Map<UUID, UUID> lastMessagedPlayers = new ConcurrentHashMap<>();

    public void updateConversation(final Player sender, final Player receiver) {
        lastMessagedPlayers.put(sender.getUniqueId(), receiver.getUniqueId());
        lastMessagedPlayers.put(receiver.getUniqueId(), sender.getUniqueId());
    }

    public UUID getReplyTarget(final Player player) {
        return lastMessagedPlayers.get(player.getUniqueId());
    }
}
