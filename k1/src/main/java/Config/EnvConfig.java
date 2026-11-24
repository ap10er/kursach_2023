package Config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class EnvConfig {
    private static final Map<String, String> VALUES = new HashMap<>();

    static {
        load();
    }

    private EnvConfig() {
    }

    private static void load() {
        String[] directPaths = {
                ".env",
                "target/classes/.env",
                "src/main/resources/.env"
        };
        for (String p : directPaths) {
            Path path = Path.of(p);
            if (Files.exists(path)) {
                try {
                    parse(Files.readString(path, StandardCharsets.UTF_8));
                    return;
                } catch (IOException ignored) {
                }
            }
        }
        ClassLoader loader = EnvConfig.class.getClassLoader();
        String[] resourceNames = {".env", "env", "config/.env"};
        for (String name : resourceNames) {
            try (InputStream in = loader.getResourceAsStream(name)) {
                if (in != null) {
                    parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
                    return;
                }
            } catch (IOException ignored) {
            }
        }
    }

    private static void parse(String content) {
        for (String line : content.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            int idx = trimmed.indexOf('=');
            if (idx <= 0) {
                continue;
            }
            String key = trimmed.substring(0, idx).trim();
            String value = trimmed.substring(idx + 1).trim();
            if (value.length() >= 2
                    && ((value.startsWith("\"") && value.endsWith("\""))
                    || (value.startsWith("'") && value.endsWith("'")))) {
                value = value.substring(1, value.length() - 1);
            }
            VALUES.put(key, value);
        }
    }

    public static String get(String key) {
        String value = System.getProperty(key);
        if (value != null && !value.isBlank()) {
            return value;
        }
        value = System.getenv(key);
        if (value != null && !value.isBlank()) {
            return value;
        }
        return VALUES.get(key);
    }

    public static String required(String key) {
        String value = get(key);
        if (value == null || value.isBlank()) {
            return "";
        }
        return value;
    }
}
