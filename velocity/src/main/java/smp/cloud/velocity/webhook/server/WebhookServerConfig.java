package smp.cloud.velocity.webhook.server;

public record WebhookServerConfig(String host, int port, int backlog) {

    public WebhookServerConfig {
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("host must not be blank");
        }
        if (port < 0 || port > 65_535) {
            throw new IllegalArgumentException("port must be in [0, 65535]");
        }
        if (backlog < 0) {
            throw new IllegalArgumentException("backlog must be >= 0");
        }
    }

    public static WebhookServerConfig defaults() {
        return new WebhookServerConfig("0.0.0.0", 8080, 0);
    }
}
