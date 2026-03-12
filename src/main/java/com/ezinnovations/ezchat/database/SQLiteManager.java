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
                        server_messages_enabled INTEGER NOT NULL DEFAULT 1,
                        staff_chat_mode_enabled INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(player_uuid) REFERENCES players(uuid)
                    )
                    """);
            ensureColumn(statement, "ALTER TABLE toggles ADD COLUMN server_messages_enabled INTEGER NOT NULL DEFAULT 1");
            ensureColumn(statement, "ALTER TABLE toggles ADD COLUMN staff_chat_mode_enabled INTEGER NOT NULL DEFAULT 0");
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
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS communication_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        log_type TEXT NOT NULL,
                        sender_uuid TEXT NOT NULL,
                        sender_name TEXT,
                        receiver_uuid TEXT,
                        receiver_name TEXT,
                        message TEXT NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS audit_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        actor_uuid TEXT,
                        actor_name TEXT,
                        audit_type TEXT NOT NULL,
                        details TEXT,
                        timestamp INTEGER NOT NULL
                    )
                    """);
            statement.execute("CREATE INDEX IF NOT EXISTS idx_comm_sender_uuid ON communication_logs(sender_uuid)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_comm_receiver_uuid ON communication_logs(receiver_uuid)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_comm_log_type ON communication_logs(log_type)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_comm_timestamp ON communication_logs(timestamp DESC)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_audit_actor_uuid ON audit_logs(actor_uuid)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_audit_type ON audit_logs(audit_type)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON audit_logs(timestamp DESC)");
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS mutes (
                        player_uuid TEXT PRIMARY KEY,
                        player_name TEXT,
                        mute_type TEXT NOT NULL,
                        reason TEXT,
                        created_at INTEGER NOT NULL,
                        expires_at INTEGER,
                        muted_by_uuid TEXT,
                        muted_by_name TEXT
                    )
                    """);
            statement.execute("CREATE INDEX IF NOT EXISTS idx_mutes_expires_at ON mutes(expires_at)");
        } catch (final SQLException exception) {
            plugin.getLogger().severe("Failed to initialize SQLite database: " + exception.getMessage());
        }
    }

    private void ensureColumn(final Statement statement, final String alterSql) throws SQLException {
        try {
            statement.execute(alterSql);
        } catch (final SQLException ignored) {
            // Column may already exist in upgraded databases.
        }
    }

    @Override
    public void close() {
        // No persistent connection to close.
    }
}
