package smp.cloud.velocity.config;

import org.slf4j.Logger;
import smp.cloud.velocity.toml.TomlParseException;
import smp.cloud.velocity.toml.TomlParser;
import smp.cloud.velocity.toml.TomlTable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class ConfigLoader {

    static final String CONFIG_FILE_NAME = "config.toml";

    private static final String DEFAULT_CONFIG = """
            # Tickets plugin configuration.

            [webhook]
            # Whether the webhook REST server should start with the plugin.
            enabled = true

            # Host to bind the webhook server to.
            host = "0.0.0.0"

            # Port the webhook server listens on.
            port = 8080

            # TCP accept backlog. 0 means "use the system default".
            backlog = 0

            [tickets]
            # Enable the ticket system (/ticket, /tickets, ? chat prefix).
            enabled = true

            # Permission required to open /tickets and accept tickets.
            staff-permission = "tickets.staff"

            # Chat prefix that routes the rest of the message into the ticket.
            # Set to an empty string to disable the alias.
            chat-prefix = "?"
            """;

    private final Path dataDirectory;
    private final Logger logger;

    public ConfigLoader(Path dataDirectory, Logger logger) {
        this.dataDirectory = Objects.requireNonNull(dataDirectory, "dataDirectory");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    public TicketsConfig load() throws IOException {
        Path configFile = dataDirectory.resolve(CONFIG_FILE_NAME);
        if (Files.notExists(configFile)) {
            writeDefault(configFile);
            logger.info("Created default configuration at {}", configFile);
            return TicketsConfig.defaults();
        }
        String source = Files.readString(configFile, StandardCharsets.UTF_8);
        try {
            TomlTable root = TomlParser.parse(source);
            return fromToml(root);
        } catch (TomlParseException | IllegalStateException e) {
            logger.error("Failed to read {}: {}. Falling back to defaults.", configFile, e.getMessage());
            return TicketsConfig.defaults();
        }
    }

    private void writeDefault(Path configFile) throws IOException {
        Files.createDirectories(configFile.getParent());
        Files.writeString(configFile, DEFAULT_CONFIG, StandardCharsets.UTF_8);
    }

    private TicketsConfig fromToml(TomlTable root) {
        WebhookConfig webhookDefaults = WebhookConfig.defaults();
        TomlTable webhookTable = root.getTable("webhook").orElseGet(TomlTable::empty);
        WebhookConfig webhook = new WebhookConfig(
                webhookTable.getBooleanOr("enabled", webhookDefaults.enabled()),
                webhookTable.getStringOr("host", webhookDefaults.host()),
                webhookTable.getIntOr("port", webhookDefaults.port()),
                webhookTable.getIntOr("backlog", webhookDefaults.backlog())
        );

        TicketingConfig ticketingDefaults = TicketingConfig.defaults();
        TomlTable ticketsTable = root.getTable("tickets").orElseGet(TomlTable::empty);
        TicketingConfig ticketing = new TicketingConfig(
                ticketsTable.getBooleanOr("enabled", ticketingDefaults.enabled()),
                ticketsTable.getStringOr("staff-permission", ticketingDefaults.staffPermission()),
                ticketsTable.getStringOr("chat-prefix", ticketingDefaults.chatPrefix())
        );

        return new TicketsConfig(webhook, ticketing);
    }
}
