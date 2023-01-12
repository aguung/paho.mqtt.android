package org.eclipse.paho.android.service;

import static android.content.Context.POWER_SERVICE;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class PingWorker extends Worker {
    private static final String TAG = "AlarmPingSender";

    public PingWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        // According to the docs, "Alarm Manager holds a CPU wake lock as
        // long as the alarm receiver's onReceive() method is executing.
        // This guarantees that the phone will not sleep until you have
        // finished handling the broadcast.", but this class still get
        // a wake lock to wait for ping finished.

        Log.d(TAG, "Sending Ping at:" + System.currentTimeMillis());

        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MQTT::tag");
        wakelock.acquire(1 * 10 * 1000L /*1 minutes*/);
        if (wakelock.isHeld()) {
            wakelock.release();
        }

        return Result.success();
    }
}
