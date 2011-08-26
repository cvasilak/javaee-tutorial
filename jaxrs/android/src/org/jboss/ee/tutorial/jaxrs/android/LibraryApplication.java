package org.jboss.ee.tutorial.jaxrs.android;


import org.jboss.ee.tutorial.jaxrs.android.data.LibraryClient;
import org.jboss.ee.tutorial.jaxrs.android.data.LibraryResteasyClient;

import android.app.Application;

public class LibraryApplication extends Application {

    private LibraryClient library;
    public static String KEY_BOOK_ISBN = "KEY_BOOK_INDEX";
    public static String LOG_TAG = "JaxrsSample";

    @Override
    public void onCreate() {
        super.onCreate();
        library = new LibraryResteasyClient(this);
    }

    public LibraryClient getLibrary() {
        return library;
    }
}
