package com.ezinnovations.ezchat.listeners;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.managers.MailManager;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class PlayerJoinListener implements Listener {

    private final EzChat plugin;
    private final MailManager mailManager;

    public PlayerJoinListener(final EzChat plugin, final MailManager mailManager) {
        this.plugin = plugin;
        this.mailManager = mailManager;
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        if (!plugin.getConfig().getBoolean("mail.unread-login-notify.enabled", true)) {
            return;
        }

        final Player player = event.getPlayer();
        final int unreadCount = mailManager.getUnreadCount(player.getUniqueId());
        if (unreadCount <= 0) {
            return;
        }

        final String message = plugin.getConfig().getString("mail.unread-login-notify.message", "&eYou have {count} unread mail(s). Use /mail inbox")
                .replace("{count}", String.valueOf(unreadCount));
        player.sendMessage(plugin.colorize(message));
    }
}
