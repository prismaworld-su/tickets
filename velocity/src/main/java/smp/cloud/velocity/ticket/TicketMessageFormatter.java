package smp.cloud.velocity.ticket;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import smp.cloud.velocity.i18n.Messages;

import java.util.Objects;

public record TicketMessageFormatter(Messages messages) {

    public TicketMessageFormatter(Messages messages) {
        this.messages = Objects.requireNonNull(messages, "messages");
    }

    public Component ticketMessage(Ticket ticket, TicketMessage message, boolean fromStaff) {
        String header = messages.format(Messages.CHAT_TICKET_PREFIX, "id", String.valueOf(ticket.displayId()));
        String separator = messages.get(Messages.CHAT_BUTTON_SEPARATOR);
        String keepOpenLabel = messages.get(Messages.CHAT_BUTTON_KEEP_OPEN);
        String closeLabel = messages.get(Messages.CHAT_BUTTON_CLOSE);

        TextComponent.Builder builder = Component.text()
                .append(Component.text(header, NamedTextColor.GOLD))
                .append(Component.text(message.authorName() + ": ",
                        fromStaff ? NamedTextColor.RED : NamedTextColor.YELLOW))
                .append(Component.text(message.content(), NamedTextColor.WHITE));

        if (fromStaff) {
            builder
                    .append(Component.text(separator, NamedTextColor.DARK_GRAY))
                    .append(button(keepOpenLabel, NamedTextColor.GREEN, "/ticket keepopen"))
                    .append(Component.text(" ", NamedTextColor.DARK_GRAY))
                    .append(button(closeLabel, NamedTextColor.RED, "/ticket close"));
        }

        return builder.build();
    }

    public Component info(String key, String... placeholders) {
        return withPrefix(messages.format(key, placeholders), NamedTextColor.WHITE);
    }

    public Component error(String key, String... placeholders) {
        return withPrefix(messages.format(key, placeholders), NamedTextColor.RED);
    }

    public Component system(String key, String... placeholders) {
        return withPrefix(messages.format(key, placeholders), NamedTextColor.GRAY);
    }

    private Component withPrefix(String body, NamedTextColor color) {
        return Component.text()
                .append(Component.text(messages.get(Messages.CHAT_PREFIX), NamedTextColor.GOLD))
                .append(Component.text(body, color))
                .build();
    }

    private static Component button(String label, NamedTextColor color, String command) {
        return Component.text(label, color)
                .decorate(TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand(command))
                .hoverEvent(HoverEvent.showText(Component.text(command, NamedTextColor.GRAY)));
    }
}
