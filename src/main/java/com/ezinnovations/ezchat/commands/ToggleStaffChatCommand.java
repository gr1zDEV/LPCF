package com.ezinnovations.ezchat.commands;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.config.StaffConfig;
import com.ezinnovations.ezchat.service.StaffChatService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ToggleStaffChatCommand implements CommandExecutor {

    private final EzChat plugin;
    private final StaffConfig staffConfig;
    private final StaffChatService staffChatService;

    public ToggleStaffChatCommand(final EzChat plugin, final StaffConfig staffConfig, final StaffChatService staffChatService) {
        this.plugin = plugin;
        this.staffConfig = staffConfig;
        this.staffChatService = staffChatService;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.colorize("&cOnly players can use this command."));
            return true;
        }

        if (!staffChatService.isFeatureEnabled()) {
            player.sendMessage(plugin.colorize(staffConfig.getMessage("feature-disabled", "&cThat feature is currently disabled.")));
            return true;
        }

        if (!player.hasPermission("ezchat.staffchat.toggle")) {
            player.sendMessage(plugin.colorize(staffConfig.getMessage("no-permission", "&cYou do not have permission.")));
            return true;
        }

        final boolean enabled = staffChatService.toggleStaffChatMode(player.getUniqueId());
        if (enabled) {
            player.sendMessage(plugin.colorize(staffConfig.getMessage("toggle-enabled", "&aStaff chat mode enabled.")));
        } else {
            player.sendMessage(plugin.colorize(staffConfig.getMessage("toggle-disabled", "&cStaff chat mode disabled.")));
        }
        return true;
    }
}
