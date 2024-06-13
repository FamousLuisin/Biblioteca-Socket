package main.library.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class BookManager {

    private final String JSON;
    private final Gson gson;
    private final List<Book> books;

    public BookManager(String JSON) {
        this.JSON = JSON;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.books = new ArrayList<>();

        this.json_to_book_list();
    }

    private void json_to_book_list(){
        try {
            BufferedReader br = new BufferedReader(new FileReader(JSON));

            JsonObject reader_json = gson.fromJson(br, JsonObject.class);
            JsonArray json_books = reader_json.get("livros").getAsJsonArray();

            json_books.forEach(element -> {
                Book book = gson.fromJson(element, Book.class);
                this.books.add(book);
            });
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void book_list_to_json(){
        JsonArray json_books = new JsonArray();
        this.books.forEach(element -> json_books.add(gson.toJsonTree(element)));

        JsonObject update = new JsonObject();
        update.add("livros", json_books);

        try(JsonWriter jw = new JsonWriter(new FileWriter(JSON))) {
            jw.setIndent("  ");
            gson.toJson(update, jw);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void list_books(ClientHandler client){
        AtomicInteger index = new AtomicInteger();

        Consumer<Book> consumer = element -> {
            try {
                client.getOut().writeObject("(" + index.getAndIncrement() + ") " + element.toString());
                client.getOut().flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        books.forEach(consumer);
    }

    public void register_book(ClientHandler client) throws IOException {
        try {
            client.getOut().writeObject("Nome do livro: ");
            client.getOut().flush();
            String book_name = (String) client.getIn().readObject();

            client.getOut().writeObject("Nome do autor: ");
            client.getOut().flush();
            String book_author = (String) client.getIn().readObject();

            client.getOut().writeObject("Genero: ");
            client.getOut().flush();
            String book_genre = (String) client.getIn().readObject();

            client.getOut().writeObject("Numero de exemplares: ");
            client.getOut().flush();
            Integer book_copies = Integer.parseInt((String) client.getIn().readObject());

            Book newBook = new Book(book_name, book_author, book_genre, book_copies);
            this.books.add(newBook);

            this.book_list_to_json();

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NumberFormatException e){
            client.getOut().writeObject("Digite um valor valido para os exemplares");
        }
    }

    public void check_out_book(ClientHandler client) throws IOException {
        this.list_books(client);

        try {
            client.getOut().writeObject("Qual livro deseja alugar?: ");
            client.getOut().flush();
            Integer index = Integer.parseInt((String) client.getIn().readObject());

            Book book = this.books.get(index);

            if (book.getExemplares() != 0){
                book.reserve();
                this.book_list_to_json();
            } else{
                client.getOut().writeObject("Livro escolhido não possui exemplares disponiveis");
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NumberFormatException | IndexOutOfBoundsException e){
            client.getOut().writeObject("Operação inválida: Digite um index valido");
        }
    }

    public void check_in_book(ClientHandler client) throws IOException{
        this.list_books(client);

        try {
            client.getOut().writeObject("Qual livro deseja devolver?: ");
            client.getOut().flush();
            Integer index = Integer.parseInt((String) client.getIn().readObject());


            Book book = this.books.get(index);
            book.refund();
            this.book_list_to_json();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NumberFormatException | IndexOutOfBoundsException e){
            client.getOut().writeObject("Operação inválida: Digite um index valido");
        }
    }
}
