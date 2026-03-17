package com.ezinnovations.ezchat.service;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.config.MuteConfig;
import com.ezinnovations.ezchat.database.repository.MuteRepository;
import com.ezinnovations.ezchat.model.MuteEntry;
import com.ezinnovations.ezchat.utils.DurationParser;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public final class MuteService {

    private final EzChat plugin;
    private final MuteConfig muteConfig;
    private final MuteRepository muteRepository;
    private final AuditLogService auditLogService;

    public MuteService(final EzChat plugin,
                       final MuteConfig muteConfig,
                       final MuteRepository muteRepository,
                       final AuditLogService auditLogService) {
        this.plugin = plugin;
        this.muteConfig = muteConfig;
        this.muteRepository = muteRepository;
        this.auditLogService = auditLogService;
    }

    public boolean isFeatureEnabled() {
        return muteConfig.isFeatureEnabled();
    }

    public Optional<MuteEntry> getActiveMute(final UUID playerUuid) {
        if (!isFeatureEnabled()) {
            return Optional.empty();
        }

        try {
            final Optional<MuteEntry> entry = muteRepository.findByPlayerUuid(playerUuid);
            if (entry.isEmpty()) {
                return Optional.empty();
            }

            final MuteEntry muteEntry = entry.get();
            if (muteEntry.isExpired(System.currentTimeMillis())) {
                muteRepository.delete(playerUuid);
                auditLogService.log(null, "SYSTEM", "MUTE_EXPIRED", "expired mute for " + muteEntry.getPlayerName());
                return Optional.empty();
            }
            return entry;
        } catch (final SQLException exception) {
            plugin.getLogger().warning("Failed to load mute entry: " + exception.getMessage());
            return Optional.empty();
        }
    }

    public boolean isMuted(final UUID playerUuid) {
        return getActiveMute(playerUuid).isPresent();
    }

    public String remainingMuteTime(final UUID playerUuid) {
        return getActiveMute(playerUuid)
                .map(entry -> entry.getExpiresAt() == null ? null : DurationParser.formatDuration(entry.getExpiresAt() - System.currentTimeMillis()))
                .orElse(null);
    }

    public boolean setPermanentMute(final UUID targetUuid,
                                    final String targetName,
                                    final UUID actorUuid,
                                    final String actorName,
                                    final String reason,
                                    final boolean replaceExisting) {
        return setMute(targetUuid, targetName, actorUuid, actorName, reason, null, replaceExisting, MuteEntry.MuteType.PERMANENT);
    }

    public boolean setTemporaryMute(final UUID targetUuid,
                                    final String targetName,
                                    final UUID actorUuid,
                                    final String actorName,
                                    final String reason,
                                    final long durationMillis,
                                    final boolean replaceExisting) {
        final long expiresAt = System.currentTimeMillis() + durationMillis;
        return setMute(targetUuid, targetName, actorUuid, actorName, reason, expiresAt, replaceExisting, MuteEntry.MuteType.TEMPORARY);
    }

    public boolean unmute(final UUID targetUuid) {
        try {
            muteRepository.delete(targetUuid);
            return true;
        } catch (final SQLException exception) {
            plugin.getLogger().warning("Failed to remove mute entry: " + exception.getMessage());
            return false;
        }
    }

    private boolean setMute(final UUID targetUuid,
                            final String targetName,
                            final UUID actorUuid,
                            final String actorName,
                            final String reason,
                            final Long expiresAt,
                            final boolean replaceExisting,
                            final MuteEntry.MuteType type) {
        if (!replaceExisting && isMuted(targetUuid)) {
            return false;
        }

        final MuteEntry entry = new MuteEntry(
                targetUuid,
                targetName,
                type,
                reason,
                System.currentTimeMillis(),
                expiresAt,
                actorUuid,
                actorName
        );

        try {
            muteRepository.upsert(entry);
            return true;
        } catch (final SQLException exception) {
            plugin.getLogger().warning("Failed to save mute entry: " + exception.getMessage());
            return false;
        }
    }

    public boolean blockPublicChat() { return muteConfig.blockPublicChat(); }
    public boolean blockPrivateMessages() { return muteConfig.blockPrivateMessages(); }
    public boolean blockMail() { return muteConfig.blockMail(); }

    public String getMessage(final String path, final String fallback) {
        return muteConfig.getMessage(path, fallback);
    }

    public void sendMuteBlockedMessage(final CommandSender sender, final String specificPath, final String specificFallback) {
        final UUID uuid = sender instanceof org.bukkit.entity.Player player ? player.getUniqueId() : null;
        String message = getMessage(specificPath, specificFallback);
        if (uuid != null) {
            final String remaining = remainingMuteTime(uuid);
            if (remaining != null) {
                message = getMessage("muted-until", "&cYou are muted for another {time}.").replace("{time}", remaining);
            }
        }
        sender.sendMessage(plugin.colorize(message));
    }
}
