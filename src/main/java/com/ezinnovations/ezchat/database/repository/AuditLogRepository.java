package com.ezinnovations.ezchat.database.repository;

import com.ezinnovations.ezchat.database.DatabaseManager;
import com.ezinnovations.ezchat.model.AuditLogEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class AuditLogRepository {

    private final DatabaseManager databaseManager;

    public AuditLogRepository(final DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void insert(final AuditLogEntry entry) throws SQLException {
        final String sql = """
                INSERT INTO audit_logs (actor_uuid, actor_name, audit_type, details, timestamp)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entry.actorUuid());
            statement.setString(2, entry.actorName());
            statement.setString(3, entry.auditType());
            statement.setString(4, entry.details());
            statement.setLong(5, entry.timestamp());
            statement.executeUpdate();
        }
    }
}
