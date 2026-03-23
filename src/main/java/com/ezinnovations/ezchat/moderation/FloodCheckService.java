package com.ezinnovations.ezchat.moderation;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.config.AntiFloodConfig;
import com.ezinnovations.ezchat.service.AuditLogService;
import com.ezinnovations.ezchat.service.DiscordNotificationService;
import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public final class FloodCheckService {

    private static final Pattern REPEATED_SPACE_PATTERN = Pattern.compile("\\s+");

    private final EzChat plugin;
    private final AntiFloodConfig antiFloodConfig;
    private final AuditLogService auditLogService;
    private final DiscordNotificationService discordNotificationService;
    private final Map<UUID, EnumMap<FloodChannel, PlayerFloodState>> floodStates = new ConcurrentHashMap<>();

    public FloodCheckService(final EzChat plugin,
                             final AntiFloodConfig antiFloodConfig,
                             final AuditLogService auditLogService,
                             final DiscordNotificationService discordNotificationService) {
        this.plugin = plugin;
        this.antiFloodConfig = antiFloodConfig;
        this.auditLogService = auditLogService;
        this.discordNotificationService = discordNotificationService;
    }

    public void reload() {
        floodStates.clear();
    }

    public boolean shouldCheckPublicChat() {
        return antiFloodConfig.isFeatureEnabled() && antiFloodConfig.shouldCheckPublicChat();
    }

    public boolean shouldCheckPrivateMessages() {
        return antiFloodConfig.isFeatureEnabled() && antiFloodConfig.shouldCheckPrivateMessages();
    }

    public boolean shouldCheckStaffChat() {
        return antiFloodConfig.isFeatureEnabled() && antiFloodConfig.shouldCheckStaffChat();
    }

    public boolean shouldBypass(final Player player) {
        return player.hasPermission(antiFloodConfig.getBypassPermission());
    }

    public FloodDetectionResult checkFlood(final UUID playerUuid,
                                           final String message,
                                           final FloodChannel channel) {
        if (!antiFloodConfig.isFeatureEnabled() || !isChannelEnabled(channel)) {
            return FloodDetectionResult.allowed();
        }

        final String normalizedMessage = normalizeMessage(message);
        final PlayerFloodState state = floodStates
                .computeIfAbsent(playerUuid, ignored -> new EnumMap<>(FloodChannel.class))
                .computeIfAbsent(channel, ignored -> new PlayerFloodState());

        return state.checkAndRecord(
                System.currentTimeMillis(),
                normalizedMessage,
                antiFloodConfig.isCooldownEnabled(),
                antiFloodConfig.getCooldownMilliseconds(),
                antiFloodConfig.isDuplicateEnabled(),
                antiFloodConfig.getDuplicateWindowMilliseconds(),
                antiFloodConfig.getMaxDuplicates(),
                antiFloodConfig.isBurstEnabled(),
                antiFloodConfig.getBurstWindowMilliseconds(),
                antiFloodConfig.getMaxMessages()
        );
    }

    public boolean handleBlockedMessage(final Player actor,
                                        final FloodChannel channel,
                                        final FloodDetectionResult detectionResult,
                                        final String originalMessage) {
        if (!detectionResult.blocked() || !antiFloodConfig.shouldBlockMessage()) {
            return false;
        }

        if (antiFloodConfig.shouldNotifySender()) {
            actor.sendMessage(plugin.colorize(resolveBlockedMessage(detectionResult.floodType())));
        }

        final String auditDetails = buildAuditDetails(channel, detectionResult.floodType(), originalMessage);
        if (antiFloodConfig.isAuditLogEnabled()) {
            auditLogService.log(actor, "FLOOD_BLOCK", auditDetails);
        }
        if (antiFloodConfig.isDiscordLogEnabled()) {
            discordNotificationService.sendAuditAction(actor.getUniqueId(), actor.getName(), auditDetails);
        }

        return true;
    }

    private boolean isChannelEnabled(final FloodChannel channel) {
        return switch (channel) {
            case PUBLIC -> antiFloodConfig.shouldCheckPublicChat();
            case MSG -> antiFloodConfig.shouldCheckPrivateMessages();
            case STAFF -> antiFloodConfig.shouldCheckStaffChat();
        };
    }

    private String normalizeMessage(final String message) {
        String normalized = message == null ? "" : message;
        if (antiFloodConfig.shouldTrimSpaces()) {
            normalized = normalized.trim();
            normalized = REPEATED_SPACE_PATTERN.matcher(normalized).replaceAll(" ");
        }
        if (antiFloodConfig.shouldNormalizeCase()) {
            normalized = normalized.toLowerCase(Locale.ROOT);
        }
        return normalized;
    }

    private String resolveBlockedMessage(final FloodType floodType) {
        return switch (floodType) {
            case COOLDOWN -> antiFloodConfig.getCooldownMessage();
            case DUPLICATE -> antiFloodConfig.getDuplicateMessage();
            case BURST -> antiFloodConfig.getBurstMessage();
        };
    }

    private String buildAuditDetails(final FloodChannel channel,
                                     final FloodType floodType,
                                     final String originalMessage) {
        String details = "blocked flood in " + channel + " (" + floodType + ")";
        if (antiFloodConfig.includeBlockedContentInAudit()) {
            details += ": " + originalMessage;
        }
        return details;
    }
}
