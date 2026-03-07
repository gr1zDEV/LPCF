package com.ezinnovations.ezchat.commands;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.managers.ChatToggleManager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ChatToggleCommand implements CommandExecutor {

    private final EzChat plugin;
    private final ChatToggleManager chatToggleManager;

    public ChatToggleCommand(final EzChat plugin, final ChatToggleManager chatToggleManager) {
        this.plugin = plugin;
        this.chatToggleManager = chatToggleManager;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof final Player player)) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("players-only", "&cOnly players can use this command.")));
            return true;
        }

        if (!player.hasPermission("lpc.chattoggle")) {
            player.sendMessage(plugin.colorize(plugin.getConfig().getString("no-permission", "&cYou do not have permission.")));
            return true;
        }

        final boolean nowHidden = chatToggleManager.toggleChat(player.getUniqueId());
        if (nowHidden) {
            player.sendMessage(plugin.colorize(plugin.getConfig().getString("chat-toggle-disabled", "&cChat is now hidden.")));
        } else {
            player.sendMessage(plugin.colorize(plugin.getConfig().getString("chat-toggle-enabled", "&aChat is now visible.")));
        }

        return true;
    }
}
