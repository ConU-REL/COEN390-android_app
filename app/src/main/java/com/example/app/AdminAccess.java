package com.example.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


public class AdminAccess extends AppCompatActivity {

    String intent_admin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_access);
        Intent intent = getIntent();
        TextView admin_name = findViewById(R.id.label_username);
        intent_admin = intent.getStringExtra("username");
        admin_name.setText(intent_admin);
    }


    public void goStartPage(View view)

    {
        Intent intent=new Intent(this,MainActivity.class);
        startActivity(intent);
    }


    public void goSavedSession(View view)
    {
        Intent intent=new Intent(this,SavedSession.class);
        intent.putExtra("username",intent_admin);
        startActivity(intent);
    }


    public void goNewSession(View view)
    {
        Intent intent=new Intent(this, DataDisplay.class);
        intent.putExtra("username",intent_admin);
        intent.putExtra("admin", true);
        startActivity(intent);
    }
    public void toManageUsers(View view)
    {
        Intent intent=new Intent(this, AdminUsersDisplay.class);
        startActivity(intent);
    }
}
