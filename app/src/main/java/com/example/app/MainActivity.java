package com.example.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    protected EditText username;
    protected EditText password;
    protected Button login;
    protected CheckBox check_admin;

    SharedPreferencesHelper sharedPreferencesHelper;
    Administrator admin=new Administrator("Admin","admin");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        username=findViewById(R.id.username);
        password=findViewById(R.id.password);
        sharedPreferencesHelper=new SharedPreferencesHelper(this);

        login = findViewById(R.id.button_login);
        check_admin = findViewById(R.id.check_admin);

        check_admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(check_admin.isChecked()){
                    password.setVisibility(View.VISIBLE);
                } else {
                    password.setVisibility(View.INVISIBLE);
                }
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(check_admin.isChecked()){
                    goAdmin();
                } else {
                    Intent intent=new Intent(MainActivity.this,DataDisplay.class);
                    intent.putExtra("username",username.getText().toString());
                    intent.putExtra("admin", false);
                    startActivity(intent);
                }
            }
        });
    }


    public void goAdmin()
    {
        String nameInput = username.getText().toString();
        String passwordInput = password.getText().toString();
        String adminName=admin.getAdministratorName();
        String adminPassword=admin.getPassword();

        if(adminName.equals(nameInput))
        {
            if (adminPassword.equals(passwordInput))
            {
                Intent intent=new Intent(this,AdminAccess.class);
                intent.putExtra("username",admin.getAdministratorName());
                intent.putExtra("admin", true);
                startActivity(intent);
            }
            else
            {
                Toast.makeText(MainActivity.this, "Wrong password!", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(MainActivity.this, "Invalid user!", Toast.LENGTH_SHORT).show();
        }
    }
}
