package smp.cloud.tickets.webhook.http;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record WebhookRequest(
        HttpMethod method,
        String path,
        Map<String, String> pathParameters,
        Map<String, List<String>> queryParameters,
        Map<String, List<String>> headers,
        byte[] body
) {

    public WebhookRequest {
        pathParameters = pathParameters == null ? Map.of() : Map.copyOf(pathParameters);
        queryParameters = queryParameters == null ? Map.of() : Map.copyOf(queryParameters);
        headers = headers == null ? Map.of() : Map.copyOf(headers);
        body = body == null ? new byte[0] : body.clone();
    }

    public Optional<String> pathParameter(String name) {
        return Optional.ofNullable(pathParameters.get(name));
    }

    public Optional<String> queryParameter(String name) {
        List<String> values = queryParameters.get(name);
        if (values == null || values.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(values.get(0));
    }

    public List<String> queryParameters(String name) {
        return queryParameters.getOrDefault(name, Collections.emptyList());
    }

    public Optional<String> header(String name) {
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name) && !entry.getValue().isEmpty()) {
                return Optional.of(entry.getValue().get(0));
            }
        }
        return Optional.empty();
    }

    public String bodyAsString() {
        return new String(body, StandardCharsets.UTF_8);
    }
}
