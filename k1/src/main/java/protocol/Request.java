package protocol;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Клиентский запрос к серверу.
 * action — имя действия (LOGIN, REGISTER, GET_EMPLOYEES, ...),
 * payload — параметры в виде "ключ-значение".
 */
public final class Request {
    private final String action;
    private final Map<String, String> payload;

    public Request(String action) {
        this(action, new LinkedHashMap<>());
    }

    public Request(String action, Map<String, String> payload) {
        this.action = action;
        this.payload = new LinkedHashMap<>(payload);
    }

    public String getAction() {
        return action;
    }

    public Map<String, String> getPayload() {
        return payload;
    }

    public Request put(String key, String value) {
        payload.put(key, value == null ? "" : value);
        return this;
    }

    public String get(String key) {
        return payload.getOrDefault(key, "");
    }

    public static Request of(String action) {
        return new Request(action);
    }
}
