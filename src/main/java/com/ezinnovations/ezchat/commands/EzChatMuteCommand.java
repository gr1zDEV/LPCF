package com.ezinnovations.ezchat.commands;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.service.AuditLogService;
import com.ezinnovations.ezchat.service.MuteService;
import com.ezinnovations.ezchat.service.DiscordNotificationService;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class EzChatMuteCommand {

    private final EzChat plugin;
    private final MuteService muteService;
    private final AuditLogService auditLogService;
    private final DiscordNotificationService discordNotificationService;

    public EzChatMuteCommand(final EzChat plugin, final MuteService muteService, final AuditLogService auditLogService, final DiscordNotificationService discordNotificationService) {
        this.plugin = plugin;
        this.muteService = muteService;
        this.auditLogService = auditLogService;
        this.discordNotificationService = discordNotificationService;
    }

    public boolean execute(final CommandSender sender, final String[] args) {
        if (!sender.hasPermission("ezchat.mute")) {
            sender.sendMessage(plugin.colorize(muteService.getMessage("no-permission", "&cYou do not have permission.")));
            return true;
        }

        if (!muteService.isFeatureEnabled()) {
            sender.sendMessage(plugin.colorize(muteService.getMessage("feature-disabled", "&cThat feature is currently disabled.")));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(plugin.colorize("&cUsage: /ezchat mute <player>"));
            return true;
        }

        final OfflinePlayer target = resolvePlayer(args[1]);
        if (target == null || target.getUniqueId() == null || (!target.isOnline() && !target.hasPlayedBefore())) {
            sender.sendMessage(plugin.colorize(muteService.getMessage("player-not-found", "&cPlayer not found.")));
            return true;
        }

        if (muteService.isMuted(target.getUniqueId())) {
            sender.sendMessage(plugin.colorize(muteService.getMessage("already-muted", "&cThat player is already muted.")));
            return true;
        }

        final UUID actorUuid = sender instanceof Player player ? player.getUniqueId() : null;
        final String actorName = sender.getName();
        final String targetName = target.getName() != null ? target.getName() : args[1];

        if (!muteService.setPermanentMute(target.getUniqueId(), targetName, actorUuid, actorName, false)) {
            sender.sendMessage(plugin.colorize("&cFailed to save mute. Check console."));
            return true;
        }

        sender.sendMessage(plugin.colorize(muteService.getMessage("mute-success", "&aMuted {player}.")
                .replace("{player}", targetName)));
        auditLogService.log(actorUuid, actorName, "MUTE_SET", "muted " + targetName + " permanently");
        discordNotificationService.sendMuteAction(actorUuid, actorName, targetName, "", false);
        return true;
    }

    private OfflinePlayer resolvePlayer(final String input) {
        final Player online = plugin.getServer().getPlayerExact(input);
        if (online != null) {
            return online;
        }

        final OfflinePlayer cached = plugin.getServer().getOfflinePlayerIfCached(input);
        if (cached != null) {
            return cached;
        }

        for (final OfflinePlayer offline : plugin.getServer().getOfflinePlayers()) {
            if (offline.getName() != null && offline.getName().equalsIgnoreCase(input)) {
                return offline;
            }
        }
        return null;
    }
}
