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

public final class UnignoreCommand implements CommandExecutor {

    private final EzChat plugin;
    private final FeatureManager featureManager;
    private final IgnoreManager ignoreManager;
    private final AuditLogService auditLogService;

    public UnignoreCommand(final EzChat plugin, final FeatureManager featureManager, final IgnoreManager ignoreManager, final AuditLogService auditLogService) {
        this.plugin = plugin;
        this.featureManager = featureManager;
        this.ignoreManager = ignoreManager;
        this.auditLogService = auditLogService;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player player)) {
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
            player.sendMessage(plugin.colorize(plugin.getConfig().getString("ignore.unignore-invalid-usage", "&cUsage: /unignore <player> <ALL|CHAT|MSG|MAIL|ALL_TYPES>")));
            return true;
        }

        final OfflinePlayer target = resolveOfflinePlayer(args[0]);
        if (target == null || target.getUniqueId() == null || (!target.hasPlayedBefore() && !target.isOnline())) {
            player.sendMessage(plugin.colorize(plugin.getConfig().getString("ignore.player-not-found", "&cPlayer not found.")));
            return true;
        }

        final String mode = args[1].toUpperCase(Locale.ROOT);
        final boolean removed;
        final String shownType;
        if ("ALL_TYPES".equals(mode)) {
            removed = ignoreManager.unignoreAllTypes(player.getUniqueId(), target.getUniqueId());
            shownType = "ALL_TYPES";
        } else {
            final IgnoreManager.IgnoreType type;
            try {
                type = IgnoreManager.IgnoreType.valueOf(mode);
            } catch (final IllegalArgumentException ignored) {
                player.sendMessage(plugin.colorize(plugin.getConfig().getString("ignore.unignore-invalid-usage", "&cUsage: /unignore <player> <ALL|CHAT|MSG|MAIL|ALL_TYPES>")));
                return true;
            }
            removed = ignoreManager.unignore(player.getUniqueId(), target.getUniqueId(), type);
            shownType = type.name();
        }

        if (!removed) {
            player.sendMessage(plugin.colorize(plugin.getConfig().getString("ignore.unignore-not-found", "&cNo matching ignore entry found.")));
            return true;
        }

        final String targetName = target.getName() != null ? target.getName() : args[0];
        player.sendMessage(plugin.colorize(plugin.getConfig().getString("ignore.unignore-success", "&aYou are no longer ignoring {player} for {type}.")
                .replace("{player}", targetName)
                .replace("{type}", shownType)));
        auditLogService.log(player, "UNIGNORE", "unignored " + targetName + " type=" + shownType);
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
