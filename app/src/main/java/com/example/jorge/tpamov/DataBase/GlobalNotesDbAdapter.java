package com.example.jorge.tpamov.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.jorge.tpamov.Classes.MessagePacket;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Jorge on 31/12/2016.
 */

public class GlobalNotesDbAdapter {

    public static final String KEY_ROWID = "_id";
    public static final String KEY_AUTHOR = "author";
    public static final String KEY_TITLE = "title";
    public static final String KEY_BODY = "body";
    public static final String KEY_DATE = "date" ;

    private static final String TAG = "GlobalNotesDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
            "create table GlobalNotes (_id integer primary key autoincrement, "
                    + "author text not null, "
                    + "title text not null, "
                    + "body text not null,"
                    + "date text not null);";

    private static final String DATABASE_NAME = "GlobalData";
    private static final String DATABASE_TABLE = "GlobalNotes";
    private static final int DATABASE_VERSION = 2;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }


    public GlobalNotesDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public GlobalNotesDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    public long createGlobalNote(String author, String title, String body) {
        Date date = new Date();
        String displayDate = new SimpleDateFormat("MMM dd, yyyy - h:mm a").format(new Date(date.getTime()));

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_AUTHOR, author);
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_BODY, body);
        initialValues.put(KEY_DATE, displayDate);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    public boolean deleteGlobalNote(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public Cursor fetchAllGlobalNotes() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID,KEY_AUTHOR, KEY_TITLE,
                KEY_BODY, KEY_DATE}, null, null, null, null, null);
    }


    public Cursor fetchGlobalNote(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,KEY_AUTHOR,
                                KEY_TITLE, KEY_BODY, KEY_DATE}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    public List<String[]> getLatestGlobalNotes() {
        List<String[]> dados = new ArrayList();
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String selectQuery = "SELECT  * FROM " + DATABASE_TABLE;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                String author = cursor.getString(1);
                String titulo = cursor.getString(2);
                String array[] = {author,titulo};
                System.out.println("AUTOR: " + author+ "TITULO:"+titulo);
                dados.add(array);
            } while (cursor.moveToNext());
        }
        cursor.close();

        String[] note = {"N/A","N/A"};
        for(int i = dados.size(); i < 3; i++){
            dados.add(note);
        }
        return dados;
    }



    public MessagePacket getGlobalNoteToSend(long id) {
        MessagePacket msg = null;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String selectQuery = "SELECT  * FROM " + DATABASE_TABLE;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                System.out.println(cursor.getDouble(0) + cursor.getString(1) + cursor.getString(2) + id);
                if(id == cursor.getDouble(0)) {
                    String author = cursor.getString(1);
                    String titulo = cursor.getString(2);
                    String obs = cursor.getString(3);
                    msg = new MessagePacket(author,titulo,obs);
                    break;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        return msg;
    }

    public boolean updateGlobalNote(long rowId,String author, String title, String body) {
        ContentValues args = new ContentValues();
        Date date = new Date();
        String displayDate = new SimpleDateFormat("MMM dd, yyyy - h:mm a").format(new Date(date.getTime()));
        args.put(KEY_AUTHOR, author);
        args.put(KEY_TITLE, title);
        args.put(KEY_BODY, body);
        args.put(KEY_DATE, displayDate);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
