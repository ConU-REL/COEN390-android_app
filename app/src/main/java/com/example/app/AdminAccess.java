package com.example.app;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


public class AdminAccess extends AppCompatActivity {
    protected Switch switch_button;
    String intent_admin;
    ConstraintLayout layout;
    CardView session_history,new_session,manage_users,settings,logout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_access);
        Intent intent = getIntent();
        TextView admin_name = findViewById(R.id.label_username);
        intent_admin = intent.getStringExtra("username");
        admin_name.setText(intent_admin);
        switch_button=findViewById(R.id.switch_button);
        layout=findViewById(R.id.layout);
        session_history=findViewById(R.id.session_historyview);
        new_session=findViewById(R.id.new_sessionview);
        manage_users=findViewById(R.id.manage_usersview);
        logout=findViewById(R.id.logoutview);

        switch_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked==true)
                {
                    Toast.makeText(getBaseContext(),"Dark Theme",Toast.LENGTH_SHORT).show();
                    layout.setBackgroundResource(R.drawable.menu_header);
                    session_history.setCardBackgroundColor(getResources().getColor(R.color.colorGray));
                    new_session.setCardBackgroundColor(getResources().getColor(R.color.colorGray));
                    manage_users.setCardBackgroundColor(getResources().getColor(R.color.colorGray));
                    logout.setCardBackgroundColor(getResources().getColor(R.color.colorGray));

                }
                else{
                    layout.setBackgroundResource(R.drawable.dashboard_header);
                    session_history.setCardBackgroundColor(getResources().getColor(R.color.colorLightBlue));
                    new_session.setCardBackgroundColor(getResources().getColor(R.color.colorLightGreen));
                    manage_users.setCardBackgroundColor(getResources().getColor(R.color.colorLavender));
                    logout.setCardBackgroundColor(getResources().getColor(R.color.colorLightRed));
                }
            }
        });
    }


    public void goStartPage(View view)

    {
        Intent intent=new Intent(this,MainActivity.class);
        startActivity(intent);
    }


    public void goSavedSession(View view)
    {
        Intent intent=new Intent(this,SavedSession.class);
        intent.putExtra("username",intent_admin);
        startActivity(intent);
    }


    public void goNewSession(View view)
    {
        Intent intent=new Intent(this, DataDisplay.class);
        intent.putExtra("username",intent_admin);
        intent.putExtra("admin", true);
        startActivity(intent);
    }
    public void toManageUsers(View view)
    {
        Intent intent=new Intent(this, AdminUsersDisplay.class);
        startActivity(intent);
    }
}
