package com.example.d_gille.manageusers;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.d_gille.manageusers.Database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class ManageUsersActivity extends AppCompatActivity
{
    protected ListView usersListView;
    protected FloatingActionButton addUserFloatingButton;

    List<User> usersList;

    private static final String TAG = "ManageUsersActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        DatabaseHelper dbHelper=new DatabaseHelper(this);

        usersListView = findViewById(R.id.usersListView);
        addUserFloatingButton=findViewById(R.id.addUserFloatingActionButton);


        loadListView();



        usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                goToUserInfosActivity(position);
            }
        });

        addUserFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InsertUserDialogFragment dialog=new InsertUserDialogFragment();
                dialog.show(getSupportFragmentManager(),"InsertUserFragment");
            }
        });


    }

    protected void loadListView() {

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        usersList = dbHelper.getAllUsers();
        ArrayList<String> usersListText = new ArrayList<>();

        for (int i = 0; i < usersList.size(); i++)
        {
            String temp = "";
            temp += "Username:"+ usersList.get(i).getUserName() + "\n";
            temp += "Role:" +usersList.get(i).getUserRole();

            usersListText.add(temp);

        }

        ArrayAdapter arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, usersListText);
        usersListView.setAdapter(arrayAdapter);

    }

    //Here, we will put a toast
    public void goToUserInfosActivity(int position)
    {
        Intent intent = new Intent(ManageUsersActivity.this, UserInfosActivity.class);
        int userID = usersList.get(position).getUserID();
        String userRole = usersList.get(position).getUserRole();
        String userName =  usersList.get(position).getUserName();
        long session_id=usersList.get(position).getSessionID();



        intent.putExtra("usersID", userID);
        intent.putExtra("userName", userName);
        intent.putExtra("userRole", userRole);
        intent.putExtra("session_id",session_id);
        startActivity(intent);

    }

}
