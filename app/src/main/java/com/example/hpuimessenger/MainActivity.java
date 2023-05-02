package com.example.hpuimessenger;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private MqttAndroidClient client;
    private Map<String, Integer> targets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        targets = new HashMap<String, Integer>() {{
                put("index_distal", R.id.index_distal);
                put("index_middle", R.id.index_middle);
                put("index_proximal", R.id.index_proximal);
                put("middle_distal", R.id.middle_distal);
                put("middle_middle", R.id.middle_middle);
                put("middle_proximal", R.id.middle_proximal);
                put("ring_distal", R.id.ring_distal);
                put("ring_middle", R.id.ring_middle);
                put("ring_proximal", R.id.ring_proximal);
                put("pinky_distal", R.id.pinky_distal);
                put("pinky_middle", R.id.pinky_middle);
                put("pinky_proximal", R.id.pinky_proximal);
        }};

        String server = "ws://206.87.23.238:80"; //      Change IP to match your local IPV4
        String clientId = "Android_HPUI_Client";
        String topic = "hpui/#";

        client = new MqttAndroidClient(this, server, clientId);

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.e("MQTT", "Connection lost", cause);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d("MQTT", "Received message: " + message.toString());

                String subTopic = topic.split("/")[1];
                for (Map.Entry<String, Integer> entry : targets.entrySet()) {
                    TextView textView = findViewById(entry.getValue());
                    if (entry.getKey().equalsIgnoreCase(subTopic)) {
                        textView.setText(message.toString());
                    } else {
                        textView.setText("");
                    }
                }

                runOnUiThread(() -> {
                    TextView textView = findViewById(R.id.fullMsg);
                    textView.setText("TOPIC: " + topic+"   MESSAGE:"+ message.toString());
                });
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setConnectionTimeout(5);
        IMqttToken token = null;
        try {
            token = client.connect(options);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
        token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("MQTT", "Connected to MQTT broker");
                    try {
                        client.subscribe(topic, 0);
                    } catch (Exception e) {
                        Log.e("MQTT", "Failed to subscribe to topic", e);
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e("MQTT", "Failed to connect to MQTT broker", exception);
                }
            });


    }

    protected void onDestroy() {
        super.onDestroy();
        try {
            client.disconnect();
        } catch (Exception e) {
            Log.e("MQTT", "Failed to disconnect from MQTT broker", e);
        }
    }

}
