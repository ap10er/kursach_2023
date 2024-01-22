package Pages;

import client.ClientApp;

public class Main {
    public static void main(String[] args) {
        int numberOfClients = 3; // Установите желаемое количество клиентов
        launchClients(numberOfClients);
    }

    private static void launchClients(int numberOfClients) {
        for (int i = 0; i < numberOfClients; i++) {
            Thread clientThread = new Thread(() -> {
                ClientApp clientApp = new ClientApp();
                //clientApp.launchClient();
            });
            clientThread.start();
        }
    }
}



