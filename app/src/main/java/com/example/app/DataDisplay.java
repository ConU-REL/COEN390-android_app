package com.example.app;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class DataDisplay extends AppCompatActivity {
    protected ImageView closeReconnect;
    protected Dialog reconnect;
    protected Button reconnect_button;
    protected Button start_engine_button;
    protected Button fuel_button;
    protected LinearLayout access_layout;
    protected Button request_access;


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
    private static final String TAG = "DataDisplay";
    SharedPreferencesHelper sharedPreferencesHelper;

    // Set the following variable to true for MQTT testing, set to false to actually use it on
    // the car properly
    boolean test_mqtt = false;
    String MQTTHOST = test_mqtt ? "tcp://broker.hivemq.com:1883" : "tcp://10.0.22.10:1883";
    private Integer rpm = 0;


    private boolean activity_running = true;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_display);

        Intent intent = getIntent();
        is_admin = intent.getBooleanExtra("admin", false);

        username = intent.getStringExtra("username");

        access_layout = findViewById(R.id.acess_layout);
        fuel_button = findViewById(R.id.fuel_button);
        start_engine_button = findViewById(R.id.start_engine_button);
        request_access=findViewById(R.id.request_access);


        sharedPreferencesHelper = new SharedPreferencesHelper(this);

        if (!is_admin) {
            fuel_button.setVisibility(View.GONE);
            start_engine_button.setVisibility(View.GONE);
            access_layout.setVisibility(View.VISIBLE);
            request_access.setVisibility(View.GONE);
        } else {
            access_layout.setVisibility(View.GONE);
        }

        reconnect = new Dialog(this);

        connection_progressBar = findViewById(R.id.connection_progressBar);


        ecu_status = findViewById(R.id.ecu_stat);
        ecu_status.setText("Disconnected");
        ecu_status.setTextColor(Color.RED);

        server_status = findViewById(R.id.server_stat);
        server_status.setText("Disconnected");
        server_status.setTextColor(Color.RED);

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


        for (int i = 0; i < fields.size(); i++) {
            fields.get(i).setText("0");
        }


        // handle starting/stopping the engine
        final boolean[] clicked = {false};
        start_engine_button.setText("START");

        start_engine_button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                //Toast.makeText(getBaseContext(),"START ENGINE",Toast.LENGTH_SHORT).show();
                m_publish_engine(true);
                clicked[0] = true;

                return false;
            }
        });

        start_engine_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if ((event.getAction() == MotionEvent.ACTION_UP ||
                        event.getAction() == MotionEvent.ACTION_CANCEL) && clicked[0]) {

                    //Toast.makeText(getBaseContext(),"STOP ENGINE",Toast.LENGTH_SHORT).show();
                    m_publish_engine(false);
                    clicked[0] = false;
                }
                return false;
            }
        });

        // handle starting/stopping the fuel pump
        final boolean[] pump_status = {false};
        fuel_button.setText("Turn On");
        fuel_button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                pump_status[0] = !pump_status[0];
                m_publish_fuel(pump_status[0]);
                String btn_text = pump_status[0] ? "Turn Off" : "Turn On";
                fuel_button.setText(btn_text);
                //Toast.makeText(getBaseContext(),"FUEL PUMP TOGGLE",Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        m_connect(findViewById(android.R.id.content));
    }

    @Override
    protected void onResume() {
        super.onResume();
        activity_running = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        activity_running = false;
    }


    //Connect to the server
    public void m_connect(View v) {
        connection_progressBar.setVisibility(View.VISIBLE);

        if (client != null && client.isConnected()) {
            return;
        }

        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), MQTTHOST,
                clientId);

        mqttThread = new Thread(new Runnable() {

            @Override
            public void run() {
                if (client != null && !client.isConnected()) {
                    try {
                        connection = client.connect();
                        connection.setActionCallback(new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                // We are connected
                                Log.d("In MQTT_Connection", "onSuccess");
                                connection_progressBar.setVisibility(View.GONE);
                                //Toast.makeText(DataDisplay.this,"Connected to server",Toast.LENGTH_SHORT).show();

                                server_status = findViewById(R.id.server_stat);
                                server_status.setText("Connected");
                                server_status.setTextColor(Color.GREEN);
                                if (!is_admin) {
                                    m_publish_add();
                                }

                                m_subscribe();
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                // Something went wrong e.g. connection timeout or firewall problems
                                Log.d("In MQTT_Connection", "onFailure");
                                connection_progressBar.setVisibility(View.GONE);
                                show_reconnect_popup();

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


    public void m_publish_add() {

        String topic = "access/request";
        JSONObject msg;
        msg = new JSONObject();

        try {
            msg.put("username", username);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        try {
            client.publish(topic, msg.toString().getBytes(), 0, true);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    public void requestAccess(View view)
    {
        m_publish_add();
    }


    public void m_publish_engine(boolean state) {
        String topic = "control/engine";
        JSONObject msg;
        msg = new JSONObject();
        int val = state ? 1 : 0;

        try {
            msg.put("crank", val);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        try {
            client.publish(topic, msg.toString().getBytes(), 0, true);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    public void m_publish_fuel(boolean state) {
        String topic = "control/fuel_pump";
        JSONObject msg;
        msg = new JSONObject();
        int val = state ? 1 : 0;

        try {
            msg.put("pump", val);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        try {
            client.publish(topic, msg.toString().getBytes(), 0, true);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    // this method subscribes to the topic passed in the parameter
    private void subscription_handler(final String topic) {
        try {
            IMqttToken subToken = client.subscribe(topic, 0);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    //Toast.makeText(DataDisplay.this, "Subscribed to " + topic, Toast.LENGTH_SHORT).show();
                    request_access.setVisibility(View.VISIBLE);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    Toast.makeText(DataDisplay.this, "Subscription to " + topic + " failed", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    private void m_subscribe() {
        subscription_handler("status/module");
        subscription_handler("sensors/critical");
        subscription_handler("sensors/non_critical");
        subscription_handler("access/request");

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Toast.makeText(DataDisplay.this, "Lost Connection to MQTT broker", Toast.LENGTH_SHORT).show();
                server_status = findViewById(R.id.server_stat);
                server_status.setText("Disconnected");
                server_status.setTextColor(Color.RED);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws JSONException {
                final JSONObject msg = new JSONObject(new String(message.getPayload()));

                switch (topic) {
                    case "sensors/critical":
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dataRed.clear();
                                try {
                                    data.clear();
                                    data.add(msg.getInt("rpm"));
                                    rpm = data.get(0);
                                    String op = rpm > 2000 ? "Stop" : "Start";
                                    start_engine_button.setText(op);
                                    data.add(msg.getInt("oil_temp"));
                                    data.add(msg.getInt("coolant_temp"));
                                    data.add(msg.getInt("fuel_pressure"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                for (int i = 0; i < data.size(); i++) {
                                    fields.get(i).setText(data.get(i).toString());
                                }

                                if (data.get(0) > 12500) {
                                    fields.get(0).setTextColor(Color.RED);
                                    dataRed.add("rpm: " + data.get(0));
                                } else {
                                    fields.get(0).setTextColor(Color.BLACK);
                                }

                                if (data.get(1) < 75) {
                                    fields.get(1).setTextColor(Color.BLUE);
                                } else if (data.get(2) > 120) {
                                    fields.get(1).setTextColor(Color.RED);
                                    dataRed.add("oil_temp: " + data.get(1));
                                } else {
                                    fields.get(1).setTextColor(Color.GREEN);
                                }

                                if (data.get(2) < 75) {
                                    fields.get(2).setTextColor(Color.BLUE);
                                } else if (data.get(2) > 110) {
                                    fields.get(2).setTextColor(Color.RED);
                                    dataRed.add("coolant_temp: " + data.get(2));

                                } else {
                                    fields.get(2).setTextColor(Color.GREEN);
                                }

                                if (data.get(3) < 390 || data.get(1) > 430) {
                                    fields.get(3).setTextColor(Color.RED);
                                    dataRed.add("fuel_pressure: " + data.get(3));
                                } else {
                                    fields.get(3).setTextColor(Color.GREEN);
                                }
                            }
                        });

                        break;
                    case "sensors/non_critical":
                        if (!non_critical) return;
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

                                if (!conn_status) {
                                    Toast.makeText(DataDisplay.this, "Module Lost Connection to ECU", Toast.LENGTH_SHORT).show();
                                    ecu_status.setText("Disconnected");
                                    ecu_status.setTextColor(Color.RED);

                                    for (int i = 0; i < fields.size(); i++) {
                                        fields.get(i).setText("0");
                                    }

                                } else {
                                    //Toast.makeText(DataDisplay.this,"Module Connected to ECU",Toast.LENGTH_SHORT).show();
                                    ecu_status.setText("Connected");
                                    ecu_status.setTextColor(Color.GREEN);
                                }
                            }
                        });

                        break;
                    case "access/request":
                        String connected_user = " ";
                        String access = " ";
                        try {
                            connected_user = msg.getString("username");
                            access = msg.getString("request");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                         if(!is_admin)
                         {
                             if(connected_user.equals(username) && access.equals("granted"))
                             {
                                 access_layout.setVisibility(View.GONE);
                             }
                         }
                         else
                         {
                             Toast.makeText(DataDisplay.this, connected_user + " is waiting for access", Toast.LENGTH_SHORT).show();

                         }
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
    private void show_reconnect_popup() {
        if (!activity_running) {
            return;
        }
        reconnect.setContentView(R.layout.popup_failed_connection);
        closeReconnect = reconnect.findViewById(R.id.closeReconnect);
        reconnect_button = reconnect.findViewById(R.id.reconnect_button);
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

    public void toggle_non_critical() {
        non_critical = !non_critical;
        if (non_critical) {
            for (int i = 4; i < fields.size(); i++) {
                fields.get(i).setVisibility(View.VISIBLE);
            }

            for (int i = 0; i < fields_nc_labels.size(); i++) {
                fields_nc_labels.get(i).setVisibility(View.VISIBLE);
            }
            div.setVisibility(View.VISIBLE);
        } else {
            for (int i = 4; i < fields.size(); i++) {
                fields.get(i).setVisibility(View.GONE);
            }

            for (int i = 0; i < fields_nc_labels.size(); i++) {
                fields_nc_labels.get(i).setVisibility(View.GONE);
            }

            div.setVisibility(View.GONE);
        }
    }

    // set up the top bar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_admin_data, menu);
        return true;
    }

    // listen for when the menu option clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toggle_data:
                // toggle non-critical data when clicked
                toggle_non_critical();
                return true;

            case R.id.save_session:
                sharedPreferencesHelper.saveSessionName("SESSION 1");

                sharedPreferencesHelper.saveSessionUsers("NO USERS");
                if (dataRed.isEmpty())
                    sharedPreferencesHelper.saveSessionError("NO WARNINGS GENERATED!!");
                else
                    sharedPreferencesHelper.saveSessionError(dataRed.toString());
                Toast.makeText(getBaseContext(), "SAVED", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.reconnect:
                m_connect(findViewById(android.R.id.content));
                return true;

            default:
                // default case, not really used here but apparently it's best practice
                return super.onOptionsItemSelected(item);
        }
    }
}
