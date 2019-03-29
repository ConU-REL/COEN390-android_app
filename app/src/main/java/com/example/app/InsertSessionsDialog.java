package com.example.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.app.Database.DatabaseHelper;

public class InsertSessionsDialog extends DialogFragment {
    private static final String TAG = "InsertCourseDialog";


    EditText titleEditText;
    EditText userEditText;
    EditText errorsEditText;

    Button cancelButton;
    Button saveButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflator, @Nullable ViewGroup container, Bundle savedInstanceState){
        View view = inflator.inflate(R.layout.fragment_insert_session, container, false);

        titleEditText = view.findViewById(R.id.titleEditText);
        userEditText = view.findViewById(R.id.userEditText);
        errorsEditText = view.findViewById(R.id.errorsEditText);

        saveButton = view.findViewById(R.id.saveButton);
        cancelButton = view.findViewById(R.id.cancelButton);

        cancelButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onclick: cancel button");
                getDialog().dismiss();
            }
        });
        saveButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = titleEditText.getText().toString();
                String users = userEditText.getText().toString();
                String errors = errorsEditText.getText().toString();


                if(!(title.equals("") || users.equals("") )) {
                    DatabaseHelper dbhelper = new DatabaseHelper(getActivity());
                    dbhelper.insertSession(new Sessions(-1, title, users,errors));
                    ((SavedSession)getActivity()).loadListView();
                    getDialog().dismiss();
                }

            }
        });
        return view;
    }
}
