package com.example.d_gille.manageusers;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class UserInfosActivity extends AppCompatActivity
{
    private static final String TAG="ManageUsersActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_infos);

        //goToManageUsersActivity();
        //customDialog("Dialog Box","this is the dialog box","cancelMethod","okMethod");
        AlertDialog.Builder userBuilder=new AlertDialog.Builder(UserInfosActivity.this);
        View view=getLayoutInflater().inflate(R.layout.dialog_manage,null);
        EditText message=(EditText) view.findViewById(R.id.messageEditText);
        Button deleteUserButton=(Button) view.findViewById(R.id.deleteUserButton);
        Button changeRoleButton=(Button) view.findViewById(R.id.changeUserRoleButton);
        int id;
        TextView userInfoTextView=(TextView) view.findViewById(R.id.userInfoTextView);

        deleteUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                toastMessage("User deleted!");
                //deleteUser(id);

            }
        });

        changeRoleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                toastMessage("Change of role successful!");
                //changeUserRole(id);

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



            }
        });

        userBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Log.d(TAG,"onClick:Cancel Called:");
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


}
