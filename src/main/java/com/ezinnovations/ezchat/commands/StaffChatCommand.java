package com.ezinnovations.ezchat.commands;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.config.StaffConfig;
import com.ezinnovations.ezchat.moderation.FloodChannel;
import com.ezinnovations.ezchat.moderation.FloodCheckService;
import com.ezinnovations.ezchat.moderation.FloodDetectionResult;
import com.ezinnovations.ezchat.moderation.ProfanityCheckService;
import com.ezinnovations.ezchat.moderation.ProfanityDetectionResult;
import com.ezinnovations.ezchat.service.StaffChatService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class StaffChatCommand implements CommandExecutor {

    private final EzChat plugin;
    private final StaffConfig staffConfig;
    private final StaffChatService staffChatService;
    private final ProfanityCheckService profanityCheckService;
    private final FloodCheckService floodCheckService;

    public StaffChatCommand(final EzChat plugin,
                            final StaffConfig staffConfig,
                            final StaffChatService staffChatService,
                            final ProfanityCheckService profanityCheckService,
                            final FloodCheckService floodCheckService) {
        this.plugin = plugin;
        this.staffConfig = staffConfig;
        this.staffChatService = staffChatService;
        this.profanityCheckService = profanityCheckService;
        this.floodCheckService = floodCheckService;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.colorize("&cOnly players can use this command."));
            return true;
        }

        if (!staffChatService.isFeatureEnabled()) {
            sender.sendMessage(plugin.colorize(staffConfig.getMessage("feature-disabled", "&cThat feature is currently disabled.")));
            return true;
        }

        if (!sender.hasPermission("ezchat.staffchat")) {
            sender.sendMessage(plugin.colorize(staffConfig.getMessage("no-permission", "&cYou do not have permission.")));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(plugin.colorize(staffConfig.getMessage("invalid-usage-staffchat", "&cUsage: /staffchat <message>")));
            return true;
        }

        final String message = String.join(" ", args);

        if (floodCheckService.shouldCheckStaffChat() && !floodCheckService.shouldBypass(player)) {
            final FloodDetectionResult detectionResult = floodCheckService.checkFlood(player.getUniqueId(), message, FloodChannel.STAFF);
            if (floodCheckService.handleBlockedMessage(player, FloodChannel.STAFF, detectionResult, message)) {
                return true;
            }
        }

        if (profanityCheckService.shouldScanStaffChat() && !profanityCheckService.shouldBypass(player)) {
            final ProfanityDetectionResult detectionResult = profanityCheckService.checkProfanity(message);
            if (profanityCheckService.handleBlockedMessage(player, ProfanityCheckService.CommunicationType.STAFF, detectionResult, message)) {
                return true;
            }
        }

        staffChatService.sendStaffChat(sender, message);
        return true;
    }
}
