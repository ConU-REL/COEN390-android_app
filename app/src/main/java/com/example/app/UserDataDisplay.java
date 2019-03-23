package com.example.app;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UserDataDisplay extends AppCompatActivity {
   protected TextView user_name;
   String username;
   String id;
   protected ListView user_data_display;
    protected ImageView closeReconnect;
    protected Dialog reconnect;
    protected Button reconnect_button;

    protected Dialog access;
    protected Button access_button;

    private TextView subu_topic;
    String topicStr="DataDisplay";
    double user_id=Math.random();
    MqttAndroidClient client;
    private ProgressBar user_progressBar;
    String MQTTHOST="tcp://broker.hivemq.com:1883";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_data_display);

        reconnect=new Dialog(this);
        access=new Dialog(this);

        user_data_display=findViewById(R.id.user_data_display);
        subu_topic=findViewById(R.id.subu_topic);
        user_progressBar=findViewById(R.id.user_progressBar);

        user_name=findViewById(R.id.user_name);
        id=getIntent().getExtras().getString("ID");
        username=getIntent().getExtras().getString("Value");
        user_name.setText(username);
        Show_access_popup();
        m_connect();
        loadListView();
    }

    private void Show_access_popup()
    {
        access.setContentView(R.layout.access_permision);
        access_button=access.findViewById(R.id.access_button);
        access_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                m_publish();
                access.dismiss();
            }
        });
        access.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        access.show();

    }

    private void m_connect()
    {
        user_progressBar.setVisibility(View.VISIBLE);

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
                    user_progressBar.setVisibility(View.GONE);
                    Toast.makeText(UserDataDisplay.this,"Connected",Toast.LENGTH_SHORT).show();
                    m_subscribe();

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d("In MQTT_Connection", "onFailure");
                    user_progressBar.setVisibility(View.GONE);
                    Show_reconnect_popup();


                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    private void Show_reconnect_popup()
    {
        reconnect.setContentView(R.layout.popup_failed_connection);
        closeReconnect= reconnect.findViewById(R.id.closeReconnect);
        reconnect_button=reconnect.findViewById(R.id.reconnect_button);
        closeReconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reconnect.dismiss();
            }
        });
        reconnect_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_connect();
                reconnect.dismiss();
            }
        });
        reconnect.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        reconnect.show();


    }
    public void m_subscribe()
    {
        String topic =topicStr;
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
        } catch (MqttException e) {
            e.printStackTrace();
        }
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message)
            {
                subu_topic.setText(new String(message.getPayload()));
                //vibrator.vibrate(500);
                //ringtone.play();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }
    public void m_publish()
    {

        String topic ="ConnectedUsers/"+id+"/online";
        int qos=0;
        //byte[] encodedPayload = new byte[0];
        try {
            //encodedPayload = message.getBytes("UTF-8");
            //MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(topic, username.getBytes(),qos,false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    public void goUserStart(View view)
    {
        Intent intent=new Intent(this,MainActivity.class);
        startActivity(intent);
    }
    public void loadListView()
    {
        List<DataInputs> dataInputsList=new ArrayList<>();
        dataInputsList.add(new DataInputs(8,8,8,8,8,8));//used as a test for the demo. Not final version

        ArrayList<String> dataInputsListText = new ArrayList<>();

        for (int i = 0; i < dataInputsList.size(); i++) {
            String temp = "";
            temp += "Oil Temperature: "+dataInputsList.get(i).getOilTemperature() + "\n";
            temp += "Oil Pressure: "+dataInputsList.get(i).getOilPressure() + "\n";
            temp += "Fuel Pressure: "+dataInputsList.get(i).getFuelPressure() + "\n";
            temp += "Coolant Temperature: "+dataInputsList.get(i).getCoolantTemperature() + "\n";
            temp += "Engine RPM: "+dataInputsList.get(i).getEngineRPM() + "\n";
            temp += "Gear: "+dataInputsList.get(i).getGear();

            dataInputsListText.add(temp);

        }

        ArrayAdapter arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataInputsListText);
        user_data_display.setAdapter(arrayAdapter);



    }

}
