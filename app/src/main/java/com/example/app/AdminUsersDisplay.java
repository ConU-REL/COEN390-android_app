package com.example.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AdminUsersDisplay extends AppCompatActivity implements RecyclerViewAdapter.OnItemListener
{
    protected RecyclerView connected_users_list;
    protected CheckBox checkBox;

    String MQTTtestHOST="tcp://broker.hivemq.com:1883";
    String MQTTHOST="tcp://10.0.22.10:1883";
    List<String> UserInputsList;
    protected Button refress_button;
    MqttAndroidClient client;
    String name=" ";
    ArrayList<String> newList;
    SharedPreferencesHelper sharedPreferencesHelper;
    private ArrayList<String> adddataInputsListText;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users_display);
        connected_users_list=findViewById(R.id.connected_users_list);
        refress_button=findViewById(R.id.refresh_button);
        checkBox=findViewById(R.id.checkbox);


        refress_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                finish();
                startActivity(getIntent());


            }
        });
        m_connect();

    }


    private void m_connect()
    {

        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), MQTTtestHOST,
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


        RecyclerViewAdapter adapter=new RecyclerViewAdapter(newList,this);
        connected_users_list.setAdapter(adapter);
        connected_users_list.setLayoutManager(new LinearLayoutManager(this));



    }

    public void m_subscribe_add()
    {
        String topic ="access/request";
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
            public void messageArrived(String topic, MqttMessage message)throws JSONException {
                final JSONObject msg = new JSONObject(new String (message.getPayload()));
                String connected_user = " ";
                try {
                    connected_user = msg.getString("username");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(!connected_user.isEmpty())
                UserInputsList.add(connected_user);

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
                newList = removeDuplicates(adddataInputsListText);
        loadListView();

    }
    public void m_publish_access(String username)
    {

        String topic ="access/request";
        JSONObject msg;
        msg = new JSONObject();

        try{
            msg.put("username",username);
            msg.put("request","granted");
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
    @Override
    public void onNoteClick(int position) {
        name=adddataInputsListText.get(position);
        m_publish_access(name);
        Toast.makeText(getBaseContext(),"Access granted to "+ name,Toast.LENGTH_SHORT).show();


    }
    public static <T> ArrayList<T> removeDuplicates(ArrayList<T> list)
    {

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
}
