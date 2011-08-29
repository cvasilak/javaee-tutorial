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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.jboss.ee.tutorial.jaxrs.android.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * 
 * @author thomas.diesler@jboss.com
 * @since 23-Aug-2011
 */
public class LibraryHttpClient implements LibraryClient {

    private final Context context;
    private final HttpClient httpClient;

    public LibraryHttpClient(Context context) {
        this.context = context;
        
        BasicHttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
        HttpProtocolParams.setUseExpectContinue(params, false);
        httpClient = new DefaultHttpClient(params);
    }

    @Override
    public List<Book> getBooks() {
        List<Book> result = new ArrayList<Book>();
        String content = get("books");
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

    @Override
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

    @Override
    public Book addBook(String isbn, String title) {
        Book result = null;
        String content = put("book/" + isbn, title);
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

    @Override
    public Book updateBook(String isbn, String title) {
        Book result = null;
        String content = post("book/" + isbn, title);
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

    @Override
    public Book removeBook(String isbn) {
        Book result = null;
        String content = delete("book/" + isbn);
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

    private String get(String path) {
        try {
            HttpGet request = new HttpGet(getRequestURI(path));
            HttpResponse res = httpClient.execute(request);
            String content = EntityUtils.toString(res.getEntity());
            return content;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private String put(String path, String title) {
        try {
            HttpPut request = new HttpPut(getRequestURI(path + "?title=" + title));
            HttpResponse res = httpClient.execute(request);
            String content = EntityUtils.toString(res.getEntity());
            return content;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    private String post(String path, String title) {
        try {
            HttpPost request = new HttpPost(getRequestURI(path));
            request.setHeader("Content-Type", "application/json");
            request.setEntity(new StringEntity(title));
            HttpResponse res = httpClient.execute(request);
            String content = EntityUtils.toString(res.getEntity());
            return content;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    private String delete(String path) {
        try {
            HttpDelete request = new HttpDelete(getRequestURI(path));
            HttpResponse res = httpClient.execute(request);
            String content = EntityUtils.toString(res.getEntity());
            return content;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private URI getRequestURI(String path) throws URISyntaxException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String hostKey = context.getString(R.string.pref_host_key);
        String host = prefs.getString(hostKey, context.getString(R.string.pref_host_default));
        String portKey = context.getString(R.string.pref_port_key);
        String port = prefs.getString(portKey, context.getString(R.string.pref_port_default));
        URI requestURI = new URI("http://" +  host + ":" + port + "/jaxrs-sample/library/" + path);
        Log.i(LOG_TAG, "requestURI: " + requestURI);
        return requestURI;
    }
}