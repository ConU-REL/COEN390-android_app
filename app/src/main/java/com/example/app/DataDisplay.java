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

/*
 * This file is probably the most important one, handles displaying data and is where the admin
 * performs all the vehicle control tasks
 */

public class DataDisplay extends AppCompatActivity {
    // UI elements
    protected Button start_engine_button;
    protected Button fuel_button;
    protected LinearLayout access_layout;
    protected Button request_access;
    // MQTT Definitions
    MqttAndroidClient client;
    Thread mqttThread;
    IMqttToken connection;
    // Set the following variable to true for MQTT testing, set to false to actually use it on
    // the car properly
    boolean test_mqtt = false;
    String MQTTHOST = test_mqtt ? "tcp://broker.hivemq.com:1883" : "tcp://10.0.22.10:1883";
    //array to store peak data from car
    ArrayList<String> dataRed = new ArrayList<>();
    // user info
    boolean is_admin = false;
    String username;
    // arrays to store data from car
    ArrayList<Integer> data = new ArrayList<>();
    ArrayList<Integer> data_nc = new ArrayList<>();
    ArrayList<TextView> fields = new ArrayList<>();
    ArrayList<TextView> fields_nc_labels = new ArrayList<>();
    boolean non_critical;
    View div;
    TextView ecu_status;
    TextView server_status;
    SharedPreferencesHelper sharedPreferencesHelper;
    private ProgressBar connection_progressBar;
    // Misc defs
    private Integer rpm = 0;
    private boolean activity_running = true;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_display);

        // get data from intent
        Intent intent = getIntent();
        is_admin = intent.getBooleanExtra("admin", false);
        username = intent.getStringExtra("field_username");

        // init UI elements
        access_layout = findViewById(R.id.acess_layout);
        fuel_button = findViewById(R.id.fuel_button);
        start_engine_button = findViewById(R.id.start_engine_button);
        request_access = findViewById(R.id.request_access);
        connection_progressBar = findViewById(R.id.connection_progressBar);
        ecu_status = findViewById(R.id.ecu_stat);
        server_status = findViewById(R.id.server_stat);
        div = findViewById(R.id.divider);

        // init SPH
        sharedPreferencesHelper = new SharedPreferencesHelper(this);

        // hide things only the admin should have access to if logged in as user
        if (!is_admin) {
            // hide admin buttons if not admin
            fuel_button.setVisibility(View.GONE);
            start_engine_button.setVisibility(View.GONE);

            TextView label_engine_control = findViewById(R.id.label_engine_control);
            TextView label_fuel_pump = findViewById(R.id.label_fuel_pump);

            label_engine_control.setVisibility(View.GONE);
            label_fuel_pump.setVisibility(View.GONE);

            invalidateOptionsMenu();
            access_layout.setVisibility(View.VISIBLE);
            request_access.setVisibility(View.GONE);
        } else {
            access_layout.setVisibility(View.GONE);
            invalidateOptionsMenu();
        }

        // connections start as disconnected until set otherwise
        ecu_status.setText("Disconnected");
        ecu_status.setTextColor(Color.RED);
        server_status.setText("Disconnected");
        server_status.setTextColor(Color.RED);


        // initialize all the field elements
        fields.add((TextView) findViewById(R.id.data_rpm));
        fields.add((TextView) findViewById(R.id.data_temp_o));
        fields.add((TextView) findViewById(R.id.data_temp_c));
        fields.add((TextView) findViewById(R.id.data_press_f));
        fields.add((TextView) findViewById(R.id.data_press_o));
        fields.add((TextView) findViewById(R.id.data_voltage));
        fields.add((TextView) findViewById(R.id.data_gear));
        fields.add((TextView) findViewById(R.id.data_launch));

        fields_nc_labels.add((TextView) findViewById(R.id.label_voltage));
        fields_nc_labels.add((TextView) findViewById(R.id.label_gear));
        fields_nc_labels.add((TextView) findViewById(R.id.label_launch));

        // init all fields to 0
        for (int i = 0; i < fields.size(); i++) {
            fields.get(i).setText("0");
        }

        // handle starting/stopping the engine
        final boolean[] clicked = {false};
        start_engine_button.setText("START");
        start_engine_button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // crank (or stop) the engine on long click
                m_publish_engine(true);
                clicked[0] = true;
                return false;
            }
        });

        start_engine_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // stop cranking on release from long click
                if ((event.getAction() == MotionEvent.ACTION_UP ||
                        event.getAction() == MotionEvent.ACTION_CANCEL) && clicked[0]) {
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
                // start/stop fuel pump on long click
                pump_status[0] = !pump_status[0];
                m_publish_fuel(pump_status[0]);
                String btn_text = pump_status[0] ? "Turn Off" : "Turn On";
                fuel_button.setText(btn_text);
                //Toast.makeText(getBaseContext(),"FUEL PUMP TOGGLE",Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        // connect to the MQTT broker
        m_connect(findViewById(android.R.id.content));
    }

    // these functions make sure that popups are only displayed if the user hasn't left this
    // activity, otherwise the app crashes
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


    //Connect to the broker
    public void m_connect(View v) {
        connection_progressBar.setVisibility(View.VISIBLE);

        // if already connected
        if (client != null && client.isConnected()) {
            return;
        }

        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), MQTTHOST,
                clientId);

        // connect in a different thread
        mqttThread = new Thread(new Runnable() {

            @Override
            public void run() {
                if (client != null && !client.isConnected()) {
                    try {
                        // try to connect
                        connection = client.connect();
                        connection.setActionCallback(new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                // We are connected
                                Log.d("In MQTT_Connection", "onSuccess");
                                connection_progressBar.setVisibility(View.GONE);
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
                                Toast.makeText(DataDisplay.this, "MQTT Broker connection failed", Toast.LENGTH_SHORT).show();
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

        // start the thread
        mqttThread.start();
    }

    // function to add access request to the topic
    public void m_publish_add() {
        String topic = "access/request";
        JSONObject msg;
        msg = new JSONObject();

        // put the username in the JSON element
        try {
            msg.put("field_username", username);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // publish the JSON element
        try {
            client.publish(topic, msg.toString().getBytes(), 0, true);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    // function to publish engine start/stop messages
    public void m_publish_engine(boolean state) {
        String topic = "control/engine";
        JSONObject msg;
        msg = new JSONObject();
        int val = state ? 1 : 0;

        // try to insert into the JSON element
        try {
            msg.put("crank", val);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // try to publish to the MQTT Broker
        try {
            client.publish(topic, msg.toString().getBytes(), 0, true);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    // function to publish fuel pump start/stop messages
    public void m_publish_fuel(boolean state) {
        String topic = "control/fuel_pump";
        JSONObject msg;
        msg = new JSONObject();
        int val = state ? 1 : 0;

        // try to insert into the JSON element
        try {
            msg.put("pump", val);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // try to publish to the MQTT Broker
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

    // this function subscribes to all required topics
    private void m_subscribe() {
        // subscribe
        subscription_handler("status/module");
        subscription_handler("sensors/critical");
        subscription_handler("sensors/non_critical");
        subscription_handler("access/request");

        // listen for subscribe errors
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Toast.makeText(DataDisplay.this, "Lost Connection to MQTT broker", Toast.LENGTH_SHORT).show();
                server_status = findViewById(R.id.server_stat);
                server_status.setText("Disconnected");
                server_status.setTextColor(Color.RED);
            }

            // when message arrives from subscribed topic
            @Override
            public void messageArrived(String topic, MqttMessage message) throws JSONException {
                final JSONObject msg = new JSONObject(new String(message.getPayload()));

                switch (topic) {
                    // set critical data values
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
                                    data.add(msg.getInt("oil_pressure"));
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

                                if (data.get(4) < 300 || data.get(2) > 450) {
                                    fields.get(4).setTextColor(Color.RED);
                                } else {
                                    fields.get(4).setTextColor(Color.GREEN);
                                }
                            }
                        });

                        break;
                    // set non-critical values
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

                        fields.get(5).setText(data_nc.get(0).toString());
                        fields.get(6).setText(data_nc.get(1).toString());
                        fields.get(7).setText(data_nc.get(2).toString());

                        break;
                    // set module connection status vars
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
                    // handle access requests
                    case "access/request":
                        String connected_user = " ";
                        String access = " ";
                        try {
                            connected_user = msg.getString("field_username");
                            access = msg.getString("request");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if (!is_admin) {
                            if (connected_user.equals(username) && access.equals("granted")) {
                                access_layout.setVisibility(View.GONE);
                            }
                        } else {
                            Toast.makeText(DataDisplay.this, connected_user + " is waiting for access", Toast.LENGTH_SHORT).show();

                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // don't care
            }
        });
    }

    // opens a popup page where the user can connect to the server again if the connection failed`
    private void show_reconnect_popup() {
        // don't crash the app here
        if (!activity_running) {
            return;
        }
        ImageView close_image;
        final Dialog reconnect = new Dialog(this);
        Button reconnect_button;

        reconnect.setContentView(R.layout.popup_failed_connection);
        close_image = reconnect.findViewById(R.id.closeReconnect);
        reconnect_button = reconnect.findViewById(R.id.reconnect_button);
        close_image.setOnClickListener(new View.OnClickListener() {
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

    // the popup for sending flags to the steering wheel
    private void show_flag_dialog() {
        ImageView close_image;
        final Dialog flag = new Dialog(this);
        ArrayList<Button> buttons = new ArrayList<>();

        flag.setContentView(R.layout.popup_send_flag);
        close_image = flag.findViewById(R.id.flag_close);

        buttons.add((Button) flag.findViewById(R.id.flag_none));
        buttons.add((Button) flag.findViewById(R.id.flag_green));
        buttons.add((Button) flag.findViewById(R.id.flag_yellow));
        buttons.add((Button) flag.findViewById(R.id.flag_red));
        buttons.add((Button) flag.findViewById(R.id.flag_orange));
        buttons.add((Button) flag.findViewById(R.id.flag_cancel));

        for (int i = 0; i < buttons.size() - 1; i++) {
            final int finalI = i;
            buttons.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    m_publish_flag(finalI);
                }
            });
        }

        buttons.get(buttons.size() - 1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag.dismiss();
            }
        });

        close_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag.dismiss();
            }
        });

        flag.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        flag.show();
    }


    // the function to publish the requested flag to the MQTT Broker
    public void m_publish_flag(int type) {
        String topic = "control/comms";
        JSONObject msg;
        msg = new JSONObject();
        try {
            msg.put("flag", type);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        try {
            client.publish(topic, msg.toString().getBytes(), 0, true);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    // the function to toggle visibility of non-critical data
    public void toggle_non_critical() {
        non_critical = !non_critical;
        if (non_critical) {
            for (int i = 5; i < fields.size(); i++) {
                fields.get(i).setVisibility(View.VISIBLE);
            }

            for (int i = 0; i < fields_nc_labels.size(); i++) {
                fields_nc_labels.get(i).setVisibility(View.VISIBLE);
            }
            div.setVisibility(View.VISIBLE);
        } else {
            for (int i = 5; i < fields.size(); i++) {
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

        if (!is_admin) {
            menu.findItem(R.id.save_session).setVisible(false);
            menu.findItem(R.id.save_session).setEnabled(false);

            menu.findItem(R.id.send_flag).setVisible(false);
            menu.findItem(R.id.send_flag).setEnabled(false);
        }

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

            case R.id.send_flag:
                show_flag_dialog();
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
