/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ee.tutorial.jaxrs.android;

import static org.jboss.ee.tutorial.jaxrs.android.LibraryApplication.LOG_TAG;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.util.Log;

/**
 * A simple JAX-RS endpoint
 * 
 * @author thomas.diesler@jboss.com
 * @since 23-Aug-2011
 */
public class LibraryAccess {

    private LibraryHttpClient httpClient;

    public LibraryAccess(Context context) {
        httpClient = new LibraryHttpClient(context);
    }

    public Collection<String> getBookTitles() {
        List<String> result = new ArrayList<String>();
        for (Book book : getBooks()) {
            String title = book.getTitle();
            result.add(title);
        }
        Log.i(LOG_TAG, "getBookTitles: " + result);
        return result;
    }

    public List<Book> getBooks() {
        List<Book> result = new ArrayList<Book>();
        String content = httpClient.get("books");
        Log.d(LOG_TAG, "Result content:" + content);
        if (content != null) {
            try {
                JSONTokener tokener = new JSONTokener(content);
                JSONArray array = (JSONArray) tokener.nextValue();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    String title = obj.getString("title");
                    String isbn = obj.getString("isbn");
                    result.add(new Book(isbn, title));
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        Log.i(LOG_TAG, "getBooks: " + result);
        return result;
    }

    public Book getBookByIndex(int index) {
        Book book = getBooks().get(index);
        Log.i(LOG_TAG, "getBookByIndex: " + book);
        return book;
    }

    public Book getBook(String isbn) {
        Book result = null;
        for (Book book : getBooks()) {
            if (isbn.equals(book.getIsbn())) {
                Log.i(LOG_TAG, "getBook: " + book);
                result = book;
                break;
            }
        }
        return result;
    }

    public Book addBook(String isbn, String title) {
        Book result = null;
        String content = httpClient.put("book/" + isbn, title);
        Log.d(LOG_TAG, "Result content:" + content);
        if (content != null) {
            try {
                JSONTokener tokener = new JSONTokener(content);
                JSONObject obj = (JSONObject) tokener.nextValue();
                String jsonTitle = obj.getString("title");
                String jsonIsbn = obj.getString("isbn");
                result = new Book(jsonIsbn, jsonTitle);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        Log.i(LOG_TAG, "addBook: " + result);
        return result;
    }

    public Book updateBook(String isbn, String title) {
        Book result = null;
        String content = httpClient.post("book/" + isbn, title);
        Log.d(LOG_TAG, "Result content:" + content);
        if (content != null) {
            try {
                JSONTokener tokener = new JSONTokener(content);
                JSONObject obj = (JSONObject) tokener.nextValue();
                String jsonTitle = obj.getString("title");
                String jsonIsbn = obj.getString("isbn");
                result = new Book(jsonIsbn, jsonTitle);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        Log.i(LOG_TAG, "updateBook: " + result);
        return result;
    }

    public Book removeBook(String isbn) {
        Book result = null;
        String content = httpClient.delete("book/" + isbn);
        Log.d(LOG_TAG, "Result content:" + content);
        if (content != null) {
            try {
                JSONTokener tokener = new JSONTokener(content);
                JSONObject obj = (JSONObject) tokener.nextValue();
                String jsonTitle = obj.getString("title");
                String jsonIsbn = obj.getString("isbn");
                result = new Book(jsonIsbn, jsonTitle);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        Log.i(LOG_TAG, "removeBook: " + result);
        return result;
    }
}