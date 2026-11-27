package protocol;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimalistichniy JSON-codec bez vneshnih bibliotek.
 * Podderzhivaet: ob'ekty {"k":"v", ...}, stroki, chisla, boolean, null
 * i massivy ["a","b"] (dlya tablichnyh dannyh).
 * Kazhdoe soobshchenie — odna stroka (odin JSON-objekt).
 */
public final class JsonCodec {

    private JsonCodec() {}

    public static String writeRequest(Request request) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"action\":").append(quote(request.getAction()));
        sb.append(",\"payload\":{");
        boolean first = true;
        for (Map.Entry<String, String> entry : request.getPayload().entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append(quote(entry.getKey())).append(':').append(quote(entry.getValue()));
        }
        sb.append("}}");
        return sb.toString();
    }

    public static Request readRequest(String json) {
        Map<String, Object> root = parseObject(json);
        String action = string(root.get("action"));
        Map<String, String> payload = new LinkedHashMap<>();
        Object payloadObj = root.get("payload");
        if (payloadObj instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                payload.put(String.valueOf(entry.getKey()), string(entry.getValue()));
            }
        }
        return new Request(action, payload);
    }

    public static String writeResponse(Response response) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"ok\":").append(response.isOk());
        sb.append(",\"data\":").append(quote(response.getData()));
        sb.append(",\"error\":").append(quote(response.getError()));
        sb.append('}');
        return sb.toString();
    }

    public static Response readResponse(String json) {
        Map<String, Object> root = parseObject(json);
        boolean ok = Boolean.TRUE.equals(root.get("ok"));
        String data = string(root.get("data"));
        String error = string(root.get("error"));
        return new Response(ok, data, error);
    }

    /** Esli data yavlyaetsya JSON-massivom — vernut' spisok strok. */
    public static List<String> parseDataList(String data) {
        List<String> result = new ArrayList<>();
        if (data == null || data.isBlank()) return result;
        String trimmed = data.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            String inner = trimmed.substring(1, trimmed.length() - 1);
            for (String part : splitTopLevel(inner)) {
                String v = part.trim();
                if (!v.isEmpty()) result.add(unquote(v));
            }
        } else {
            result.add(unquote(trimmed));
        }
        return result;
    }

    public static String writeStringArray(List<String> values) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(quote(values.get(i)));
        }
        return sb.append(']').toString();
    }

    private static String quote(String value) {
        if (value == null) return "null";
        StringBuilder sb = new StringBuilder(value.length() + 2).append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.append('"').toString();
    }

    private static String unquote(String value) {
        String v = value.trim();
        if (v.length() >= 2 && v.startsWith("\"") && v.endsWith("\"")) {
            return unescape(v.substring(1, v.length() - 1));
        }
        if (v.equals("null")) return "";
        return unescape(v);
    }

    private static String unescape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char n = s.charAt(++i);
                switch (n) {
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case 'u' -> {
                        if (i + 4 < s.length()) {
                            sb.append((char) Integer.parseInt(s.substring(i + 1, i + 5), 16));
                            i += 4;
                        }
                    }
                    default -> sb.append(n);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String string(Object value) {
        if (value == null) return "";
        if (value instanceof String s) return unquote(s);
        return unquote(String.valueOf(value));
    }

    private static Map<String, Object> parseObject(String json) {
        Map<String, Object> result = new LinkedHashMap<>();
        String s = json == null ? "" : json.trim();
        if (!s.startsWith("{") || !s.endsWith("}")) return result;
        String inner = s.substring(1, s.length() - 1);
        int start = 0;
        boolean inString = false;
        int depth = 0;
        for (int i = 0; i < inner.length(); i++) {
            char c = inner.charAt(i);
            if (c == '"' && (i == 0 || inner.charAt(i - 1) != '\\')) inString = !inString;
            if (!inString) {
                if (c == '{' || c == '[') depth++;
                else if (c == '}' || c == ']') depth--;
                else if (c == ',' && depth == 0) {
                    parsePair(inner.substring(start, i), result);
                    start = i + 1;
                }
            }
        }
        if (start < inner.length()) parsePair(inner.substring(start), result);
        return result;
    }

    private static void parsePair(String pair, Map<String, Object> target) {
        int idx = pair.indexOf(':');
        if (idx <= 0) return;
        String key = unquote(pair.substring(0, idx).trim());
        String raw = pair.substring(idx + 1).trim();
        target.put(key, parseValue(raw));
    }

    private static Object parseValue(String raw) {
        if (raw.startsWith("{")) return parseObject(raw);
        if (raw.startsWith("[")) {
            List<String> list = new ArrayList<>();
            for (String part : splitTopLevel(raw.substring(1, raw.length() - 1))) {
                if (!part.trim().isEmpty()) list.add(unquote(part));
            }
            return list;
        }
        if (raw.equals("true")) return Boolean.TRUE;
        if (raw.equals("false")) return Boolean.FALSE;
        return raw;
    }

    private static List<String> splitTopLevel(String inner) {
        List<String> parts = new ArrayList<>();
        boolean inString = false;
        int depth = 0;
        int start = 0;
        for (int i = 0; i < inner.length(); i++) {
            char c = inner.charAt(i);
            if (c == '"' && (i == 0 || inner.charAt(i - 1) != '\\')) inString = !inString;
            if (!inString) {
                if (c == '{' || c == '[') depth++;
                else if (c == '}' || c == ']') depth--;
                else if (c == ',' && depth == 0) {
                    parts.add(inner.substring(start, i));
                    start = i + 1;
                }
            }
        }
        parts.add(inner.substring(start));
        return parts;
    }
}
