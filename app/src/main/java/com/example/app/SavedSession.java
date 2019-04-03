package com.example.app;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.app.Database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class SavedSession extends AppCompatActivity {
    protected FloatingActionButton insertSessionButton;
    protected ListView SessionsListView;
    private static final String TAG = "SavedSession";
    List<Sessions> sessions = null;
    Sessions session;
    SharedPreferencesHelper sharedPreferencesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_session);

        SessionsListView = findViewById(R.id.SessionsListView);
        //insertSessionButton = findViewById(R.id.InsertSessionButton);
        sharedPreferencesHelper=new SharedPreferencesHelper(this);

        DatabaseHelper db= new DatabaseHelper(this);
        Intent intent=getIntent();
        String title=sharedPreferencesHelper.getSessionName();
        String users=sharedPreferencesHelper.getSessionUsers();
        String errors=sharedPreferencesHelper.getSessionErrors();
        db.insertSession(new Sessions(-1, title, users,errors));
        loadListView();
        /*insertSessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "floating button onclick");
                InsertSessionsDialog dialog = new InsertSessionsDialog();
                dialog.show(getSupportFragmentManager(), "Insert Session");
            }
        });*/
    }
    protected void loadListView()
    {
        DatabaseHelper dbhelper = new DatabaseHelper(this);
        sessions = dbhelper.getAllSessions();
        ArrayList<String> listSessions = new ArrayList<>();

        for(int i=0; i< sessions.size(); i++){
            String temp = "";
            listSessions.add(sessions.get(i).getSession_name() + "\n"+ sessions.get(i).getUsers()+ "\n"+ sessions.get(i).getWarnings());
        }
        ArrayList<String>
                newList = removeDuplicates(listSessions);
        ArrayAdapter adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,newList);
        SessionsListView.setAdapter(adapter);

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
