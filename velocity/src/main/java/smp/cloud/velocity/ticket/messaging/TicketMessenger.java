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
import smp.cloud.common.messaging.TicketChannel;
import smp.cloud.common.messaging.TicketPayload;
import smp.cloud.common.messaging.TicketProtocol;
import smp.cloud.common.messaging.TicketSummary;
import smp.cloud.velocity.ticket.Ticket;
import smp.cloud.velocity.ticket.TicketMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public final class TicketMessenger {

    private static final int PREVIEW_LIMIT = 60;

    private final Logger logger;
    private final ChannelIdentifier channel;
    private volatile Consumer<AcceptTicketPayload> acceptHandler = payload -> {};

    public TicketMessenger(Logger logger) {
        this.logger = Objects.requireNonNull(logger, "logger");
        this.channel = MinecraftChannelIdentifier.create(TicketChannel.NAMESPACE, TicketChannel.NAME);
    }

    public ChannelIdentifier channel() {
        return channel;
    }

    public void setAcceptHandler(Consumer<AcceptTicketPayload> handler) {
        this.acceptHandler = Objects.requireNonNull(handler, "handler");
    }

    public boolean sendOpenGui(Player viewer, List<Ticket> tickets) {
        Optional<ServerConnection> connection = viewer.getCurrentServer();
        if (connection.isEmpty()) {
            return false;
        }
        List<TicketSummary> summaries = new ArrayList<>(tickets.size());
        for (Ticket ticket : tickets) {
            summaries.add(toSummary(ticket));
        }
        OpenGuiPayload payload = new OpenGuiPayload(viewer.getUniqueId(), summaries);
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
            if (payload instanceof AcceptTicketPayload accept) {
                acceptHandler.accept(accept);
            } else {
                logger.warn("Unexpected payload on channel {}: {}", channel.getId(), payload.getClass().getSimpleName());
            }
        } catch (IOException e) {
            logger.warn("Failed to decode plugin message on {}", channel.getId(), e);
        }
    }

    private TicketSummary toSummary(Ticket ticket) {
        String preview = ticket.lastMessage()
                .map(TicketMessage::content)
                .orElse("(нет сообщений)");
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
