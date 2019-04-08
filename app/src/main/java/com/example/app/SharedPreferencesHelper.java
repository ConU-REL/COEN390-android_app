package com.example.app;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

public class SharedPreferencesHelper
{
    private SharedPreferences sharedPreferences;
    public SharedPreferencesHelper(Context context)
    {
        sharedPreferences=context.getSharedPreferences("Team project 390", Context.MODE_PRIVATE);

    }


    public void saveUserName(String name)
    {
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("User Name",name);
        editor.commit();
    }

    public void saveSessionName(String name)
    {
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("session_name",name);
        editor.commit();
    }
    public void saveSessionError(String error)
    {
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("session_error",error);
        editor.commit();
    }
    public void saveSessionUsers(String users)
    {
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("session_users",users);
        editor.commit();
    }
    public void saveAccess(String user)
    {
        SharedPreferences.Editor editor=sharedPreferences.edit();

        editor.putString("added_user",user);
        editor.commit();
    }
    public String getSessionName()
    {
        return sharedPreferences.getString("session_name",null );
    }
    public String getSessionUsers()
    {
        return sharedPreferences.getString("session_users",null );
    }
    public String getSessionErrors()
    {
        return sharedPreferences.getString("session_error",null );
    }

    public String getAccess()
    {
        return sharedPreferences.getString("added_user",null );
    }

    public String getUserName()
    {
        return sharedPreferences.getString("User Name",null );
    }

    public String getDataDisplay()
    {
        return sharedPreferences.getString("Data Inputs",null);
    }


}
