package com.ezinnovations.ezchat.managers;

import com.ezinnovations.ezchat.EzChat;

public final class FeatureManager {

    private final EzChat plugin;

    private boolean publicChatEnabled;
    private boolean chatToggleEnabled;
    private boolean privateMessagesEnabled;
    private boolean privateMessageToggleEnabled;
    private boolean ignoreEnabled;
    private boolean mailEnabled;
    private boolean mailToggleEnabled;
    private boolean unreadLoginNotifyEnabled;

    public FeatureManager(final EzChat plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        publicChatEnabled = plugin.getConfig().getBoolean("features.public-chat.enabled", true);
        chatToggleEnabled = plugin.getConfig().getBoolean("features.chat-toggle.enabled", true);
        privateMessagesEnabled = plugin.getConfig().getBoolean("features.private-messages.enabled", true);
        privateMessageToggleEnabled = plugin.getConfig().getBoolean("features.private-message-toggle.enabled", true);
        ignoreEnabled = plugin.getConfig().getBoolean("features.ignore.enabled", true);
        mailEnabled = plugin.getConfig().getBoolean("features.mail.enabled", true);
        mailToggleEnabled = plugin.getConfig().getBoolean("features.mail-toggle.enabled", true);
        unreadLoginNotifyEnabled = plugin.getConfigManager().getMailConfig().getBoolean("settings.unread-login-notify.enabled", true);
    }

    public boolean isPublicChatEnabled() {
        return publicChatEnabled;
    }

    public boolean isChatToggleEnabled() {
        return chatToggleEnabled;
    }

    public boolean isPrivateMessagesEnabled() {
        return privateMessagesEnabled;
    }

    public boolean isPrivateMessageToggleEnabled() {
        return privateMessageToggleEnabled;
    }

    public boolean isIgnoreEnabled() {
        return ignoreEnabled;
    }

    public boolean isMailEnabled() {
        return mailEnabled;
    }

    public boolean isMailToggleEnabled() {
        return mailToggleEnabled;
    }

    public boolean isUnreadLoginNotifyEnabled() {
        return unreadLoginNotifyEnabled;
    }

    public String getFeatureDisabledMessage() {
        return plugin.getConfig().getString("general.feature-disabled", "&cThat feature is currently disabled.");
    }
}
