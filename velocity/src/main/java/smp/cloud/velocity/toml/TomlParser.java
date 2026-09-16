package smp.cloud.velocity.toml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Minimal, self-contained TOML parser covering the subset needed by plugin configuration:
 * comments, blank lines, table headers (including dotted paths), key/value pairs with
 * bare and quoted keys, basic and literal strings, integers, floats, booleans, and arrays.
 */
public final class TomlParser {

    private final String source;
    private int pos;
    private int line = 1;
    private int column = 1;

    public static TomlTable parse(String source) {
        return new TomlParser(source).parseDocument();
    }

    private TomlParser(String source) {
        this.source = source;
    }

    private TomlTable parseDocument() {
        Map<String, Object> root = new LinkedHashMap<>();
        Map<String, Object> currentTable = root;
        Set<List<String>> definedTables = new HashSet<>();

        while (!atEnd()) {
            skipInlineWhitespace();
            if (atEnd()) {
                break;
            }
            char c = peekChar();
            if (c == '\n' || c == '\r') {
                advance();
                continue;
            }
            if (c == '#') {
                skipComment();
                continue;
            }
            if (c == '[') {
                List<String> tablePath = parseTableHeader();
                if (!definedTables.add(tablePath)) {
                    throw error("Duplicate table definition: " + join(tablePath));
                }
                currentTable = openTable(root, tablePath);
                skipInlineWhitespaceAndComment();
                consumeLineEnd();
                continue;
            }
            parseKeyValue(currentTable);
            skipInlineWhitespaceAndComment();
            consumeLineEnd();
        }
        return new TomlTable(freeze(root));
    }

    private List<String> parseTableHeader() {
        expect('[');
        skipInlineWhitespace();
        List<String> path = parseKeyPath();
        skipInlineWhitespace();
        expect(']');
        return path;
    }

    private void parseKeyValue(Map<String, Object> table) {
        List<String> path = parseKeyPath();
        skipInlineWhitespace();
        expect('=');
        skipInlineWhitespace();
        Object value = parseValue();
        assign(table, path, value);
    }

    private List<String> parseKeyPath() {
        List<String> segments = new ArrayList<>();
        segments.add(parseKey());
        while (true) {
            skipInlineWhitespace();
            if (atEnd() || peekChar() != '.') {
                break;
            }
            advance();
            skipInlineWhitespace();
            segments.add(parseKey());
        }
        return List.copyOf(segments);
    }

    private String parseKey() {
        if (atEnd()) {
            throw error("Expected key");
        }
        char c = peekChar();
        if (c == '"') {
            return parseBasicString();
        }
        if (c == '\'') {
            return parseLiteralString();
        }
        if (isBareKeyChar(c)) {
            int start = pos;
            while (!atEnd() && isBareKeyChar(peekChar())) {
                advance();
            }
            return source.substring(start, pos);
        }
        throw error("Invalid key character: '" + c + "'");
    }

    private Object parseValue() {
        if (atEnd()) {
            throw error("Expected value");
        }
        char c = peekChar();
        if (c == '"') {
            return parseBasicString();
        }
        if (c == '\'') {
            return parseLiteralString();
        }
        if (c == '[') {
            return parseArray();
        }
        if (c == 't' || c == 'f') {
            return parseBoolean();
        }
        if (c == '-' || c == '+' || (c >= '0' && c <= '9')) {
            return parseNumber();
        }
        throw error("Unexpected value: '" + c + "'");
    }

    private String parseBasicString() {
        expect('"');
        StringBuilder builder = new StringBuilder();
        while (!atEnd()) {
            char c = peekChar();
            if (c == '"') {
                advance();
                return builder.toString();
            }
            if (c == '\n' || c == '\r') {
                throw error("Unterminated string");
            }
            if (c == '\\') {
                advance();
                if (atEnd()) {
                    throw error("Truncated escape sequence");
                }
                char esc = advance();
                switch (esc) {
                    case '"' -> builder.append('"');
                    case '\\' -> builder.append('\\');
                    case 'n' -> builder.append('\n');
                    case 't' -> builder.append('\t');
                    case 'r' -> builder.append('\r');
                    case 'b' -> builder.append('\b');
                    case 'f' -> builder.append('\f');
                    case '/' -> builder.append('/');
                    case 'u' -> builder.append(parseHexEscape(4));
                    case 'U' -> builder.append(parseHexEscape(8));
                    default -> throw error("Invalid escape sequence: \\" + esc);
                }
                continue;
            }
            builder.append(c);
            advance();
        }
        throw error("Unterminated string");
    }

