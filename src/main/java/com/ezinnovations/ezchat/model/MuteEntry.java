package com.ezinnovations.ezchat.model;

import java.util.UUID;

public final class MuteEntry {

    public enum MuteType {
        PERMANENT,
        TEMPORARY
    }

    private final UUID playerUuid;
    private final String playerName;
    private final MuteType muteType;
    private final String reason;
    private final long createdAt;
    private final Long expiresAt;
    private final UUID mutedByUuid;
    private final String mutedByName;

    public MuteEntry(final UUID playerUuid,
                     final String playerName,
                     final MuteType muteType,
                     final String reason,
                     final long createdAt,
                     final Long expiresAt,
                     final UUID mutedByUuid,
                     final String mutedByName) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.muteType = muteType;
        this.reason = reason;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.mutedByUuid = mutedByUuid;
        this.mutedByName = mutedByName;
    }

    public UUID getPlayerUuid() { return playerUuid; }
    public String getPlayerName() { return playerName; }
    public MuteType getMuteType() { return muteType; }
    public String getReason() { return reason; }
    public long getCreatedAt() { return createdAt; }
    public Long getExpiresAt() { return expiresAt; }
    public UUID getMutedByUuid() { return mutedByUuid; }
    public String getMutedByName() { return mutedByName; }

    public boolean isExpired(final long now) {
        return expiresAt != null && expiresAt <= now;
    }
}
