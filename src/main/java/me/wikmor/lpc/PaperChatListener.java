package me.wikmor.lpc;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class PaperChatListener implements Listener {

    private final LPC plugin;
    private final ChatToggleManager chatToggleManager;
    private final FloodgateHook floodgateHook;

    public PaperChatListener(final LPC plugin, final ChatToggleManager chatToggleManager, final FloodgateHook floodgateHook) {
        this.plugin = plugin;
        this.chatToggleManager = chatToggleManager;
        this.floodgateHook = floodgateHook;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(final AsyncChatEvent event) {
        event.viewers().removeIf(viewerAudience -> !shouldReceiveAudience(viewerAudience));

        final Player player = event.getPlayer();

        String format = plugin.buildFormat(player);
        String processedMessage = plugin.processMessage(player,
                LegacyComponentSerializer.legacySection().serialize(event.message()));

        String finalFormat = format.replace("{message}", processedMessage);
        Component rendered = LegacyComponentSerializer.legacySection().deserialize(finalFormat);

        event.renderer((source, sourceDisplayName, msg, audience) ->
                shouldReceiveAudience(audience) ? rendered : Component.empty());
    }

    private boolean shouldReceiveAudience(final Audience audience) {
        if (audience instanceof Player player) {
            return shouldReceiveChat(player);
        }

        if (audience instanceof ForwardingAudience forwardingAudience) {
            for (final Audience nestedAudience : forwardingAudience.audiences()) {
                if (!shouldReceiveAudience(nestedAudience)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean shouldReceiveChat(final Player player) {
        if (!chatToggleManager.isChatHidden(player.getUniqueId())) {
            return true;
        }

        if (floodgateHook.isBedrockPlayer(player)) {
            return false;
        }

        return false;
    }
}
