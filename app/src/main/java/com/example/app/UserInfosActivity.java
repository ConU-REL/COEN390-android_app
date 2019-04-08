package com.example.d_gille.manageusers;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;

public class UserInfosActivity extends AppCompatActivity
{
    private static final String TAG="ManageUsersActivity";

    DatabaseHelper dbHelper=new DatabaseHelper(this);
    int userID;
    String name;
    String role;
    long session_id;
    SharedPreferencesHelper sharedPreferencesHelper;
    DatabaseHelper databaseHelper;
    User user;
    List<User> userList;
    int position;
    boolean isDeleted;
    boolean isUpdated;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_infos);
        final Bundle bundle=getIntent().getExtras();
        userID=bundle.getInt("usersID");
        role=bundle.getString("userRole");
        session_id=bundle.getLong("session_id");
        name=bundle.getString("userName");
        user=new User(name,role,userID,session_id);
        sharedPreferencesHelper=new SharedPreferencesHelper(this);
        databaseHelper=new DatabaseHelper(this);


        AlertDialog.Builder userBuilder=new AlertDialog.Builder(UserInfosActivity.this);
        View view=getLayoutInflater().inflate(R.layout.dialog_manage,null);
        EditText message=(EditText) view.findViewById(R.id.messageEditText);
        Button deleteUserButton=(Button) view.findViewById(R.id.deleteUserButton);
        Button changeRoleButton=(Button) view.findViewById(R.id.changeUserRoleButton);

        TextView userInfoTextView=(TextView) view.findViewById(R.id.userInfoTextView);

        deleteUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                toastMessage("User deleted!");
                deleteUser(userID);

            }
        });

        changeRoleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
              updateUserRole(position);

            }
        });

        //userBuilder.setIcon(R.mipmap.ic_launcher_round);
        userBuilder.setTitle("User Info:");
        userBuilder.setMessage("This is the message:");



        userBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Log.d(TAG,"onClick:Cancel Called:");
                cancelMethod();
                isUpdated==databaseHelper.updateRole(user);
                isUpdated==false;
                isDeleted==deleteUserbyID(userID);
                isDeleted==false;
                Intent intent=new Intent(UserInfosActivity.this,ManageUsersActivity.class);
                startActivity(intent);




            }
        });

        userBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Log.d(TAG,"onClick:Ok Called:");
                okMethod();

            }
        });



        userBuilder.setView(view);
        AlertDialog dialog=userBuilder.create();
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
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    public void updateUserRole(int position)
    {
        userList=dbHelper.getAllUsers();
        Intent intent=new Intent(UserInfosActivity.this,ChangeRoleActivity.class);
        int userID = userList.get(position).getUserID();
        String userRole = userList.get(position).getUserRole();
        String userName =  userList.get(position).getUserName();
        long session_id=userList.get(position).getSessionID();

        intent.putExtra("usersID", userID);
        intent.putExtra("userName", userName);
        intent.putExtra("userRole", userRole);
        intent.putExtra("session_id",session_id);
        startActivity(intent);

    }

    public void deleteUser(int userID)
    {
        dbHelper.deleteUserbyID(userID);
        Intent intent=new Intent(UserInfosActivity.this,ManageUsersActivity.class);
        startActivity(intent);
    }





}
