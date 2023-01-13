package org.eclipse.paho.android.service;

import androidx.annotation.NonNull;

import org.eclipse.paho.client.mqttv3.internal.ClientComms;

public class QiscusMqtt {
    private static QiscusMqtt qiscusMqttInstance;
    private ClientComms comms;

    private QiscusMqtt() {
    }

    @NonNull
    public static QiscusMqtt getInstance() {
        if (qiscusMqttInstance == null) {
            qiscusMqttInstance = new QiscusMqtt();
        }

        return qiscusMqttInstance;
    }

    public ClientComms getComms() {
        return comms;
    }

    public void setComms(ClientComms comms) {
        this.comms = comms;
    }
}
