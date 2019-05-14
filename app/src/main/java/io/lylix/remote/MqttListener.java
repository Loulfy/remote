package io.lylix.remote;

import android.util.Log;
import android.widget.ToggleButton;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.*;

import java.util.HashMap;
import java.util.Map;

public class MqttListener implements MqttCallbackExtended
{
    private MqttAndroidClient mqtt;

    private Map<String,ToggleButton> map;
    private Activator activator;

    MqttListener(MqttAndroidClient mqtt)
    {
        this.mqtt = mqtt;
        this.map = new HashMap<>();
        this.activator = new Activator(this);
    }

    @Override
    public void connectComplete(boolean reconnect, String uri)
    {
        for (Map.Entry<String,ToggleButton> entry : map.entrySet()) entry.getValue().setEnabled(true);

        if(reconnect) Log.i("remote","Reconnected to: " + uri);
        else Log.i("remote","Connected to: " + uri);
        try
        {
            for (Map.Entry<String,ToggleButton> entry : map.entrySet())
            {
                mqtt.subscribe(entry.getKey(), 0, null, new IMqttActionListener()
                {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken)
                    {
                        Log.i("remote","Subscribed!");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception)
                    {
                        Log.i("remote","Failed to subscribe");
                    }
                });
            }
        }
        catch (MqttException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable cause)
    {
        Log.i("remote", "The Connection was lost.");
        for (Map.Entry<String,ToggleButton> entry : map.entrySet()) entry.getValue().setEnabled(false);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception
    {
        String state = new String(message.getPayload());

        ToggleButton toggle = map.get(topic);

        if(toggle != null)
        {
            toggle.setOnCheckedChangeListener(null);
            toggle.setChecked(state.equals("on"));
            toggle.setOnCheckedChangeListener(activator);
        }

        Log.i("remote","Incoming message: (" + topic + ") " + state);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token)
    {

    }

    void register(ToggleButton toggle, String topic)
    {
        map.put(topic, toggle);

        toggle.setEnabled(false);
        toggle.setOnCheckedChangeListener(activator);
    }

    void publish(ToggleButton toggle, Boolean b)
    {
        for (Map.Entry<String,ToggleButton> entry : map.entrySet()) {
            if (entry.getValue().equals(toggle))
            {
                final String payload = b ? "on"  : "off";
                try
                {
                    if(mqtt.isConnected()) mqtt.publish(entry.getKey(), payload.getBytes(), 1,true);
                }
                catch (MqttException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
