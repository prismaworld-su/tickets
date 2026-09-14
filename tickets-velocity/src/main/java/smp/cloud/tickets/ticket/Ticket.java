package smp.cloud.tickets.ticket;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public final class Ticket {

    private final UUID id;
    private final long displayId;
    private final UUID ownerId;
    private final String ownerName;
    private final Instant createdAt;
    private final List<TicketMessage> messages = new CopyOnWriteArrayList<>();

    private volatile UUID acceptedBy;
    private volatile String acceptedByName;
    private volatile boolean closed;

    Ticket(UUID id, long displayId, UUID ownerId, String ownerName, Instant createdAt) {
        this.id = id;
        this.displayId = displayId;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.createdAt = createdAt;
    }

    public UUID id() {
        return id;
    }

    public long displayId() {
        return displayId;
    }

    public UUID ownerId() {
        return ownerId;
    }

    public String ownerName() {
        return ownerName;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Optional<UUID> acceptedBy() {
        return Optional.ofNullable(acceptedBy);
    }

    public Optional<String> acceptedByName() {
        return Optional.ofNullable(acceptedByName);
    }

    public boolean accepted() {
        return acceptedBy != null;
    }

    public boolean closed() {
        return closed;
    }

    public List<TicketMessage> messages() {
        return List.copyOf(messages);
    }

    public Optional<TicketMessage> lastMessage() {
        int size = messages.size();
        if (size == 0) {
            return Optional.empty();
        }
        return Optional.of(messages.get(size - 1));
    }

    void addMessage(TicketMessage message) {
        messages.add(message);
    }

    void accept(UUID staffId, String staffName) {
        this.acceptedBy = staffId;
        this.acceptedByName = staffName;
    }

    void close() {
        this.closed = true;
    }
}
