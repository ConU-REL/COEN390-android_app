package com.example.d_gille.manageusers;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.d_gille.manageusers.Database.DatabaseHelper;

public class UserInfosActivity extends AppCompatActivity
{
    private static final String TAG="UserInfosActivity";


    int userID;
    String name;
    String role;
    long session_id;
    SharedPreferencesHelper sharedPreferencesHelper;
    DatabaseHelper databaseHelper;
    boolean isUpdated;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_infos);
        final Bundle bundle = getIntent().getExtras();
        userID = bundle.getInt("usersID");
        //role=bundle.getString("userRole");
        session_id = bundle.getLong("session_id");
        name = bundle.getString("userName");
        sharedPreferencesHelper = new SharedPreferencesHelper(this);
        databaseHelper = new DatabaseHelper(this);
        AlertDialog.Builder userBuilder = new AlertDialog.Builder(UserInfosActivity.this);
        View view = getLayoutInflater().inflate(R.layout.dialog_manage, null);
        Button deleteUserButton = (Button) view.findViewById(R.id.cancelUserButton);


        TextView userInfoTextView = (TextView) view.findViewById(R.id.userInfoTextView);

        deleteUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toastMessage("User deleted!");
                deleteUser(userID);

            }
        });


        //userBuilder.setIcon(R.mipmap.ic_launcher_round);
        userBuilder.setTitle("User Info:");
        userBuilder.setMessage("This is the message:");


        userBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Log.d(TAG, "onClick:Cancel Called:");
                cancelMethod();
            }
        });

        userBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "onClick:Ok Called:");
                okMethod();
                Intent intent = new Intent(UserInfosActivity.this, ManageUsersActivity.class);
                startActivity(intent);
            }
        });


        userBuilder.setView(view);
        AlertDialog dialog = userBuilder.create();
        dialog.show();


    }

    public void cancelMethod()
    {
        Log.d(TAG,"cancelMethod:called");
    }

    public void okMethod()
    {
        Log.d(TAG,"okMethod:called");
    }

    public void toastMessage(String message)
    {
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }

    public void deleteUser(int userID)
    {
        databaseHelper.deleteUserbyID(userID);
        Intent intent=new Intent(UserInfosActivity.this,ManageUsersActivity.class);
        startActivity(intent);
    }

}
