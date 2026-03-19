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
    private final Set<UUID> knownTogglePlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> hiddenChatPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> privateMessagesDisabledPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> mailDisabledPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> serverMessagesDisabledPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> staffChatModeEnabledPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> deathMessagesDisabledPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> joinLeaveMessagesDisabledPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> socialSpyEnabledPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> mailSpyEnabledPlayers = ConcurrentHashMap.newKeySet();

    public ChatToggleManager(final EzChat plugin, final ToggleRepository toggleRepository) {
        this.plugin = plugin;
        this.toggleRepository = toggleRepository;
    }

    public void load() {
        knownTogglePlayers.clear();
        hiddenChatPlayers.clear();
        privateMessagesDisabledPlayers.clear();
        mailDisabledPlayers.clear();
        serverMessagesDisabledPlayers.clear();
        staffChatModeEnabledPlayers.clear();
        deathMessagesDisabledPlayers.clear();
        joinLeaveMessagesDisabledPlayers.clear();
        socialSpyEnabledPlayers.clear();
        mailSpyEnabledPlayers.clear();

        try {
            final Map<UUID, ToggleRepository.ToggleState> toggles = toggleRepository.loadAll();
            for (final Map.Entry<UUID, ToggleRepository.ToggleState> entry : toggles.entrySet()) {
                final UUID uuid = entry.getKey();
                knownTogglePlayers.add(uuid);
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
                if (!state.serverMessagesEnabled()) {
                    serverMessagesDisabledPlayers.add(uuid);
                }
                if (state.staffChatModeEnabled()) {
                    staffChatModeEnabledPlayers.add(uuid);
                }
                if (!state.deathMessagesEnabled()) {
                    deathMessagesDisabledPlayers.add(uuid);
                }
                if (!state.joinLeaveMessagesEnabled()) {
                    joinLeaveMessagesDisabledPlayers.add(uuid);
                }
                if (state.socialSpyEnabled()) {
                    socialSpyEnabledPlayers.add(uuid);
                }
                if (state.mailSpyEnabled()) {
                    mailSpyEnabledPlayers.add(uuid);
                }
            }
        } catch (final SQLException exception) {
            plugin.getLogger().severe("Failed to load toggle data from SQLite: " + exception.getMessage());
        }
    }

    public boolean hasToggleState(final UUID uuid) {
        return knownTogglePlayers.contains(uuid);
    }

    public boolean isChatHidden(final UUID uuid) {
        return hiddenChatPlayers.contains(uuid);
    }

    public boolean isChatEnabled(final UUID uuid) {
        return !isChatHidden(uuid);
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

    public boolean isPrivateMessagesEnabled(final UUID uuid) {
        return !arePrivateMessagesDisabled(uuid);
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

    public boolean isMailEnabled(final UUID uuid) {
        return !isMailDisabled(uuid);
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

    public boolean areServerMessagesDisabled(final UUID uuid) {
        return serverMessagesDisabledPlayers.contains(uuid);
    }

    public boolean isServerMessagesEnabled(final UUID uuid) {
        return !areServerMessagesDisabled(uuid);
    }

    public boolean setServerMessagesDisabled(final UUID uuid, final boolean disabled) {
        final boolean changed;
        if (disabled) {
            changed = serverMessagesDisabledPlayers.add(uuid);
        } else {
            changed = serverMessagesDisabledPlayers.remove(uuid);
        }

        if (changed) {
            persist(uuid);
        }

        return changed;
    }

    public boolean toggleServerMessages(final UUID uuid) {
        final boolean nowDisabled;
        if (serverMessagesDisabledPlayers.contains(uuid)) {
            serverMessagesDisabledPlayers.remove(uuid);
            nowDisabled = false;
        } else {
            serverMessagesDisabledPlayers.add(uuid);
            nowDisabled = true;
        }
        persist(uuid);
        return nowDisabled;
    }

    public boolean isStaffChatModeEnabled(final UUID uuid) {
        return staffChatModeEnabledPlayers.contains(uuid);
    }

    public boolean setStaffChatModeEnabled(final UUID uuid, final boolean enabled) {
        final boolean changed;
        if (enabled) {
            changed = staffChatModeEnabledPlayers.add(uuid);
        } else {
            changed = staffChatModeEnabledPlayers.remove(uuid);
        }

        if (changed) {
            persist(uuid);
        }

        return changed;
    }

    public boolean toggleStaffChatMode(final UUID uuid) {
        final boolean nowEnabled;
        if (staffChatModeEnabledPlayers.contains(uuid)) {
            staffChatModeEnabledPlayers.remove(uuid);
            nowEnabled = false;
        } else {
            staffChatModeEnabledPlayers.add(uuid);
            nowEnabled = true;
        }
        persist(uuid);
        return nowEnabled;
    }

    public boolean areDeathMessagesDisabled(final UUID uuid) {
        return deathMessagesDisabledPlayers.contains(uuid);
    }

    public boolean isDeathMessagesEnabled(final UUID uuid) {
        return !areDeathMessagesDisabled(uuid);
    }

    public boolean setDeathMessagesDisabled(final UUID uuid, final boolean disabled) {
        final boolean changed;
        if (disabled) {
            changed = deathMessagesDisabledPlayers.add(uuid);
        } else {
            changed = deathMessagesDisabledPlayers.remove(uuid);
        }

        if (changed) {
            persist(uuid);
        }

        return changed;
    }

    public boolean toggleDeathMessages(final UUID uuid) {
        final boolean nowDisabled;
        if (deathMessagesDisabledPlayers.contains(uuid)) {
            deathMessagesDisabledPlayers.remove(uuid);
            nowDisabled = false;
        } else {
            deathMessagesDisabledPlayers.add(uuid);
            nowDisabled = true;
        }
        persist(uuid);
        return nowDisabled;
    }

    public boolean areJoinLeaveMessagesDisabled(final UUID uuid) {
        return joinLeaveMessagesDisabledPlayers.contains(uuid);
    }

    public boolean isJoinLeaveMessagesEnabled(final UUID uuid) {
        return !areJoinLeaveMessagesDisabled(uuid);
    }

    public boolean setJoinLeaveMessagesDisabled(final UUID uuid, final boolean disabled) {
        final boolean changed;
        if (disabled) {
            changed = joinLeaveMessagesDisabledPlayers.add(uuid);
        } else {
            changed = joinLeaveMessagesDisabledPlayers.remove(uuid);
        }

        if (changed) {
            persist(uuid);
        }

        return changed;
    }

    public boolean toggleJoinLeaveMessages(final UUID uuid) {
        final boolean nowDisabled;
        if (joinLeaveMessagesDisabledPlayers.contains(uuid)) {
            joinLeaveMessagesDisabledPlayers.remove(uuid);
            nowDisabled = false;
        } else {
            joinLeaveMessagesDisabledPlayers.add(uuid);
            nowDisabled = true;
        }
        persist(uuid);
        return nowDisabled;
    }

    public boolean isSocialSpyEnabled(final UUID uuid) {
        return socialSpyEnabledPlayers.contains(uuid);
    }

    public boolean toggleSocialSpy(final UUID uuid) {
        final boolean nowEnabled;
        if (socialSpyEnabledPlayers.contains(uuid)) {
            socialSpyEnabledPlayers.remove(uuid);
            nowEnabled = false;
        } else {
            socialSpyEnabledPlayers.add(uuid);
            nowEnabled = true;
        }
        persist(uuid);
        return nowEnabled;
    }

    public boolean isMailSpyEnabled(final UUID uuid) {
        return mailSpyEnabledPlayers.contains(uuid);
    }

    public boolean toggleMailSpy(final UUID uuid) {
        final boolean nowEnabled;
        if (mailSpyEnabledPlayers.contains(uuid)) {
            mailSpyEnabledPlayers.remove(uuid);
            nowEnabled = false;
        } else {
            mailSpyEnabledPlayers.add(uuid);
            nowEnabled = true;
        }
        persist(uuid);
        return nowEnabled;
    }

    public void save() {
        // Persistence is immediate.
    }

    private void persist(final UUID uuid) {
        knownTogglePlayers.add(uuid);
        try {
            toggleRepository.upsert(
                    uuid,
                    !hiddenChatPlayers.contains(uuid),
                    !privateMessagesDisabledPlayers.contains(uuid),
                    !mailDisabledPlayers.contains(uuid),
                    !serverMessagesDisabledPlayers.contains(uuid),
                    staffChatModeEnabledPlayers.contains(uuid),
                    !deathMessagesDisabledPlayers.contains(uuid),
                    !joinLeaveMessagesDisabledPlayers.contains(uuid),
                    socialSpyEnabledPlayers.contains(uuid),
                    mailSpyEnabledPlayers.contains(uuid)
            );
        } catch (final SQLException exception) {
            plugin.getLogger().warning("Failed to persist toggle for " + uuid + ": " + exception.getMessage());
        }
    }
}
