package com.example.app.Database;

public class Config {

    public static final String DATABASE_NAME = "courses-db";

    //column names of student table
    public static final String TABLE_SESSION= "session";
    public static final String COLUMN_SESSION_ID = "_id";
    public static final String COLUMN_SESSION_TITLE = "title";
    public static final String COLUMN_SESSION_USERS = "users";
    public static final String COLUMN_SESSION_ERRORS = "warnings";
    
    //column names for the users
    public static final String USERS_TABLE="Users";
    public static final String COLUMN_USER_NAMES="user_name";
    public static final String COLUMN_USERS_ID="user_id";
}
