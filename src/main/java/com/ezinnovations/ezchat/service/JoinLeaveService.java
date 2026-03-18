package com.ezinnovations.ezchat.service;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.config.JoinLeaveConfig;
import com.ezinnovations.ezchat.managers.ChatToggleManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class JoinLeaveService {

    private final EzChat plugin;
    private final JoinLeaveConfig joinLeaveConfig;
    private final ChatToggleManager chatToggleManager;
    private final CommunicationLogService communicationLogService;
    private final DiscordNotificationService discordNotificationService;

    public JoinLeaveService(final EzChat plugin,
                            final JoinLeaveConfig joinLeaveConfig,
                            final ChatToggleManager chatToggleManager,
                            final CommunicationLogService communicationLogService,
                            final DiscordNotificationService discordNotificationService) {
        this.plugin = plugin;
        this.joinLeaveConfig = joinLeaveConfig;
        this.chatToggleManager = chatToggleManager;
        this.communicationLogService = communicationLogService;
        this.discordNotificationService = discordNotificationService;
    }

    public boolean isFeatureEnabled() {
        return joinLeaveConfig.isFeatureEnabled();
    }

    public boolean isJoinLeaveMessagesEnabled(final UUID playerUuid) {
        if (chatToggleManager.hasToggleState(playerUuid)) {
            return !chatToggleManager.areJoinLeaveMessagesDisabled(playerUuid);
        }

        return joinLeaveConfig.isDefaultEnabled();
    }

    public boolean setJoinLeaveMessagesEnabled(final UUID playerUuid, final boolean enabled) {
        return chatToggleManager.setJoinLeaveMessagesDisabled(playerUuid, !enabled);
    }

    public boolean toggleJoinLeaveMessages(final UUID playerUuid) {
        final boolean nowDisabled = chatToggleManager.toggleJoinLeaveMessages(playerUuid);
        return !nowDisabled;
    }

    public void handleJoinMessage(final PlayerJoinEvent event) {
        if (!isFeatureEnabled()) {
            return;
        }

        final Player joinedPlayer = event.getPlayer();
        final String baseMessage = resolveJoinMessage(event, joinedPlayer);
        final String formatted = formatConfiguredMessage(
                "join-message",
                "&8[&aJoin&8] &f{message}",
                joinedPlayer,
                baseMessage
        );
        if (formatted.isBlank()) {
            return;
        }

        event.joinMessage(null);
        plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
            for (final Player online : plugin.getServer().getOnlinePlayers()) {
                if (!isJoinLeaveMessagesEnabled(online.getUniqueId())) {
                    continue;
                }

                online.sendMessage(formatted);
                playReceiveSound(online, "join", "ENTITY_EXPERIENCE_ORB_PICKUP");
            }
        });

        if (joinLeaveConfig.shouldLogJoinLeaveMessages()) {
            communicationLogService.logJoin(joinedPlayer.getUniqueId(), joinedPlayer.getName(), formatted);
        }
        discordNotificationService.sendJoinMessage(joinedPlayer.getUniqueId(), joinedPlayer.getName(), stripColors(formatted));
    }

    public void handleLeaveMessage(final PlayerQuitEvent event) {
        if (!isFeatureEnabled()) {
            return;
        }

        final Player leftPlayer = event.getPlayer();
        final String baseMessage = resolveLeaveMessage(event, leftPlayer);
        final String formatted = formatConfiguredMessage(
                "leave-message",
                "&8[&cLeave&8] &f{message}",
                leftPlayer,
                baseMessage
        );
        if (formatted.isBlank()) {
            return;
        }

        event.quitMessage(null);
        plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
            for (final Player online : plugin.getServer().getOnlinePlayers()) {
                if (!isJoinLeaveMessagesEnabled(online.getUniqueId())) {
                    continue;
                }

                online.sendMessage(formatted);
                playReceiveSound(online, "leave", "BLOCK_NOTE_BLOCK_BASS");
            }
        });

        if (joinLeaveConfig.shouldLogJoinLeaveMessages()) {
            communicationLogService.logLeave(leftPlayer.getUniqueId(), leftPlayer.getName(), formatted);
        }
        discordNotificationService.sendLeaveMessage(leftPlayer.getUniqueId(), leftPlayer.getName(), stripColors(formatted));
    }

    private String formatConfiguredMessage(final String formatKey,
                                           final String fallbackFormat,
                                           final Player player,
                                           final String originalMessage) {
        final String template = joinLeaveConfig.getFormat(formatKey, fallbackFormat);
        final Map<String, String> placeholders = new LinkedHashMap<>();
        placeholders.put("player", player.getName());
        placeholders.put("displayname", resolveDisplayName(player));
        placeholders.put("message", originalMessage == null ? "" : originalMessage);

        return plugin.colorize(applyPlaceholders(template, placeholders));
    }

    private String resolveDisplayName(final Player player) {
        final String displayName = player.getDisplayName();
        if (displayName == null || displayName.isBlank()) {
            return player.getName();
        }
        return displayName;
    }

    private String applyPlaceholders(final String template, final Map<String, String> placeholders) {
        String formatted = template == null ? "" : template;
        for (final Map.Entry<String, String> entry : placeholders.entrySet()) {
            formatted = formatted.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return formatted;
    }

    private String resolveJoinMessage(final PlayerJoinEvent event, final Player joinedPlayer) {
        if (joinLeaveConfig.useVanillaJoinMessage()) {
            final Component component = event.joinMessage();
            final String text = resolveComponentText(component);
            if (text != null && !text.isBlank()) {
                return text;
            }
        }

        return joinedPlayer.getName() + " joined the game";
    }

    private String resolveLeaveMessage(final PlayerQuitEvent event, final Player leftPlayer) {
        if (joinLeaveConfig.useVanillaLeaveMessage()) {
            final Component component = event.quitMessage();
            final String text = resolveComponentText(component);
            if (text != null && !text.isBlank()) {
                return text;
            }
        }

        return leftPlayer.getName() + " left the game";
    }

    private String resolveComponentText(final Component component) {
        if (component == null) {
            return null;
        }

        final String legacyText = LegacyComponentSerializer.legacySection().serialize(component);
        if (!legacyText.isBlank()) {
            return legacyText;
        }

        final String plainText = PlainTextComponentSerializer.plainText().serialize(component);
        if (!plainText.isBlank()) {
            return plainText;
        }

        return null;
    }

    private void playReceiveSound(final Player player, final String type, final String fallback) {
        if (!joinLeaveConfig.isSoundEnabled(type)) {
            return;
        }

        final Sound sound = joinLeaveConfig.getSound(type, fallback);
        if (sound == null) {
            return;
        }

        player.playSound(player, sound, joinLeaveConfig.getSoundVolume(type), joinLeaveConfig.getSoundPitch(type));
    }

    private String stripColors(final String input) {
        return input.replaceAll("(?i)§[0-9A-FK-ORX]", "");
    }
}
