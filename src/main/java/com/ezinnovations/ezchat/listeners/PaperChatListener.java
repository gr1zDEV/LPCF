package com.ezinnovations.ezchat.listeners;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.managers.ChatToggleManager;
import com.ezinnovations.ezchat.moderation.AdvertisingCheckService;
import com.ezinnovations.ezchat.moderation.AdvertisingDetectionResult;
import com.ezinnovations.ezchat.managers.FeatureManager;
import com.ezinnovations.ezchat.managers.IgnoreManager;
import com.ezinnovations.ezchat.service.CommunicationLogService;
import com.ezinnovations.ezchat.service.DiscordNotificationService;
import com.ezinnovations.ezchat.service.MuteService;
import com.ezinnovations.ezchat.service.StaffChatService;
import com.ezinnovations.ezchat.utils.FloodgateHook;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.UUID;

public final class PaperChatListener implements Listener {

    private final EzChat plugin;
    private final FeatureManager featureManager;
    private final ChatToggleManager chatToggleManager;
    private final IgnoreManager ignoreManager;
    private final FloodgateHook floodgateHook;
    private final CommunicationLogService communicationLogService;
    private final MuteService muteService;
    private final DiscordNotificationService discordNotificationService;
    private final AdvertisingCheckService advertisingCheckService;
    private final StaffChatService staffChatService;

    public PaperChatListener(final EzChat plugin,
                             final FeatureManager featureManager,
                             final ChatToggleManager chatToggleManager,
                             final IgnoreManager ignoreManager,
                             final FloodgateHook floodgateHook,
                             final CommunicationLogService communicationLogService,
                             final MuteService muteService,
                             final DiscordNotificationService discordNotificationService,
                             final AdvertisingCheckService advertisingCheckService,
                             final StaffChatService staffChatService) {
        this.plugin = plugin;
        this.featureManager = featureManager;
        this.chatToggleManager = chatToggleManager;
        this.ignoreManager = ignoreManager;
        this.floodgateHook = floodgateHook;
        this.communicationLogService = communicationLogService;
        this.muteService = muteService;
        this.discordNotificationService = discordNotificationService;
        this.advertisingCheckService = advertisingCheckService;
        this.staffChatService = staffChatService;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(final AsyncChatEvent event) {
        if (!featureManager.isPublicChatEnabled()) {
            return;
        }

        if (muteService.isFeatureEnabled() && muteService.blockPublicChat() && muteService.isMuted(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            muteService.sendMuteBlockedMessage(event.getPlayer(), "muted-chat", "&cYou are muted and cannot send chat messages.");
            return;
        }

        final Player player = event.getPlayer();

        if (advertisingCheckService.shouldScanPublicChat() && !advertisingCheckService.shouldBypass(player)) {
            final String rawMessage = LegacyComponentSerializer.legacySection().serialize(event.message());
            final AdvertisingDetectionResult detectionResult = advertisingCheckService.checkAdvertising(rawMessage);
            if (advertisingCheckService.handleBlockedMessage(player, AdvertisingCheckService.CommunicationType.PUBLIC, detectionResult, rawMessage)) {
                event.setCancelled(true);
                return;
            }
        }

        if (staffChatService.isFeatureEnabled() && staffChatService.isStaffChatModeEnabled(player.getUniqueId()) && player.hasPermission("ezchat.staffchat")) {
            event.setCancelled(true);
            final String message = plugin.processMessage(player, LegacyComponentSerializer.legacySection().serialize(event.message()));
            staffChatService.sendStaffChat(player, message);
            return;
        }

        final UUID senderUuid = player.getUniqueId();
        event.viewers().removeIf(viewerAudience -> !shouldReceiveAudience(viewerAudience, senderUuid));

        final String format = plugin.buildFormat(player);
        final String processedMessage = plugin.processMessage(player,
                LegacyComponentSerializer.legacySection().serialize(event.message()));

        final String finalFormat = format.replace("{message}", processedMessage);
        final Component rendered = LegacyComponentSerializer.legacySection().deserialize(finalFormat);

        event.renderer((source, sourceDisplayName, msg, audience) ->
                shouldReceiveAudience(audience, senderUuid) ? rendered : Component.empty());

        communicationLogService.logPublicChat(player.getUniqueId(), player.getName(), processedMessage);
        discordNotificationService.sendPublicChat(player, processedMessage);
    }

    private boolean shouldReceiveAudience(final Audience audience, final UUID senderUuid) {
        if (audience instanceof Player player) {
            return shouldReceiveChat(player, senderUuid);
        }

        if (audience instanceof ForwardingAudience forwardingAudience) {
            for (final Audience nestedAudience : forwardingAudience.audiences()) {
                if (!shouldReceiveAudience(nestedAudience, senderUuid)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean shouldReceiveChat(final Player player, final UUID senderUuid) {
        if (player.getUniqueId().equals(senderUuid)) {
            return true;
        }

        if (featureManager.isIgnoreEnabled()
                && ignoreManager.isIgnoring(player.getUniqueId(), senderUuid, IgnoreManager.IgnoreType.CHAT)) {
            return false;
        }

        if (!featureManager.isChatToggleEnabled() || !chatToggleManager.isChatHidden(player.getUniqueId())) {
            return true;
        }

        if (floodgateHook.isBedrockPlayer(player)) {
            return false;
        }

        return false;
    }
}
