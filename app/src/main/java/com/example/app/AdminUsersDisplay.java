package com.example.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
import java.util.List;

public class AdminUsersDisplay extends AppCompatActivity implements RecyclerViewAdapter.OnItemListener {
    protected RecyclerView connected_users_list;
    protected CheckBox checkBox;
    protected Button btn_refresh;
    String MQTTtestHOST = "tcp://broker.hivemq.com:1883";
    List<String> UserInputsList;
    MqttAndroidClient client;
    String name = " ";
    ArrayList<String> newList;

    public static <T> ArrayList<T> removeDuplicates(ArrayList<T> list) {

        // Create a new ArrayList
        ArrayList<T> newList = new ArrayList<T>();

        // Traverse through the first list
        for (T element : list) {

            // If this element is not present in newList
            // then add it
            if (!newList.contains(element)) {
                newList.add(element);
            }
        }
        // return the new list
        return newList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users_display);
        // setup the activity
        connected_users_list = findViewById(R.id.connected_users_list);
        btn_refresh = findViewById(R.id.refresh_button);
        checkBox = findViewById(R.id.checkbox);

        // listen for clicks on the refresh button
        btn_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(getIntent());
            }
        });
        m_connect();

    }


    // function to connect to the MQTT broker
    private void m_connect() {
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), MQTTtestHOST,
                clientId);
        UserInputsList = new ArrayList<>();
        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
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


    // function to initialize the list view and link the adapter
    private void listview_init() {
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(newList, this);
        connected_users_list.setAdapter(adapter);
        connected_users_list.setLayoutManager(new LinearLayoutManager(this));
    }


    // function to fill the list view
    public void listview_populate() {
        ArrayList<String> adddataInputsListText = new ArrayList<>(UserInputsList);
        newList = removeDuplicates(adddataInputsListText);
        listview_init();
    }


    // function to subscribe to a given topic
    public void m_subscribe_add() {
        String topic = "access/request";
        try {
            IMqttToken subToken = client.subscribe(topic, 0);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // don't care
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // don't care
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                // don't care
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws JSONException {
                // if message received on subscribed topic, extract the user and add to the list
                final JSONObject msg = new JSONObject(new String(message.getPayload()));
                String connected_user = " ";
                try {
                    connected_user = msg.getString("field_username");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (!connected_user.isEmpty())
                    UserInputsList.add(connected_user);

                listview_populate();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // don't care
            }
        });
    }


    // function to publish the level of access that has been granted to the user (if any)
    public void m_publish_access(String username) {
        String topic = "access/request";
        JSONObject msg;
        msg = new JSONObject();

        // extract the info if it exists
        try {
            msg.put("field_username", username);
            msg.put("request", "granted");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // push the info if connected
        try {
            client.publish(topic, msg.toString().getBytes(), 0, true);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    // listen for list item clicks, grant access on click
    public void onNoteClick(int position) {
        name = newList.get(position);
        m_publish_access(name);
        Toast.makeText(getBaseContext(), "Access granted to " + name, Toast.LENGTH_SHORT).show();
    }
}
