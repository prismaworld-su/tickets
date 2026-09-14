package smp.cloud.tickets.config;

import smp.cloud.tickets.webhook.server.WebhookServerConfig;

public record WebhookConfig(boolean enabled, String host, int port, int backlog) {

    public static WebhookConfig defaults() {
        return new WebhookConfig(true, "0.0.0.0", 8080, 0);
    }

    public WebhookServerConfig toServerConfig() {
        return new WebhookServerConfig(host, port, backlog);
    }
}
