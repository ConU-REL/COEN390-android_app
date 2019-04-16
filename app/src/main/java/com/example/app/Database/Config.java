package com.example.app.Database;

/*
 * Database config class, stores our configuration for the SQL db
 */

class Config {

    static final String DATABASE_NAME = "courses-db";

    //column names of student table
    static final String TABLE_SESSION = "session";
    static final String COLUMN_SESSION_ID = "_id";
    static final String COLUMN_SESSION_TITLE = "title";
    static final String COLUMN_SESSION_USERS = "users";
    static final String COLUMN_SESSION_ERRORS = "warnings";


}