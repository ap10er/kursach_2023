package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class ClientHandler implements Runnable {
    private final Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String input;
            while ((input = reader.readLine()) != null) {
                System.out.println("Received from client: " + input);

                String response = "Response from server";
                writer.println(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
