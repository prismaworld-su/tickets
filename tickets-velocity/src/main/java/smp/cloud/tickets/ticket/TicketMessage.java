package smp.cloud.tickets.ticket;

import java.time.Instant;
import java.util.UUID;

public record TicketMessage(UUID authorId, String authorName, Instant timestamp, String content) {

    public TicketMessage {
        if (authorName == null || authorName.isBlank()) {
            throw new IllegalArgumentException("authorName must not be blank");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("timestamp must not be null");
        }
        if (content == null) {
            content = "";
        }
    }
}
