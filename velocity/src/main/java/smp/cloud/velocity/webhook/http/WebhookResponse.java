package smp.cloud.velocity.webhook.http;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class WebhookResponse {

    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_TYPE_JSON = "application/json; charset=utf-8";
    public static final String CONTENT_TYPE_TEXT = "text/plain; charset=utf-8";

    private final int status;
    private final Map<String, String> headers;
    private final byte[] body;

    private WebhookResponse(int status, Map<String, String> headers, byte[] body) {
        this.status = status;
        this.headers = Map.copyOf(headers);
        this.body = body;
    }

    public int status() {
        return status;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public byte[] body() {
        return body;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static WebhookResponse noContent() {
        return builder().status(204).build();
    }

    public static WebhookResponse ok() {
        return builder().status(200).build();
    }

    public static WebhookResponse text(int status, String body) {
        return builder()
                .status(status)
                .contentType(CONTENT_TYPE_TEXT)
                .body(body)
                .build();
    }

    public static WebhookResponse json(int status, String json) {
        return builder()
                .status(status)
                .contentType(CONTENT_TYPE_JSON)
                .body(json)
                .build();
    }

    public static final class Builder {

        private int status = 200;
        private final Map<String, String> headers = new LinkedHashMap<>();
        private byte[] body = new byte[0];

        private Builder() {
        }

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder header(String name, String value) {
            headers.put(Objects.requireNonNull(name, "name"), Objects.requireNonNull(value, "value"));
            return this;
        }

        public Builder contentType(String contentType) {
            return header(HEADER_CONTENT_TYPE, contentType);
        }

        public Builder body(String body) {
            this.body = body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8);
            return this;
        }

        public Builder body(byte[] body) {
            this.body = body == null ? new byte[0] : body.clone();
            return this;
        }

        public WebhookResponse build() {
            return new WebhookResponse(status, headers, body);
        }
    }
}
