package server;

import protocol.JsonCodec;
import protocol.Request;
import protocol.Response;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Obrabotka odnogo klienta: chitaet JSON-stroki, peredaet v RequestDispatcher,
 * otvechaet JSON-strokoy. Odin potok na klienta (pul v Server).
 */
public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final RequestDispatcher dispatcher;

    public ClientHandler(Socket clientSocket, RequestDispatcher dispatcher) {
        this.clientSocket = clientSocket;
        this.dispatcher = dispatcher;
    }

    @Override
    public void run() {
        try (Socket socket = clientSocket;
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter writer = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                System.out.println("[SERVER] <- " + line);

                Response response;
                try {
                    Request request = JsonCodec.readRequest(line);
                    response = dispatcher.dispatch(request);
                } catch (Exception e) {
                    response = Response.failure("Некорректный запрос: " + e.getMessage());
                }

                String json = JsonCodec.writeResponse(response);
                System.out.println("[SERVER] -> " + json);
                writer.println(json);
            }
        } catch (IOException e) {
            System.out.println("[SERVER] Клиент отключился: " + e.getMessage());
        }
    }
}
