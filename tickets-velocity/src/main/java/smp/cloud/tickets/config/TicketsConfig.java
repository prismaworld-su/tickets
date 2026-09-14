package smp.cloud.tickets.config;

public record TicketsConfig(WebhookConfig webhook, TicketingConfig ticketing) {

    public static TicketsConfig defaults() {
        return new TicketsConfig(WebhookConfig.defaults(), TicketingConfig.defaults());
    }
}
