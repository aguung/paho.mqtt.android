/*******************************************************************************
 * Copyright (c) 2014 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution. 
 *
 * The Eclipse Public License is available at 
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.eclipse.paho.android.service;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttPingSender;
import org.eclipse.paho.client.mqttv3.internal.ClientComms;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

/**
 * Default ping sender implementation on Android. It is based on AlarmManager.
 *
 * <p>This class implements the {@link MqttPingSender} pinger interface
 * allowing applications to send ping packet to server every keep alive interval.
 * </p>
 *
 * @see MqttPingSender
 */
class AlarmPingSender implements MqttPingSender {
    // Identifier for Intents, log messages, etc..
    private static final String TAG = "AlarmPingSender";
    private static final String PING_JOB = "PING_JOB";

    // TODO: Add log.
    private ClientComms comms;
    private MqttService service;
    private AlarmPingSender that;
    private volatile boolean hasStarted = false;
    private WorkManager workManager;

    public AlarmPingSender(MqttService service) {
        if (service == null) {
            throw new IllegalArgumentException(
                    "Neither service nor client can be null.");
        }
        this.service = service;
        this.workManager = WorkManager.getInstance(this.service);
        that = this;
    }

    @Override
    public void init(ClientComms comms) {
        this.comms = comms;
    }

    @Override
    public void start() {
        String action = MqttServiceConstants.PING_SENDER
                + comms.getClient().getClientId();
        Log.d(TAG, "Register ping to MqttService" + action);

        schedule(comms.getKeepAlive());
        hasStarted = true;
    }

    @Override
    public void stop() {
        Log.d(TAG, "Unregister ping to MqttService" + comms.getClient().getClientId());
        if (hasStarted) {
            workManager.cancelUniqueWork(PING_JOB);
            hasStarted = false;
        }
    }

    @Override
    public void schedule(long delayInMilliseconds) {
        long nextAlarmInMilliseconds = System.currentTimeMillis()
                + delayInMilliseconds;
        Log.d(TAG, "Schedule next alarm at " + nextAlarmInMilliseconds +" with delay "+delayInMilliseconds);
        workManager.enqueueUniqueWork(
                PING_JOB,
                ExistingWorkPolicy.REPLACE,
                new OneTimeWorkRequest
                        .Builder(PingWorker.class)
                        .setInitialDelay(delayInMilliseconds, TimeUnit.MILLISECONDS)
                        .build()
        );
    }
}
