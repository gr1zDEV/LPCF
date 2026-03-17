package com.ezinnovations.ezchat.commands;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.model.MuteEntry;
import com.ezinnovations.ezchat.service.MuteService;
import com.ezinnovations.ezchat.utils.DurationParser;
import com.ezinnovations.ezchat.utils.TimeFormatUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class EzChatMuteInfoCommand {

    private final EzChat plugin;
    private final MuteService muteService;

    public EzChatMuteInfoCommand(final EzChat plugin, final MuteService muteService) {
        this.plugin = plugin;
        this.muteService = muteService;
    }

    public boolean execute(final CommandSender sender, final String[] args) {
        if (!sender.hasPermission("ezchat.muteinfo")) {
            sender.sendMessage(plugin.colorize(muteService.getMessage("no-permission", "&cYou do not have permission.")));
            return true;
        }
        if (!muteService.isFeatureEnabled()) {
            sender.sendMessage(plugin.colorize(muteService.getMessage("feature-disabled", "&cThat feature is currently disabled.")));
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage(plugin.colorize(muteService.getMessage("muteinfo-invalid-usage", "&cUsage: /ezchat muteinfo <player>")));
            return true;
        }

        final OfflinePlayer target = resolvePlayer(args[1]);
        if (target == null || target.getUniqueId() == null || (!target.isOnline() && !target.hasPlayedBefore())) {
            sender.sendMessage(plugin.colorize(muteService.getMessage("player-not-found", "&cPlayer not found.")));
            return true;
        }

        final Optional<MuteEntry> activeMute = muteService.getActiveMute(target.getUniqueId());
        if (activeMute.isEmpty()) {
            sender.sendMessage(plugin.colorize(muteService.getMessage("muteinfo-not-muted", "&cThat player is not muted.")));
            return true;
        }

        final MuteEntry muteEntry = activeMute.get();
        final String playerName = target.getName() != null ? target.getName() : args[1];
        sender.sendMessage(plugin.colorize(muteService.getMessage("muteinfo-header", "&8&m----------------")));
        sender.sendMessage(plugin.colorize(muteService.getMessage("muteinfo-player", "&ePlayer: &f{player}").replace("{player}", playerName)));
        sender.sendMessage(plugin.colorize(muteService.getMessage("muteinfo-status", "&eStatus: &f{status}").replace("{status}", "MUTED")));
        sender.sendMessage(plugin.colorize(muteService.getMessage("muteinfo-type", "&eType: &f{type}").replace("{type}", muteEntry.getMuteType().name())));
        sender.sendMessage(plugin.colorize(muteService.getMessage("muteinfo-reason", "&eReason: &f{reason}").replace("{reason}", muteEntry.getReason() == null ? "N/A" : muteEntry.getReason())));
        sender.sendMessage(plugin.colorize(muteService.getMessage("muteinfo-muted-by", "&eMuted By: &f{staff}").replace("{staff}", muteEntry.getMutedByName() == null ? "SYSTEM" : muteEntry.getMutedByName())));
        sender.sendMessage(plugin.colorize(muteService.getMessage("muteinfo-created-at", "&eCreated: &f{time}").replace("{time}", TimeFormatUtil.formatTimestamp(muteEntry.getCreatedAt()))));

        if (muteEntry.getExpiresAt() != null) {
            sender.sendMessage(plugin.colorize(muteService.getMessage("muteinfo-expires-at", "&eExpires: &f{time}").replace("{time}", TimeFormatUtil.formatTimestamp(muteEntry.getExpiresAt()))));
            final String remaining = DurationParser.formatDuration(muteEntry.getExpiresAt() - System.currentTimeMillis());
            sender.sendMessage(plugin.colorize(muteService.getMessage("muteinfo-remaining", "&eRemaining: &f{time}").replace("{time}", remaining)));
        }

        sender.sendMessage(plugin.colorize(muteService.getMessage("muteinfo-header", "&8&m----------------")));
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
