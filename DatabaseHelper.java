package com.example.d_gille.myapplication.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.example.d_gille.myapplication.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper
{

    private static final String TAG = "DatabaseHelper";

    // All Static variables
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = Config.DATABASE_NAME;

    private Context context = null;
    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }
    /*
        public static synchronized DatabaseHelper getInstance(Context context){
            if(databaseHelper==null){
                databaseHelper = new DatabaseHelper(context);
            }
            return databaseHelper;
        }
    */
    @Override
    public void onCreate(SQLiteDatabase db) {


        String CREATE_USERS_TABLE = "CREATE TABLE " + Config.USERS_TABLE + "("
                + Config.COLUMN_USERS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Config.COLUMN_USER_NAMES + " TEXT NOT NULL,"
                + Config.COLUMN_SESSION_ID + " TEXT "
                + ")";

        Log.d(TAG, "Table create SQL: " + CREATE_USERS_TABLE);

        db.execSQL(CREATE_USERS_TABLE);

        Log.d(TAG,"DB created!");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + Config.USERS_TABLE);

        // Create tables again
        onCreate(db);
    }



    public long insertUsers(User users){

        long id = -1;
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(Config.COLUMN_USER_NAMES, users.getUserName());
        contentValues.put(Config.COLUMN_USERS_ID, users.getUserID());
        contentValues.put(Config.COLUMN_SESSION_ID,users.getSessionID());



        try {
            id = sqLiteDatabase.insertOrThrow(Config.USERS_TABLE, null, contentValues);
        } catch (SQLiteException e){
            Log.d(TAG,"Exception: " + e.getMessage());
            Toast.makeText(context, "Operation failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            sqLiteDatabase.close();
        }

        return id;
    }

    public List<User> getAllUsers()
    {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        Cursor cursor = null;
        try {

            cursor = sqLiteDatabase.query(Config.USERS_TABLE, null, null, null, null, null, null, null);

            /**
             // If you want to execute raw query then uncomment below 2 lines. And comment out above line.
             String SELECT_QUERY = String.format("SELECT %s, %s, %s, %s, %s FROM %s", Config.COLUMN_STUDENT_ID, Config.COLUMN_STUDENT_NAME, Config.COLUMN_STUDENT_REGISTRATION, Config.COLUMN_STUDENT_EMAIL, Config.COLUMN_STUDENT_PHONE, Config.TABLE_STUDENT);
             cursor = sqLiteDatabase.rawQuery(SELECT_QUERY, null);
             */

            if (cursor != null)
                if (cursor.moveToFirst()) {
                    List<User> usersList = new ArrayList<>();
                    do {
                        int id = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_USERS_ID));
                        String name = cursor.getString(cursor.getColumnIndex(Config.COLUMN_USER_NAMES));
                        long session_id=cursor.getInt(cursor.getColumnIndex(Config.COLUMN_SESSION_ID));


                        usersList.add(new User(name,id,session_id));
                    } while (cursor.moveToNext());

                    return usersList;
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

    public List<User> getListUsersByID(long sessionID)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(Config.USERS_TABLE, null, Config.COLUMN_SESSION_ID + "= ?", new String[] {String.valueOf(sessionID)}, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    List<User> users = new ArrayList<>();
                    do {
                        int id = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_USERS_ID));
                        String name = cursor.getString(cursor.getColumnIndex(Config.COLUMN_USER_NAMES));

                        users.add(new User(name,id,sessionID));
                    } while (cursor.moveToNext());

                    return users;
                }

            }
        } catch (SQLException e) {
            Log.d(TAG, "EXCEPTION:" + e);
            Toast.makeText(context, "Operation Failed!: " + e, Toast.LENGTH_LONG).show();
        } finally {
            if (cursor != null)
                cursor.close();

            db.close();
        }


        return Collections.emptyList();
    }

    public User searchUserByID(Integer userID)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(Config.USERS_TABLE, null, Config.COLUMN_USERS_ID + "= ?", new String[]{String.valueOf(userID)}, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {

                    int id = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_USERS_ID));
                    String name = cursor.getString(cursor.getColumnIndex(Config.COLUMN_USER_NAMES));


                    return new User(name,id);
                }
            }


        } catch (SQLException e) {
            Log.d(TAG, "EXCEPTION:" + e);
            Toast.makeText(context, "Operation Failed!: " + e, Toast.LENGTH_LONG).show();
        } finally {
            if (cursor != null)
                cursor.close();

            db.close();
        }

        return null;

    }


}
