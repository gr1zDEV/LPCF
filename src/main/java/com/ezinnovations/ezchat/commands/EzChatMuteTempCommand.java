package com.ezinnovations.ezchat.commands;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.service.AuditLogService;
import com.ezinnovations.ezchat.service.MuteService;
import com.ezinnovations.ezchat.service.DiscordNotificationService;
import com.ezinnovations.ezchat.service.StaffAlertService;
import com.ezinnovations.ezchat.utils.DurationParser;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.OptionalLong;
import java.util.UUID;

public final class EzChatMuteTempCommand {

    private final EzChat plugin;
    private final MuteService muteService;
    private final AuditLogService auditLogService;
    private final DiscordNotificationService discordNotificationService;
    private final StaffAlertService staffAlertService;

    public EzChatMuteTempCommand(final EzChat plugin, final MuteService muteService, final AuditLogService auditLogService, final DiscordNotificationService discordNotificationService, final StaffAlertService staffAlertService) {
        this.plugin = plugin;
        this.muteService = muteService;
        this.auditLogService = auditLogService;
        this.discordNotificationService = discordNotificationService;
        this.staffAlertService = staffAlertService;
    }

    public boolean execute(final CommandSender sender, final String[] args) {
        if (!sender.hasPermission("ezchat.mutetemp")) {
            sender.sendMessage(plugin.colorize(muteService.getMessage("no-permission", "&cYou do not have permission.")));
            return true;
        }

        if (!muteService.isFeatureEnabled()) {
            sender.sendMessage(plugin.colorize(muteService.getMessage("feature-disabled", "&cThat feature is currently disabled.")));
            return true;
        }

        if (args.length != 3) {
            sender.sendMessage(plugin.colorize("&cUsage: /ezchat mutetemp <player> <duration>"));
            return true;
        }

        final OptionalLong durationMillis = DurationParser.parseToMillis(args[2]);
        if (durationMillis.isEmpty()) {
            sender.sendMessage(plugin.colorize(muteService.getMessage("invalid-duration", "&cInvalid duration.")));
            return true;
        }

        final OfflinePlayer target = resolvePlayer(args[1]);
        if (target == null || target.getUniqueId() == null || (!target.isOnline() && !target.hasPlayedBefore())) {
            sender.sendMessage(plugin.colorize(muteService.getMessage("player-not-found", "&cPlayer not found.")));
            return true;
        }

        final UUID actorUuid = sender instanceof Player player ? player.getUniqueId() : null;
        final String actorName = sender.getName();
        final String targetName = target.getName() != null ? target.getName() : args[1];

        if (!muteService.setTemporaryMute(target.getUniqueId(), targetName, actorUuid, actorName, durationMillis.getAsLong(), true)) {
            sender.sendMessage(plugin.colorize("&cFailed to save mute. Check console."));
            return true;
        }

        sender.sendMessage(plugin.colorize(muteService.getMessage("temp-mute-success", "&aTemporarily muted {player} for {duration}.")
                .replace("{player}", targetName)
                .replace("{duration}", args[2])));
        auditLogService.log(actorUuid, actorName, "TEMP_MUTE_SET", "temp-muted " + targetName + " for " + args[2]);
        discordNotificationService.sendMuteAction(actorUuid, actorName, targetName, args[2], true);
        if (staffAlertService.isAlertsEnabled()) {
            staffAlertService.sendStaffAlert("Player temp-muted: " + targetName + " by " + actorName + " for " + args[2]);
        }
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
