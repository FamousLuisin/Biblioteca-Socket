package main.library.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class LibraryClient {

    private static final int SERVER_PORT = 8080;
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final Scanner SCANNER = new Scanner(System.in);
    private static volatile boolean running = true;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            ObjectInputStream in =  new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

            System.out.println("Connected to the server.");

            Thread readerThread = new Thread(() -> {
                try {
                    while (running) {
                        String serverMessage = (String) in.readObject();
                        if (serverMessage != null) {
                            System.out.println(serverMessage);
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    if (running) {
                        e.printStackTrace();
                    }
                }
            });
            readerThread.start();

            String string = "";
            while (!string.equalsIgnoreCase("sair")) {
                string = SCANNER.nextLine();
                out.writeObject(string);
            }

            running = false;

            socket.close();

            try {
                readerThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
