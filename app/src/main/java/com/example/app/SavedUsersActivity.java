package com.example.d_gille.myapplication;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.example.d_gille.myapplication.Database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class SavedUsersActivity extends AppCompatActivity
{

    protected ListView usersListView;
    protected FloatingActionButton addUserFloatingButton;
    SharedPreferencesHelper sharedPreferencesHelper;

    long sessionID=-1;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_users);

        //DatabaseHelper dbHelper=new DatabaseHelper(this);
        //dbHelper.insertUsers(new User("Dave",1));
        sharedPreferencesHelper=new SharedPreferencesHelper(this);
        DatabaseHelper db=new DatabaseHelper(this);

        usersListView=findViewById(R.id.usersListView);
        addUserFloatingButton=findViewById(R.id.addUserFloatingActionButton);

        String name=sharedPreferencesHelper.getUserName();
        final Bundle bundle=getIntent().getExtras();
        //Intent intent=getIntent();
        userID=sharedPreferencesHelper.getUserID();

        if (userID != null) {
            db.insertUsers(new User(name,Integer.parseInt(userID),sessionID));
        }


        loadListView(sessionID);



        addUserFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InsertUserDialogFragment dialog=new InsertUserDialogFragment();
                dialog.show(getSupportFragmentManager(),"InsertUserFragment");

            }
        });
    }

    protected void loadListView(long sessionID)
    {
        DatabaseHelper dbHelper=new DatabaseHelper(this);
        List<User> users=dbHelper.getAllUsers();
        ArrayList<String> usersListText =new ArrayList<>();

        for (int i=0;i<users.size();i++)
        {
            String temp= " ";
            temp+=users.get(i).getUserName() + "\n";
            temp+=users.get(i).getUserID();

            usersListText.add(temp);
        }

        ArrayAdapter arrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,usersListText);

        usersListView.setAdapter(arrayAdapter);


    }


    public static <T> ArrayList<T> removeDuplicates(ArrayList<T> list)
    {

        // Create a new ArrayList
        ArrayList<T> newList = new ArrayList<T>();

        // Traverse through the first list
        for (T element : list) {

            // If this element is not present in newList
            // then add it
            if (!newList.contains(element)) {

                newList.add(element);
            }
        }

        // return the new list
        return newList;
    }
}
