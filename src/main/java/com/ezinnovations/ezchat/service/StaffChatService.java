package com.ezinnovations.ezchat.service;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.config.StaffConfig;
import com.ezinnovations.ezchat.managers.ChatToggleManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class StaffChatService {

    private final EzChat plugin;
    private final StaffConfig staffConfig;
    private final ChatToggleManager chatToggleManager;
    private final CommunicationLogService communicationLogService;

    public StaffChatService(final EzChat plugin,
                            final StaffConfig staffConfig,
                            final ChatToggleManager chatToggleManager,
                            final CommunicationLogService communicationLogService) {
        this.plugin = plugin;
        this.staffConfig = staffConfig;
        this.chatToggleManager = chatToggleManager;
        this.communicationLogService = communicationLogService;
    }

    public boolean isFeatureEnabled() {
        return staffConfig.isStaffChatEnabled();
    }

    public boolean isStaffChatModeEnabled(final UUID playerUuid) {
        return chatToggleManager.isStaffChatModeEnabled(playerUuid);
    }

    public boolean toggleStaffChatMode(final UUID playerUuid) {
        return chatToggleManager.toggleStaffChatMode(playerUuid);
    }

    public void disableStaffChatMode(final UUID playerUuid) {
        chatToggleManager.setStaffChatModeEnabled(playerUuid, false);
    }

    public void sendStaffChat(final CommandSender sender, final String message) {
        final String senderName = sender.getName();
        final String format = staffConfig.getFormat("staff-chat", "&8[&bStaff&8] &f{sender}: &b{message}")
                .replace("{sender}", senderName)
                .replace("{message}", message);
        final String rendered = plugin.colorize(format);

        plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
            for (final Player online : plugin.getServer().getOnlinePlayers()) {
                if (online.hasPermission("ezchat.staffchat")) {
                    online.sendMessage(rendered);
                }
            }
        });

        if (staffConfig.shouldLogStaffChat() && sender instanceof Player playerSender) {
            communicationLogService.logStaffChat(playerSender.getUniqueId(), senderName, message);
        }
    }
}
