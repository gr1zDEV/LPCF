package me.wikmor.lpc;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class PaperChatListener implements Listener {

    private final LPC plugin;
    private final ChatToggleManager chatToggleManager;

    public PaperChatListener(final LPC plugin, final ChatToggleManager chatToggleManager) {
        this.plugin = plugin;
        this.chatToggleManager = chatToggleManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(final AsyncChatEvent event) {
        event.viewers().removeIf(audience -> audience instanceof Player viewer
                && chatToggleManager.isChatHidden(viewer.getUniqueId()));

        final Player player = event.getPlayer();

        String format = plugin.buildFormat(player);
        String processedMessage = plugin.processMessage(player,
                LegacyComponentSerializer.legacySection().serialize(event.message()));

        String finalFormat = format.replace("{message}", processedMessage);
        Component rendered = LegacyComponentSerializer.legacySection().deserialize(finalFormat);

        event.renderer((source, sourceDisplayName, msg, audience) -> rendered);
    }
}
