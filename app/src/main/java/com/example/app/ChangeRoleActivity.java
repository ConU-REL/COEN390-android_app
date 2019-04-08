package com.example.d_gille.manageusers;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.d_gille.manageusers.Database.DatabaseHelper;

public class ChangeRoleActivity extends AppCompatActivity
{
    protected Button updateButton;
    protected EditText changeRoleEditText;
    DatabaseHelper dbHelper=new DatabaseHelper(this);
    int userID;
    String name;
    String role;
    long session_id;
    User user;
    SharedPreferencesHelper sharedPreferencesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_role);

        updateButton=(Button) findViewById(R.id.updateRoleButton);
        changeRoleEditText=(EditText) findViewById(R.id.changeRoleEditText);
        sharedPreferencesHelper=new SharedPreferencesHelper(this);
        final Bundle bundle=getIntent().getExtras();
        role=bundle.getString("userRole");
        userID=bundle.getInt("usersID");
        session_id=bundle.getLong("session_id");
        name=bundle.getString("userName");

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateRole();
            }
        });





    }

    public void updateRole()
    {
        role=changeRoleEditText.getText().toString();

        boolean isUpdated=dbHelper.updateRole(new User(name,role,session_id));


        if (isUpdated==true)
        {
            Toast.makeText(ChangeRoleActivity.this,"Data updated!",Toast.LENGTH_LONG).show();
            sharedPreferencesHelper.saveUserRole(role);
            Intent intent=new Intent(ChangeRoleActivity.this,ManageUsersActivity.class);
            startActivity(intent);
        }

        else
        {
            Toast.makeText(ChangeRoleActivity.this,"Data not updated!",Toast.LENGTH_LONG).show();
        }
    }
}
