package com.example.d_gille.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

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
        //Consider using `apply()` instead; `commit` writes its fields to persistent storage immediately, whereas `apply` will handle it in the background
        editor.commit();
    }

    public void saveUserRole(String role)
    {
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("User Role",role);
        //Consider using `apply()` instead; `commit` writes its fields to persistent storage immediately, whereas `apply` will handle it in the background
        editor.commit();
    }

   
    public void saveSessionName(String name)
    {
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("session_name",name);
        //Consider using `apply()` instead; `commit` writes its fields to persistent storage immediately, whereas `apply` will handle it in the background
        editor.commit();
    }
    public void saveSessionError(String error)
    {
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("session_error",error);
        //Consider using `apply()` instead; `commit` writes its fields to persistent storage immediately, whereas `apply` will handle it in the background
        editor.commit();
    }
    public void saveSessionUsers(String users)
    {
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("session_users",users);
        //Consider using `apply()` instead; `commit` writes its fields to persistent storage immediately, whereas `apply` will handle it in the background
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


    public String getUserName()
    {
        return sharedPreferences.getString("User Name",null );
    }

   
    public String getUserRole()
    {
        return sharedPreferences.getString("User Role",null );
    }

    public String getDataDisplay()
    {
        return sharedPreferences.getString("Data Inputs",null);
    }

    
}
