package com.ezinnovations.ezchat.database.repository;

import com.ezinnovations.ezchat.database.DatabaseManager;
import com.ezinnovations.ezchat.managers.IgnoreManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class IgnoreRepository {

    private final DatabaseManager databaseManager;

    public IgnoreRepository(final DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public Map<UUID, Map<UUID, IgnoreManager.IgnoreType>> loadAll() throws SQLException {
        final Map<UUID, Map<UUID, IgnoreManager.IgnoreType>> data = new ConcurrentHashMap<>();
        final String sql = "SELECT owner_uuid, target_uuid, ignore_type FROM ignores";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                final UUID ownerUuid = UUID.fromString(resultSet.getString("owner_uuid"));
                final UUID targetUuid = UUID.fromString(resultSet.getString("target_uuid"));
                final IgnoreManager.IgnoreType type = IgnoreManager.IgnoreType.valueOf(resultSet.getString("ignore_type"));

                data.computeIfAbsent(ownerUuid, ignored -> new ConcurrentHashMap<>()).put(targetUuid, type);
            }
        }

        return data;
    }

    public void setIgnore(final UUID ownerUuid, final UUID targetUuid, final IgnoreManager.IgnoreType type) throws SQLException {
        final String deleteSql = "DELETE FROM ignores WHERE owner_uuid = ? AND target_uuid = ?";
        final String insertSql = "INSERT INTO ignores(owner_uuid, target_uuid, ignore_type) VALUES(?, ?, ?)";

        try (Connection connection = databaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSql);
                 PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
                deleteStatement.setString(1, ownerUuid.toString());
                deleteStatement.setString(2, targetUuid.toString());
                deleteStatement.executeUpdate();

                insertStatement.setString(1, ownerUuid.toString());
                insertStatement.setString(2, targetUuid.toString());
                insertStatement.setString(3, type.name());
                insertStatement.executeUpdate();

                connection.commit();
            } catch (final SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void removeIgnore(final UUID ownerUuid, final UUID targetUuid) throws SQLException {
        final String deleteSql = "DELETE FROM ignores WHERE owner_uuid = ? AND target_uuid = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(deleteSql)) {
            statement.setString(1, ownerUuid.toString());
            statement.setString(2, targetUuid.toString());
            statement.executeUpdate();
        }
    }

    public boolean isEmpty() throws SQLException {
        final String sql = "SELECT COUNT(*) FROM ignores";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() && resultSet.getInt(1) == 0;
        }
    }
}
