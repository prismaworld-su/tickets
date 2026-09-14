package smp.cloud.velocity.ticket;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public final class TicketMessageFormatter {

    private TicketMessageFormatter() {
    }

    public static Component ticketMessage(Ticket ticket, TicketMessage message, boolean fromStaff) {
        Component header = Component.text("[Тикет #" + ticket.displayId() + "] ", NamedTextColor.GOLD);
        Component author = Component.text(message.authorName() + ": ",
                fromStaff ? NamedTextColor.AQUA : NamedTextColor.YELLOW);
        Component body = Component.text(message.content(), NamedTextColor.WHITE);
        return Component.text()
                .append(header)
                .append(author)
                .append(body)
                .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                .append(button("[Проблема актуальна]", NamedTextColor.GREEN, "/ticket keepopen"))
                .append(Component.text(" ", NamedTextColor.DARK_GRAY))
                .append(button("[Закрыть обращение]", NamedTextColor.RED, "/ticket close"))
                .build();
    }

    public static Component system(String text) {
        return Component.text()
                .append(Component.text("[Тикет] ", NamedTextColor.GOLD))
                .append(Component.text(text, NamedTextColor.GRAY))
                .build();
    }

    public static Component info(String text) {
        return Component.text()
                .append(Component.text("[Тикет] ", NamedTextColor.GOLD))
                .append(Component.text(text, NamedTextColor.WHITE))
                .build();
    }

    public static Component error(String text) {
        return Component.text()
                .append(Component.text("[Тикет] ", NamedTextColor.GOLD))
                .append(Component.text(text, NamedTextColor.RED))
                .build();
    }

    private static Component button(String label, NamedTextColor color, String command) {
        return Component.text(label, color)
                .decorate(TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand(command))
                .hoverEvent(HoverEvent.showText(Component.text(command, NamedTextColor.GRAY)));
    }
}
