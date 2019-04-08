package com.example.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

public class AdminLogin extends AppCompatActivity {


    protected EditText adminName;
    protected EditText adminPassword;
    protected Switch switch1 ;

    SharedPreferencesHelper sharedPreferencesHelper;
    Administrator admin=new Administrator("Admin","admin");
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);
        adminName=findViewById(R.id.adminName);
        adminPassword=findViewById(R.id.adminPassword);

        sharedPreferencesHelper=new SharedPreferencesHelper(this);

        switch1 = findViewById(R.id.switch1);

        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked)
                {
                    Intent intent=new Intent(getBaseContext(),UserLogin.class);
                    startActivity(intent);
                }
            }
        });
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
