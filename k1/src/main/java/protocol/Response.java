package protocol;

/**
 * Ответ сервера: ok=true при успехе, error — текст ошибки, data — полезная нагрузка.
 */
public final class Response {
    private final boolean ok;
    private final String data;
    private final String error;

    public Response(boolean ok, String data, String error) {
        this.ok = ok;
        this.data = data == null ? "" : data;
        this.error = error == null ? "" : error;
    }

    public static Response success(String data) {
        return new Response(true, data, "");
    }

    public static Response failure(String error) {
        return new Response(false, "", error);
    }

    public boolean isOk() {
        return ok;
    }

    public String getData() {
        return data;
    }

    public String getError() {
        return error;
    }
}
