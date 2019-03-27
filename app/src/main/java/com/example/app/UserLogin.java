package com.example.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class UserLogin extends AppCompatActivity
{
    protected EditText user_login_text;
    protected EditText user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);
        user_login_text=findViewById(R.id.user_login_text);
        user_id=findViewById(R.id.user_id);
    }
    public void goUserDisplay(View view)
    {
        Intent intent=new Intent(this,DataDisplay.class);
        String username=user_login_text.getText().toString();
        String id=user_id.getText().toString();
        intent.putExtra("username",username);
        intent.putExtra("id",id);
        intent.putExtra("admin", false);
        startActivity(intent);
    }
}
