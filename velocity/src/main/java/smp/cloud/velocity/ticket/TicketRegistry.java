package smp.cloud.velocity.ticket;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class TicketRegistry {

    private final Object lock = new Object();
    private final Map<UUID, Ticket> ticketsById = new HashMap<>();
    private final Map<UUID, UUID> openTicketByOwner = new HashMap<>();
    private final Map<UUID, UUID> acceptedTicketByStaff = new HashMap<>();
    private final Clock clock;
    private long nextDisplayId = 1;

    public TicketRegistry() {
        this(Clock.systemUTC());
    }

    public TicketRegistry(Clock clock) {
        this.clock = clock;
    }

    public Optional<Ticket> createTicket(UUID ownerId, String ownerName) {
        synchronized (lock) {
            if (openTicketByOwner.containsKey(ownerId)) {
                return Optional.empty();
            }
            UUID id = UUID.randomUUID();
            Ticket ticket = new Ticket(id, nextDisplayId++, ownerId, ownerName, clock.instant());
            ticketsById.put(id, ticket);
            openTicketByOwner.put(ownerId, id);
            return Optional.of(ticket);
        }
    }

    public Optional<Ticket> getTicket(UUID id) {
        synchronized (lock) {
            return Optional.ofNullable(ticketsById.get(id));
        }
    }

    public Optional<Ticket> getOpenTicketByOwner(UUID ownerId) {
        synchronized (lock) {
            UUID id = openTicketByOwner.get(ownerId);
            return id == null ? Optional.empty() : Optional.ofNullable(ticketsById.get(id));
        }
    }

    public Optional<Ticket> getAcceptedTicketByStaff(UUID staffId) {
        synchronized (lock) {
            UUID id = acceptedTicketByStaff.get(staffId);
            return id == null ? Optional.empty() : Optional.ofNullable(ticketsById.get(id));
        }
    }

    public List<Ticket> listUnaccepted() {
        synchronized (lock) {
            List<Ticket> result = new ArrayList<>();
            for (Ticket ticket : ticketsById.values()) {
                if (!ticket.closed() && !ticket.accepted()) {
                    result.add(ticket);
                }
            }
            return List.copyOf(result);
        }
    }

    public boolean accept(UUID ticketId, UUID staffId, String staffName) {
        synchronized (lock) {
            Ticket ticket = ticketsById.get(ticketId);
            if (ticket == null || ticket.closed() || ticket.accepted()) {
                return false;
            }
            if (acceptedTicketByStaff.containsKey(staffId)) {
                return false;
            }
            ticket.accept(staffId, staffName);
            acceptedTicketByStaff.put(staffId, ticketId);
            return true;
        }
    }

    public boolean close(UUID ticketId) {
        synchronized (lock) {
            Ticket ticket = ticketsById.get(ticketId);
            if (ticket == null || ticket.closed()) {
                return false;
            }
            ticket.close();
            openTicketByOwner.remove(ticket.ownerId(), ticketId);
            ticket.acceptedBy().ifPresent(staffId -> acceptedTicketByStaff.remove(staffId, ticketId));
            return true;
        }
    }

    public void addMessage(Ticket ticket, UUID authorId, String authorName, String content) {
        ticket.addMessage(new TicketMessage(authorId, authorName, clock.instant(), content));
    }
}
