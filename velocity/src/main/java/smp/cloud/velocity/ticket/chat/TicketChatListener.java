package smp.cloud.velocity.ticket.chat;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import smp.cloud.velocity.ticket.TicketService;

import java.util.Objects;

public final class TicketChatListener {

    private final TicketService service;
    private final String prefix;

    public TicketChatListener(TicketService service, String prefix) {
        this.service = Objects.requireNonNull(service, "service");
        this.prefix = prefix == null ? "" : prefix;
    }

    @Subscribe(order = PostOrder.EARLY)
    public void onChat(PlayerChatEvent event) {
        if (!event.getResult().isAllowed()) {
            return;
        }
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (!prefix.isEmpty() && message.startsWith(prefix)) {
            String content = message.substring(prefix.length()).trim();
            if (!content.isEmpty()) {
                service.sendMessage(player, content);
            }
            event.setResult(PlayerChatEvent.ChatResult.denied());
            return;
        }

        if (service.hasOpenOwnedTicket(player.getUniqueId())) {
            service.sendMessage(player, message);
            event.setResult(PlayerChatEvent.ChatResult.denied());
        }
    }
}
