package com.ezinnovations.ezchat.service;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.config.DeathMessageConfig;
import com.ezinnovations.ezchat.managers.ChatToggleManager;
import com.ezinnovations.ezchat.model.DeathMessageCategory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class DeathMessageService {

    private static final Set<EntityDamageEvent.DamageCause> ENVIRONMENTAL_CAUSES = Set.of(
            EntityDamageEvent.DamageCause.FALL,
            EntityDamageEvent.DamageCause.FIRE,
            EntityDamageEvent.DamageCause.FIRE_TICK,
            EntityDamageEvent.DamageCause.LAVA,
            EntityDamageEvent.DamageCause.DROWNING,
            EntityDamageEvent.DamageCause.VOID,
            EntityDamageEvent.DamageCause.SUFFOCATION,
            EntityDamageEvent.DamageCause.DRYOUT,
            EntityDamageEvent.DamageCause.FREEZE,
            EntityDamageEvent.DamageCause.HOT_FLOOR,
            EntityDamageEvent.DamageCause.WITHER,
            EntityDamageEvent.DamageCause.POISON,
            EntityDamageEvent.DamageCause.STARVATION,
            EntityDamageEvent.DamageCause.CRAMMING,
            EntityDamageEvent.DamageCause.CONTACT,
            EntityDamageEvent.DamageCause.MAGIC,
            EntityDamageEvent.DamageCause.SUICIDE
    );

    private final EzChat plugin;
    private final DeathMessageConfig deathMessageConfig;
    private final ChatToggleManager chatToggleManager;
    private final CommunicationLogService communicationLogService;
    private final DiscordNotificationService discordNotificationService;

    public DeathMessageService(final EzChat plugin,
                               final DeathMessageConfig deathMessageConfig,
                               final ChatToggleManager chatToggleManager,
                               final CommunicationLogService communicationLogService,
                               final DiscordNotificationService discordNotificationService) {
        this.plugin = plugin;
        this.deathMessageConfig = deathMessageConfig;
        this.chatToggleManager = chatToggleManager;
        this.communicationLogService = communicationLogService;
        this.discordNotificationService = discordNotificationService;
    }

    public boolean isFeatureEnabled() {
        return deathMessageConfig.isFeatureEnabled();
    }

    public boolean isDeathMessagesEnabled(final UUID playerUuid) {
        if (chatToggleManager.hasToggleState(playerUuid)) {
            return !chatToggleManager.areDeathMessagesDisabled(playerUuid);
        }

        return deathMessageConfig.isDefaultEnabled();
    }

    public boolean setDeathMessagesEnabled(final UUID playerUuid, final boolean enabled) {
        return chatToggleManager.setDeathMessagesDisabled(playerUuid, !enabled);
    }

    public boolean toggleDeathMessages(final UUID playerUuid) {
        final boolean nowDisabled = chatToggleManager.toggleDeathMessages(playerUuid);
        return !nowDisabled;
    }

    public void handleDeathMessage(final PlayerDeathEvent event) {
        if (!isFeatureEnabled()) {
            return;
        }

        final Player deadPlayer = event.getPlayer();
        final String baseMessage = resolveBaseMessage(event, deadPlayer);
        if (baseMessage == null || baseMessage.isBlank()) {
            return;
        }

        final DeathMessageCategory category = categorizeDeath(deadPlayer);
        final String formatted = plugin.colorize(applyPlaceholders(resolveFormat(category), deadPlayer, baseMessage));

        event.deathMessage(null);
        plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
            for (final Player online : plugin.getServer().getOnlinePlayers()) {
                if (!isDeathMessagesEnabled(online.getUniqueId())) {
                    continue;
                }

                online.sendMessage(formatted);
                playReceiveSound(online);
            }
        });

        if (deathMessageConfig.shouldLogDeathMessages()) {
            communicationLogService.logDeath(deadPlayer.getUniqueId(), deadPlayer.getName(), formatted);
        }
        discordNotificationService.sendDeathMessage(deadPlayer.getUniqueId(), deadPlayer.getName(), stripColors(formatted));
    }

    public DeathMessageCategory categorizeDeath(final Player deadPlayer) {
        final EntityDamageEvent lastDamage = deadPlayer.getLastDamageCause();
        if (lastDamage instanceof final EntityDamageByEntityEvent byEntityEvent) {
            final Entity damager = byEntityEvent.getDamager();
            if (damager instanceof Projectile) {
                return DeathMessageCategory.PROJECTILE_KILL;
            }

            if (damager instanceof Player) {
                return DeathMessageCategory.PLAYER_KILL;
            }

            return DeathMessageCategory.MOB_KILL;
        }

        if (lastDamage != null) {
            final EntityDamageEvent.DamageCause cause = lastDamage.getCause();
            if (ENVIRONMENTAL_CAUSES.contains(cause) || isVersionSafeEnvironmentalCause(cause)) {
                return DeathMessageCategory.ENVIRONMENTAL;
            }
        }

        return DeathMessageCategory.DEFAULT;
    }

    private boolean isVersionSafeEnvironmentalCause(final EntityDamageEvent.DamageCause cause) {
        final String causeName = cause.name().toUpperCase(Locale.ROOT);
        return "CACTUS".equals(causeName) || "SWEET_BERRY_BUSH".equals(causeName);
    }

    private String resolveBaseMessage(final PlayerDeathEvent event, final Player deadPlayer) {
        if (deathMessageConfig.useVanillaDeathMessage()) {
            final Component deathComponent = event.deathMessage();
            if (deathComponent != null) {
                final String legacyText = LegacyComponentSerializer.legacySection().serialize(deathComponent);
                if (!legacyText.isBlank()) {
                    return legacyText;
                }

                final String plainText = PlainTextComponentSerializer.plainText().serialize(deathComponent);
                if (!plainText.isBlank()) {
                    return plainText;
                }
            }
        }

        return deadPlayer.getName() + " died";
    }

    private String resolveFormat(final DeathMessageCategory category) {
        return switch (category) {
            case PLAYER_KILL -> deathMessageConfig.getFormat("player-kill", "&8[&cPvP&8] &f{message}");
            case MOB_KILL -> deathMessageConfig.getFormat("mob-kill", "&8[&6Mob&8] &f{message}");
            case PROJECTILE_KILL -> deathMessageConfig.getFormat("projectile-kill", "&8[&dProjectile&8] &f{message}");
            case ENVIRONMENTAL -> deathMessageConfig.getFormat("environmental", "&8[&7Death&8] &f{message}");
            case DEFAULT -> deathMessageConfig.getFormat("default", "&8[&4Death&8] &f{message}");
        };
    }

    private String applyPlaceholders(final String template,
                                     final Player deadPlayer,
                                     final String baseMessage) {
        final Map<String, String> placeholders = new LinkedHashMap<>();
        placeholders.put("player", deadPlayer.getName());
        placeholders.put("displayname", deadPlayer.getDisplayName());
        placeholders.put("message", baseMessage);

        String rendered = template;
        for (final Map.Entry<String, String> entry : placeholders.entrySet()) {
            rendered = rendered.replace("{" + entry.getKey() + "}", entry.getValue() == null ? "" : entry.getValue());
        }

        return rendered;
    }

    private void playReceiveSound(final Player player) {
        if (!deathMessageConfig.isSoundEnabled()) {
            return;
        }

        final Sound sound = deathMessageConfig.getSound();
        if (sound == null) {
            return;
        }

        player.playSound(player, sound, deathMessageConfig.getSoundVolume(), deathMessageConfig.getSoundPitch());
    }

    private String stripColors(final String input) {
        return input.replaceAll("(?i)§[0-9A-FK-ORX]", "");
    }
}
