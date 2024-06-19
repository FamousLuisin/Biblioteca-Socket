package main.library.server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class LibraryServer {

    private static final Integer PORT = 8080;
    private static final String JSON = System.getProperty("user.dir") + File.separator + "Biblioteca-Socket" + File.separator + "src" + File.separator + "main"+ File.separator + "resources" + File.separator + "livros.json";
    private static final List<ClientHandler> clients = new ArrayList<>();
    private static volatile boolean running = true;

    public static void main(String[] args) {
        try(ServerSocket serverLibrary = new ServerSocket(PORT)) {
        	System.out.println(JSON);
            System.out.println("Server inicializado");
            BookManager bookManager = new BookManager(JSON);

            Thread serverControll = new Thread(() -> {
                try {
                    while (running) {
                        Socket clientSocket = serverLibrary.accept();

                        if (running){
                            System.out.println("New client connected: " + clientSocket);

                            ClientHandler clientHandler = new ClientHandler(clientSocket, bookManager);
                            clientHandler.setName(String.valueOf(clientSocket.getPort()));
                            clients.add(clientHandler);
                            clientHandler.start();
                        }
                    }
                } catch (IOException e) {
                    if (running){
                        throw new RuntimeException(e);
                    }
                }
            });

            serverControll.start();
            serverControll.join();

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeClient(ClientHandler clientHandler){
        clients.remove(clientHandler);
    }
}
