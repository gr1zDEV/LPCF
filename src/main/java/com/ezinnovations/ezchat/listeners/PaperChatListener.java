package com.ezinnovations.ezchat.listeners;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.managers.ChatToggleManager;
import com.ezinnovations.ezchat.managers.FeatureManager;
import com.ezinnovations.ezchat.managers.IgnoreManager;
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

    public PaperChatListener(final EzChat plugin, final FeatureManager featureManager, final ChatToggleManager chatToggleManager, final IgnoreManager ignoreManager, final FloodgateHook floodgateHook) {
        this.plugin = plugin;
        this.featureManager = featureManager;
        this.chatToggleManager = chatToggleManager;
        this.ignoreManager = ignoreManager;
        this.floodgateHook = floodgateHook;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(final AsyncChatEvent event) {
        if (!featureManager.isPublicChatEnabled()) {
            return;
        }

        final UUID senderUuid = event.getPlayer().getUniqueId();
        event.viewers().removeIf(viewerAudience -> !shouldReceiveAudience(viewerAudience, senderUuid));

        final Player player = event.getPlayer();

        String format = plugin.buildFormat(player);
        String processedMessage = plugin.processMessage(player,
                LegacyComponentSerializer.legacySection().serialize(event.message()));

        String finalFormat = format.replace("{message}", processedMessage);
        Component rendered = LegacyComponentSerializer.legacySection().deserialize(finalFormat);

        event.renderer((source, sourceDisplayName, msg, audience) ->
                shouldReceiveAudience(audience, senderUuid) ? rendered : Component.empty());
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
