package com.example.app.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.example.app.Sessions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * Database Helper class, used to interface with the db
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    // All Static variables
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = Config.DATABASE_NAME;

    private Context context;

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create tables SQL execution
        String CREATE_COURSE_TABLE = "CREATE TABLE " + Config.TABLE_SESSION + "("
                + Config.COLUMN_SESSION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Config.COLUMN_SESSION_TITLE + " TEXT NOT NULL, "
                + Config.COLUMN_SESSION_ERRORS + " TEXT NOT NULL, "
                + Config.COLUMN_SESSION_USERS + " TEXT NOT NULL "
                + ")";

        Log.d(TAG, "Table create SQL: " + CREATE_COURSE_TABLE);

        db.execSQL(CREATE_COURSE_TABLE);

        Log.d(TAG, "DB created!");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + Config.TABLE_SESSION);

        // Create tables again
        onCreate(db);
    }


    // function to insert a session into the db
    public void insertSession(Sessions session) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Config.COLUMN_SESSION_TITLE, session.getSession_name());
        contentValues.put(Config.COLUMN_SESSION_ERRORS, session.getWarnings());
        contentValues.put(Config.COLUMN_SESSION_USERS, session.getUsers());

        try {
        } catch (SQLiteException e) {
            Log.d(TAG, "Exception: " + e.getMessage());
            //Toast.makeText(context, "Operation failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            sqLiteDatabase.close();
        }
    }

    // function to return all sessions stored in the db
    public List<Sessions> getAllSessions() {

        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.query(Config.TABLE_SESSION, null, null, null, null, null, null, null);
            if (cursor != null)
                if (cursor.moveToFirst()) {
                    List<Sessions> sessionList = new ArrayList<>();
                    do {
                        int id = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_SESSION_ID));
                        String title = cursor.getString(cursor.getColumnIndex(Config.COLUMN_SESSION_TITLE));
                        String users = cursor.getString(cursor.getColumnIndex(Config.COLUMN_SESSION_USERS));
                        String errors = cursor.getString(cursor.getColumnIndex(Config.COLUMN_SESSION_ERRORS));

                        sessionList.add(new Sessions(id, title, users, errors));
                    } while (cursor.moveToNext());

                    return sessionList;
                }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.getMessage());
            Toast.makeText(context, "Operation failed", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null)
                cursor.close();
            sqLiteDatabase.close();
        }
        return Collections.emptyList();
    }
}