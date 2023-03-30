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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;



public class MainActivity extends AppCompatActivity {

    private MqttAndroidClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String server = "ws://192.168.1.64:80";
        String clientId = "Android_HPUI_Client";
        String topic = "Messaging";

        client = new MqttAndroidClient(this, server, clientId);

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.e("MQTT", "Connection lost", cause);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d("MQTT", "Received message: " + message.toString());

                runOnUiThread(() -> {
                    TextView textView = findViewById(R.id.fullMsg);
                    textView.setText(message.toString());
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