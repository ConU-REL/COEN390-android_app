package com.example.app;

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



    public String getUserName()
    {
        return sharedPreferences.getString("User Name",null );
    }

    public String getDataDisplay()
    {
        return sharedPreferences.getString("Data Inputs",null);
    }


}
