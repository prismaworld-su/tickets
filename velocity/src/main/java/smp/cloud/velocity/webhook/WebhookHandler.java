package smp.cloud.velocity.webhook;

/**
 * Marker interface for classes that expose HTTP endpoints to the webhook server.
 * <p>
 * Implementations declare handler methods annotated with
 * {@link smp.cloud.velocity.webhook.annotation.Get},
 * {@link smp.cloud.velocity.webhook.annotation.Post},
 * {@link smp.cloud.velocity.webhook.annotation.Put} or
 * {@link smp.cloud.velocity.webhook.annotation.Delete}. Each handler method must
 * accept a single {@link smp.cloud.velocity.webhook.http.WebhookRequest} argument
 * and return a {@link smp.cloud.velocity.webhook.http.WebhookResponse}.
 */
public interface WebhookHandler {
}
