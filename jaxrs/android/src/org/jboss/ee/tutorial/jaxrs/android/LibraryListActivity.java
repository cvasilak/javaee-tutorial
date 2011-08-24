package org.jboss.ee.tutorial.jaxrs.android;

import java.util.Collection;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class LibraryListActivity extends ListActivity {

    private static final int ACTIVITY_CREATE = 0;
    private static final int ACTIVITY_EDIT = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_list);
        fillData();
        registerForContextMenu(getListView());
    }

    private void fillData() {
        LibraryAccess library = getLibraryAccess();
        Collection<String> bookNames = library.getBookTitles();
        String[] items = new String[bookNames.size()];
        bookNames.toArray(items);
        ArrayAdapter<String> books = new ArrayAdapter<String>(this, R.layout.book_row, R.id.text1, items);
        setListAdapter(books);
    }

    private LibraryAccess getLibraryAccess() {
        LibraryApplication app = (LibraryApplication) getApplication();
        return app.getLibraryAccess();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.list_menu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_insert:
                createBook();
                return true;
            case R.id.menu_settings:
                Intent intent = new Intent(this, LibraryPreferences.class);
                startActivity(intent);
                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.list_menu_item_longpress, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                LibraryAccess library = getLibraryAccess();
                Book book = library.getBookByIndex((int) info.id);
                library.removeBook(book.getIsbn());
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void createBook() {
        Intent i = new Intent(this, BookEditActivity.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, BookEditActivity.class);
        Book book = getLibraryAccess().getBookByIndex((int) id);
        i.putExtra(LibraryApplication.KEY_BOOK_ISBN, book.getIsbn());
        startActivityForResult(i, ACTIVITY_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }
}
