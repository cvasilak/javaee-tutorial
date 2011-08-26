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
package org.jboss.ee.tutorial.jaxrs.android.data;

import static org.jboss.ee.tutorial.jaxrs.android.LibraryApplication.LOG_TAG;

import java.util.List;

import org.jboss.ee.tutorial.jaxrs.android.R;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * 
 * @author thomas.diesler@jboss.com
 * @since 23-Aug-2011
 */
public class LibraryResteasyClient implements LibraryClient {

    private final Context context;
    private LibraryClient client;

    public LibraryResteasyClient(Context context) {
        this.context = context;
        
        RegisterBuiltin.register(ResteasyProviderFactory.getInstance());
        client = ProxyFactory.create(LibraryClient.class, getRequestURI());
    }

    @Override
    public List<Book> getBooks() {
        List<Book> result = client.getBooks();
        Log.i(LOG_TAG, "getBooks: " + result);
        return result;
    }

    @Override
    public Book getBook(String isbn) {
        Book result = client.getBook(isbn);
        Log.i(LOG_TAG, "getBook: " + result);
        return result;
    }

    @Override
    public Book addBook(String isbn, String title) {
        Book result = client.addBook(isbn, title);
        Log.i(LOG_TAG, "addBook: " + result);
        return result;
    }

    @Override
    public Book updateBook(String isbn, String title) {
        Book result = client.updateBook(isbn, title);
        Log.i(LOG_TAG, "updateBook: " + result);
        return result;
    }

    @Override
    public Book removeBook(String isbn) {
        Book result = client.removeBook(isbn);
        Log.i(LOG_TAG, "removeBook: " + result);
        return result;
    }

    private String getRequestURI() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String hostKey = context.getString(R.string.pref_host_key);
        String host = prefs.getString(hostKey, "javaeetutorial-tdiesler.rhcloud.com");
        String portKey = context.getString(R.string.pref_port_key);
        int port = Integer.parseInt(prefs.getString(portKey, "80"));
        String requestURI = "http://" +  host + ":" + port + "/jaxrs-sample/library";
        Log.i(LOG_TAG, "requestURI: " + requestURI);
        return requestURI;
    }
}