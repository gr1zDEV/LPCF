package com.ezinnovations.ezchat.discord;

import com.ezinnovations.ezchat.config.DiscordConfig;

public final class DiscordWebhookRouter {

    private final DiscordConfig config;

    public DiscordWebhookRouter(final DiscordConfig config) {
        this.config = config;
    }

    public String resolveWebhookUrl(final DiscordEventType type) {
        final String eventWebhook = config.getWebhookFor(type);
        if (!eventWebhook.isBlank()) {
            return eventWebhook;
        }

        final String defaultWebhook = config.getDefaultWebhook();
        if (!defaultWebhook.isBlank()) {
            return defaultWebhook;
        }

        return "";
    }
}
