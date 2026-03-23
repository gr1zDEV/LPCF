package com.ezinnovations.ezchat.metrics;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.managers.ConfigManager;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;

public final class EzChatMetrics {

    private static final int PLUGIN_ID = 28547;

    private EzChatMetrics() {
    }

    public static void initialize(final EzChat plugin, final ConfigManager configManager) {
        final Metrics metrics = new Metrics(plugin, PLUGIN_ID);

        metrics.addCustomChart(new SimplePie("discord_support", () -> enabledDisabled(configManager.getDiscordConfig().isFeatureEnabled())));
        metrics.addCustomChart(new SimplePie("placeholderapi_installed", () -> enabledDisabled(plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI"))));
        metrics.addCustomChart(new SimplePie("anti_spam_enabled", () -> enabledDisabled(configManager.getAntiSpamConfig().isFeatureEnabled())));
        metrics.addCustomChart(new SimplePie("anti_flood_enabled", () -> enabledDisabled(configManager.getAntiFloodConfig().isFeatureEnabled())));
        metrics.addCustomChart(new SimplePie("logs_enabled", () -> enabledDisabled(configManager.getLogsConfig().isLoggingEnabled())));
    }

    private static String enabledDisabled(final boolean enabled) {
        return enabled ? "enabled" : "disabled";
    }
}
