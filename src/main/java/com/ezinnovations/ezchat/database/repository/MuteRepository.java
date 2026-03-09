package com.ezinnovations.ezchat.database.repository;

import com.ezinnovations.ezchat.database.DatabaseManager;
import com.ezinnovations.ezchat.model.MuteEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public final class MuteRepository {

    private final DatabaseManager databaseManager;

    public MuteRepository(final DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public Optional<MuteEntry> findByPlayerUuid(final UUID playerUuid) throws SQLException {
        final String sql = "SELECT player_uuid, player_name, mute_type, reason, created_at, expires_at, muted_by_uuid, muted_by_name FROM mutes WHERE player_uuid = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerUuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                final String mutedByUuidRaw = resultSet.getString("muted_by_uuid");
                return Optional.of(new MuteEntry(
                        UUID.fromString(resultSet.getString("player_uuid")),
                        resultSet.getString("player_name"),
                        MuteEntry.MuteType.valueOf(resultSet.getString("mute_type")),
                        resultSet.getString("reason"),
                        resultSet.getLong("created_at"),
                        resultSet.getObject("expires_at") == null ? null : resultSet.getLong("expires_at"),
                        mutedByUuidRaw == null ? null : UUID.fromString(mutedByUuidRaw),
                        resultSet.getString("muted_by_name")
                ));
            }
        }
    }

    public void upsert(final MuteEntry entry) throws SQLException {
        final String sql = """
                INSERT INTO mutes(player_uuid, player_name, mute_type, reason, created_at, expires_at, muted_by_uuid, muted_by_name)
                VALUES(?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(player_uuid) DO UPDATE SET
                    player_name=excluded.player_name,
                    mute_type=excluded.mute_type,
                    reason=excluded.reason,
                    created_at=excluded.created_at,
                    expires_at=excluded.expires_at,
                    muted_by_uuid=excluded.muted_by_uuid,
                    muted_by_name=excluded.muted_by_name
                """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entry.getPlayerUuid().toString());
            statement.setString(2, entry.getPlayerName());
            statement.setString(3, entry.getMuteType().name());
            statement.setString(4, entry.getReason());
            statement.setLong(5, entry.getCreatedAt());
            if (entry.getExpiresAt() == null) {
                statement.setNull(6, java.sql.Types.BIGINT);
            } else {
                statement.setLong(6, entry.getExpiresAt());
            }
            statement.setString(7, entry.getMutedByUuid() != null ? entry.getMutedByUuid().toString() : null);
            statement.setString(8, entry.getMutedByName());
            statement.executeUpdate();
        }
    }

    public void delete(final UUID playerUuid) throws SQLException {
        final String sql = "DELETE FROM mutes WHERE player_uuid = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerUuid.toString());
            statement.executeUpdate();
        }
    }
}
