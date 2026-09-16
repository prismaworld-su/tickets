package smp.cloud.velocity.i18n;

import org.slf4j.Logger;
import smp.cloud.velocity.toml.TomlParseException;
import smp.cloud.velocity.toml.TomlParser;
import smp.cloud.velocity.toml.TomlTable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class MessagesLoader {

    static final String MESSAGES_FILE_NAME = "messages.toml";

    private static final String DEFAULT_TEMPLATE = """
            # Ticket plugin messages.
            # Placeholders inside values use the {name} syntax and are substituted at runtime.

            [chat]
            prefix = "[Тикет] "
            # Placeholders: {id}
            ticket-prefix = "[Тикет #{id}] "
            button-separator = " | "

            [chat.button]
            keep-open = "[Проблема актуальна]"
            close = "[Закрыть обращение]"

            [system]
            empty-message = "Пустое сообщение."
            already-open = "У вас уже есть открытый тикет."
            # Placeholders: {id}
            created = "Тикет #{id} создан. Ожидайте ответа администратора."
            no-active = "У вас нет активного тикета."
            close-failed = "Не удалось закрыть тикет."
            # Placeholders: {id}, {actor}
            closed = "Тикет #{id} закрыт {actor}."
            # Placeholders: {actor}, {id}
            keep-open-broadcast = "{actor} отметил тикет #{id} как актуальный."
            keep-open-content = "«Проблема актуальна»"
            not-connected = "Вы не подключены к серверу."
            already-accepted = "Тикет уже принят или недоступен."
            # Placeholders: {id}, {staff}
            accepted = "Тикет #{id} принят: {staff}."

            [command]
            players-only = "Команда доступна только игрокам."
            usage = "Использование: /ticket <сообщение> либо /ticket close."
            no-permission = "Недостаточно прав."

            [gui]
            title = "Открытые тикеты"
            hint = "Нажмите, чтобы принять тикет"
            preview-empty = "(нет сообщений)"
            """;

    private final Path dataDirectory;
    private final Logger logger;

    public MessagesLoader(Path dataDirectory, Logger logger) {
        this.dataDirectory = Objects.requireNonNull(dataDirectory, "dataDirectory");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    public Messages load() throws IOException {
        Path file = dataDirectory.resolve(MESSAGES_FILE_NAME);
        if (Files.notExists(file)) {
            Files.createDirectories(file.getParent());
            Files.writeString(file, DEFAULT_TEMPLATE, StandardCharsets.UTF_8);
            logger.info("Created default messages at {}", file);
            return Messages.defaults();
        }
        String source = Files.readString(file, StandardCharsets.UTF_8);
        try {
            TomlTable root = TomlParser.parse(source);
            return Messages.fromToml(root);
        } catch (TomlParseException | IllegalStateException e) {
            logger.error("Failed to read {}: {}. Falling back to defaults.", file, e.getMessage());
            return Messages.defaults();
        }
    }
}
