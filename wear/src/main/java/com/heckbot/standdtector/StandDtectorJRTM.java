package com.heckbot.standdtector;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Joey on 11/25/2014.
 */
public class StandDtectorJRTM extends IntentService implements SensorEventListener {

    PendingIntent pendingIntent;
    Intent returnIntent;

    SensorManager mSensorManager;
    Sensor mStepCounterSensor;

    public StandDtectorJRTM() {
        super("StandDtectorJRTM");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String action = intent.getAction();
            Log.d("Intent", action);
            pendingIntent = intent.getParcelableExtra("pendingIntent");
            returnIntent = new Intent();
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

            if (action.equals("LastStep")) {
                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)) {
                    Log.d("Step_Counter", "Step Counter Available");
                    mStepCounterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
                    mSensorManager.registerListener(this, mStepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
                } else {
                    Log.d("Step_Counter", "No Step Counter Available");
                    returnIntent.putExtra("Step_Hardware", false);
                    try {
                        pendingIntent.send(this, 0, returnIntent);
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                    stopSelf();
                }
            } else {
                Log.d("MySensorEventListener", "Unrecognized Event");
                stopSelf();
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mStepCounterSensor) {
            mSensorManager.unregisterListener(this, mStepCounterSensor);
            long timestamp = event.timestamp / 1000000L;
            Log.d("SensorEvent", "Event Mills: " + timestamp);
            returnIntent.setAction("LastStep");
            returnIntent.putExtra("Step_Hardware", true);
            returnIntent.putExtra("Last_Step", timestamp);
            try {
                pendingIntent.send(this, 0 , returnIntent);
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
            stopSelf();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}





















