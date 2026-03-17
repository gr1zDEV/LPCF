package com.ezinnovations.ezchat.service;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.config.StaffConfig;
import com.ezinnovations.ezchat.managers.ChatToggleManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class SpyService {

    private final EzChat plugin;
    private final StaffConfig staffConfig;
    private final ChatToggleManager chatToggleManager;

    public SpyService(final EzChat plugin, final StaffConfig staffConfig, final ChatToggleManager chatToggleManager) {
        this.plugin = plugin;
        this.staffConfig = staffConfig;
        this.chatToggleManager = chatToggleManager;
    }

    public boolean toggleSocialSpy(final Player player) {
        return chatToggleManager.toggleSocialSpy(player.getUniqueId());
    }

    public boolean toggleMailSpy(final Player player) {
        return chatToggleManager.toggleMailSpy(player.getUniqueId());
    }

    public void dispatchPrivateMessageSpy(final Player sender, final Player receiver, final String message) {
        final String format = staffConfig.getFormat("social-spy", "&8[&5Spy:PM&8] &f{sender} &7-> &f{receiver}&7: &f{message}")
                .replace("{sender}", sender.getName())
                .replace("{receiver}", receiver.getName())
                .replace("{message}", message);
        final String rendered = plugin.colorize(format);

        for (final Player online : plugin.getServer().getOnlinePlayers()) {
            if (online.getUniqueId().equals(sender.getUniqueId()) || online.getUniqueId().equals(receiver.getUniqueId())) {
                continue;
            }
            if (!online.hasPermission("ezchat.socialspy")) {
                continue;
            }
            if (!chatToggleManager.isSocialSpyEnabled(online.getUniqueId())) {
                continue;
            }
            online.sendMessage(rendered);
        }
    }

    public void dispatchMailSpy(final CommandSender sender, final String receiverName, final String message) {
        final String format = staffConfig.getFormat("mail-spy", "&8[&5Spy:Mail&8] &f{sender} &7-> &f{receiver}&7: &f{message}")
                .replace("{sender}", sender.getName())
                .replace("{receiver}", receiverName)
                .replace("{message}", message);
        final String rendered = plugin.colorize(format);

        for (final Player online : plugin.getServer().getOnlinePlayers()) {
            if (online.getName().equalsIgnoreCase(sender.getName()) || online.getName().equalsIgnoreCase(receiverName)) {
                continue;
            }
            if (!online.hasPermission("ezchat.mailspy")) {
                continue;
            }
            if (!chatToggleManager.isMailSpyEnabled(online.getUniqueId())) {
                continue;
            }
            online.sendMessage(rendered);
        }
    }
}
