package smp.cloud.velocity.toml;

public final class TomlParseException extends RuntimeException {

    private final int line;
    private final int column;

    public TomlParseException(String message, int line, int column) {
        super(message + " (at line " + line + ", column " + column + ")");
        this.line = line;
        this.column = column;
    }

    public int line() {
        return line;
    }

    public int column() {
        return column;
    }
}
