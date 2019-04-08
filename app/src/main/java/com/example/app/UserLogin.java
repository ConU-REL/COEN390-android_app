package com.example.app;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class UserLogin extends AppCompatActivity
{
    protected EditText user_login_text;
    protected EditText user_id;
    String username;
protected Switch switch2;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);
        user_login_text=findViewById(R.id.user_login_text);
        user_id=findViewById(R.id.user_id);


        switch2 = findViewById(R.id.switch2);

        switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked)
                {
                    Intent intent=new Intent(getBaseContext(),AdminLogin.class);
                    startActivity(intent);
                }
            }
        });

    }
    public void goUserDisplay(View view)
    {
        Intent intent=new Intent(this,DataDisplay.class);
        username=user_login_text.getText().toString();
        String id=user_id.getText().toString();
        intent.putExtra("username",username);
        intent.putExtra("id",id);
        intent.putExtra("admin", false);
        startActivity(intent);
    }

}
