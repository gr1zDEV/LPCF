package com.ezinnovations.ezchat.utils;

import com.ezinnovations.ezchat.EzChat;

import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Level;

public final class FloodgateHook {

    private final EzChat plugin;
    private Method isFloodgatePlayerMethod;
    private Object floodgateApiInstance;
    private boolean initialized;

    public FloodgateHook(final EzChat plugin) {
        this.plugin = plugin;
    }

    public boolean isBedrockPlayer(final Player player) {
        if (!initialized) {
            initialize();
        }

        if (isFloodgatePlayerMethod == null || floodgateApiInstance == null) {
            return false;
        }

        try {
            return (boolean) isFloodgatePlayerMethod.invoke(floodgateApiInstance, player.getUniqueId());
        } catch (IllegalAccessException | InvocationTargetException exception) {
            plugin.getLogger().log(Level.FINE, "Failed to query Floodgate API, disabling hook.", exception);
            isFloodgatePlayerMethod = null;
            floodgateApiInstance = null;
            return false;
        }
    }

    private void initialize() {
        initialized = true;

        try {
            final Class<?> floodgateApiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            final Method getInstanceMethod = floodgateApiClass.getMethod("getInstance");
            final Object instance = getInstanceMethod.invoke(null);
            final Method isFloodgatePlayer = floodgateApiClass.getMethod("isFloodgatePlayer", UUID.class);
            if (instance != null) {
                floodgateApiInstance = instance;
                isFloodgatePlayerMethod = isFloodgatePlayer;
                plugin.getLogger().info("Floodgate detected; Bedrock chat toggle support enabled.");
            }
        } catch (ClassNotFoundException ignored) {
            // Floodgate is not present, fallback gracefully.
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            plugin.getLogger().log(Level.WARNING, "Floodgate detected but API is incompatible; Bedrock chat detection disabled.", exception);
        }
    }
}
