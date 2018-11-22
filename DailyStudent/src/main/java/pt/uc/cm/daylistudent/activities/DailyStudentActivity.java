package pt.uc.cm.daylistudent.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import pt.uc.cm.daylistudent.R;
import pt.uc.cm.daylistudent.adapters.BudgetDbAdapter;
import pt.uc.cm.daylistudent.adapters.NotesDbAdapter;
import pt.uc.cm.daylistudent.fragments.NoteEdit;
import pt.uc.cm.daylistudent.utils.SharedPreferencesUtils;

public class DailyStudentActivity extends AppCompatActivity {

    private final String TAG = DailyStudentActivity.class.getSimpleName();

    private static final int ACTIVITY_CREATE = 0;
    private static final int ACTIVITY_EDIT = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private static final int DELETE_ID = Menu.FIRST + 1;

    private NotesDbAdapter mDbHelper;
    private BudgetDbAdapter mDbBudgetHelper;

    private Cursor mNotesCursor;

    static ListView lv_notes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferencesUtils.INSTANCE.readPreferencesUser(getApplicationContext());
        setContentView(R.layout.notes_list);


        setTitle(getString(R.string.dayliStudentActivityTitle));

        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();

        mDbBudgetHelper = new BudgetDbAdapter(this);
        mDbBudgetHelper.open();

        fillData();

        lv_notes = findViewById(R.id.lv_notes);
        registerForContextMenu(lv_notes);

        lv_notes.setOnItemClickListener((adapterView, view, position, id) -> {
            Cursor c = mNotesCursor;
            c.moveToPosition(position);
            Intent i = new Intent(getApplicationContext(), NoteEdit.class);
            i.putExtra(NotesDbAdapter.KEY_ROWID, position);
            i.putExtra(NotesDbAdapter.KEY_TITLE, c.getString(c.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));
            i.putExtra(NotesDbAdapter.KEY_BODY, c.getString(c.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));
            startActivityForResult(i, ACTIVITY_EDIT);
        });

        isReadStoragePermissionGranted();
        isWriteStoragePermissionGranted();
    }

    public boolean isReadStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted1");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked1");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted1");
            return true;
        }
    }

    public boolean isWriteStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted2");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked2");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted2");
            return true;
        }
    }

    private void fillData() {
        mNotesCursor = mDbHelper.fetchAllNotes();
        startManagingCursor(mNotesCursor);

        String[] from = new String[]{NotesDbAdapter.KEY_TITLE, NotesDbAdapter.KEY_BODY, NotesDbAdapter.KEY_DATE};

        int[] to = new int[]{R.id.tvTitulo, R.id.tvBody, R.id.tvDate};

        SimpleCursorAdapter notes =
                new SimpleCursorAdapter(this, R.layout.notes_row, mNotesCursor, from, to);
        lv_notes = findViewById(R.id.lv_notes);
        lv_notes.setAdapter(notes);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_sup_notes, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.add(0, DELETE_ID, 0, R.string.DayliStudentActivityDeleteNote);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case DELETE_ID:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deleteNote(info.id);
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    public void createBudgetAction(MenuItem item) {
        Intent i = new Intent(this, NoteEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {

            Bundle extras = intent.getExtras();

            switch (requestCode) {
                case ACTIVITY_CREATE:
                    String title = extras.getString(NotesDbAdapter.KEY_TITLE);
                    String body = extras.getString(NotesDbAdapter.KEY_BODY);
                    mDbHelper.createNote(title, body);
                    fillData();
                    break;
                case ACTIVITY_EDIT:
                    Integer mRowId = extras.getInt(NotesDbAdapter.KEY_ROWID);
                    if (mRowId != null) {
                        String editTitle = extras.getString(NotesDbAdapter.KEY_TITLE);
                        String editBody = extras.getString(NotesDbAdapter.KEY_BODY);
                        mDbHelper.updateNote(mRowId + 1, editTitle, editBody);
                    }
                    fillData();
                    break;
                default:
            }
        }
    }
}