package com.ezinnovations.ezchat.database.repository;

import com.ezinnovations.ezchat.database.DatabaseManager;
import com.ezinnovations.ezchat.model.CommunicationLogEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class CommunicationLogRepository {

    private final DatabaseManager databaseManager;

    public CommunicationLogRepository(final DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void insert(final CommunicationLogEntry entry) throws SQLException {
        final String sql = """
                INSERT INTO communication_logs (log_type, sender_uuid, sender_name, receiver_uuid, receiver_name, message, timestamp)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entry.logType());
            statement.setString(2, entry.senderUuid());
            statement.setString(3, entry.senderName());
            statement.setString(4, entry.receiverUuid());
            statement.setString(5, entry.receiverName());
            statement.setString(6, entry.message());
            statement.setLong(7, entry.timestamp());
            statement.executeUpdate();
        }
    }

    public List<CommunicationLogEntry> findByPlayer(final String playerName, final int page, final int pageSize) throws SQLException {
        final String sql = """
                SELECT * FROM communication_logs
                WHERE LOWER(sender_name) = LOWER(?) OR LOWER(receiver_name) = LOWER(?)
                ORDER BY timestamp DESC
                LIMIT ? OFFSET ?
                """;
        return queryPaged(sql, statement -> {
            statement.setString(1, playerName);
            statement.setString(2, playerName);
            statement.setInt(3, pageSize);
            statement.setInt(4, (page - 1) * pageSize);
        });
    }

    public int countByPlayer(final String playerName) throws SQLException {
        final String sql = """
                SELECT COUNT(*) FROM communication_logs
                WHERE LOWER(sender_name) = LOWER(?) OR LOWER(receiver_name) = LOWER(?)
                """;
        return count(sql, statement -> {
            statement.setString(1, playerName);
            statement.setString(2, playerName);
        });
    }

    public List<CommunicationLogEntry> findBetween(final String one, final String two, final int page, final int pageSize) throws SQLException {
        final String sql = """
                SELECT * FROM communication_logs
                WHERE log_type IN ('MSG', 'MAIL')
                  AND ((LOWER(sender_name) = LOWER(?) AND LOWER(receiver_name) = LOWER(?))
                    OR (LOWER(sender_name) = LOWER(?) AND LOWER(receiver_name) = LOWER(?)))
                ORDER BY timestamp DESC
                LIMIT ? OFFSET ?
                """;
        return queryPaged(sql, statement -> {
            statement.setString(1, one);
            statement.setString(2, two);
            statement.setString(3, two);
            statement.setString(4, one);
            statement.setInt(5, pageSize);
            statement.setInt(6, (page - 1) * pageSize);
        });
    }

    public int countBetween(final String one, final String two) throws SQLException {
        final String sql = """
                SELECT COUNT(*) FROM communication_logs
                WHERE log_type IN ('MSG', 'MAIL')
                  AND ((LOWER(sender_name) = LOWER(?) AND LOWER(receiver_name) = LOWER(?))
                    OR (LOWER(sender_name) = LOWER(?) AND LOWER(receiver_name) = LOWER(?)))
                """;
        return count(sql, statement -> {
            statement.setString(1, one);
            statement.setString(2, two);
            statement.setString(3, two);
            statement.setString(4, one);
        });
    }

    public List<CommunicationLogEntry> findByTypeAndPlayer(final String type, final String playerName, final int page, final int pageSize) throws SQLException {
        final String sql = """
                SELECT * FROM communication_logs
                WHERE log_type = ?
                  AND (LOWER(sender_name) = LOWER(?) OR LOWER(receiver_name) = LOWER(?))
                ORDER BY timestamp DESC
                LIMIT ? OFFSET ?
                """;
        return queryPaged(sql, statement -> {
            statement.setString(1, type);
            statement.setString(2, playerName);
            statement.setString(3, playerName);
            statement.setInt(4, pageSize);
            statement.setInt(5, (page - 1) * pageSize);
        });
    }

    public int countByTypeAndPlayer(final String type, final String playerName) throws SQLException {
        final String sql = """
                SELECT COUNT(*) FROM communication_logs
                WHERE log_type = ?
                  AND (LOWER(sender_name) = LOWER(?) OR LOWER(receiver_name) = LOWER(?))
                """;
        return count(sql, statement -> {
            statement.setString(1, type);
            statement.setString(2, playerName);
            statement.setString(3, playerName);
        });
    }

    public List<CommunicationLogEntry> searchByKeyword(final String keyword, final int page, final int pageSize) throws SQLException {
        final String sql = """
                SELECT * FROM communication_logs
                WHERE LOWER(message) LIKE LOWER(?)
                ORDER BY timestamp DESC
                LIMIT ? OFFSET ?
                """;
        return queryPaged(sql, statement -> {
            statement.setString(1, "%" + keyword + "%");
            statement.setInt(2, pageSize);
            statement.setInt(3, (page - 1) * pageSize);
        });
    }

    public int countSearchByKeyword(final String keyword) throws SQLException {
        final String sql = "SELECT COUNT(*) FROM communication_logs WHERE LOWER(message) LIKE LOWER(?)";
        return count(sql, statement -> statement.setString(1, "%" + keyword + "%"));
    }

    private List<CommunicationLogEntry> queryPaged(final String sql, final StatementBinder binder) throws SQLException {
        final List<CommunicationLogEntry> entries = new ArrayList<>();
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    entries.add(new CommunicationLogEntry(
                            resultSet.getLong("id"),
                            resultSet.getString("log_type"),
                            resultSet.getString("sender_uuid"),
                            resultSet.getString("sender_name"),
                            resultSet.getString("receiver_uuid"),
                            resultSet.getString("receiver_name"),
                            resultSet.getString("message"),
                            resultSet.getLong("timestamp")
                    ));
                }
            }
        }
        return entries;
    }

    private int count(final String sql, final StatementBinder binder) throws SQLException {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        }
    }

    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement statement) throws SQLException;
    }
}
