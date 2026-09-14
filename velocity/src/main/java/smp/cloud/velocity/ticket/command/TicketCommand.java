package smp.cloud.velocity.ticket.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import smp.cloud.velocity.ticket.TicketMessageFormatter;
import smp.cloud.velocity.ticket.TicketService;

import java.util.Objects;

public final class TicketCommand implements SimpleCommand {

    private final TicketService service;

    public TicketCommand(TicketService service) {
        this.service = Objects.requireNonNull(service, "service");
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendMessage(TicketMessageFormatter.error("Команда доступна только игрокам."));
            return;
        }
        String[] args = invocation.arguments();
        if (args.length == 0) {
            player.sendMessage(TicketMessageFormatter.info(
                    "Использование: /ticket <сообщение> либо /ticket close."));
            return;
        }
        if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "close" -> {
                    service.close(player);
                    return;
                }
                case "keepopen" -> {
                    service.keepOpen(player);
                    return;
                }
                default -> {
                }
            }
        }
        service.sendMessage(player, String.join(" ", args));
    }
}
