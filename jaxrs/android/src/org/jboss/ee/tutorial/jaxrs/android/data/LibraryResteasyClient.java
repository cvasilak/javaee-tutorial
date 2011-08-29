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

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpVersion;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.jboss.ee.tutorial.jaxrs.android.LibraryApplication;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import android.content.Context;
import android.util.Log;

/**
 * 
 * @author thomas.diesler@jboss.com
 * @since 23-Aug-2011
 */
public class LibraryResteasyClient implements LibraryClient {

    private final Context context;
    private String lastRequestURI;
    private LibraryClient client;

    public LibraryResteasyClient(Context context) {
        this.context = context;
        RegisterBuiltin.register(ResteasyProviderFactory.getInstance());
    }

    @Override
    public List<Book> getBooks() {
        List<Book> result = new ArrayList<Book>();
        try {
            result = getLibraryClient().getBooks();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }
        Log.i(LOG_TAG, "getBooks: " + result);
        return result;
    }

    @Override
    public Book getBook(String isbn) {
        Book result = null;
        try {
            result = getLibraryClient().getBook(isbn);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }
        Log.i(LOG_TAG, "getBook: " + result);
        return result;
    }

    @Override
    public Book addBook(String isbn, String title) {
        Book result = null;
        try {
            result = getLibraryClient().addBook(isbn, title);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }
        Log.i(LOG_TAG, "addBook: " + result);
        return result;
    }

    @Override
    public Book updateBook(String isbn, String title) {
        Book result = null;
        try {
            result = getLibraryClient().updateBook(isbn, title);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }
        Log.i(LOG_TAG, "updateBook: " + result);
        return result;
    }

    @Override
    public Book removeBook(String isbn) {
        Book result = null;
        try {
            result = getLibraryClient().removeBook(isbn);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }
        Log.i(LOG_TAG, "removeBook: " + result);
        return result;
    }

    private LibraryClient getLibraryClient() {
        String requestURI = LibraryApplication.getRequestURI(context);
        if (client == null || !requestURI.equals(lastRequestURI)) {
            BasicHttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
            HttpProtocolParams.setUseExpectContinue(params, false);
            client = ProxyFactory.create(LibraryClient.class, requestURI, new ApacheHttpClient4Executor(params));
            lastRequestURI = requestURI;
        }
        return client;
    }

}