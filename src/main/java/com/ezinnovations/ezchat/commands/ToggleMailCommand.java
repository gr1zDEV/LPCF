package com.ezinnovations.ezchat.commands;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.managers.ChatToggleManager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ToggleMailCommand implements CommandExecutor {

    private final EzChat plugin;
    private final ChatToggleManager chatToggleManager;

    public ToggleMailCommand(final EzChat plugin, final ChatToggleManager chatToggleManager) {
        this.plugin = plugin;
        this.chatToggleManager = chatToggleManager;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof final Player player)) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("players-only", "&cOnly players can use this command.")));
            return true;
        }

        if (!player.hasPermission("ezchat.togglemail")) {
            player.sendMessage(plugin.colorize(plugin.getConfig().getString("mail.no-permission", "&cYou do not have permission.")));
            return true;
        }

        final boolean nowDisabled = chatToggleManager.toggleMail(player.getUniqueId());
        if (nowDisabled) {
            player.sendMessage(plugin.colorize(plugin.getConfig().getString("mail.toggle-disabled", "&cYou will no longer receive mail.")));
        } else {
            player.sendMessage(plugin.colorize(plugin.getConfig().getString("mail.toggle-enabled", "&aYou can now receive mail.")));
        }
        return true;
    }
}
