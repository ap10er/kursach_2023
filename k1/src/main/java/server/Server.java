package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Server: odin RequestDispatcher na vse soedineniya,
 * po odnomu potoku iz pul na kazhdogo klienta.
 */
public class Server {

    private static final int PORT = 12345;

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        RequestDispatcher dispatcher = new RequestDispatcher();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT
                    + " (" + dispatcher.getHandlers().size() + " actions registered)");

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("[SERVER] New client: " + clientSocket);
                    executorService.submit(new ClientHandler(clientSocket, dispatcher));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
    }
}
