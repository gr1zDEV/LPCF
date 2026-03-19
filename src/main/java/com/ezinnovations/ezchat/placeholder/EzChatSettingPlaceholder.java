package com.ezinnovations.ezchat.placeholder;

import com.ezinnovations.ezchat.managers.ChatToggleManager;

import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;
import java.util.function.BiFunction;

public enum EzChatSettingPlaceholder {
    TOGGLE_CHAT("togglechat", ChatToggleManager::isChatEnabled),
    TOGGLE_MSG("togglemsg", ChatToggleManager::isPrivateMessagesEnabled),
    TOGGLE_MAIL("togglemail", ChatToggleManager::isMailEnabled),
    TOGGLE_SERVER_MESSAGES("toggleservermsg", ChatToggleManager::isServerMessagesEnabled),
    TOGGLE_DEATH_MESSAGES("toggledeathmsg", ChatToggleManager::isDeathMessagesEnabled),
    TOGGLE_JOIN_LEAVE_MESSAGES("togglejoinleavemsg", ChatToggleManager::isJoinLeaveMessagesEnabled),
    TOGGLE_STAFF_CHAT("togglestaffchat", ChatToggleManager::isStaffChatModeEnabled),
    TOGGLE_SOCIAL_SPY("togglesocialspy", ChatToggleManager::isSocialSpyEnabled),
    TOGGLE_MAIL_SPY("togglemailspy", ChatToggleManager::isMailSpyEnabled);

    private final String placeholderKey;
    private final BiFunction<ChatToggleManager, UUID, Boolean> resolver;

    EzChatSettingPlaceholder(final String placeholderKey,
                             final BiFunction<ChatToggleManager, UUID, Boolean> resolver) {
        this.placeholderKey = placeholderKey;
        this.resolver = resolver;
    }

    public boolean resolve(final ChatToggleManager chatToggleManager, final UUID playerUuid) {
        return resolver.apply(chatToggleManager, playerUuid);
    }

    public static EzChatSettingPlaceholder from(final String params) {
        final String normalized = params.toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(value -> value.placeholderKey.equals(normalized))
                .findFirst()
                .orElse(null);
    }
}
