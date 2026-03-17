package com.ezinnovations.ezchat.commands;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.managers.FeatureManager;
import com.ezinnovations.ezchat.managers.IgnoreManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class IgnoreListCommand implements CommandExecutor {

    private final EzChat plugin;
    private final FeatureManager featureManager;
    private final IgnoreManager ignoreManager;

    public IgnoreListCommand(final EzChat plugin, final FeatureManager featureManager, final IgnoreManager ignoreManager) {
        this.plugin = plugin;
        this.featureManager = featureManager;
        this.ignoreManager = ignoreManager;
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

        final List<IgnoreManager.IgnoreEntry> entries = ignoreManager.getIgnoreEntries(player.getUniqueId());
        if (entries.isEmpty()) {
            player.sendMessage(plugin.colorize(plugin.getConfig().getString("ignore.ignorelist-empty", "&7You are not ignoring anyone.")));
            return true;
        }

        player.sendMessage(plugin.colorize("&6&lIgnore List"));
        for (final IgnoreManager.IgnoreEntry entry : entries) {
            final OfflinePlayer target = plugin.getServer().getOfflinePlayer(entry.targetUuid());
            final String name = target.getName() == null ? entry.targetUuid().toString() : target.getName();
            player.sendMessage(plugin.colorize("&e- &f" + name + " &7(" + entry.ignoreType().name() + ")"));
        }
        return true;
    }
}
