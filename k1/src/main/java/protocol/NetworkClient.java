package protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Odin Socket na sesiyu. Potokobezopasnaya otpravka zaprosov i chtenie otvetov.
 * Sozdaetsya odin raz pri zapuske prilozheniya i ispolzuetsya vsemi kontrollerami.
 */
public final class NetworkClient implements AutoCloseable {

    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 12345;

    private final String host;
    private final int port;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public NetworkClient() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    public NetworkClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /** Podklyuchaetsya k serveru (ili vosstanavlivaet soedinenie). */
    public synchronized void connect() throws IOException {
        if (isConnected()) return;
        socket = new Socket(host, port);
        socket.setSoTimeout(15000);
        reader = new BufferedReader(new InputStreamReader(
                socket.getInputStream(), StandardCharsets.UTF_8));
        writer = new PrintWriter(new java.io.OutputStreamWriter(
                socket.getOutputStream(), StandardCharsets.UTF_8), true);
    }

    public synchronized boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    /** Otpravlyaet zapros i vozvrashchaet otvet servera. */
    public synchronized Response send(Request request) throws IOException {
        if (!isConnected()) connect();
        String json = JsonCodec.writeRequest(request);
        writer.println(json);
        writer.flush();

        String line = reader.readLine();
        if (line == null) {
            disconnect();
            throw new IOException("Server zakryil soedinenie bez otveta");
        }
        return JsonCodec.readResponse(line);
    }

    /** Udobnaya versiya dlya prostyh deystviy. */
    public Response send(String action) throws IOException {
        return send(Request.of(action));
    }

    public synchronized void disconnect() {
        try {
            if (reader != null) reader.close();
        } catch (IOException ignored) {}
        try {
            if (writer != null) writer.close();
        } catch (Exception ignored) {}
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
        socket = null;
        reader = null;
        writer = null;
    }

    @Override
    public void close() {
        disconnect();
    }
}
