package com.heckbot.standdtector;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by Joey on 12/20/2014.
 */
public class StandDtectorStepCounter extends Service implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mStepCounterSensor;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mStepCounterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mSensorManager.registerListener(this, mStepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);

        SharedPreferences.Editor editor = getSharedPreferences(Constants.STANDDTECTORTM_SHARED_PREFERENCES, Context.MODE_PRIVATE).edit();
        editor.remove(Constants.DEVICE_LAST_STEP);
        editor.commit();

        LocalBroadcastManager.getInstance(this).registerReceiver(deviceStopStepCounter,
                new IntentFilter(Constants.STOP_DEVICE_STEP_COUNTER));

        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mStepCounterSensor) {
            new StepEventLoggerTask().execute(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class StepEventLoggerTask extends AsyncTask<SensorEvent, Void, Void> {
        @Override
        protected Void doInBackground(SensorEvent... events) {
            long timestamp = System.currentTimeMillis();
            Log.d("StepCounterSensor", "Step Detected at: " + timestamp);
            SharedPreferences.Editor editor = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).edit();
            editor.putLong(Constants.DEVICE_LAST_STEP, timestamp);
//            editor.putInt("TOTALDEVICESTEPS", getSharedPreferences(Constants.STANDDTECTORTM_SHARED_PREFERENCES, Context.MODE_PRIVATE).getInt("TOTALDEVICESTEPS", 0) + 1);
            editor.commit();

            return null;
        }
    }

    private BroadcastReceiver deviceStopStepCounter = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mSensorManager.unregisterListener(StandDtectorStepCounter.this, mStepCounterSensor);
            stopSelf();
        }
    };
}
