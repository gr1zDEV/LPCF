package com.ezinnovations.ezchat.moderation;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.config.ProfanityConfig;
import com.ezinnovations.ezchat.service.AuditLogService;
import com.ezinnovations.ezchat.service.DiscordNotificationService;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class ProfanityCheckService {

    public enum CommunicationType {
        PUBLIC,
        MSG,
        MAIL,
        STAFF
    }

    private final EzChat plugin;
    private final ProfanityConfig profanityConfig;
    private final AuditLogService auditLogService;
    private final DiscordNotificationService discordNotificationService;

    private final List<Pattern> wordPatterns = new ArrayList<>();
    private final List<Pattern> regexPatterns = new ArrayList<>();

    public ProfanityCheckService(final EzChat plugin,
                                 final ProfanityConfig profanityConfig,
                                 final AuditLogService auditLogService,
                                 final DiscordNotificationService discordNotificationService) {
        this.plugin = plugin;
        this.profanityConfig = profanityConfig;
        this.auditLogService = auditLogService;
        this.discordNotificationService = discordNotificationService;
        rebuildPatterns();
    }

    public void reload() {
        rebuildPatterns();
    }

    public boolean shouldScanPublicChat() {
        return profanityConfig.isFeatureEnabled() && profanityConfig.shouldScanPublicChat();
    }

    public boolean shouldScanPrivateMessages() {
        return profanityConfig.isFeatureEnabled() && profanityConfig.shouldScanPrivateMessages();
    }

    public boolean shouldScanMail() {
        return profanityConfig.isFeatureEnabled() && profanityConfig.shouldScanMail();
    }

    public boolean shouldScanStaffChat() {
        return profanityConfig.isFeatureEnabled() && profanityConfig.shouldScanStaffChat();
    }

    public ProfanityDetectionResult checkProfanity(final String message) {
        if (message == null || message.isBlank()) {
            return ProfanityDetectionResult.clear();
        }

        for (final Pattern wordPattern : wordPatterns) {
            final Matcher matcher = wordPattern.matcher(message);
            if (matcher.find()) {
                return ProfanityDetectionResult.flagged(ProfanityMatchType.WORD_LIST, matcher.group());
            }
        }

        for (final Pattern regexPattern : regexPatterns) {
            final Matcher matcher = regexPattern.matcher(message);
            if (matcher.find()) {
                return ProfanityDetectionResult.flagged(ProfanityMatchType.REGEX, matcher.group());
            }
        }

        return ProfanityDetectionResult.clear();
    }

    public boolean shouldBypass(final Player player) {
        return player.hasPermission(profanityConfig.getBypassPermission());
    }

    public boolean handleBlockedMessage(final Player actor,
                                        final CommunicationType communicationType,
                                        final ProfanityDetectionResult detectionResult,
                                        final String originalMessage) {
        if (!detectionResult.detected() || !profanityConfig.shouldBlockMessage()) {
            return false;
        }

        if (profanityConfig.shouldNotifySender()) {
            actor.sendMessage(plugin.colorize(profanityConfig.getBlockedMessage()));
        }

        final String auditDetails = buildAuditDetails(communicationType, detectionResult, originalMessage);
        if (profanityConfig.isAuditLogEnabled()) {
            auditLogService.log(actor, "PROFANITY_BLOCK", auditDetails);
        }
        if (profanityConfig.isDiscordLogEnabled()) {
            discordNotificationService.sendAuditAction(actor.getUniqueId(), actor.getName(), auditDetails);
        }

        return true;
    }

    private void rebuildPatterns() {
        wordPatterns.clear();
        regexPatterns.clear();

        if (profanityConfig.isWordListEnabled()) {
            for (final String blockedWord : profanityConfig.getBlockedWords()) {
                wordPatterns.add(Pattern.compile("\\b" + Pattern.quote(blockedWord) + "\\b", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
            }
        }

        if (profanityConfig.isRegexEnabled()) {
            for (final String regexPattern : profanityConfig.getRegexPatterns()) {
                try {
                    regexPatterns.add(Pattern.compile(regexPattern));
                } catch (final PatternSyntaxException exception) {
                    plugin.getLogger().warning("[EzChat] Skipping invalid profanity regex pattern '" + regexPattern + "': " + exception.getDescription());
                }
            }
        }
    }

    private String buildAuditDetails(final CommunicationType communicationType,
                                     final ProfanityDetectionResult detectionResult,
                                     final String originalMessage) {
        String details = "blocked profanity in " + communicationType
                + " (" + detectionResult.type() + "): " + detectionResult.match();

        if (profanityConfig.includeBlockedContentInAudit()) {
            details += " | content: " + originalMessage;
        }

        return details;
    }
}
