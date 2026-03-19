package com.ezinnovations.ezchat.placeholder;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.config.PlaceholdersConfig;
import com.ezinnovations.ezchat.managers.ChatToggleManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class EzChatPlaceholderExpansion extends PlaceholderExpansion {

    private static final String FORMATTED_SUFFIX = "_formatted";

    private final EzChat plugin;
    private final ChatToggleManager chatToggleManager;
    private final PlaceholdersConfig placeholdersConfig;

    public EzChatPlaceholderExpansion(final EzChat plugin,
                                      final ChatToggleManager chatToggleManager,
                                      final PlaceholdersConfig placeholdersConfig) {
        this.plugin = plugin;
        this.chatToggleManager = chatToggleManager;
        this.placeholdersConfig = placeholdersConfig;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "ezchat";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(final Player player, @NotNull final String params) {
        if (player == null) {
            return null;
        }
        if (!placeholdersConfig.isFeatureEnabled() || !placeholdersConfig.areSettingsPlaceholdersEnabled()) {
            return null;
        }

        final boolean formatted = params.length() > FORMATTED_SUFFIX.length()
                && params.regionMatches(true, params.length() - FORMATTED_SUFFIX.length(), FORMATTED_SUFFIX, 0, FORMATTED_SUFFIX.length());
        final String baseParams = formatted
                ? params.substring(0, params.length() - FORMATTED_SUFFIX.length())
                : params;

        final EzChatSettingPlaceholder placeholder = EzChatSettingPlaceholder.from(baseParams);
        if (placeholder == null) {
            return null;
        }

        final boolean enabled = placeholder.resolve(chatToggleManager, player.getUniqueId());
        if (formatted) {
            return getFormattedToggleValue(enabled);
        }
        return getRawToggleValue(enabled);
    }

    private String getRawToggleValue(final boolean enabled) {
        return switch (placeholdersConfig.getRawMode()) {
            case "on-off" -> enabled ? "on" : "off";
            default -> enabled ? "true" : "false";
        };
    }

    private String getFormattedToggleValue(final boolean enabled) {
        final String configured = enabled
                ? placeholdersConfig.getFormattedTrueText()
                : placeholdersConfig.getFormattedFalseText();
        return plugin.renderConfigText(configured);
    }
}
