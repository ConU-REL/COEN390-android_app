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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;


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

        // Create tables SQL execution
        String CREATE_COURSE_TABLE = "CREATE TABLE " + Config.TABLE_SESSION+ "("
                + Config.COLUMN_SESSION_ID+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Config.COLUMN_SESSION_TITLE+ " TEXT NOT NULL, "
                + Config.COLUMN_SESSION_ERRORS+ " TEXT NOT NULL, "
                + Config.COLUMN_SESSION_USERS+ " TEXT NOT NULL "
                + ")";

        Log.d(TAG,"Table create SQL: " + CREATE_COURSE_TABLE);

        db.execSQL(CREATE_COURSE_TABLE);


        String CREATE_USERS_TABLE = "CREATE TABLE " + Config.USERS_TABLE + "("
                + Config.COLUMN_USERS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Config.COLUMN_USER_NAMES + " TEXT NOT NULL, "
                + Config.COLUMN_SESSION_ID + " TEXT, "
                + Config.COLUMN_USERS_ROLES + " TEXT "
                + ")";

        Log.d(TAG, "Table create SQL: " + CREATE_USERS_TABLE);

        db.execSQL(CREATE_USERS_TABLE);

        Log.d(TAG,"DB created!");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + Config.USERS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + Config.TABLE_SESSION);

        // Create tables again
        onCreate(db);
    }

    public long insertSession(Sessions session){

        long id = -1;
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(Config.COLUMN_SESSION_TITLE, session.getSession_name());
        contentValues.put(Config.COLUMN_SESSION_ERRORS, session.getWarnings());
        contentValues.put(Config.COLUMN_SESSION_USERS, session.getUsers());


        try {
            id = sqLiteDatabase.insertOrThrow(Config.TABLE_SESSION, null, contentValues);
        } catch (SQLiteException e){
            Log.d(TAG,"Exception: " + e.getMessage());
            //Toast.makeText(context, "Operation failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            sqLiteDatabase.close();
        }

        return id;
    }

    public List<Sessions> getAllSessions(){

        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        Cursor cursor = null;
        try {

            cursor = sqLiteDatabase.query(Config.TABLE_SESSION, null, null, null, null, null, null, null);

            /**
             // If you want to execute raw query then uncomment below 2 lines. And comment out above line.
             String SELECT_QUERY = String.format("SELECT %s, %s, %s, %s, %s FROM %s", Config.COLUMN_STUDENT_ID, Config.COLUMN_STUDENT_NAME, Config.COLUMN_STUDENT_REGISTRATION, Config.COLUMN_STUDENT_EMAIL, Config.COLUMN_STUDENT_PHONE, Config.TABLE_STUDENT);
             cursor = sqLiteDatabase.rawQuery(SELECT_QUERY, null);
             */

            if(cursor!=null)
                if(cursor.moveToFirst()){
                    List<Sessions> sessionList = new ArrayList<>();
                    do {
                        int id = cursor.getInt(cursor.getColumnIndex(Config.COLUMN_SESSION_ID));
                        String title = cursor.getString(cursor.getColumnIndex(Config.COLUMN_SESSION_TITLE));
                        String users = cursor.getString(cursor.getColumnIndex(Config.COLUMN_SESSION_USERS));
                        String errors = cursor.getString(cursor.getColumnIndex(Config.COLUMN_SESSION_ERRORS));

                        sessionList.add(new Sessions(id, title, users,errors));
                    }   while (cursor.moveToNext());

                    return sessionList;
                }
        } catch (Exception e){
            Log.d(TAG,"Exception: " + e.getMessage());
            Toast.makeText(context, "Operation failed", Toast.LENGTH_SHORT).show();
        } finally {
            if(cursor!=null)
                cursor.close();
            sqLiteDatabase.close();
        }

        return Collections.emptyList();
    }

    public long updateSessionInfo(Sessions session){

        long rowCount = 0;
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(Config.COLUMN_SESSION_TITLE, session.getSession_name());
        contentValues.put(Config.COLUMN_SESSION_USERS, session.getUsers());
        contentValues.put(Config.COLUMN_SESSION_ERRORS, session.getWarnings());

        try {
            rowCount = sqLiteDatabase.update(Config.TABLE_SESSION, contentValues,
                    Config.COLUMN_SESSION_ID + " = ? ",
                    new String[] {String.valueOf(session.getSession_id())});
        } catch (SQLiteException e){
            Log.d(TAG,"Exception: " + e.getMessage());
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            sqLiteDatabase.close();
        }

        return rowCount;
    }


    public boolean deleteAllSessions(){
        boolean deleteStatus = false;
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        try {
            //for "1" delete() method returns number of deleted rows
            //if you don't want row count just use delete(TABLE_NAME, null, null)
            sqLiteDatabase.delete(Config.TABLE_SESSION, null, null);

            long count = DatabaseUtils.queryNumEntries(sqLiteDatabase, Config.TABLE_SESSION);

            if(count==0)
                deleteStatus = true;

        } catch (SQLiteException e){
            Log.d(TAG,"Exception: " + e.getMessage());
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            sqLiteDatabase.close();
        }

        return deleteStatus;
    }






    public long insertUsers(User users){

        long id = -1;
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(Config.COLUMN_USER_NAMES, users.getUserName());
        contentValues.put(Config.COLUMN_SESSION_ID,users.getSessionID());
        contentValues.put(Config.COLUMN_USERS_ROLES,users.getUserRole());



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
                        String role=cursor.getString(cursor.getColumnIndex(Config.COLUMN_USERS_ROLES));



                        usersList.add(new User(name,id,session_id,role));
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


   
