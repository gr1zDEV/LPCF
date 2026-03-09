package com.ezinnovations.ezchat.service;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.config.LogsConfig;
import com.ezinnovations.ezchat.database.repository.AuditLogRepository;
import com.ezinnovations.ezchat.model.AuditLogEntry;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;

public final class AuditLogService {

    private final EzChat plugin;
    private final LogsConfig logsConfig;
    private final AuditLogRepository repository;

    public AuditLogService(final EzChat plugin,
                           final LogsConfig logsConfig,
                           final AuditLogRepository repository) {
        this.plugin = plugin;
        this.logsConfig = logsConfig;
        this.repository = repository;
    }

    public void log(final UUID actorUuid, final String actorName, final String auditType, final String details) {
        if (!logsConfig.isLoggingEnabled() || !logsConfig.isAuditLoggingEnabled()) {
            return;
        }

        try {
            repository.insert(new AuditLogEntry(
                    0,
                    actorUuid != null ? actorUuid.toString() : null,
                    actorName,
                    auditType,
                    details,
                    System.currentTimeMillis()
            ));
        } catch (final SQLException exception) {
            plugin.getLogger().warning("Failed to write audit log: " + exception.getMessage());
        }
    }

    public void log(final Player actor, final String auditType, final String details) {
        log(actor.getUniqueId(), actor.getName(), auditType, details);
    }
}
