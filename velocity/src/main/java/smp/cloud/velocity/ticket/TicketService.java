package smp.cloud.velocity.ticket;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;
import smp.cloud.common.messaging.AcceptTicketPayload;
import smp.cloud.velocity.ticket.messaging.TicketMessenger;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TicketService {

    private final ProxyServer proxy;
    private final TicketRegistry registry;
    private final TicketMessenger messenger;
    private final Logger logger;
    private final Set<UUID> backendStaff = ConcurrentHashMap.newKeySet();

    public TicketService(ProxyServer proxy, TicketRegistry registry, TicketMessenger messenger, Logger logger) {
        this.proxy = Objects.requireNonNull(proxy, "proxy");
        this.registry = Objects.requireNonNull(registry, "registry");
        this.messenger = Objects.requireNonNull(messenger, "messenger");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    public TicketRegistry registry() {
        return registry;
    }

    public boolean isBackendStaff(UUID playerId) {
        return backendStaff.contains(playerId);
    }

    public void updateBackendStaff(UUID playerId, boolean staff) {
        if (staff) {
            backendStaff.add(playerId);
        } else {
            backendStaff.remove(playerId);
        }
    }

    public void sendMessage(Player sender, String content) {
        String trimmed = content == null ? "" : content.trim();
        if (trimmed.isEmpty()) {
            sender.sendMessage(TicketMessageFormatter.error("Пустое сообщение."));
            return;
        }
        Optional<Ticket> owned = registry.getOpenTicketByOwner(sender.getUniqueId());
        if (owned.isPresent()) {
            appendMessage(owned.get(), sender, trimmed, false);
            return;
        }
        Optional<Ticket> accepted = registry.getAcceptedTicketByStaff(sender.getUniqueId());
        if (accepted.isPresent()) {
            appendMessage(accepted.get(), sender, trimmed, true);
            return;
        }
        Optional<Ticket> created = registry.createTicket(sender.getUniqueId(), sender.getUsername());
        if (created.isEmpty()) {
            sender.sendMessage(TicketMessageFormatter.error("У вас уже есть открытый тикет."));
            return;
        }
        Ticket ticket = created.get();
        sender.sendMessage(TicketMessageFormatter.info(
                "Тикет #" + ticket.displayId() + " создан. Ожидайте ответа администратора."));
        appendMessage(ticket, sender, trimmed, false);
        logger.info("Ticket #{} opened by {} ({})", ticket.displayId(), sender.getUsername(), sender.getUniqueId());
    }

    public void close(Player caller) {
        Optional<Ticket> ticket = findActiveTicket(caller);
        if (ticket.isEmpty()) {
            caller.sendMessage(TicketMessageFormatter.error("У вас нет активного тикета."));
            return;
        }
        Ticket t = ticket.get();
        if (!registry.close(t.id())) {
            caller.sendMessage(TicketMessageFormatter.error("Не удалось закрыть тикет."));
            return;
        }
        Component notice = TicketMessageFormatter.system(
                "Тикет #" + t.displayId() + " закрыт " + caller.getUsername() + ".");
        deliverToParticipants(t, notice);
        logger.info("Ticket #{} closed by {}", t.displayId(), caller.getUsername());
    }

    public void keepOpen(Player caller) {
        Optional<Ticket> ticket = findActiveTicket(caller);
        if (ticket.isEmpty()) {
            caller.sendMessage(TicketMessageFormatter.error("У вас нет активного тикета."));
            return;
        }
        Ticket t = ticket.get();
        registry.addMessage(t, caller.getUniqueId(), caller.getUsername(), "«Проблема актуальна»");
        Component notice = TicketMessageFormatter.system(
                caller.getUsername() + " отметил тикет #" + t.displayId() + " как актуальный.");
        deliverToParticipants(t, notice);
    }

    public void openGui(Player staff) {
        if (!messenger.sendOpenGui(staff, registry.listUnaccepted())) {
            staff.sendMessage(TicketMessageFormatter.error("Вы не подключены к серверу."));
        }
    }

    public void onAcceptFromBackend(AcceptTicketPayload payload) {
        boolean accepted = registry.accept(payload.ticketId(), payload.staffId(), payload.staffName());
        Ticket ticket = registry.getTicket(payload.ticketId()).orElse(null);
        if (ticket == null) {
            logger.warn("Received accept for unknown ticket {}", payload.ticketId());
            return;
        }
        Optional<Player> staff = proxy.getPlayer(payload.staffId());
        if (!accepted) {
            staff.ifPresent(p -> p.sendMessage(
                    TicketMessageFormatter.error("Тикет уже принят или недоступен.")));
            return;
        }
        Component notice = TicketMessageFormatter.info(
                "Тикет #" + ticket.displayId() + " принят: " + payload.staffName() + ".");
        deliverToParticipants(ticket, notice);
        logger.info("Ticket #{} accepted by {}", ticket.displayId(), payload.staffName());
    }

    private Optional<Ticket> findActiveTicket(Player caller) {
        Optional<Ticket> owned = registry.getOpenTicketByOwner(caller.getUniqueId());
        if (owned.isPresent()) {
            return owned;
        }
        return registry.getAcceptedTicketByStaff(caller.getUniqueId());
    }

    private void appendMessage(Ticket ticket, Player sender, String content, boolean fromStaff) {
        registry.addMessage(ticket, sender.getUniqueId(), sender.getUsername(), content);
        TicketMessage last = ticket.lastMessage().orElseThrow();
        Component component = TicketMessageFormatter.ticketMessage(ticket, last, fromStaff);
        deliverToParticipants(ticket, component);
    }

    private void deliverToParticipants(Ticket ticket, Component component) {
        proxy.getPlayer(ticket.ownerId()).ifPresent(p -> p.sendMessage(component));
        ticket.acceptedBy().flatMap(proxy::getPlayer).ifPresent(p -> p.sendMessage(component));
    }
}
