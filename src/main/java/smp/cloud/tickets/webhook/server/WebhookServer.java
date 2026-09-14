package smp.cloud.tickets.webhook.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import smp.cloud.tickets.webhook.WebhookHandler;
import smp.cloud.tickets.webhook.http.HttpMethod;
import smp.cloud.tickets.webhook.http.WebhookRequest;
import smp.cloud.tickets.webhook.http.WebhookResponse;
import smp.cloud.tickets.webhook.router.WebhookRouter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class WebhookServer {

    private final WebhookServerConfig config;
    private final WebhookRouter router;
    private final Logger logger;
    private final Executor executor;

    private HttpServer httpServer;

    private WebhookServer(WebhookServerConfig config, WebhookRouter router, Logger logger, Executor executor) {
        this.config = Objects.requireNonNull(config, "config");
        this.router = Objects.requireNonNull(router, "router");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.executor = executor;
    }

    public synchronized void start() throws IOException {
        if (httpServer != null) {
            throw new IllegalStateException("Webhook server is already running");
        }
        HttpServer server = HttpServer.create(new InetSocketAddress(config.host(), config.port()), config.backlog());
        server.createContext("/", new RouterHandler());
        server.setExecutor(executor);
        server.start();
        this.httpServer = server;
        logger.info("Webhook server listening on {}:{}", config.host(), config.port());
    }

    public synchronized void stop() {
        if (httpServer == null) {
            return;
        }
        httpServer.stop(0);
        httpServer = null;
        logger.info("Webhook server stopped");
    }

    public static Builder builder() {
        return new Builder();
    }

    private final class RouterHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) {
            try (exchange) {
                WebhookResponse response = route(exchange);
                write(exchange, response);
            } catch (IOException e) {
                logger.error("Failed to handle webhook request", e);
            }
        }

        private WebhookResponse route(HttpExchange exchange) throws IOException {
            HttpMethod method = HttpMethod.fromString(exchange.getRequestMethod());
            if (method == null) {
                return WebhookResponse.text(405, "Method Not Allowed");
            }
            URI uri = exchange.getRequestURI();
            String path = uri.getPath();
            Map<String, List<String>> query = parseQuery(uri.getRawQuery());
            Map<String, List<String>> headers = Map.copyOf(exchange.getRequestHeaders());
            byte[] body = readBody(exchange.getRequestBody());
            try {
                WebhookRouter.DispatchResult result = router.dispatch(method, path, pathParameters ->
                        new WebhookRequest(method, path, pathParameters, query, headers, body));
                return switch (result.status()) {
                    case MATCHED -> result.response();
                    case NOT_FOUND -> WebhookResponse.text(404, "Not Found");
                    case METHOD_NOT_ALLOWED -> WebhookResponse.text(405, "Method Not Allowed");
                };
            } catch (Exception e) {
                logger.error("Unhandled error while dispatching {} {}", method, path, e);
                return WebhookResponse.text(500, "Internal Server Error");
            }
        }

        private byte[] readBody(InputStream body) throws IOException {
            try (body) {
                return body.readAllBytes();
            }
        }

        private void write(HttpExchange exchange, WebhookResponse response) throws IOException {
            for (Map.Entry<String, String> header : response.headers().entrySet()) {
                exchange.getResponseHeaders().set(header.getKey(), header.getValue());
            }
            byte[] payload = response.body();
            int status = response.status();
            if (payload.length == 0 || status == 204 || status == 304) {
                exchange.sendResponseHeaders(status, -1);
                return;
            }
            exchange.sendResponseHeaders(status, payload.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(payload);
            }
        }

        private Map<String, List<String>> parseQuery(String rawQuery) {
            if (rawQuery == null || rawQuery.isEmpty()) {
                return Map.of();
            }
            Map<String, List<String>> parameters = new LinkedHashMap<>();
            for (String pair : rawQuery.split("&")) {
                if (pair.isEmpty()) {
                    continue;
                }
                int separator = pair.indexOf('=');
                String key = separator == -1 ? pair : pair.substring(0, separator);
                String value = separator == -1 ? "" : pair.substring(separator + 1);
                String decodedKey = URLDecoder.decode(key, StandardCharsets.UTF_8);
                String decodedValue = URLDecoder.decode(value, StandardCharsets.UTF_8);
                parameters.computeIfAbsent(decodedKey, ignored -> new ArrayList<>()).add(decodedValue);
            }
            return Map.copyOf(parameters);
        }
    }

    public static final class Builder {

        private WebhookServerConfig config = WebhookServerConfig.defaults();
        private Logger logger;
        private Executor executor;
        private final List<WebhookHandler> handlers = new ArrayList<>();

        private Builder() {
        }

        public Builder config(WebhookServerConfig config) {
            this.config = Objects.requireNonNull(config, "config");
            return this;
        }

        public Builder logger(Logger logger) {
            this.logger = Objects.requireNonNull(logger, "logger");
            return this;
        }

        public Builder executor(Executor executor) {
            this.executor = executor;
            return this;
        }

        public Builder register(WebhookHandler handler) {
            handlers.add(Objects.requireNonNull(handler, "handler"));
            return this;
        }

        public WebhookServer build() {
            if (logger == null) {
                throw new IllegalStateException("logger must be provided");
            }
            WebhookRouter router = new WebhookRouter();
            for (WebhookHandler handler : handlers) {
                router.register(handler);
            }
            Executor threadPool = executor != null
                    ? executor
                    : Executors.newVirtualThreadPerTaskExecutor();
            return new WebhookServer(config, router, logger, threadPool);
        }
    }
}
