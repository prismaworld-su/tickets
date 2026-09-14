package smp.cloud.tickets.config;

import org.slf4j.Logger;
import smp.cloud.tickets.toml.TomlParseException;
import smp.cloud.tickets.toml.TomlParser;
import smp.cloud.tickets.toml.TomlTable;

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
        WebhookConfig defaults = WebhookConfig.defaults();
        TomlTable webhookTable = root.getTable("webhook").orElseGet(TomlTable::empty);
        WebhookConfig webhook = new WebhookConfig(
                webhookTable.getBooleanOr("enabled", defaults.enabled()),
                webhookTable.getStringOr("host", defaults.host()),
                webhookTable.getIntOr("port", defaults.port()),
                webhookTable.getIntOr("backlog", defaults.backlog())
        );
        return new TicketsConfig(webhook);
    }
}
