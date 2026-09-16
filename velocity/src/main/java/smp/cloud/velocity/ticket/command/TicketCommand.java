package smp.cloud.velocity.ticket.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import smp.cloud.velocity.i18n.Messages;
import smp.cloud.velocity.ticket.TicketMessageFormatter;
import smp.cloud.velocity.ticket.TicketService;

import java.util.Objects;

public final class TicketCommand implements SimpleCommand {

    private final TicketService service;
    private final TicketMessageFormatter formatter;

    public TicketCommand(TicketService service) {
        this.service = Objects.requireNonNull(service, "service");
        this.formatter = service.formatter();
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendMessage(formatter.error(Messages.COMMAND_PLAYERS_ONLY));
            return;
        }
        String[] args = invocation.arguments();
        if (args.length == 0) {
            player.sendMessage(formatter.info(Messages.COMMAND_USAGE));
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
