package com.example.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;


/*
 * This is the main "Dashboard" page for the admin
 */


public class AdminAccess extends AppCompatActivity {
    String username_admin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_access);
        // get the field_username from the intent, display it
        Intent intent = getIntent();
        TextView admin_name = findViewById(R.id.label_username);
        username_admin = intent.getStringExtra("field_username");
        admin_name.setText(username_admin);
    }

    // go back to the btn_login activity
    public void goLoginPage(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    // go to the saved sessions activity
    public void goSavedSession(View view) {
        Intent intent = new Intent(this, SavedSession.class);
        intent.putExtra("field_username", username_admin);
        startActivity(intent);
    }


    // go to the new session activity
    public void goNewSession(View view) {
        Intent intent = new Intent(this, DataDisplay.class);
        intent.putExtra("field_username", username_admin);
        intent.putExtra("admin", true);
        startActivity(intent);
    }

    // go to the user management activity
    public void goManageUsers(View view) {
        Intent intent = new Intent(this, AdminUsersDisplay.class);
        startActivity(intent);
    }
}
