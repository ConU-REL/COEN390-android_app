package com.example.app;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
    String MQTTtestHOST="tcp://broker.hivemq.com:1883";
    String MQTTHOST="tcp://10.0.22.10:1883";

    MqttAndroidClient client;
    String username;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);
        user_login_text=findViewById(R.id.user_login_text);
        user_id=findViewById(R.id.user_id);
    }
    public void goUserDisplay(View view)
    {
        Intent intent=new Intent(this,DataDisplay.class);
        username=user_login_text.getText().toString();
        String id=user_id.getText().toString();
        m_connect();
        intent.putExtra("username",username);
        intent.putExtra("id",id);
        intent.putExtra("admin", false);
        startActivity(intent);
    }
    private void m_connect()
    {
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), MQTTHOST,
                clientId);

        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener()
            {
                @Override
                public void onSuccess(IMqttToken asyncActionToken)
                {
                    // We are connected
                    Log.d("In MQTT_Connection", "onSuccess");
                    m_publish_add();

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d("In MQTT_Connection", "onFailure");


                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    public void m_publish_add()
    {

        String topic ="adduser/"+username;
        int qos=1;
        try {
            client.publish(topic, username.getBytes(),qos,true);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}
