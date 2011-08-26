package org.jboss.ee.tutorial.jaxrs.android.data;

import java.util.List;


public interface Library {

    List<Book> getBooks();

    Book getBook(String isbn);

    Book addBook(String isbn, String title);

    Book updateBook(String isbn, String title);

    Book removeBook(String isbn);

}