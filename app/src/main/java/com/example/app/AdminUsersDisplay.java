package com.example.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AdminUsersDisplay extends AppCompatActivity
{
    protected ListView connected_users_list;
    protected ListView disconnected_users_list;

    String MQTTHOST="tcp://10.0.22.10:1883";
    List<String> UserInputsList;
    protected Button refress_button;
    MqttAndroidClient client;

    private ArrayList<String> adddataInputsListText;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users_display);
        connected_users_list=findViewById(R.id.connected_users_list);
        refress_button=findViewById(R.id.refresh_button);
        m_connect();
        refress_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                finish();
                startActivity(getIntent());


            }
        });

    }
    private void m_connect()
    {

        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), "tcp://10.0.22.10:1883",
                clientId);
        UserInputsList=new ArrayList<>();

        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener()
            {
                @Override
                public void onSuccess(IMqttToken asyncActionToken)
                {
                    // We are connected
                    Log.d("In MQTT_Connection", "onSuccess");

                    //m_subscribe_delete();
                    m_subscribe_add();


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

    private void loadListView()
    {
       // if(isActive) { refresh(5000);}

        ArrayAdapter arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1,adddataInputsListText);
        connected_users_list.setAdapter(arrayAdapter);
        connected_users_list.deferNotifyDataSetChanged();

    }
  /* private void refresh(int milliseconds) {
        final Handler handler=new Handler();
        final Runnable runnable=new Runnable()
        {
            @Override
            public void run()
            {
                UserInputsList.clear();
                m_subscribe_add();
            }
        };

    }*/

    public void m_subscribe_add()
    {
        String topic ="adduser/+";
        int qos = 1;
        try {
            IMqttToken subToken = client.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener()
            {
                @Override
                public void onSuccess(IMqttToken asyncActionToken)
                {
                    // The message was published
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards

                }
            });
        } catch (MqttException e)
        {
            e.printStackTrace();
        }

        client.setCallback(new MqttCallback()
        {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message)
            {
                UserInputsList.add(new String (message.getPayload()));
                addloadListView();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });



    }

    public void addloadListView()
    {

       adddataInputsListText = new ArrayList<>();
       for (int i = 0; i < UserInputsList.size(); i++)
       {
           String temp = "";

               temp += UserInputsList.get(i);
              adddataInputsListText.add(temp);

       }
        loadListView();



    }

}
