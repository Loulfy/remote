package io.lylix.remote;

import android.widget.CompoundButton;
import android.widget.ToggleButton;

public class Activator  implements CompoundButton.OnCheckedChangeListener
{
    private MqttListener service;

    Activator(MqttListener service)
    {
        this.service = service;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b)
    {
        service.publish((ToggleButton) compoundButton, b);
    }
}
