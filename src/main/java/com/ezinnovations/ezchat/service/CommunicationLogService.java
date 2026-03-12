package com.ezinnovations.ezchat.service;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.config.LogsConfig;
import com.ezinnovations.ezchat.database.repository.CommunicationLogRepository;
import com.ezinnovations.ezchat.model.CommunicationLogEntry;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public final class CommunicationLogService {

    private final EzChat plugin;
    private final LogsConfig logsConfig;
    private final CommunicationLogRepository repository;

    public CommunicationLogService(final EzChat plugin,
                                   final LogsConfig logsConfig,
                                   final CommunicationLogRepository repository) {
        this.plugin = plugin;
        this.logsConfig = logsConfig;
        this.repository = repository;
    }

    public void logPublicChat(final UUID senderUuid, final String senderName, final String message) {
        if (!logsConfig.isLoggingEnabled() || !logsConfig.isPublicChatLoggingEnabled()) {
            return;
        }
        insert("PUBLIC", senderUuid, senderName, null, null, message);
    }

    public void logPrivateMessage(final UUID senderUuid,
                                  final String senderName,
                                  final UUID receiverUuid,
                                  final String receiverName,
                                  final String message) {
        if (!logsConfig.isLoggingEnabled() || !logsConfig.isPrivateMessageLoggingEnabled()) {
            return;
        }
        insert("MSG", senderUuid, senderName, receiverUuid, receiverName, message);
    }

    public void logMail(final UUID senderUuid,
                        final String senderName,
                        final UUID receiverUuid,
                        final String receiverName,
                        final String message) {
        if (!logsConfig.isLoggingEnabled() || !logsConfig.isMailLoggingEnabled()) {
            return;
        }
        insert("MAIL", senderUuid, senderName, receiverUuid, receiverName, message);
    }

    public void logStaffChat(final UUID senderUuid, final String senderName, final String message) {
        if (!logsConfig.isLoggingEnabled()) {
            return;
        }
        insert("STAFF", senderUuid, senderName, null, null, message);
    }

    public void logStaffAlert(final UUID senderUuid, final String senderName, final String message) {
        if (!logsConfig.isLoggingEnabled()) {
            return;
        }
        insert("STAFF_ALERT", senderUuid, senderName, null, null, message);
    }

    public QueryResult findByPlayer(final String playerName, final int page) {
        return find(() -> repository.findByPlayer(playerName, page, logsConfig.getPageSize()), () -> repository.countByPlayer(playerName));
    }

    public QueryResult findBetween(final String one, final String two, final int page) {
        return find(() -> repository.findBetween(one, two, page, logsConfig.getPageSize()), () -> repository.countBetween(one, two));
    }

    public QueryResult findPublicByPlayer(final String playerName, final int page) {
        return find(() -> repository.findByTypeAndPlayer("PUBLIC", playerName, page, logsConfig.getPageSize()), () -> repository.countByTypeAndPlayer("PUBLIC", playerName));
    }

    public QueryResult findMsgByPlayer(final String playerName, final int page) {
        return find(() -> repository.findByTypeAndPlayer("MSG", playerName, page, logsConfig.getPageSize()), () -> repository.countByTypeAndPlayer("MSG", playerName));
    }

    public QueryResult findMailByPlayer(final String playerName, final int page) {
        return find(() -> repository.findByTypeAndPlayer("MAIL", playerName, page, logsConfig.getPageSize()), () -> repository.countByTypeAndPlayer("MAIL", playerName));
    }

    public QueryResult search(final String keyword, final int page) {
        return find(() -> repository.searchByKeyword(keyword, page, logsConfig.getPageSize()), () -> repository.countSearchByKeyword(keyword));
    }

    private QueryResult find(final QuerySupplier listSupplier, final CountSupplier countSupplier) {
        try {
            final int totalCount = countSupplier.get();
            final int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / logsConfig.getPageSize()));
            return new QueryResult(listSupplier.get(), totalPages);
        } catch (final SQLException exception) {
            plugin.getLogger().warning("Failed to query communication logs: " + exception.getMessage());
            return new QueryResult(List.of(), 1);
        }
    }

    private void insert(final String type,
                        final UUID senderUuid,
                        final String senderName,
                        final UUID receiverUuid,
                        final String receiverName,
                        final String message) {
        try {
            repository.insert(new CommunicationLogEntry(
                    0,
                    type,
                    senderUuid != null ? senderUuid.toString() : null,
                    senderName,
                    receiverUuid != null ? receiverUuid.toString() : null,
                    receiverName,
                    message,
                    System.currentTimeMillis()
            ));
        } catch (final SQLException exception) {
            plugin.getLogger().warning("Failed to write communication log: " + exception.getMessage());
        }
    }

    public record QueryResult(List<CommunicationLogEntry> entries, int totalPages) {
    }

    @FunctionalInterface
    private interface QuerySupplier {
        List<CommunicationLogEntry> get() throws SQLException;
    }

    @FunctionalInterface
    private interface CountSupplier {
        int get() throws SQLException;
    }
}
