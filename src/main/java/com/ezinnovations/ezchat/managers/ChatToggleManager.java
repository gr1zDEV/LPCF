package com.ezinnovations.ezchat.managers;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.database.repository.ToggleRepository;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ChatToggleManager {

    private final EzChat plugin;
    private final ToggleRepository toggleRepository;
    private final Set<UUID> hiddenChatPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> privateMessagesDisabledPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> mailDisabledPlayers = ConcurrentHashMap.newKeySet();

    public ChatToggleManager(final EzChat plugin, final ToggleRepository toggleRepository) {
        this.plugin = plugin;
        this.toggleRepository = toggleRepository;
    }

    public void load() {
        hiddenChatPlayers.clear();
        privateMessagesDisabledPlayers.clear();
        mailDisabledPlayers.clear();

        try {
            final Map<UUID, ToggleRepository.ToggleState> toggles = toggleRepository.loadAll();
            for (final Map.Entry<UUID, ToggleRepository.ToggleState> entry : toggles.entrySet()) {
                final UUID uuid = entry.getKey();
                final ToggleRepository.ToggleState state = entry.getValue();
                if (!state.chatEnabled()) {
                    hiddenChatPlayers.add(uuid);
                }
                if (!state.msgEnabled()) {
                    privateMessagesDisabledPlayers.add(uuid);
                }
                if (!state.mailEnabled()) {
                    mailDisabledPlayers.add(uuid);
                }
            }
        } catch (final SQLException exception) {
            plugin.getLogger().severe("Failed to load toggle data from SQLite: " + exception.getMessage());
        }
    }

    public boolean isChatHidden(final UUID uuid) {
        return hiddenChatPlayers.contains(uuid);
    }

    public boolean toggleChat(final UUID uuid) {
        final boolean nowHidden;
        if (hiddenChatPlayers.contains(uuid)) {
            hiddenChatPlayers.remove(uuid);
            nowHidden = false;
        } else {
            hiddenChatPlayers.add(uuid);
            nowHidden = true;
        }
        persist(uuid);
        return nowHidden;
    }

    public boolean arePrivateMessagesDisabled(final UUID uuid) {
        return privateMessagesDisabledPlayers.contains(uuid);
    }

    public boolean setChatHidden(final UUID uuid, final boolean hidden) {
        final boolean changed;
        if (hidden) {
            changed = hiddenChatPlayers.add(uuid);
        } else {
            changed = hiddenChatPlayers.remove(uuid);
        }

        if (changed) {
            persist(uuid);
        }

        return changed;
    }

    public boolean setPrivateMessagesDisabled(final UUID uuid, final boolean disabled) {
        final boolean changed;
        if (disabled) {
            changed = privateMessagesDisabledPlayers.add(uuid);
        } else {
            changed = privateMessagesDisabledPlayers.remove(uuid);
        }

        if (changed) {
            persist(uuid);
        }

        return changed;
    }

    public boolean togglePrivateMessages(final UUID uuid) {
        final boolean nowDisabled;
        if (privateMessagesDisabledPlayers.contains(uuid)) {
            privateMessagesDisabledPlayers.remove(uuid);
            nowDisabled = false;
        } else {
            privateMessagesDisabledPlayers.add(uuid);
            nowDisabled = true;
        }
        persist(uuid);
        return nowDisabled;
    }

    public boolean isMailDisabled(final UUID uuid) {
        return mailDisabledPlayers.contains(uuid);
    }

    public boolean setMailDisabled(final UUID uuid, final boolean disabled) {
        final boolean changed;
        if (disabled) {
            changed = mailDisabledPlayers.add(uuid);
        } else {
            changed = mailDisabledPlayers.remove(uuid);
        }

        if (changed) {
            persist(uuid);
        }

        return changed;
    }

    public boolean toggleMail(final UUID uuid) {
        final boolean nowDisabled;
        if (mailDisabledPlayers.contains(uuid)) {
            mailDisabledPlayers.remove(uuid);
            nowDisabled = false;
        } else {
            mailDisabledPlayers.add(uuid);
            nowDisabled = true;
        }
        persist(uuid);
        return nowDisabled;
    }

    public void save() {
        // Persistence is immediate.
    }

    private void persist(final UUID uuid) {
        try {
            toggleRepository.upsert(
                    uuid,
                    !hiddenChatPlayers.contains(uuid),
                    !privateMessagesDisabledPlayers.contains(uuid),
                    !mailDisabledPlayers.contains(uuid)
            );
        } catch (final SQLException exception) {
            plugin.getLogger().warning("Failed to persist toggle for " + uuid + ": " + exception.getMessage());
        }
    }
}
