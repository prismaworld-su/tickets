package smp.cloud.tickets.config;

public record TicketsConfig(WebhookConfig webhook) {

    public static TicketsConfig defaults() {
        return new TicketsConfig(WebhookConfig.defaults());
    }
}