    private String parseHexEscape(int length) {
        StringBuilder hex = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            if (atEnd()) {
                throw error("Truncated unicode escape");
            }
            char c = peekChar();
            if (!isHex(c)) {
                throw error("Invalid unicode escape digit: '" + c + "'");
            }
            hex.append(c);
            advance();
        }
        int codePoint = Integer.parseInt(hex.toString(), 16);
        if (!Character.isValidCodePoint(codePoint)) {
            throw error("Invalid unicode code point: U+" + hex);
        }
        return new String(Character.toChars(codePoint));
    }

    private String parseLiteralString() {
        expect('\'');
        int start = pos;
        while (!atEnd()) {
            char c = peekChar();
            if (c == '\'') {
                String value = source.substring(start, pos);
                advance();
                return value;
            }
            if (c == '\n' || c == '\r') {
                throw error("Unterminated literal string");
            }
            advance();
        }
        throw error("Unterminated literal string");
    }

    private Boolean parseBoolean() {
        if (matchesLiteral("true")) {
            consumeChars(4);
            return Boolean.TRUE;
        }
        if (matchesLiteral("false")) {
            consumeChars(5);
            return Boolean.FALSE;
        }
        throw error("Invalid boolean literal");
    }

    private Object parseNumber() {
        int start = pos;
        boolean isFloat = false;
        if (peekChar() == '+' || peekChar() == '-') {
            advance();
        }
        while (!atEnd() && (isDigit(peekChar()) || peekChar() == '_')) {
            advance();
        }
        if (!atEnd() && peekChar() == '.') {
            isFloat = true;
            advance();
            while (!atEnd() && (isDigit(peekChar()) || peekChar() == '_')) {
                advance();
            }
        }
        if (!atEnd() && (peekChar() == 'e' || peekChar() == 'E')) {
            isFloat = true;
            advance();
            if (!atEnd() && (peekChar() == '+' || peekChar() == '-')) {
                advance();
            }
            while (!atEnd() && (isDigit(peekChar()) || peekChar() == '_')) {
                advance();
            }
        }
        String text = source.substring(start, pos).replace("_", "");
        try {
            if (isFloat) {
                return Double.parseDouble(text);
            }
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            throw error("Invalid numeric literal: '" + text + "'");
        }
    }

    private List<Object> parseArray() {
        expect('[');
        List<Object> items = new ArrayList<>();
        skipWhitespaceNewlinesAndComments();
        if (!atEnd() && peekChar() == ']') {
            advance();
            return List.copyOf(items);
        }
        while (true) {
            skipWhitespaceNewlinesAndComments();
            items.add(parseValue());
            skipWhitespaceNewlinesAndComments();
            if (atEnd()) {
                throw error("Unterminated array");
            }
            char c = peekChar();
            if (c == ',') {
                advance();
                skipWhitespaceNewlinesAndComments();
                if (!atEnd() && peekChar() == ']') {
                    advance();
                    return List.copyOf(items);
                }
                continue;
            }
            if (c == ']') {
                advance();
                return List.copyOf(items);
            }
            throw error("Expected ',' or ']' in array");
        }
    }

    private void assign(Map<String, Object> table, List<String> path, Object value) {
        Map<String, Object> current = table;
        for (int i = 0; i < path.size() - 1; i++) {
            String segment = path.get(i);
            Object existing = current.get(segment);
            if (existing == null) {
                Map<String, Object> next = new LinkedHashMap<>();
                current.put(segment, next);
                current = next;
            } else if (existing instanceof Map<?, ?> map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> next = (Map<String, Object>) map;
                current = next;
            } else {
                throw error("Cannot use '" + join(path.subList(0, i + 1)) + "' as a table");
            }
        }
        String last = path.get(path.size() - 1);
        if (current.containsKey(last)) {
            throw error("Duplicate key: " + join(path));
        }
        current.put(last, value);
    }

    private Map<String, Object> openTable(Map<String, Object> root, List<String> path) {
        Map<String, Object> current = root;
        for (String segment : path) {
            Object existing = current.get(segment);
            if (existing == null) {
                Map<String, Object> next = new LinkedHashMap<>();
                current.put(segment, next);
                current = next;
            } else if (existing instanceof Map<?, ?> map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> next = (Map<String, Object>) map;
                current = next;
            } else {
                throw error("Cannot use '" + segment + "' as a table");
            }
        }
        return current;
    }

    private Map<String, Object> freeze(Map<String, Object> map) {
        Map<String, Object> result = new LinkedHashMap<>(map.size());
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> nested) {
                @SuppressWarnings("unchecked")
                Map<String, Object> cast = (Map<String, Object>) nested;
                result.put(entry.getKey(), freeze(cast));
            } else {
                result.put(entry.getKey(), value);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    private void skipInlineWhitespace() {
        while (!atEnd()) {
            char c = peekChar();
            if (c == ' ' || c == '\t') {
                advance();
            } else {
                break;
            }
        }
    }

    private void skipInlineWhitespaceAndComment() {
        skipInlineWhitespace();
        if (!atEnd() && peekChar() == '#') {
            skipComment();
        }
    }

    private void skipWhitespaceNewlinesAndComments() {
        while (!atEnd()) {
            char c = peekChar();
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                advance();
                continue;
            }
            if (c == '#') {
                skipComment();
                continue;
            }
            break;
        }
    }

    private void skipComment() {
        while (!atEnd() && peekChar() != '\n' && peekChar() != '\r') {
            advance();
        }
    }

    private void consumeLineEnd() {
        if (atEnd()) {
            return;
        }
        char c = peekChar();
        if (c == '\r') {
            advance();
            if (!atEnd() && peekChar() == '\n') {
                advance();
            }
            return;
        }
        if (c == '\n') {
            advance();
            return;
        }
        throw error("Expected end of line, got '" + c + "'");
    }

    private char peekChar() {
        if (atEnd()) {
            throw error("Unexpected end of file");
        }
        return source.charAt(pos);
    }

    private char advance() {
        char c = source.charAt(pos++);
        if (c == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
        return c;
    }

    private void consumeChars(int count) {
        for (int i = 0; i < count; i++) {
            advance();
        }
    }

    private void expect(char expected) {
        if (atEnd() || peekChar() != expected) {
            throw error("Expected '" + expected + "'");
        }
        advance();
    }

    private boolean matchesLiteral(String literal) {
        if (!source.regionMatches(pos, literal, 0, literal.length())) {
            return false;
        }
        int next = pos + literal.length();
        return next >= source.length() || !isBareKeyChar(source.charAt(next));
    }

    private boolean atEnd() {
        return pos >= source.length();
    }

    private boolean isBareKeyChar(char c) {
        return (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || (c >= '0' && c <= '9')
                || c == '_'
                || c == '-';
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isHex(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    private TomlParseException error(String message) {
        return new TomlParseException(message, line, column);
    }

    private static String join(List<String> segments) {
        return String.join(".", segments);
    }
}
