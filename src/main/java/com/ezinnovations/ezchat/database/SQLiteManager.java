package com.ezinnovations.ezchat.database;

import com.ezinnovations.ezchat.EzChat;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class SQLiteManager implements DatabaseManager {

    private final EzChat plugin;
    private final File databaseFile;

    public SQLiteManager(final EzChat plugin) {
        this.plugin = plugin;
        this.databaseFile = new File(plugin.getDataFolder(), "database.db");
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
    }

    @Override
    public void initialize() {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().severe("Failed to create plugin data directory for SQLite database.");
            return;
        }

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON;");
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS players (
                        uuid TEXT PRIMARY KEY,
                        last_known_name TEXT
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS toggles (
                        player_uuid TEXT PRIMARY KEY,
                        chat_enabled INTEGER NOT NULL,
                        msg_enabled INTEGER NOT NULL,
                        mail_enabled INTEGER NOT NULL,
                        FOREIGN KEY(player_uuid) REFERENCES players(uuid)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS ignores (
                        owner_uuid TEXT NOT NULL,
                        target_uuid TEXT NOT NULL,
                        ignore_type TEXT NOT NULL,
                        PRIMARY KEY (owner_uuid, target_uuid, ignore_type),
                        FOREIGN KEY(owner_uuid) REFERENCES players(uuid),
                        FOREIGN KEY(target_uuid) REFERENCES players(uuid)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS mail (
                        id TEXT PRIMARY KEY,
                        sender_uuid TEXT NOT NULL,
                        receiver_uuid TEXT NOT NULL,
                        sender_name TEXT,
                        receiver_name TEXT,
                        message TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        read INTEGER NOT NULL,
                        FOREIGN KEY(sender_uuid) REFERENCES players(uuid),
                        FOREIGN KEY(receiver_uuid) REFERENCES players(uuid)
                    )
                    """);
            statement.execute("CREATE INDEX IF NOT EXISTS idx_mail_receiver_timestamp ON mail(receiver_uuid, timestamp DESC)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_mail_sender_timestamp ON mail(sender_uuid, timestamp DESC)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_ignores_owner_target ON ignores(owner_uuid, target_uuid)");
        } catch (final SQLException exception) {
            plugin.getLogger().severe("Failed to initialize SQLite database: " + exception.getMessage());
        }
    }

    @Override
    public void close() {
        // No persistent connection to close.
    }
}
