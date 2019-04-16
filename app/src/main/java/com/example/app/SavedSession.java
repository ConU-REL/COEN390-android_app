package com.example.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.example.app.Database.DatabaseHelper;
import java.util.ArrayList;
import java.util.List;

/*
 *
 */

public class SavedSession extends AppCompatActivity {
    protected ListView SessionsListView;
    List<Sessions> sessions = null;
    SharedPreferencesHelper sharedPreferencesHelper;

    // remote duplicate entries
    public static <T> ArrayList<T> removeDuplicates(ArrayList<T> list) {
        // Create a new ArrayList
        ArrayList<T> newList = new ArrayList<>();

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_session);

        SessionsListView = findViewById(R.id.SessionsListView);
        sharedPreferencesHelper = new SharedPreferencesHelper(this);

        // initialize database connection
        DatabaseHelper db = new DatabaseHelper(this);
        String title = sharedPreferencesHelper.getSessionName();
        String users = sharedPreferencesHelper.getSessionUsers();
        String errors = sharedPreferencesHelper.getSessionErrors();
        // add session to db
        db.insertSession(new Sessions(-1, title, users, errors));
        loadListView();
    }


    // function to load the list view
    protected void loadListView() {
        DatabaseHelper dbhelper = new DatabaseHelper(this);
        sessions = dbhelper.getAllSessions();
        ArrayList<String> listSessions = new ArrayList<>();

        for (int i = 0; i < sessions.size(); i++) {
            listSessions.add(sessions.get(i).getSession_name() + "\n" + sessions.get(i).getUsers() + "\n" + sessions.get(i).getWarnings());
        }
        ArrayList<String>
                newList = removeDuplicates(listSessions);
        ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, newList);
        SessionsListView.setAdapter(adapter);
    }
}
