package org.eclipse.paho.android.service;

import static android.content.Context.POWER_SERVICE;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

public class PingWorker extends Worker {
    private static final String TAG = "PingWorker";
    private static final String WAKELOG_TAG = "MQTTQiscus::tag";

    public PingWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Ping Workmanager at:" + System.currentTimeMillis());

        // Handle when client commons is null when app is killed
        if (QiscusMqtt.getInstance().getComms() != null){
            // Assign new callback to token to execute code after PingResq
            // arrives. Get another wakelock even receiver already has one,
            // release it until ping response returns.
            PowerManager pm = (PowerManager) getApplicationContext().getSystemService(POWER_SERVICE);
            PowerManager.WakeLock wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOG_TAG);
            wakelock.acquire(30 * 1000L /* 30 seconds*/);

            IMqttToken token = QiscusMqtt.getInstance().getComms().checkForActivity(new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "Success. Release lock(" + WAKELOG_TAG + "):"
                            + System.currentTimeMillis());
                    //Release wakelock when it is done.
                    wakelock.release();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    Log.d(TAG, "Failure. Release lock(" + WAKELOG_TAG + "):"
                            + System.currentTimeMillis());
                    //Release wakelock when it is done.
                    wakelock.release();
                }
            });


            if (token == null && wakelock.isHeld()) {
                wakelock.release();
            }
        }

        return Result.success();
    }
}
