package smp.cloud.velocity.webhook.router;

import smp.cloud.velocity.webhook.WebhookHandler;
import smp.cloud.velocity.webhook.annotation.Delete;
import smp.cloud.velocity.webhook.annotation.Get;
import smp.cloud.velocity.webhook.annotation.Post;
import smp.cloud.velocity.webhook.annotation.Put;
import smp.cloud.velocity.webhook.http.HttpMethod;
import smp.cloud.velocity.webhook.http.WebhookRequest;
import smp.cloud.velocity.webhook.http.WebhookResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WebhookRouter {

    private static final Pattern PATH_VARIABLE = Pattern.compile("\\{([a-zA-Z_][a-zA-Z0-9_]*)}");

    private final Map<HttpMethod, List<Route>> routes = new EnumMap<>(HttpMethod.class);

    public WebhookRouter() {
        for (HttpMethod method : HttpMethod.values()) {
            routes.put(method, new ArrayList<>());
        }
    }

    public void register(WebhookHandler handler) {
        Class<?> handlerClass = handler.getClass();
        for (Method method : handlerClass.getDeclaredMethods()) {
            Get get = method.getAnnotation(Get.class);
            if (get != null) {
                bind(HttpMethod.GET, get.value(), handler, method);
            }
            Post post = method.getAnnotation(Post.class);
            if (post != null) {
                bind(HttpMethod.POST, post.value(), handler, method);
            }
            Put put = method.getAnnotation(Put.class);
            if (put != null) {
                bind(HttpMethod.PUT, put.value(), handler, method);
            }
            Delete delete = method.getAnnotation(Delete.class);
            if (delete != null) {
                bind(HttpMethod.DELETE, delete.value(), handler, method);
            }
        }
    }

    public DispatchResult dispatch(HttpMethod method, String path, RequestFactory requestFactory) throws Exception {
        boolean pathMatchedForOtherMethod = false;
        for (Map.Entry<HttpMethod, List<Route>> entry : routes.entrySet()) {
            for (Route route : entry.getValue()) {
                Matcher matcher = route.pattern().matcher(path);
                if (!matcher.matches()) {
                    continue;
                }
                if (entry.getKey() != method) {
                    pathMatchedForOtherMethod = true;
                    continue;
                }
                Map<String, String> pathParameters = new LinkedHashMap<>();
                for (String name : route.variables()) {
                    pathParameters.put(name, matcher.group(name));
                }
                WebhookRequest request = requestFactory.create(pathParameters);
                WebhookResponse response = invoke(route, request);
                return DispatchResult.matched(response);
            }
        }
        return pathMatchedForOtherMethod ? DispatchResult.methodNotAllowed() : DispatchResult.notFound();
    }

    private void bind(HttpMethod httpMethod, String template, WebhookHandler handler, Method method) {
        validateSignature(method);
        method.setAccessible(true);
        CompiledPath compiled = compile(template);
        routes.get(httpMethod).add(new Route(handler, method, compiled.pattern(), compiled.variables()));
    }

    private WebhookResponse invoke(Route route, WebhookRequest request) throws Exception {
        try {
            Object result = route.method().invoke(route.handler(), request);
            if (result == null) {
                return WebhookResponse.noContent();
            }
            if (!(result instanceof WebhookResponse response)) {
                throw new IllegalStateException(
                        "Handler method " + route.method() + " must return " + WebhookResponse.class.getName());
            }
            return response;
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception ex) {
                throw ex;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw e;
        }
    }

    private void validateSignature(Method method) {
        Class<?>[] parameters = method.getParameterTypes();
        if (parameters.length != 1 || !WebhookRequest.class.equals(parameters[0])) {
            throw new IllegalStateException(
                    "Handler method " + method + " must accept a single " + WebhookRequest.class.getName() + " argument");
        }
        if (!WebhookResponse.class.equals(method.getReturnType())) {
            throw new IllegalStateException(
                    "Handler method " + method + " must return " + WebhookResponse.class.getName());
        }
    }

    private CompiledPath compile(String template) {
        String normalized = template.isEmpty() ? "/" : template;
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        List<String> variables = new ArrayList<>();
        Matcher matcher = PATH_VARIABLE.matcher(normalized);
        StringBuilder regex = new StringBuilder("^");
        int cursor = 0;
        while (matcher.find()) {
            regex.append(Pattern.quote(normalized.substring(cursor, matcher.start())));
            String name = matcher.group(1);
            variables.add(name);
            regex.append("(?<").append(name).append(">[^/]+)");
            cursor = matcher.end();
        }
        regex.append(Pattern.quote(normalized.substring(cursor)));
        regex.append("$");
        return new CompiledPath(Pattern.compile(regex.toString()), List.copyOf(variables));
    }

    @FunctionalInterface
    public interface RequestFactory {

        WebhookRequest create(Map<String, String> pathParameters);
    }

    public record DispatchResult(Status status, WebhookResponse response) {

        public enum Status { MATCHED, NOT_FOUND, METHOD_NOT_ALLOWED }

        public static DispatchResult matched(WebhookResponse response) {
            return new DispatchResult(Status.MATCHED, response);
        }

        public static DispatchResult notFound() {
            return new DispatchResult(Status.NOT_FOUND, null);
        }

        public static DispatchResult methodNotAllowed() {
            return new DispatchResult(Status.METHOD_NOT_ALLOWED, null);
        }
    }

    private record Route(WebhookHandler handler, Method method, Pattern pattern, List<String> variables) {
    }

    private record CompiledPath(Pattern pattern, List<String> variables) {
    }
}
