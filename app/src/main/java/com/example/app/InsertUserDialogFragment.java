package com.example.d_gille.manageusers;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.d_gille.manageusers.Database.DatabaseHelper;

public class InsertUserDialogFragment extends DialogFragment
{
    protected EditText userNameEditText;
    protected EditText userRoleEditText;
    protected Button saveUserButton;
    protected Button cancelUserButton;
    SharedPreferencesHelper sharedPreferencesHelper;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View view=inflater.inflate(R.layout.fragment_insert_user,container,false);
        userNameEditText= view.findViewById(R.id.userNameEditText);
        userRoleEditText=view.findViewById(R.id.userRoleEditText);
        saveUserButton=view.findViewById(R.id.saveUserButton);
        cancelUserButton=view.findViewById(R.id.cancelUserButton);
        Context thisContext=getActivity();
        sharedPreferencesHelper=new SharedPreferencesHelper(thisContext);



        saveUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name=userNameEditText.getText().toString();
                String role=userRoleEditText.getText().toString();

                DatabaseHelper dbHelper= new DatabaseHelper(getActivity());
                if(!(name.equals("") || role.equals("")))
                    dbHelper.insertUsers(new User(name,role,-1));
                ((ManageUsersActivity)getActivity()).loadListView();
                sharedPreferencesHelper.saveUserRole(role);
                sharedPreferencesHelper.saveUserName(name);
                getDialog().dismiss();

            }
        });

        cancelUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        return view;
    }


}

