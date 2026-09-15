package smp.cloud.velocity.i18n;

import smp.cloud.velocity.toml.TomlTable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class Messages {

    public static final String CHAT_PREFIX = "chat.prefix";
    public static final String CHAT_TICKET_PREFIX = "chat.ticket-prefix";
    public static final String CHAT_BUTTON_SEPARATOR = "chat.button-separator";
    public static final String CHAT_BUTTON_KEEP_OPEN = "chat.button.keep-open";
    public static final String CHAT_BUTTON_CLOSE = "chat.button.close";

    public static final String SYSTEM_EMPTY_MESSAGE = "system.empty-message";
    public static final String SYSTEM_ALREADY_OPEN = "system.already-open";
    public static final String SYSTEM_CREATED = "system.created";
    public static final String SYSTEM_NO_ACTIVE = "system.no-active";
    public static final String SYSTEM_CLOSE_FAILED = "system.close-failed";
    public static final String SYSTEM_CLOSED = "system.closed";
    public static final String SYSTEM_KEEP_OPEN_BROADCAST = "system.keep-open-broadcast";
    public static final String SYSTEM_KEEP_OPEN_CONTENT = "system.keep-open-content";
    public static final String SYSTEM_NOT_CONNECTED = "system.not-connected";
    public static final String SYSTEM_ALREADY_ACCEPTED = "system.already-accepted";
    public static final String SYSTEM_ACCEPTED = "system.accepted";

    public static final String COMMAND_PLAYERS_ONLY = "command.players-only";
    public static final String COMMAND_USAGE = "command.usage";
    public static final String COMMAND_NO_PERMISSION = "command.no-permission";

    public static final String GUI_TITLE = "gui.title";
    public static final String GUI_HINT = "gui.hint";
    public static final String GUI_PREVIEW_EMPTY = "gui.preview-empty";

    private final Map<String, String> values;

    private Messages(Map<String, String> values) {
        this.values = Map.copyOf(values);
    }

    public static Messages defaults() {
        return new Messages(defaultsMap());
    }

    public static Messages fromToml(TomlTable root) {
        Map<String, String> merged = new LinkedHashMap<>(defaultsMap());
        for (String key : merged.keySet()) {
            root.getString(key).ifPresent(value -> merged.put(key, value));
        }
        return new Messages(merged);
    }

    public String get(String key) {
        String value = values.get(key);
        return value != null ? value : "!" + key + "!";
    }

    public String format(String key, String... placeholderValuePairs) {
        Objects.requireNonNull(placeholderValuePairs, "placeholderValuePairs");
        if ((placeholderValuePairs.length & 1) != 0) {
            throw new IllegalArgumentException("placeholderValuePairs must contain pairs of name/value");
        }
        String result = get(key);
        for (int i = 0; i < placeholderValuePairs.length; i += 2) {
            String name = placeholderValuePairs[i];
            String value = placeholderValuePairs[i + 1];
            result = result.replace("{" + name + "}", value == null ? "" : value);
        }
        return result;
    }

    private static Map<String, String> defaultsMap() {
        Map<String, String> defaults = new LinkedHashMap<>();
        defaults.put(CHAT_PREFIX, "[Тикет] ");
        defaults.put(CHAT_TICKET_PREFIX, "[Тикет #{id}] ");
        defaults.put(CHAT_BUTTON_SEPARATOR, " | ");
        defaults.put(CHAT_BUTTON_KEEP_OPEN, "[Проблема актуальна]");
        defaults.put(CHAT_BUTTON_CLOSE, "[Закрыть обращение]");

        defaults.put(SYSTEM_EMPTY_MESSAGE, "Пустое сообщение.");
        defaults.put(SYSTEM_ALREADY_OPEN, "У вас уже есть открытый тикет.");
        defaults.put(SYSTEM_CREATED, "Тикет #{id} создан. Ожидайте ответа администратора.");
        defaults.put(SYSTEM_NO_ACTIVE, "У вас нет активного тикета.");
        defaults.put(SYSTEM_CLOSE_FAILED, "Не удалось закрыть тикет.");
        defaults.put(SYSTEM_CLOSED, "Тикет #{id} закрыт {actor}.");
        defaults.put(SYSTEM_KEEP_OPEN_BROADCAST, "{actor} отметил тикет #{id} как актуальный.");
        defaults.put(SYSTEM_KEEP_OPEN_CONTENT, "«Проблема актуальна»");
        defaults.put(SYSTEM_NOT_CONNECTED, "Вы не подключены к серверу.");
        defaults.put(SYSTEM_ALREADY_ACCEPTED, "Тикет уже принят или недоступен.");
        defaults.put(SYSTEM_ACCEPTED, "Тикет #{id} принят: {staff}.");

        defaults.put(COMMAND_PLAYERS_ONLY, "Команда доступна только игрокам.");
        defaults.put(COMMAND_USAGE, "Использование: /ticket <сообщение> либо /ticket close.");
        defaults.put(COMMAND_NO_PERMISSION, "Недостаточно прав.");

        defaults.put(GUI_TITLE, "Открытые тикеты");
        defaults.put(GUI_HINT, "Нажмите, чтобы принять тикет");
        defaults.put(GUI_PREVIEW_EMPTY, "(нет сообщений)");
        return defaults;
    }
}
