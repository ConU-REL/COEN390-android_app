package com.example.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AdminLogin extends AppCompatActivity {


    protected EditText adminName;
    protected EditText adminPassword;
    SharedPreferencesHelper sharedPreferencesHelper;
    Administrator admin=new Administrator("Admin","admin");
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);
        adminName=findViewById(R.id.username);
        adminPassword=findViewById(R.id.password);

        sharedPreferencesHelper=new SharedPreferencesHelper(this);
    }
    public void goAdminSettings(View view)
    {
        String nameInput = adminName.getText().toString();
        String passwordInput = adminPassword.getText().toString();
        String adminName=admin.getAdministratorName();
        String adminPassword=admin.getPassword();

        if(adminName.equals(nameInput))
        {
            System.out.println("Admin name = " + admin.getAdministratorName());
            if (adminPassword.equals(passwordInput))
            {
                Intent intent=new Intent(this,AdminAccess.class);
                intent.putExtra("username",admin.getAdministratorName());
                startActivity(intent);
            }
            else
            {
                Toast.makeText(AdminLogin.this, "Wrong password!", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(AdminLogin.this, "Invalid user!", Toast.LENGTH_SHORT).show();
        }

    }

}
