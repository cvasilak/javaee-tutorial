package org.jboss.ee.tutorial.jaxrs.android;


import org.jboss.ee.tutorial.jaxrs.android.data.Library;
import org.jboss.ee.tutorial.jaxrs.android.data.LibraryHttpClient;

import android.app.Application;

public class LibraryApplication extends Application {

    private Library library;
    public static String KEY_BOOK_ISBN = "KEY_BOOK_INDEX";
    public static String LOG_TAG = "JaxrsSample";

    @Override
    public void onCreate() {
        super.onCreate();
        library = new LibraryHttpClient(this);
    }

    public Library getLibrary() {
        return library;
    }
}
