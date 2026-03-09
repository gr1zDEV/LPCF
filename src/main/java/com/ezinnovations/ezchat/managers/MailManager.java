package com.ezinnovations.ezchat.managers;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.database.repository.MailRepository;
import com.ezinnovations.ezchat.model.MailEntry;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class MailManager {

    private final EzChat plugin;
    private final MailRepository mailRepository;

    public MailManager(final EzChat plugin, final MailRepository mailRepository) {
        this.plugin = plugin;
        this.mailRepository = mailRepository;
    }

    public void load() {
        // Mail data is fetched lazily from SQLite per request.
    }

    public MailEntry sendMail(final UUID senderUuid,
                              final String senderName,
                              final UUID receiverUuid,
                              final String receiverName,
                              final String message) {
        final MailEntry entry = new MailEntry(
                UUID.randomUUID().toString(),
                senderUuid,
                senderName,
                receiverUuid,
                receiverName,
                message,
                System.currentTimeMillis(),
                false
        );

        try {
            mailRepository.upsert(entry);
        } catch (final SQLException exception) {
            plugin.getLogger().warning("Failed to persist mail " + entry.getId() + ": " + exception.getMessage());
        }

        return entry;
    }

    public List<MailEntry> getInbox(final UUID playerUuid) {
        try {
            return mailRepository.findInbox(playerUuid);
        } catch (final SQLException exception) {
            plugin.getLogger().warning("Failed to load inbox for " + playerUuid + ": " + exception.getMessage());
            return new ArrayList<>();
        }
    }

    public List<MailEntry> getUnreadInbox(final UUID playerUuid) {
        final List<MailEntry> unread = new ArrayList<>();
        for (final MailEntry entry : getInbox(playerUuid)) {
            if (!entry.isRead()) {
                unread.add(entry);
            }
        }
        return unread;
    }

    public List<MailEntry> getInboxFromSender(final UUID playerUuid, final UUID senderUuid) {
        final List<MailEntry> filtered = new ArrayList<>();
        for (final MailEntry entry : getInbox(playerUuid)) {
            if (entry.getSender().equals(senderUuid)) {
                filtered.add(entry);
            }
        }
        return filtered;
    }

    public List<MailEntry> getSent(final UUID playerUuid) {
        try {
            return mailRepository.findSent(playerUuid);
        } catch (final SQLException exception) {
            plugin.getLogger().warning("Failed to load sent mail for " + playerUuid + ": " + exception.getMessage());
            return new ArrayList<>();
        }
    }

    public List<MailEntry> getSentToReceiver(final UUID playerUuid, final UUID receiverUuid) {
        final List<MailEntry> filtered = new ArrayList<>();
        for (final MailEntry entry : getSent(playerUuid)) {
            if (entry.getReceiver().equals(receiverUuid)) {
                filtered.add(entry);
            }
        }
        return filtered;
    }

    public Set<String> getUniqueSentTargetNames(final UUID playerUuid) {
        final Set<String> targets = new LinkedHashSet<>();
        for (final MailEntry entry : getSent(playerUuid)) {
            targets.add(entry.getReceiverName());
        }
        return targets;
    }

    public int getUnreadCount(final UUID playerUuid) {
        try {
            return mailRepository.getUnreadCount(playerUuid);
        } catch (final SQLException exception) {
            plugin.getLogger().warning("Failed to count unread mail for " + playerUuid + ": " + exception.getMessage());
            return 0;
        }
    }

    public void markAsRead(final UUID playerUuid, final List<MailEntry> entries) {
        if (entries.isEmpty()) {
            return;
        }

        final List<String> ids = new ArrayList<>();
        for (final MailEntry entry : entries) {
            ids.add(entry.getId());
        }

        try {
            mailRepository.markAsRead(playerUuid, ids);
        } catch (final SQLException exception) {
            plugin.getLogger().warning("Failed to mark mail as read for " + playerUuid + ": " + exception.getMessage());
        }
    }

    public void save() {
        // Persistence is immediate.
    }
}
