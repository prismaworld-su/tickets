package smp.cloud.tickets.ticket.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import smp.cloud.tickets.ticket.TicketMessageFormatter;
import smp.cloud.tickets.ticket.TicketService;

import java.util.Objects;

public final class TicketsCommand implements SimpleCommand {

    private final TicketService service;
    private final String permission;

    public TicketsCommand(TicketService service, String permission) {
        this.service = Objects.requireNonNull(service, "service");
        this.permission = Objects.requireNonNull(permission, "permission");
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendMessage(TicketMessageFormatter.error("Команда доступна только игрокам."));
            return;
        }
        if (!player.hasPermission(permission)) {
            player.sendMessage(TicketMessageFormatter.error("Недостаточно прав."));
            return;
        }
        service.openGui(player);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return !(invocation.source() instanceof Player player) || player.hasPermission(permission);
    }
}
