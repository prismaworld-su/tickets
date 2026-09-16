package smp.cloud.velocity.ticket.messaging;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.slf4j.Logger;
import smp.cloud.common.messaging.AcceptTicketPayload;
import smp.cloud.common.messaging.OpenGuiPayload;
import smp.cloud.common.messaging.StaffStatusPayload;
import smp.cloud.common.messaging.TicketChannel;
import smp.cloud.common.messaging.TicketPayload;
import smp.cloud.common.messaging.TicketProtocol;
import smp.cloud.common.messaging.TicketSummary;
import smp.cloud.velocity.i18n.Messages;
import smp.cloud.velocity.ticket.Ticket;
import smp.cloud.velocity.ticket.TicketMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class TicketMessenger {

    private static final int PREVIEW_LIMIT = 60;

    private final Logger logger;
    private final Supplier<Messages> messagesSupplier;
    private final ChannelIdentifier channel;
    private volatile Consumer<AcceptTicketPayload> acceptHandler = payload -> {};
    private volatile Consumer<StaffStatusPayload> staffStatusHandler = payload -> {};

    public TicketMessenger(Logger logger, Supplier<Messages> messagesSupplier) {
        this.logger = Objects.requireNonNull(logger, "logger");
        this.messagesSupplier = Objects.requireNonNull(messagesSupplier, "messagesSupplier");
        this.channel = MinecraftChannelIdentifier.create(TicketChannel.NAMESPACE, TicketChannel.NAME);
    }

    public ChannelIdentifier channel() {
        return channel;
    }

    public void setAcceptHandler(Consumer<AcceptTicketPayload> handler) {
        this.acceptHandler = Objects.requireNonNull(handler, "handler");
    }

    public void setStaffStatusHandler(Consumer<StaffStatusPayload> handler) {
        this.staffStatusHandler = Objects.requireNonNull(handler, "handler");
    }

    public boolean sendOpenGui(Player viewer, List<Ticket> tickets) {
        Optional<ServerConnection> connection = viewer.getCurrentServer();
        if (connection.isEmpty()) {
            return false;
        }
        Messages messages = messagesSupplier.get();
        List<TicketSummary> summaries = new ArrayList<>(tickets.size());
        for (Ticket ticket : tickets) {
            summaries.add(toSummary(ticket, messages));
        }
        OpenGuiPayload payload = new OpenGuiPayload(
                viewer.getUniqueId(),
                messages.get(Messages.GUI_TITLE),
                messages.get(Messages.GUI_HINT),
                summaries
        );
        connection.get().sendPluginMessage(channel, TicketProtocol.encode(payload));
        return true;
    }

    @Subscribe(order = PostOrder.EARLY)
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(channel)) {
            return;
        }
        event.setResult(PluginMessageEvent.ForwardResult.handled());
        if (!(event.getSource() instanceof ServerConnection)) {
            return;
        }
        try {
            TicketPayload payload = TicketProtocol.decode(event.getData());
            switch (payload) {
                case AcceptTicketPayload accept -> acceptHandler.accept(accept);
                case StaffStatusPayload staff -> staffStatusHandler.accept(staff);
                default -> logger.warn("Unexpected payload on channel {}: {}", channel.getId(),
                        payload.getClass().getSimpleName());
            }
        } catch (IOException e) {
            logger.warn("Failed to decode plugin message on {}", channel.getId(), e);
        }
    }

    private TicketSummary toSummary(Ticket ticket, Messages messages) {
        String preview = ticket.lastMessage()
                .map(TicketMessage::content)
                .orElseGet(() -> messages.get(Messages.GUI_PREVIEW_EMPTY));
        if (preview.length() > PREVIEW_LIMIT) {
            preview = preview.substring(0, PREVIEW_LIMIT - 3) + "...";
        }
        return new TicketSummary(
                ticket.id(),
                ticket.ownerId(),
                ticket.ownerName(),
                preview,
                ticket.createdAt().toEpochMilli()
        );
    }
}
