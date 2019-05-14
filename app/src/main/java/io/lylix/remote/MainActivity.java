package io.lylix.remote;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ToggleButton;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.*;

public class MainActivity extends AppCompatActivity
{
    private MqttAndroidClient mqttAndroidClient;
    private final String uri = "ssl://lylix.io:8883";
    private String clientId = "android-remote";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        clientId = clientId + System.currentTimeMillis();

        final ToggleButton r1 = findViewById(R.id.r1);
        final ToggleButton r2 = findViewById(R.id.r2);

        Log.i("remote", "START : " + clientId);

        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), uri, clientId);

        MqttListener executor = new MqttListener(mqttAndroidClient);
        executor.register(r1, "vac/relay/r1");
        executor.register(r2, "vac/relay/r2");

        mqttAndroidClient.setCallback(executor);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        Log.i("remote", "PAUSE!");
        try
        {
            if(mqttAndroidClient.isConnected()) mqttAndroidClient.disconnect();
        }
        catch (MqttException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostResume()
    {
        super.onPostResume();

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);

        try
        {
            mqttAndroidClient.connect(options, null, new IMqttActionListener()
            {
                @Override
                public void onSuccess(IMqttToken asyncActionToken)
                {
                    Log.i("remote","connected ONCE");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception)
                {
                    Log.i("mqtt", "Failed to connect to: " + uri);
                    Log.e("mqtt", exception.toString());
                }
            });


        }
        catch (MqttException ex)
        {
            ex.printStackTrace();
        }
    }
}
