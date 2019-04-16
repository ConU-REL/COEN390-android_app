package com.example.app;

import android.content.Context;
import android.content.SharedPreferences;

/*
 * Shared Preferences Helper class, used throughout project as helper
 */

public class SharedPreferencesHelper {
    private SharedPreferences sharedPreferences;

    // init
    SharedPreferencesHelper(Context context) {
        sharedPreferences = context.getSharedPreferences("Team project 390", Context.MODE_PRIVATE);

    }

    // save the session name into the shared preferences
    void saveSessionName(String name) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("session_name", name);
        editor.apply();
    }


    // save the session errors into the shared preferences
    void saveSessionError(String error) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("session_error", error);
        editor.apply();
    }

    // save the session users into the shared preferences
    void saveSessionUsers(String users) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("session_users", users);
        editor.apply();
    }

    // get the session name
    String getSessionName() {
        return sharedPreferences.getString("session_name", null);
    }

    // get the session users

    String getSessionUsers() {
        return sharedPreferences.getString("session_users", null);
    }

    // get the session errors

    String getSessionErrors() {
        return sharedPreferences.getString("session_error", null);
    }
}
