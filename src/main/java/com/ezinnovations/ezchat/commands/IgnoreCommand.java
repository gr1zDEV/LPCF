package com.ezinnovations.ezchat.commands;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.managers.FeatureManager;
import com.ezinnovations.ezchat.managers.IgnoreManager;
import com.ezinnovations.ezchat.service.AuditLogService;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;

public final class IgnoreCommand implements CommandExecutor {

    private final EzChat plugin;
    private final FeatureManager featureManager;
    private final IgnoreManager ignoreManager;
    private final AuditLogService auditLogService;

    public IgnoreCommand(final EzChat plugin, final FeatureManager featureManager, final IgnoreManager ignoreManager, final AuditLogService auditLogService) {
        this.plugin = plugin;
        this.featureManager = featureManager;
        this.ignoreManager = ignoreManager;
        this.auditLogService = auditLogService;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof final Player player)) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("players-only", "&cOnly players can use this command.")));
            return true;
        }

        if (!featureManager.isIgnoreEnabled()) {
            player.sendMessage(plugin.colorize(featureManager.getFeatureDisabledMessage()));
            return true;
        }

        if (!player.hasPermission("ezchat.ignore")) {
            player.sendMessage(plugin.colorize(plugin.getConfig().getString("ignore.no-permission", "&cYou do not have permission.")));
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(plugin.colorize("&cUsage: /ignore <player> <ALL|CHAT|MSG|MAIL>"));
            return true;
        }

        final OfflinePlayer target = resolveOfflinePlayer(args[0]);
        if (target == null || target.getUniqueId() == null || (!target.hasPlayedBefore() && !target.isOnline())) {
            player.sendMessage(plugin.colorize(plugin.getConfig().getString("ignore.player-not-found", "&cPlayer not found.")));
            return true;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(plugin.colorize(plugin.getConfig().getString("ignore.cannot-ignore-self", "&cYou cannot ignore yourself.")));
            return true;
        }

        final IgnoreManager.IgnoreType ignoreType;
        try {
            ignoreType = IgnoreManager.IgnoreType.valueOf(args[1].toUpperCase(Locale.ROOT));
        } catch (final IllegalArgumentException exception) {
            player.sendMessage(plugin.colorize("&cUsage: /ignore <player> <ALL|CHAT|MSG|MAIL>"));
            return true;
        }

        final boolean enabled = ignoreManager.toggleIgnore(player.getUniqueId(), target.getUniqueId(), ignoreType);
        ignoreManager.save();

        final String targetName = target.getName() != null ? target.getName() : args[0];
        if (!enabled) {
            auditLogService.log(player, "IGNORE_REMOVE", "removed ignore for " + targetName + " with type " + ignoreType.name());
            player.sendMessage(plugin.colorize(plugin.getConfig().getString("ignore.disabled", "&aYou are no longer ignoring {player}.")
                    .replace("{player}", targetName)));
            return true;
        }

        final String key;
        switch (ignoreType) {
            case CHAT -> key = "ignore.enabled-chat";
            case MSG -> key = "ignore.enabled-msg";
            case MAIL -> key = "ignore.enabled-mail";
            default -> key = "ignore.enabled-all";
        }

        auditLogService.log(player, "IGNORE_SET", "ignored " + targetName + " with type " + ignoreType.name());
        player.sendMessage(plugin.colorize(plugin.getConfig().getString(key, "&cYou are now ignoring {player}.")
                .replace("{player}", targetName)));
        return true;
    }

    private OfflinePlayer resolveOfflinePlayer(final String name) {
        final Player online = plugin.getServer().getPlayerExact(name);
        if (online != null) {
            return online;
        }

        final OfflinePlayer cached = plugin.getServer().getOfflinePlayerIfCached(name);
        if (cached != null && (cached.hasPlayedBefore() || cached.isOnline())) {
            return cached;
        }

        for (final OfflinePlayer offline : plugin.getServer().getOfflinePlayers()) {
            if (offline.getName() != null && offline.getName().equalsIgnoreCase(name)
                    && (offline.hasPlayedBefore() || offline.isOnline())) {
                return offline;
            }
        }

        return null;
    }
}
