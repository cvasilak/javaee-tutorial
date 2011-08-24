package org.jboss.ee.tutorial.jaxrs.android;


import android.app.Application;

public class LibraryApplication extends Application {

    private LibraryAccess library;
    public static String KEY_BOOK_ISBN = "KEY_BOOK_INDEX";
    public static String LOG_TAG = "JaxrsSample";

    @Override
    public void onCreate() {
        super.onCreate();
        library = new LibraryAccess(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        library.shutdown();
    }

    public LibraryAccess getLibraryAccess() {
        return library;
    }
}
