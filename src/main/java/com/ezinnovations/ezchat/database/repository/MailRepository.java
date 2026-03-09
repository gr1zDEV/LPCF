package com.ezinnovations.ezchat.database.repository;

import com.ezinnovations.ezchat.database.DatabaseManager;
import com.ezinnovations.ezchat.model.MailEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class MailRepository {

    private final DatabaseManager databaseManager;

    public MailRepository(final DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void upsert(final MailEntry entry) throws SQLException {
        final String sql = """
                INSERT INTO mail(id, sender_uuid, receiver_uuid, sender_name, receiver_name, message, timestamp, read)
                VALUES(?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    sender_uuid=excluded.sender_uuid,
                    receiver_uuid=excluded.receiver_uuid,
                    sender_name=excluded.sender_name,
                    receiver_name=excluded.receiver_name,
                    message=excluded.message,
                    timestamp=excluded.timestamp,
                    read=excluded.read
                """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entry.getId());
            statement.setString(2, entry.getSender().toString());
            statement.setString(3, entry.getReceiver().toString());
            statement.setString(4, entry.getSenderName());
            statement.setString(5, entry.getReceiverName());
            statement.setString(6, entry.getMessage());
            statement.setLong(7, entry.getTimestamp());
            statement.setInt(8, entry.isRead() ? 1 : 0);
            statement.executeUpdate();
        }
    }

    public List<MailEntry> findInbox(final UUID playerUuid) throws SQLException {
        return findBy("receiver_uuid", playerUuid);
    }

    public List<MailEntry> findSent(final UUID playerUuid) throws SQLException {
        return findBy("sender_uuid", playerUuid);
    }

    public int getUnreadCount(final UUID playerUuid) throws SQLException {
        final String sql = "SELECT COUNT(*) FROM mail WHERE receiver_uuid = ? AND read = 0";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerUuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }
        return 0;
    }

    public void markAsRead(final UUID playerUuid, final List<String> ids) throws SQLException {
        if (ids.isEmpty()) {
            return;
        }

        final String sql = "UPDATE mail SET read = 1 WHERE receiver_uuid = ? AND id = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (final String id : ids) {
                statement.setString(1, playerUuid.toString());
                statement.setString(2, id);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    public boolean isEmpty() throws SQLException {
        final String sql = "SELECT COUNT(*) FROM mail";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() && resultSet.getInt(1) == 0;
        }
    }

    private List<MailEntry> findBy(final String column, final UUID playerUuid) throws SQLException {
        final List<MailEntry> entries = new ArrayList<>();
        final String sql = "SELECT id, sender_uuid, receiver_uuid, sender_name, receiver_name, message, timestamp, read FROM mail WHERE " + column + " = ? ORDER BY timestamp DESC";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerUuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    entries.add(new MailEntry(
                            resultSet.getString("id"),
                            UUID.fromString(resultSet.getString("sender_uuid")),
                            resultSet.getString("sender_name"),
                            UUID.fromString(resultSet.getString("receiver_uuid")),
                            resultSet.getString("receiver_name"),
                            resultSet.getString("message"),
                            resultSet.getLong("timestamp"),
                            resultSet.getInt("read") == 1
                    ));
                }
            }
        }

        return entries;
    }
}
