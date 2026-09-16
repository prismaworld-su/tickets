package smp.cloud.velocity.webhook.http;

public enum HttpMethod {

    GET,
    POST,
    PUT,
    DELETE;

    public static HttpMethod fromString(String raw) {
        if (raw == null) {
            return null;
        }
        return switch (raw.toUpperCase()) {
            case "GET" -> GET;
            case "POST" -> POST;
            case "PUT" -> PUT;
            case "DELETE" -> DELETE;
            default -> null;
        };
    }
}
