package com.ezinnovations.ezchat.commands;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.model.MuteEntry;
import com.ezinnovations.ezchat.service.AuditLogService;
import com.ezinnovations.ezchat.service.DiscordNotificationService;
import com.ezinnovations.ezchat.service.MuteService;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public final class EzChatUnmuteCommand {

    private final EzChat plugin;
    private final MuteService muteService;
    private final AuditLogService auditLogService;
    private final DiscordNotificationService discordNotificationService;

    public EzChatUnmuteCommand(final EzChat plugin,
                               final MuteService muteService,
                               final AuditLogService auditLogService,
                               final DiscordNotificationService discordNotificationService) {
        this.plugin = plugin;
        this.muteService = muteService;
        this.auditLogService = auditLogService;
        this.discordNotificationService = discordNotificationService;
    }

    public boolean execute(final CommandSender sender, final String[] args) {
        if (!sender.hasPermission("ezchat.unmute")) {
            sender.sendMessage(plugin.colorize(muteService.getMessage("no-permission", "&cYou do not have permission.")));
            return true;
        }
        if (!muteService.isFeatureEnabled()) {
            sender.sendMessage(plugin.colorize(muteService.getMessage("feature-disabled", "&cThat feature is currently disabled.")));
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(plugin.colorize(muteService.getMessage("unmute-invalid-usage", "&cUsage: /ezchat unmute <player> [reason]")));
            return true;
        }

        final OfflinePlayer target = resolvePlayer(args[1]);
        if (target == null || target.getUniqueId() == null || (!target.isOnline() && !target.hasPlayedBefore())) {
            sender.sendMessage(plugin.colorize(muteService.getMessage("player-not-found", "&cPlayer not found.")));
            return true;
        }

        final Optional<MuteEntry> activeMute = muteService.getActiveMute(target.getUniqueId());
        if (activeMute.isEmpty()) {
            sender.sendMessage(plugin.colorize(muteService.getMessage("unmute-not-muted", "&cThat player is not muted.")));
            return true;
        }

        if (!muteService.unmute(target.getUniqueId())) {
            sender.sendMessage(plugin.colorize("&cFailed to unmute. Check console."));
            return true;
        }

        final String targetName = target.getName() != null ? target.getName() : args[1];
        final String reason = args.length >= 3 ? Arrays.stream(args).skip(2).reduce((a, b) -> a + " " + b).orElse("") : "";
        final UUID actorUuid = sender instanceof Player player ? player.getUniqueId() : null;
        final String actorName = sender.getName();

        sender.sendMessage(plugin.colorize(muteService.getMessage("unmute-success", "&aUnmuted {player}.").replace("{player}", targetName)));
        final String details = reason.isBlank()
                ? "unmuted " + targetName
                : "unmuted " + targetName + " reason=" + reason;
        auditLogService.log(actorUuid, actorName, "UNMUTE", details);
        discordNotificationService.sendAuditAction(actorUuid, actorName, "UNMUTE " + details);
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
