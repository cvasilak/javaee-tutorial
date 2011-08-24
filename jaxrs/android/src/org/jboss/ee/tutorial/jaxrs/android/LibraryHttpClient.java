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

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;


/**
 * The Library HttpClient
 * 
 * @author thomas.diesler@jboss.com
 * @since 23-Aug-2011
 */
public class LibraryHttpClient implements OnSharedPreferenceChangeListener {

    private final Context context;
    private ClientConnectionManager conManager;
    private HttpClient httpClient;
    private String requestURL;
    
    public LibraryHttpClient(Context context) {
        this.context = context;
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.registerOnSharedPreferenceChangeListener(this);
        
        initHttpClient();
    }

    private void initHttpClient() {
        if (conManager != null) {
            conManager.shutdown();
        }
        
        BasicHttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
        HttpProtocolParams.setUseExpectContinue(params, false);
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String hostKey = context.getString(R.string.pref_host_key);
        String host = prefs.getString(hostKey, context.getString(R.string.pref_host_summary));
        String portKey = context.getString(R.string.pref_port_key);
        int port = Integer.parseInt(prefs.getString(portKey, "80"));
        
        SchemeRegistry sreg = new SchemeRegistry();
        sreg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), port));
        conManager = new ThreadSafeClientConnManager(params, sreg);
        httpClient = new DefaultHttpClient(conManager, params);
        requestURL = "http://" +  host + ":" + port + "/jaxrs-sample/library";
        Log.i(LOG_TAG, "requestURL: " + requestURL);
    }

    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        initHttpClient();
    }
    
    public void shutdown() {
        if (conManager != null) {
            conManager.shutdown();
        }
    }
    
    public String get(String path) {
        try {
            HttpGet request = new HttpGet(new URI(requestURL + "/" + path));
            HttpResponse res = httpClient.execute(request);
            String content = EntityUtils.toString(res.getEntity());
            return content;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public String put(String path, String title) {
        try {
            HttpPut request = new HttpPut(new URI(requestURL + "/" + path + "?title=" + title));
            HttpResponse res = httpClient.execute(request);
            String content = EntityUtils.toString(res.getEntity());
            return content;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public String post(String path, String title) {
        try {
            HttpPost request = new HttpPost(new URI(requestURL + "/" + path));
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
    
    public String delete(String path) {
        try {
            HttpDelete request = new HttpDelete(new URI(requestURL + "/" + path));
            HttpResponse res = httpClient.execute(request);
            String content = EntityUtils.toString(res.getEntity());
            return content;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}