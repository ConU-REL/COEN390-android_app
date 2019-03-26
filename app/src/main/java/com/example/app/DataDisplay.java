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

    MqttAndroidClient client;
    private ProgressBar connection_progressBar;
    String MQTTHOST="tcp://10.0.22.10:1883";
    Thread mqttThread;
    IMqttToken connection;

    ArrayList<Integer> data = new ArrayList<>();

    // UI elements
    ArrayList<TextView> fields = new ArrayList<>();
    TextView ecu_status;
    TextView server_status;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_display);

        reconnect=new Dialog(this);

        connection_progressBar=findViewById(R.id.connection_progressBar);

        //The following lines are for the navigation menu
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(this,drawer,R.string.open,R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ecu_status = findViewById(R.id.ecu_stat);
        ecu_status.setText("Disconnected");
        ecu_status.setTextColor(Color.RED);

        server_status = findViewById(R.id.server_stat);
        server_status.setText("Disconnected");
        server_status.setTextColor(Color.RED);


        fields.add((TextView) findViewById(R.id.data_rpm));
        fields.add((TextView) findViewById(R.id.data_temp_c));
        fields.add((TextView) findViewById(R.id.data_temp_o));
        fields.add((TextView) findViewById(R.id.data_press_f));

        for(int i=0; i<fields.size(); i++){
            fields.get(i).setText("0");
        }



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

        mqttThread = new Thread(new Runnable() {

            @Override
            public void run() {
                if (client != null && !client.isConnected()) {
                    try {
                        connection = client.connect();
                        connection.setActionCallback(new IMqttActionListener()
                        {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken)
                            {
                                // We are connected
                                Log.d("In MQTT_Connection", "onSuccess");
                                connection_progressBar.setVisibility(View.GONE);
                                Toast.makeText(AdminDataDisplay.this,"Connected to server",Toast.LENGTH_SHORT).show();

                                server_status = findViewById(R.id.server_stat);
                                server_status.setText("Connected");
                                server_status.setTextColor(Color.GREEN);

                                m_subscribe();
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
            }
        });

        mqttThread.start();
    }

    // this method subscribes to the topic passed in the parameter
    private void subscription_handler(final String topic){
        try {
            IMqttToken subToken = client.subscribe(topic, 0);
            subToken.setActionCallback(new IMqttActionListener()
            {
                @Override
                public void onSuccess(IMqttToken asyncActionToken)
                {
                    Toast.makeText(AdminDataDisplay.this,"Subscribed to "+topic,Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    Toast.makeText(AdminDataDisplay.this,"Subscription to "+topic+" failed",Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    private void m_subscribe(){
        subscription_handler("status/module");
        subscription_handler("sensors/critical");
        subscription_handler("sensors/non_critical");

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Toast.makeText(AdminDataDisplay.this,"Lost Connection to MQTT broker",Toast.LENGTH_SHORT).show();
                server_status = findViewById(R.id.server_stat);
                server_status.setText("Disconnected");
                server_status.setTextColor(Color.RED);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws JSONException {
                final JSONObject msg = new JSONObject(new String (message.getPayload()));

                switch (topic) {
                    case "sensors/critical":
                        runOnUiThread(new Runnable() {
                   @Override
                            public void run() {
                                try {
                                    data.clear();
                                    data.add(msg.getInt("rpm"));
                                    data.add(msg.getInt("oil_temp"));
                                    data.add(msg.getInt("coolant_temp"));
                                    data.add(msg.getInt("fuel_pressure"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                for(int i=0; i<fields.size(); i++){
                                    fields.get(i).setText(data.get(i).toString());

                                }
                            }
                        });

                        break;
                    case "sensors/non-critical":

                        break;
                    case "status/module":
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                boolean conn_status = false;
                                try {
                                    conn_status = Integer.parseInt(msg.getString("ecu_conn")) == 1;
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                if(!conn_status)
                                {
                                    Toast.makeText(AdminDataDisplay.this,"Module Lost Connection to ECU",Toast.LENGTH_SHORT).show();
                                    ecu_status.setText("Disconnected");
                                    ecu_status.setTextColor(Color.RED);

                                } else {
                                    Toast.makeText(AdminDataDisplay.this,"Module Connected to ECU",Toast.LENGTH_SHORT).show();
                                    ecu_status.setText("Connected");
                                    ecu_status.setTextColor(Color.GREEN);
                                }
                            }
                        });

                        break;
                    default:

                        break;
                }
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
}
