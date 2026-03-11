package com.ezinnovations.ezchat.discord;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.config.DiscordConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DiscordWebhookService {

    private static final long ERROR_LOG_COOLDOWN_MS = 60000L;

    private final EzChat plugin;
    private final DiscordConfig config;
    private final DiscordWebhookRouter router;
    private final DiscordMessageBuilder messageBuilder;
    private final HttpClient httpClient;
    private final Map<String, Long> lastErrorByWebhook;

    public DiscordWebhookService(final EzChat plugin,
                                 final DiscordConfig config,
                                 final DiscordWebhookRouter router,
                                 final DiscordMessageBuilder messageBuilder) {
        this.plugin = plugin;
        this.config = config;
        this.router = router;
        this.messageBuilder = messageBuilder;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        this.lastErrorByWebhook = new ConcurrentHashMap<>();
    }

    public void send(final DiscordEventType type,
                     final UUID actorUuid,
                     final String actorName,
                     final String formatKey,
                     final String fallbackFormat,
                     final Map<String, String> placeholders) {
        if (!config.isFeatureEnabled() || !config.isEventEnabled(type)) {
            return;
        }

        final String webhookUrl = router.resolveWebhookUrl(type);
        if (webhookUrl.isBlank()) {
            return;
        }

        final String payload = messageBuilder.buildPayload(type, actorUuid, actorName, formatKey, fallbackFormat, placeholders);
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> postWebhook(webhookUrl, payload));
    }

    private void postWebhook(final String webhookUrl, final String payload) {
        try {
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .timeout(Duration.ofSeconds(6))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            final HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() >= 300) {
                logErrorWithCooldown(webhookUrl, "Discord webhook returned status " + response.statusCode());
            }
        } catch (final IllegalArgumentException exception) {
            logErrorWithCooldown(webhookUrl, "Invalid Discord webhook URL format.");
        } catch (final IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            logErrorWithCooldown(webhookUrl, "Discord webhook delivery failed: " + exception.getMessage());
        }
    }

    private void logErrorWithCooldown(final String webhookUrl, final String message) {
        final long now = System.currentTimeMillis();
        final long lastLogged = lastErrorByWebhook.getOrDefault(webhookUrl, 0L);
        if (now - lastLogged < ERROR_LOG_COOLDOWN_MS) {
            return;
        }

        lastErrorByWebhook.put(webhookUrl, now);
        plugin.getLogger().warning("[EzChat] " + message);
    }
}
