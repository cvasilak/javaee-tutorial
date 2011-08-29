package org.jboss.ee.tutorial.jaxrs.android;


import org.jboss.ee.tutorial.jaxrs.android.data.LibraryClient;
import org.jboss.ee.tutorial.jaxrs.android.data.LibraryResteasyClient;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class LibraryApplication extends Application {

    private LibraryClient library;
    public static String KEY_BOOK_ISBN = "KEY_BOOK_INDEX";
    public static String LOG_TAG = "JaxrsSample";

    @Override
    public void onCreate() {
        super.onCreate();
        library = new LibraryResteasyClient(this);
    }

    public LibraryClient getLibraryClient() {
        return library;
    }

    public static String getRequestURI(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String hostKey = context.getString(R.string.pref_host_key);
        String host = prefs.getString(hostKey, context.getString(R.string.pref_host_default));
        String portKey = context.getString(R.string.pref_port_key);
        String port = prefs.getString(portKey, context.getString(R.string.pref_port_default));
        String requestURI = "http://" +  host + ":" + port + "/jaxrs-sample/library";
        Log.i(LOG_TAG, "requestURI: " + requestURI);
        return requestURI;
    }
}
