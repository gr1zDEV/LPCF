package com.ezinnovations.ezchat.service;

import com.ezinnovations.ezchat.discord.DiscordEventType;
import com.ezinnovations.ezchat.discord.DiscordWebhookService;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public final class DiscordNotificationService {

    private final DiscordWebhookService webhookService;

    public DiscordNotificationService(final DiscordWebhookService webhookService) {
        this.webhookService = webhookService;
    }

    public void sendPublicChat(final Player player, final String message) {
        webhookService.send(
                DiscordEventType.PUBLIC_CHAT,
                player.getUniqueId(),
                player.getName(),
                "public-chat",
                "{player}: {message}",
                Map.of(
                        "player", player.getName(),
                        "message", message
                )
        );
    }

    public void sendPrivateMessage(final UUID senderUuid,
                                   final String senderName,
                                   final UUID receiverUuid,
                                   final String receiverName,
                                   final String message) {
        webhookService.send(
                DiscordEventType.PRIVATE_MESSAGES,
                senderUuid,
                senderName,
                "private-message",
                "[MSG] {sender} -> {receiver}: {message}",
                Map.of(
                        "sender", senderName,
                        "receiver", receiverName,
                        "message", message,
                        "target_uuid", receiverUuid == null ? "" : receiverUuid.toString()
                )
        );
    }

    public void sendMail(final UUID senderUuid,
                         final String senderName,
                         final UUID receiverUuid,
                         final String receiverName,
                         final String message) {
        webhookService.send(
                DiscordEventType.MAIL,
                senderUuid,
                senderName,
                "mail",
                "[MAIL] {sender} -> {receiver}: {message}",
                Map.of(
                        "sender", senderName,
                        "receiver", receiverName,
                        "message", message,
                        "target_uuid", receiverUuid == null ? "" : receiverUuid.toString()
                )
        );
    }

    public void sendMuteAction(final UUID actorUuid,
                               final String actorName,
                               final String target,
                               final String duration,
                               final boolean temporary) {
        final String key = temporary ? "temp-mute-action" : "mute-action";
        final String fallback = temporary
                ? "[TEMP-MUTE] {actor} temp-muted {target} for {duration}"
                : "[MUTE] {actor} muted {target}";

        webhookService.send(
                DiscordEventType.MUTE_ACTIONS,
                actorUuid,
                actorName,
                key,
                fallback,
                Map.of(
                        "actor", actorName,
                        "target", target,
                        "duration", duration
                )
        );
    }


    public void sendServerBroadcast(final org.bukkit.command.CommandSender sender, final String message) {
        final String senderName = sender.getName();
        final boolean fromConsole = !(sender instanceof Player);
        final String formatKey = fromConsole ? "server-broadcast-console" : "server-broadcast-player";
        final String fallback = fromConsole
                ? "[BROADCAST] [Console] {message}"
                : "[BROADCAST] [{sender}] {message}";

        webhookService.send(
                DiscordEventType.SERVER_BROADCASTS,
                sender instanceof Player player ? player.getUniqueId() : null,
                senderName,
                formatKey,
                fallback,
                Map.of(
                        "sender", senderName,
                        "message", message
                )
        );
    }


    public void sendDeathMessage(final UUID playerUuid,
                                 final String playerName,
                                 final String message) {
        webhookService.send(
                DiscordEventType.DEATH_MESSAGES,
                playerUuid,
                playerName,
                "death-message",
                "[DEATH] {player}: {message}",
                Map.of(
                        "player", playerName,
                        "message", message
                )
        );
    }


    public void sendJoinMessage(final UUID playerUuid,
                                final String playerName,
                                final String message) {
        webhookService.send(
                DiscordEventType.JOIN_MESSAGES,
                playerUuid,
                playerName,
                "join-message",
                "[JOIN] {player}: {message}",
                Map.of(
                        "player", playerName,
                        "message", message
                )
        );
    }

    public void sendLeaveMessage(final UUID playerUuid,
                                 final String playerName,
                                 final String message) {
        webhookService.send(
                DiscordEventType.LEAVE_MESSAGES,
                playerUuid,
                playerName,
                "leave-message",
                "[LEAVE] {player}: {message}",
                Map.of(
                        "player", playerName,
                        "message", message
                )
        );
    }

    public void sendAuditAction(final UUID actorUuid,
                                final String actorName,
                                final String details) {
        webhookService.send(
                DiscordEventType.AUDIT_ACTIONS,
                actorUuid,
                actorName,
                "audit-action",
                "[AUDIT] {actor}: {details}",
                Map.of(
                        "actor", actorName,
                        "details", details
                )
        );
    }
}
