package main.library.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler extends Thread{

    private final Socket clientSocket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private final BookManager bookManager;

    public ClientHandler(Socket socket, BookManager bookManager) {
        this.clientSocket = socket;
        this.out = getObjectOut();
        this.in = getObjectIn();
        this.bookManager = bookManager;
    }

    private ObjectOutputStream getObjectOut(){
        try {
            return new ObjectOutputStream(this.clientSocket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ObjectInputStream getObjectIn(){
        try {
            return new ObjectInputStream(this.clientSocket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void run() {
        menu();

        try {
            this.out.close();
            this.in.close();
            this.clientSocket.close();

            LibraryServer.removeClient(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void menu(){
        try {
            String escolha;

            do{
                this.out.writeObject("Escolha a opção:");
                this.out.writeObject("(1) listar");
                this.out.writeObject("(2) registrar");
                this.out.writeObject("(3) alugar");
                this.out.writeObject("(4) devolver");
                this.out.writeObject("sair");

                escolha = (String) this.in.readObject();

                switch (escolha){
                    case "listar": case "1":
                        this.bookManager.list_books(this);
                        break;
                    case "registrar": case "2":
                        this.bookManager.register_book(this);
                        break;
                    case "alugar": case "3":
                        this.bookManager.check_out_book(this);
                        break;
                    case "devolvel": case "4":
                        this.bookManager.check_in_book(this);
                        break;
                    case "sair":
                        break;
                }
            } while (!escolha.equals("sair"));

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public ObjectOutputStream getOut() {
        return out;
    }
    public Socket getClientSocket() {
        return clientSocket;
    }
    public ObjectInputStream getIn() {
        return in;
    }

}
