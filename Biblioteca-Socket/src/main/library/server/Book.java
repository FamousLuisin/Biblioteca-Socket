package main.library.server;

import java.util.concurrent.atomic.AtomicInteger;

public class Book {
    private String titulo;
    private String autor;
    private String genero;
    private AtomicInteger exemplares;

    public Book(String name, String author, String genre, Integer copies) {
        this.titulo = name;
        this.autor = author;
        this.genero = genre;
        this.exemplares = new AtomicInteger(copies);
    }

    @Override
    public String toString() {
        String book = "Nome: %s  Autor: %s  Gênero: %s  Cópias disponíveis: %d";
        return String.format(book, titulo, autor, genero, exemplares.get());
    }

    public void reserve(){
        this.exemplares.decrementAndGet();
    }

    public void refund(){
        this.exemplares.incrementAndGet();
    }

    public Integer getExemplares(){
        return this.exemplares.get();
    }
}
