package com.example.app;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DataDisplay extends AppCompatActivity {


    protected ImageView closeReconnect;
    protected Dialog reconnect;
    protected Button reconnect_button;
    protected Button start_engine_button;


    MqttAndroidClient client;
    private ProgressBar connection_progressBar;
    Thread mqttThread;
    IMqttToken connection;
    //array to store peak data from car
    ArrayList<String> dataRed = new ArrayList<>();

    // user info
    boolean is_admin = false;
    String username;

    // array to store data from car
    ArrayList<Integer> data = new ArrayList<>();
    ArrayList<Integer> data_nc = new ArrayList<>();

    // UI elements
    boolean non_critical;
    ArrayList<TextView> fields = new ArrayList<>();
    ArrayList<TextView> fields_nc_labels = new ArrayList<>();
    View div;
    TextView ecu_status;
    TextView server_status;
    Button action_reconnect;
    protected Button insertSessionButton;
    private Button logout_button;
    private static final String TAG = "DataDisplay";
    SharedPreferencesHelper sharedPreferencesHelper;

    // Set the following variable to true for MQTT testing, set to false to actually use it on
    // the car properly
    boolean test_mqtt = false;
    String MQTTHOST = test_mqtt ? "tcp://broker.hivemq.com:1883" : "tcp://10.0.22.10:1883";


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_display);

        Intent intent = getIntent();
        is_admin = intent.getBooleanExtra("admin", false);
        final Intent intent2=new Intent(this,UserLogin.class);

        insertSessionButton = findViewById(R.id.InsertSessionButton);
        username = intent.getStringExtra("username");

        logout_button=findViewById(R.id.logout_button);

        sharedPreferencesHelper=new SharedPreferencesHelper(this);
        if(!is_admin)
        {
            insertSessionButton.setVisibility(View.GONE);
            logout_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    m_disconnect();
                    startActivity(intent2);


                }
            });
        }
        else
        {   logout_button.setVisibility(View.GONE);
        }
        insertSessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sharedPreferencesHelper.saveSessionName("SESSION 1");

                sharedPreferencesHelper.saveSessionUsers("NO USERS");
                if (dataRed.isEmpty())
                    sharedPreferencesHelper.saveSessionError("NO WARNINGS GENERATED!!");
                else
                    sharedPreferencesHelper.saveSessionError(dataRed.toString());
                Toast.makeText(getBaseContext(),"SAVED",Toast.LENGTH_SHORT).show();

            }
        });

        TextView label_username = findViewById(R.id.label_username);
        label_username.setText(username);

        reconnect = new Dialog(this);

        connection_progressBar=findViewById(R.id.connection_progressBar);


        ecu_status = findViewById(R.id.ecu_stat);
        ecu_status.setText("Disconnected");
        ecu_status.setTextColor(Color.RED);

        server_status = findViewById(R.id.server_stat);
        server_status.setText("Disconnected");
        server_status.setTextColor(Color.RED);

        action_reconnect = findViewById(R.id.action_reconnect);

        fields.add((TextView) findViewById(R.id.data_rpm));
        fields.add((TextView) findViewById(R.id.data_temp_o));
        fields.add((TextView) findViewById(R.id.data_temp_c));
        fields.add((TextView) findViewById(R.id.data_press_f));
        fields.add((TextView) findViewById(R.id.data_voltage));
        fields.add((TextView) findViewById(R.id.data_gear));
        fields.add((TextView) findViewById(R.id.data_launch));

        div = findViewById(R.id.divider);
        fields_nc_labels.add((TextView) findViewById(R.id.label_voltage));
        fields_nc_labels.add((TextView) findViewById(R.id.label_gear));
        fields_nc_labels.add((TextView) findViewById(R.id.label_launch));


        for(int i=0; i<fields.size(); i++){
            fields.get(i).setText("0");
        }


        // handle starting/stopping the engine
        final boolean[] clicked = {false};
        start_engine_button=findViewById(R.id.start_engine_button);
        start_engine_button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                Toast.makeText(getBaseContext(),"START ENGINE",Toast.LENGTH_SHORT).show();
                m_publish_engine(true);
                clicked[0] = true;
                return false;
            }
        });

        start_engine_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if((event.getAction() == MotionEvent.ACTION_UP ||
                        event.getAction() == MotionEvent.ACTION_CANCEL ) && clicked[0]){
                    Toast.makeText(getBaseContext(),"STOP ENGINE",Toast.LENGTH_SHORT).show();
                    m_publish_engine(false);
                    clicked[0] = false;
                }
                return false;
            }
        });

        m_connect(findViewById(android.R.id.content));
    }
    private void m_disconnect()
    {
        String topic ="adduser/"+username;
        try {
            client.publish(topic, new byte[]{}, 0, true);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    //Connect to the server
    public void m_connect(View v)
    {
        connection_progressBar.setVisibility(View.VISIBLE);
        action_reconnect.setVisibility(View.GONE);


        if(client != null && client.isConnected()){
            return;
        }

        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(),MQTTHOST,
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
                                Toast.makeText(DataDisplay.this,"Connected to server",Toast.LENGTH_SHORT).show();

                                server_status = findViewById(R.id.server_stat);
                                server_status.setText("Connected");
                                server_status.setTextColor(Color.GREEN);
                                action_reconnect.setVisibility(View.GONE);
                                if(!is_admin)
                                {
                                    m_publish_add();
                                }

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

    public void m_publish_engine(boolean state){
        String topic ="control/engine";
        JSONObject msg;
        msg = new JSONObject();
        int val = state ? 1 : 0;

        try{
            msg.put("crank",val);
        }catch(JSONException e)
        {
            throw new RuntimeException(e);
        }

        try {
            client.publish(topic, msg.toString().getBytes(),0,true);
        } catch (MqttException e) {
            e.printStackTrace();
        }
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
                    Toast.makeText(DataDisplay.this,"Subscribed to "+topic,Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    Toast.makeText(DataDisplay.this,"Subscription to "+topic+" failed",Toast.LENGTH_SHORT).show();
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
                Toast.makeText(DataDisplay.this,"Lost Connection to MQTT broker",Toast.LENGTH_SHORT).show();
                server_status = findViewById(R.id.server_stat);
                server_status.setText("Disconnected");
                server_status.setTextColor(Color.RED);
                action_reconnect.setVisibility(View.VISIBLE);
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

                                for(int i=0; i<data.size(); i++){
                                    fields.get(i).setText(data.get(i).toString());
                                }

                                if(data.get(0) > 12500){
                                    fields.get(0).setTextColor(Color.RED);
                                    dataRed.add("rpm: "+ data.get(0));
                                } else {
                                    fields.get(0).setTextColor(Color.BLACK);
                                }

                                if(data.get(1) < 75){
                                    fields.get(1).setTextColor(Color.BLUE);
                                } else if(data.get(2) > 120) {
                                    fields.get(1).setTextColor(Color.RED);
                                    dataRed.add("oil_temp: "+ data.get(1));
                                } else {
                                    fields.get(1).setTextColor(Color.GREEN);
                                }

                                if(data.get(2) < 75){
                                    fields.get(2).setTextColor(Color.BLUE);
                                } else if(data.get(2) > 110) {
                                    fields.get(2).setTextColor(Color.RED);
                                    dataRed.add("coolant_temp: "+ data.get(2));

                                } else {
                                    fields.get(2).setTextColor(Color.GREEN);
                                }

                                if(data.get(3) < 390 || data.get(1) > 430){
                                    fields.get(3).setTextColor(Color.RED);
                                    dataRed.add("fuel_pressure: "+ data.get(3));
                                } else {
                                    fields.get(3).setTextColor(Color.GREEN);
                                }
                            }
                        });

                        break;
                    case "sensors/non_critical":
                        if(!non_critical) return;
                        try {
                            data_nc.clear();
                            data_nc.add(msg.getInt("voltage"));
                            data_nc.add(msg.getInt("gear"));
                            data_nc.add(msg.getInt("launch"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        fields.get(4).setText(data_nc.get(0).toString());
                        fields.get(5).setText(data_nc.get(1).toString());
                        fields.get(6).setText(data_nc.get(2).toString());

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
                                    Toast.makeText(DataDisplay.this,"Module Lost Connection to ECU",Toast.LENGTH_SHORT).show();
                                    ecu_status.setText("Disconnected");
                                    ecu_status.setTextColor(Color.RED);

                                    for(int i=0; i<fields.size(); i++){
                                        fields.get(i).setText("0");
                                    }

                                } else {
                                    Toast.makeText(DataDisplay.this,"Module Connected to ECU",Toast.LENGTH_SHORT).show();
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
                m_connect(v);
                reconnect.dismiss();
            }
        });
        reconnect.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        reconnect.show();
    }

    public void btn_data(View v){
        non_critical = !non_critical;
        if(non_critical) {
            for (int i = 4; i < fields.size(); i++) {
                fields.get(i).setVisibility(View.VISIBLE);
            }

            for(int i=0; i < fields_nc_labels.size(); i++){
                fields_nc_labels.get(i).setVisibility(View.VISIBLE);
            }
            div.setVisibility(View.VISIBLE);
        } else {
            for (int i = 4; i < fields.size(); i++) {
                fields.get(i).setVisibility(View.GONE);
            }

            for(int i=0; i < fields_nc_labels.size(); i++){
                fields_nc_labels.get(i).setVisibility(View.GONE);
            }

            div.setVisibility(View.GONE);
        }
    }
}
