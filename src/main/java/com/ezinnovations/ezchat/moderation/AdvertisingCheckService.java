package com.ezinnovations.ezchat.moderation;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.config.AntiSpamConfig;
import com.ezinnovations.ezchat.service.AuditLogService;
import com.ezinnovations.ezchat.service.DiscordNotificationService;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class AdvertisingCheckService {

    public enum CommunicationType {
        PUBLIC,
        MSG,
        MAIL
    }

    private static final Pattern IPV4_PATTERN = Pattern.compile("(?<!\\d)(?:(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(?::\\d{1,5})?(?!\\d)");
    private static final Pattern DOMAIN_PATTERN = Pattern.compile("(?i)(?<![a-z0-9-])(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z]{2,24}(?::\\d{1,5})?(?:/[\\w\\-./?%&=+#]*)?");
    private static final Pattern DISCORD_INVITE_PATTERN = Pattern.compile("(?i)(?:https?://)?(?:www\\.)?(?:discord\\.gg/[a-z0-9-]+|discord(?:app)?\\.com/invite/[a-z0-9-]+)");

    private final EzChat plugin;
    private final AntiSpamConfig antiSpamConfig;
    private final AuditLogService auditLogService;
    private final DiscordNotificationService discordNotificationService;

    private final List<Pattern> customPatterns = new ArrayList<>();

    public AdvertisingCheckService(final EzChat plugin,
                                   final AntiSpamConfig antiSpamConfig,
                                   final AuditLogService auditLogService,
                                   final DiscordNotificationService discordNotificationService) {
        this.plugin = plugin;
        this.antiSpamConfig = antiSpamConfig;
        this.auditLogService = auditLogService;
        this.discordNotificationService = discordNotificationService;
        rebuildCustomPatterns();
    }

    public void reload() {
        rebuildCustomPatterns();
    }

    public boolean shouldScanPublicChat() {
        return antiSpamConfig.isFeatureEnabled() && antiSpamConfig.shouldScanPublicChat();
    }

    public boolean shouldScanPrivateMessages() {
        return antiSpamConfig.isFeatureEnabled() && antiSpamConfig.shouldScanPrivateMessages();
    }

    public boolean shouldScanMail() {
        return antiSpamConfig.isFeatureEnabled() && antiSpamConfig.shouldScanMail();
    }

    public AdvertisingDetectionResult checkAdvertising(final String message) {
        if (message == null || message.isBlank()) {
            return AdvertisingDetectionResult.clear();
        }

        if (antiSpamConfig.shouldBlockIpv4()) {
            final Matcher ipMatcher = IPV4_PATTERN.matcher(message);
            if (ipMatcher.find() && !isWhitelistedToken(ipMatcher.group())) {
                return AdvertisingDetectionResult.flagged(AdvertisingType.IP, ipMatcher.group());
            }
        }

        if (antiSpamConfig.shouldBlockDiscordInvites()) {
            final Matcher inviteMatcher = DISCORD_INVITE_PATTERN.matcher(message);
            if (inviteMatcher.find() && !isWhitelistedToken(inviteMatcher.group())) {
                return AdvertisingDetectionResult.flagged(AdvertisingType.DISCORD_INVITE, inviteMatcher.group());
            }
        }

        if (antiSpamConfig.shouldBlockDomains()) {
            final Matcher domainMatcher = DOMAIN_PATTERN.matcher(message);
            while (domainMatcher.find()) {
                final String domainValue = domainMatcher.group();
                if (!isWhitelistedDomain(domainValue) && !isWhitelistedToken(domainValue)) {
                    return AdvertisingDetectionResult.flagged(AdvertisingType.DOMAIN, domainValue);
                }
            }
        }

        for (final Pattern customPattern : customPatterns) {
            final Matcher customMatcher = customPattern.matcher(message);
            if (customMatcher.find()) {
                return AdvertisingDetectionResult.flagged(AdvertisingType.PATTERN, customMatcher.group());
            }
        }

        return AdvertisingDetectionResult.clear();
    }

    public boolean shouldBypass(final Player player) {
        return player.hasPermission(antiSpamConfig.getBypassPermission());
    }

    public boolean handleBlockedMessage(final Player actor,
                                        final CommunicationType communicationType,
                                        final AdvertisingDetectionResult detectionResult,
                                        final String originalMessage) {
        if (!detectionResult.detected() || !antiSpamConfig.shouldBlockMessage()) {
            return false;
        }

        if (antiSpamConfig.shouldNotifySender()) {
            actor.sendMessage(plugin.colorize(antiSpamConfig.getBlockedMessage()));
        }

        final String auditDetails = buildAuditDetails(actor, communicationType, detectionResult, originalMessage);
        if (antiSpamConfig.isAuditLogEnabled()) {
            auditLogService.log(actor, "ADVERTISING_BLOCK", auditDetails);
        }
        if (antiSpamConfig.isDiscordLogEnabled()) {
            discordNotificationService.sendAuditAction(actor.getUniqueId(), actor.getName(), auditDetails);
        }

        return true;
    }

    private void rebuildCustomPatterns() {
        customPatterns.clear();
        for (final String rawPattern : antiSpamConfig.getCustomBlockedPatterns()) {
            try {
                customPatterns.add(Pattern.compile(rawPattern, Pattern.CASE_INSENSITIVE));
            } catch (final PatternSyntaxException exception) {
                customPatterns.add(Pattern.compile(Pattern.quote(rawPattern), Pattern.CASE_INSENSITIVE));
            }
        }
    }

    private boolean isWhitelistedDomain(final String rawDomain) {
        final String normalized = normalizeHost(rawDomain);
        for (final String whitelistedDomain : antiSpamConfig.getWhitelistedDomains()) {
            final String allowed = normalizeHost(whitelistedDomain);
            if (normalized.equals(allowed) || normalized.endsWith('.' + allowed)) {
                return true;
            }
        }
        return false;
    }

    private boolean isWhitelistedToken(final String token) {
        final String normalized = normalizeHost(token);
        for (final String exact : antiSpamConfig.getWhitelistedExact()) {
            if (normalized.equals(normalizeHost(exact))) {
                return true;
            }
        }
        return false;
    }

    private String normalizeHost(final String input) {
        String normalized = input.toLowerCase(Locale.ROOT).trim();
        normalized = normalized.replaceFirst("^https?://", "");
        normalized = normalized.replaceFirst("^www\\.", "");
        normalized = normalized.replaceFirst("/.*$", "");
        normalized = normalized.replaceFirst(":\\d+$", "");
        return normalized;
    }

    private String buildAuditDetails(final Player actor,
                                     final CommunicationType communicationType,
                                     final AdvertisingDetectionResult detectionResult,
                                     final String originalMessage) {
        String details = "blocked advertising in " + communicationType
                + " (" + detectionResult.type() + "): " + detectionResult.match();

        if (antiSpamConfig.includeBlockedContentInAudit()) {
            details += " | content: " + originalMessage;
        }

        return details;
    }
}
