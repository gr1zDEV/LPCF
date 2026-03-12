package com.ezinnovations.ezchat.database.repository;

import com.ezinnovations.ezchat.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ToggleRepository {

    private final DatabaseManager databaseManager;

    public ToggleRepository(final DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public Map<UUID, ToggleState> loadAll() throws SQLException {
        final Map<UUID, ToggleState> states = new HashMap<>();
        final String sql = "SELECT player_uuid, chat_enabled, msg_enabled, mail_enabled, server_messages_enabled, staff_chat_mode_enabled FROM toggles";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                final UUID uuid = UUID.fromString(resultSet.getString("player_uuid"));
                final boolean chatEnabled = resultSet.getInt("chat_enabled") == 1;
                final boolean msgEnabled = resultSet.getInt("msg_enabled") == 1;
                final boolean mailEnabled = resultSet.getInt("mail_enabled") == 1;
                final boolean serverMessagesEnabled = resultSet.getInt("server_messages_enabled") == 1;
                final boolean staffChatModeEnabled = resultSet.getInt("staff_chat_mode_enabled") == 1;
                states.put(uuid, new ToggleState(chatEnabled, msgEnabled, mailEnabled, serverMessagesEnabled, staffChatModeEnabled));
            }
        }

        return states;
    }

    public void upsert(final UUID playerUuid,
                       final boolean chatEnabled,
                       final boolean msgEnabled,
                       final boolean mailEnabled,
                       final boolean serverMessagesEnabled,
                       final boolean staffChatModeEnabled) throws SQLException {
        final String sql = """
                INSERT INTO toggles(player_uuid, chat_enabled, msg_enabled, mail_enabled, server_messages_enabled, staff_chat_mode_enabled)
                VALUES(?, ?, ?, ?, ?, ?)
                ON CONFLICT(player_uuid) DO UPDATE SET
                    chat_enabled=excluded.chat_enabled,
                    msg_enabled=excluded.msg_enabled,
                    mail_enabled=excluded.mail_enabled,
                    server_messages_enabled=excluded.server_messages_enabled,
                    staff_chat_mode_enabled=excluded.staff_chat_mode_enabled
                """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerUuid.toString());
            statement.setInt(2, chatEnabled ? 1 : 0);
            statement.setInt(3, msgEnabled ? 1 : 0);
            statement.setInt(4, mailEnabled ? 1 : 0);
            statement.setInt(5, serverMessagesEnabled ? 1 : 0);
            statement.setInt(6, staffChatModeEnabled ? 1 : 0);
            statement.executeUpdate();
        }
    }

    public boolean isEmpty() throws SQLException {
        final String sql = "SELECT COUNT(*) FROM toggles";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() && resultSet.getInt(1) == 0;
        }
    }

    public record ToggleState(boolean chatEnabled,
                              boolean msgEnabled,
                              boolean mailEnabled,
                              boolean serverMessagesEnabled,
                              boolean staffChatModeEnabled) {
    }
}
