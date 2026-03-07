package com.ezinnovations.ezchat.managers;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.model.MailEntry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MailManager {

    private final EzChat plugin;
    private final File mailFile;
    private final Map<UUID, List<MailEntry>> inboxByPlayer = new ConcurrentHashMap<>();
    private final Map<UUID, List<MailEntry>> sentByPlayer = new ConcurrentHashMap<>();

    public MailManager(final EzChat plugin) {
        this.plugin = plugin;
        this.mailFile = new File(plugin.getDataFolder(), "mail.yml");
    }

    public void load() {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().warning("Failed to create plugin data folder for mail.yml.");
        }

        if (!mailFile.exists()) {
            try {
                if (!mailFile.createNewFile()) {
                    plugin.getLogger().warning("Failed to create mail.yml file.");
                }
            } catch (final IOException exception) {
                plugin.getLogger().warning("Failed to create mail.yml: " + exception.getMessage());
                return;
            }
        }

        final FileConfiguration mailConfig = YamlConfiguration.loadConfiguration(mailFile);
        inboxByPlayer.clear();
        sentByPlayer.clear();

        final ConfigurationSection playersSection = mailConfig.getConfigurationSection("players");
        if (playersSection == null) {
            return;
        }

        for (final String uuidRaw : playersSection.getKeys(false)) {
            final UUID playerUuid;
            try {
                playerUuid = UUID.fromString(uuidRaw);
            } catch (final IllegalArgumentException ignored) {
                plugin.getLogger().warning("Ignoring invalid player UUID in mail.yml: " + uuidRaw);
                continue;
            }

            final String basePath = "players." + uuidRaw;
            loadInbox(mailConfig.getMapList(basePath + ".inbox"), playerUuid);
            loadSent(mailConfig.getMapList(basePath + ".sent"), playerUuid);
        }
    }

    private void loadInbox(final List<Map<?, ?>> rawInboxList, final UUID ownerUuid) {
        final List<MailEntry> inboxEntries = new ArrayList<>();
        for (final Map<?, ?> rawEntry : rawInboxList) {
            final MailEntry entry = deserializeInboxEntry(rawEntry);
            if (entry != null) {
                inboxEntries.add(entry);
            }
        }
        sortNewestFirst(inboxEntries);
        if (!inboxEntries.isEmpty()) {
            inboxByPlayer.put(ownerUuid, inboxEntries);
        }
    }

    private void loadSent(final List<Map<?, ?>> rawSentList, final UUID ownerUuid) {
        final List<MailEntry> sentEntries = new ArrayList<>();
        for (final Map<?, ?> rawEntry : rawSentList) {
            final MailEntry entry = deserializeSentEntry(rawEntry);
            if (entry != null) {
                sentEntries.add(entry);
            }
        }
        sortNewestFirst(sentEntries);
        if (!sentEntries.isEmpty()) {
            sentByPlayer.put(ownerUuid, sentEntries);
        }
    }

    public MailEntry sendMail(final UUID senderUuid,
                              final String senderName,
                              final UUID receiverUuid,
                              final String receiverName,
                              final String message) {
        final String id = UUID.randomUUID().toString();
        final long timestamp = System.currentTimeMillis();

        final MailEntry inboxEntry = new MailEntry(id, senderUuid, senderName, receiverUuid, receiverName, message, timestamp, false);
        final MailEntry sentEntry = new MailEntry(id, senderUuid, senderName, receiverUuid, receiverName, message, timestamp, true);

        inboxByPlayer.computeIfAbsent(receiverUuid, ignored -> new ArrayList<>()).add(inboxEntry);
        sentByPlayer.computeIfAbsent(senderUuid, ignored -> new ArrayList<>()).add(sentEntry);

        sortNewestFirst(inboxByPlayer.get(receiverUuid));
        sortNewestFirst(sentByPlayer.get(senderUuid));
        save();

        return inboxEntry;
    }

    public List<MailEntry> getInbox(final UUID playerUuid) {
        final List<MailEntry> list = inboxByPlayer.getOrDefault(playerUuid, new ArrayList<>());
        return new ArrayList<>(list);
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
        final List<MailEntry> list = sentByPlayer.getOrDefault(playerUuid, new ArrayList<>());
        return new ArrayList<>(list);
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
        int count = 0;
        for (final MailEntry entry : getInbox(playerUuid)) {
            if (!entry.isRead()) {
                count++;
            }
        }
        return count;
    }

    public void markAsRead(final UUID playerUuid, final List<MailEntry> entries) {
        if (entries.isEmpty()) {
            return;
        }

        final Map<String, MailEntry> byId = new HashMap<>();
        for (final MailEntry entry : entries) {
            byId.put(entry.getId(), entry);
        }

        boolean changed = false;
        for (final MailEntry inboxEntry : inboxByPlayer.getOrDefault(playerUuid, new ArrayList<>())) {
            final MailEntry viewedEntry = byId.get(inboxEntry.getId());
            if (viewedEntry != null && !inboxEntry.isRead()) {
                inboxEntry.setRead(true);
                changed = true;
            }
        }

        if (changed) {
            save();
        }
    }

    public void save() {
        final FileConfiguration mailConfig = new YamlConfiguration();

        final Set<UUID> allPlayers = new LinkedHashSet<>();
        allPlayers.addAll(inboxByPlayer.keySet());
        allPlayers.addAll(sentByPlayer.keySet());

        for (final UUID uuid : allPlayers) {
            final String basePath = "players." + uuid;

            final List<Map<String, Object>> inboxMaps = new ArrayList<>();
            for (final MailEntry entry : inboxByPlayer.getOrDefault(uuid, new ArrayList<>())) {
                inboxMaps.add(entry.toInboxMap());
            }
            mailConfig.set(basePath + ".inbox", inboxMaps);

            final List<Map<String, Object>> sentMaps = new ArrayList<>();
            for (final MailEntry entry : sentByPlayer.getOrDefault(uuid, new ArrayList<>())) {
                sentMaps.add(entry.toSentMap());
            }
            mailConfig.set(basePath + ".sent", sentMaps);
        }

        try {
            mailConfig.save(mailFile);
        } catch (final IOException exception) {
            plugin.getLogger().warning("Failed to save mail.yml: " + exception.getMessage());
        }
    }

    private MailEntry deserializeInboxEntry(final Map<?, ?> map) {
        try {
            final String id = String.valueOf(map.get("id"));
            final UUID sender = UUID.fromString(String.valueOf(map.get("sender")));
            final String senderName = String.valueOf(map.get("sender-name"));
            final String message = String.valueOf(map.get("message"));
            final long timestamp = asLong(map.get("timestamp"));
            final boolean read = asBoolean(map.get("read"));
            return new MailEntry(id, sender, senderName, null, null, message, timestamp, read);
        } catch (final Exception exception) {
            plugin.getLogger().warning("Skipping invalid inbox mail entry in mail.yml: " + exception.getMessage());
            return null;
        }
    }

    private MailEntry deserializeSentEntry(final Map<?, ?> map) {
        try {
            final String id = String.valueOf(map.get("id"));
            final UUID receiver = UUID.fromString(String.valueOf(map.get("receiver")));
            final String receiverName = String.valueOf(map.get("receiver-name"));
            final String message = String.valueOf(map.get("message"));
            final long timestamp = asLong(map.get("timestamp"));
            return new MailEntry(id, null, null, receiver, receiverName, message, timestamp, true);
        } catch (final Exception exception) {
            plugin.getLogger().warning("Skipping invalid sent mail entry in mail.yml: " + exception.getMessage());
            return null;
        }
    }

    private long asLong(final Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private boolean asBoolean(final Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private void sortNewestFirst(final List<MailEntry> list) {
        list.sort(Comparator.comparingLong(MailEntry::getTimestamp).reversed());
    }
}
