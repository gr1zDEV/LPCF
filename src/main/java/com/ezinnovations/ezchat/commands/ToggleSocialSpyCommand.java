package com.ezinnovations.ezchat.commands;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.service.AuditLogService;
import com.ezinnovations.ezchat.service.SpyService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ToggleSocialSpyCommand implements CommandExecutor {

    private final EzChat plugin;
    private final SpyService spyService;
    private final AuditLogService auditLogService;

    public ToggleSocialSpyCommand(final EzChat plugin, final SpyService spyService, final AuditLogService auditLogService) {
        this.plugin = plugin;
        this.spyService = spyService;
        this.auditLogService = auditLogService;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("players-only", "&cOnly players can use this command.")));
            return true;
        }
        if (!player.hasPermission("ezchat.socialspy")) {
            player.sendMessage(plugin.colorize(plugin.getConfigManager().getStaffConfig().getMessage("no-permission", "&cYou do not have permission.")));
            return true;
        }

        final boolean enabled = spyService.toggleSocialSpy(player);
        final String key = enabled ? "social-spy-enabled" : "social-spy-disabled";
        final String fallback = enabled ? "&aSocial spy enabled." : "&cSocial spy disabled.";
        player.sendMessage(plugin.colorize(plugin.getConfigManager().getStaffConfig().getMessage(key, fallback)));
        auditLogService.log(player, "SOCIAL_SPY_TOGGLE", "social spy " + (enabled ? "enabled" : "disabled"));
        return true;
    }
}
