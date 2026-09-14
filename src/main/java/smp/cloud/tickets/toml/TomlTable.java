package smp.cloud.tickets.toml;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class TomlTable {

    private static final TomlTable EMPTY = new TomlTable(Map.of());

    private final Map<String, Object> values;

    TomlTable(Map<String, Object> values) {
        this.values = values;
    }

    public static TomlTable empty() {
        return EMPTY;
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public Optional<String> getString(String path) {
        return get(path, String.class);
    }

    public Optional<Long> getLong(String path) {
        return get(path, Long.class);
    }

    public Optional<Integer> getInt(String path) {
        return getLong(path).map(this::toInt);
    }

    public Optional<Double> getDouble(String path) {
        return get(path, Double.class);
    }

    public Optional<Boolean> getBoolean(String path) {
        return get(path, Boolean.class);
    }

    public Optional<TomlTable> getTable(String path) {
        return resolve(path).map(value -> {
            if (value instanceof Map<?, ?> map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> cast = (Map<String, Object>) map;
                return new TomlTable(cast);
            }
            throw new IllegalStateException("Value at '" + path + "' is not a table");
        });
    }

    public Optional<List<Object>> getArray(String path) {
        return resolve(path).map(value -> {
            if (value instanceof List<?> list) {
                @SuppressWarnings("unchecked")
                List<Object> cast = (List<Object>) list;
                return cast;
            }
            throw new IllegalStateException("Value at '" + path + "' is not an array");
        });
    }

    public String getStringOr(String path, String defaultValue) {
        return getString(path).orElse(defaultValue);
    }

    public long getLongOr(String path, long defaultValue) {
        return getLong(path).orElse(defaultValue);
    }

    public int getIntOr(String path, int defaultValue) {
        return getInt(path).orElse(defaultValue);
    }

    public double getDoubleOr(String path, double defaultValue) {
        return getDouble(path).orElse(defaultValue);
    }

    public boolean getBooleanOr(String path, boolean defaultValue) {
        return getBoolean(path).orElse(defaultValue);
    }

    private <T> Optional<T> get(String path, Class<T> type) {
        return resolve(path).map(value -> {
            if (type.isInstance(value)) {
                return type.cast(value);
            }
            throw new IllegalStateException(
                    "Value at '" + path + "' is not of type " + type.getSimpleName());
        });
    }

    private Optional<Object> resolve(String path) {
        if (path == null || path.isEmpty()) {
            return Optional.empty();
        }
        Object current = values;
        for (String segment : path.split("\\.")) {
            if (!(current instanceof Map<?, ?> map)) {
                return Optional.empty();
            }
            current = map.get(segment);
            if (current == null) {
                return Optional.empty();
            }
        }
        return Optional.of(current);
    }

    private int toInt(long value) {
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new IllegalStateException("Value " + value + " does not fit into an int");
        }
        return (int) value;
    }
}
