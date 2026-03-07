package com.ezinnovations.ezchat.commands;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.managers.MessageManager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

public final class ReplyCommand implements CommandExecutor {

    private final EzChat plugin;
    private final MessageManager messageManager;

    public ReplyCommand(final EzChat plugin, final MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof final Player player)) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("players-only", "&cOnly players can use this command.")));
            return true;
        }

        if (!player.hasPermission("ezchat.reply")) {
            player.sendMessage(plugin.colorize(plugin.getConfig().getString("private-messages.no-permission", "&cYou do not have permission.")));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(plugin.colorize("&cUsage: /reply <message>"));
            return true;
        }

        final UUID lastTargetUuid = messageManager.getReplyTarget(player);
        if (lastTargetUuid == null) {
            player.sendMessage(plugin.colorize(plugin.getConfig().getString("private-messages.reply-no-target", "&cYou have nobody to reply to.")));
            return true;
        }

        final Player receiver = plugin.getServer().getPlayer(lastTargetUuid);
        if (receiver == null || !receiver.isOnline()) {
            player.sendMessage(plugin.colorize(plugin.getConfig().getString("private-messages.player-not-found", "&cPlayer not found.")));
            return true;
        }

        if (receiver.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(plugin.colorize(plugin.getConfig().getString("private-messages.cannot-message-self", "&cYou cannot message yourself.")));
            return true;
        }

        final String message = Arrays.stream(args).collect(Collectors.joining(" "));
        final String sentFormat = plugin.getConfig().getString("private-messages.sent-format", "&8[&aTo {receiver}&8] &f{message}");
        final String receivedFormat = plugin.getConfig().getString("private-messages.received-format", "&8[&6From {sender}&8] &f{message}");

        player.sendMessage(plugin.colorize(sentFormat
                .replace("{sender}", player.getName())
                .replace("{receiver}", receiver.getName())
                .replace("{message}", message)));

        receiver.sendMessage(plugin.colorize(receivedFormat
                .replace("{sender}", player.getName())
                .replace("{receiver}", receiver.getName())
                .replace("{message}", message)));

        messageManager.updateConversation(player, receiver);
        return true;
    }
}
