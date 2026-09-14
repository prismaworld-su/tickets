package smp.cloud.tickets.webhook;

/**
 * Marker interface for classes that expose HTTP endpoints to the webhook server.
 * <p>
 * Implementations declare handler methods annotated with
 * {@link smp.cloud.tickets.webhook.annotation.Get},
 * {@link smp.cloud.tickets.webhook.annotation.Post},
 * {@link smp.cloud.tickets.webhook.annotation.Put} or
 * {@link smp.cloud.tickets.webhook.annotation.Delete}. Each handler method must
 * accept a single {@link smp.cloud.tickets.webhook.http.WebhookRequest} argument
 * and return a {@link smp.cloud.tickets.webhook.http.WebhookResponse}.
 */
public interface WebhookHandler {
}
