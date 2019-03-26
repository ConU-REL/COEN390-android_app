package com.example.app;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class AdminDataDisplay extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    protected ImageView closeReconnect;
    protected Dialog reconnect;
    protected Button reconnect_button;
    String connected="1";
    String disconnected="0";

    String topicStr="sensors/critical";
    MqttAndroidClient client;
    private ProgressBar connection_progressBar;
    String MQTTHOST="tcp://10.0.22.10:1883";

    ArrayList<TextView> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_data_display);

        reconnect=new Dialog(this);

        connection_progressBar=findViewById(R.id.connection_progressBar);

        //The following lines are for the navigation menu
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(this,drawer,R.string.open,R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        data.add((TextView) findViewById(R.id.data_rpm));
        data.add((TextView) findViewById(R.id.data_temp_c));
        data.add((TextView) findViewById(R.id.data_temp_o));
        data.add((TextView) findViewById(R.id.data_press_f));

        data.get(0).setText("0");

        m_connect();
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer =findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        int id = item.getItemId();

        if(id==R.id.db)
        {

        startActivity(new Intent(this, AdminAccess.class));
        }
        else if (id==R.id.Data)
        {

        startActivity(new Intent(this, DataDisplaySettings.class));
        }
        else if (id==R.id.Contact_Driver)
        {

        startActivity(new Intent(this, AdminAccess.class));
        }
         else if (id==R.id.Manage_Users)
        {

        startActivity(new Intent(this, AdminUsersDisplay.class));
        }
        else if (id==R.id.Logout) {

        startActivity(new Intent(this, MainActivity.class));
        }

        DrawerLayout drawer =findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

  //Connect to the server
    private void m_connect()
    {
        connection_progressBar.setVisibility(View.VISIBLE);

        String clientId = MqttClient.generateClientId();
         client = new MqttAndroidClient(this.getApplicationContext(),"tcp://10.0.22.10:1883",
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
                    connection_progressBar.setVisibility(View.GONE);
                    Toast.makeText(AdminDataDisplay.this,"Connected",Toast.LENGTH_SHORT).show();


                    m_subscribe();
                    //ecu_connection_subscribe();

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d("In MQTT_Connection", "onFailure");
                    connection_progressBar.setVisibility(View.GONE);
                    Show_reconnect_popup();

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void ecu_connection_subscribe()
    {
        String topic ="status/module";
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
                String status=new String (message.getPayload());
                String connection_value=status.substring(12,13);

                if(connection_value.equals(disconnected))
                {
                    Toast.makeText(AdminDataDisplay.this,"Lost Connection to ECU !!!",Toast.LENGTH_SHORT).show();

               }

                else if(connection_value.equals(connected))
                {
                    Toast.makeText(AdminDataDisplay.this,"Connected to ECU !!!",Toast.LENGTH_SHORT).show();

                }
                else if (!connection_value.equals(connected) && !connection_value.equals(disconnected))
                {
                    Toast.makeText(AdminDataDisplay.this, "No Connection to ECU !!!", Toast.LENGTH_SHORT).show();

                }

                //vibrator.vibrate(500);
                //ringtone.play();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

    }

    //opens a popup page where the user can connect to the server again
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

    //subscribe to a topic in server
    public void m_subscribe()
    {
        String topic = "sensors/critical";
        int qos = 0;
        try {
            IMqttToken subToken = client.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener()
            {
                @Override
                public void onSuccess(IMqttToken asyncActionToken)

                {
                    Toast.makeText(AdminDataDisplay.this,"Subscribed to "+topicStr,Toast.LENGTH_SHORT).show();

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
            public void messageArrived(String topic, MqttMessage message) throws JSONException {
                JSONObject msg = new JSONObject(new String (message.getPayload()));

                data.get(0).setText(msg.getString("rpm"));
                data.get(1).setText(msg.getString("oil_temp"));
                data.get(2).setText(msg.getString("coolant_temp"));
                data.get(3).setText(msg.getString("fuel_pressure"));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }
}
