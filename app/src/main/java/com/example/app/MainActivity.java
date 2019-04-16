package com.example.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

/*
 * This is the MainActivity, the btn_login screen
 */

public class MainActivity extends AppCompatActivity {
    protected EditText field_username;
    protected EditText field_password;
    protected Button btn_login;
    protected CheckBox checkbox_admin;
    SharedPreferencesHelper sharedPreferencesHelper;
    // create the administrator btn_login, would be improved in further sprints
    Administrator admin = new Administrator("Admin", "admin");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferencesHelper = new SharedPreferencesHelper(this);

        // locate fields
        field_username = findViewById(R.id.username);
        field_password = findViewById(R.id.password);
        btn_login = findViewById(R.id.button_login);
        checkbox_admin = findViewById(R.id.check_admin);

        // listen for checkbox clicks, show/hide the password field when necessary
        checkbox_admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkbox_admin.isChecked()) {
                    field_password.setVisibility(View.VISIBLE);
                } else {
                    field_password.setVisibility(View.INVISIBLE);
                }
            }
        });

        // listen for clicks on the login button, validate login when clicked
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkbox_admin.isChecked()) {
                    // validate login
                    goAdmin();
                } else {
                    // login as user
                    Intent intent = new Intent(MainActivity.this, DataDisplay.class);
                    intent.putExtra("field_username", field_username.getText().toString());
                    intent.putExtra("admin", false);
                    startActivity(intent);
                }
            }
        });
    }


    // validate credentials and goto admin dashboard if logging in as admin
    public void goAdmin() {
        int validation = admin.validateCreds(field_username, field_password);
        switch (validation) {
            case 0:
                Intent intent = new Intent(this, AdminAccess.class);
                intent.putExtra("field_username", admin.getUsername());
                intent.putExtra("admin", true);
                startActivity(intent);
                break;
            case 1:
                Toast.makeText(MainActivity.this, "Invalid password.", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                Toast.makeText(MainActivity.this, "Invalid user.", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }
}
