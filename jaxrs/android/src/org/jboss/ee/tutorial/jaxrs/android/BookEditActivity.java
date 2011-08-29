package org.jboss.ee.tutorial.jaxrs.android;


import org.jboss.ee.tutorial.jaxrs.android.data.Book;
import org.jboss.ee.tutorial.jaxrs.android.data.LibraryClient;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class BookEditActivity extends Activity {

    private EditText isbnText;
    private EditText titleText;
    private Button saveButton;
    private Book currBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.book_edit);

        isbnText = (EditText) findViewById(R.id.isbn);
        titleText = (EditText) findViewById(R.id.title);
        saveButton = (Button) findViewById(R.id.confirm);

        if (savedInstanceState != null) {
            long index = savedInstanceState.getLong(LibraryApplication.KEY_BOOK_ISBN);
            LibraryClient library = getLibrary();
            currBook = library.getBooks().get((int) index);
        }

        registerButtonListeners();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currBook == null) {
            Bundle extras = getIntent().getExtras();
            String isbn = extras != null ? extras.getString(LibraryApplication.KEY_BOOK_ISBN) : null;
            currBook = isbn != null ? getLibrary().getBook(isbn) : null;
        }
        populateFields();
    }

    private void registerButtonListeners() {

        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                saveState();
                setResult(RESULT_OK);
                Toast.makeText(BookEditActivity.this, getString(R.string.book_saved_message), Toast.LENGTH_SHORT).show();
                finish();
            }

        });
    }

    private void populateFields() {
        if (currBook != null) {
            titleText.setText(currBook.getTitle());
            isbnText.setText(currBook.getIsbn());
            
            isbnText.setEnabled(false);
            titleText.requestFocus();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(LibraryApplication.KEY_BOOK_ISBN, currBook.getIsbn());
    }

    private void saveState() {
        String title = titleText.getText().toString();
        String isbn = isbnText.getText().toString();
        LibraryClient library = getLibrary();
        if (currBook == null) {
            library.addBook(isbn, title);
        }
        else {
            library.updateBook(isbn, title);
        }
    }

    private LibraryClient getLibrary() {
        LibraryApplication app = (LibraryApplication) getApplication();
        return app.getLibraryClient();
    }
}
