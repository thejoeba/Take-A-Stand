package com.heckbot.standdtector;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.Time;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;

public class StandDtectorTM extends IntentService implements SensorEventListener, GoogleApiClient.ConnectionCallbacks {
    private SensorManager mSensorManager;
    private Sensor mRotationSensor;
    private Sensor mLightSensor;
    private List<Float> RotXList;
    private List<Float> RotYList;
    private List<Float> RotZList;
    private List<Float> RotXListOld;
    private List<Float> RotYListOld;
    private List<Float> RotZListOld;
    public float RotX;
    public float RotY;
    public float RotZ;
    public float RotXAverage;
    public float RotYAverage;
    public float RotZAverage;
    public float RotXAverageOld;
    public float RotYAverageOld;
    public float RotZAverageOld;
    public float flCalibratedVariation;
    public boolean bDetectedStand = false;

    public float mLux;

    private List<Float> PreCalRotXList;
    private List<Float> PreCalRotYList;
    private List<Float> PreCalRotZList;

    private float PreCalRotXAverage;
    private float PreCalRotYAverage;
    private float PreCalRotZAverage;

    private float[] RotXMinMax = new float[2];
    private float[] RotYMinMax = new float[2];
    private float[] RotZMinMax = new float[2];

    private Time tCalibrationReady = new Time();
    private Time tCalibrationStart = new Time();
    private Time tSensorEnd = new Time();

    public float LargestVariation;

    private boolean bStartCalibration = false;
    public boolean bCalibrate = false;
    public boolean bPocketDetected = false;

    private Vibrator vibration;

    SharedPreferences sharedPref;

    PendingIntent pendingIntent;
    Intent returnIntent;

    public static final String PATH_GET_STEP = "/getsteptime";
    GoogleApiClient mGoogleApiClient;
    Handler wearResultsTimoutHandler;

    public StandDtectorTM() {
        super("StandDtectorTM");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        sharedPref = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        String action = intent.getAction();
        pendingIntent = intent.getParcelableExtra(Constants.PENDING_INTENT);
        returnIntent = new Intent();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        if (action.equals(Constants.DEVICE_LAST_STEP)) {
            long timestamp = sharedPref.getLong(Constants.DEVICE_LAST_STEP, -1);
            //determine time(ms) since last step
            if (timestamp > 86400000) {
                timestamp = (System.currentTimeMillis() - timestamp);
            }

            if (timestamp <= 0) {
                returnIntent.putExtra(Constants.DEVICE_STEP_HARDWARE, false);
                try {
                    pendingIntent.send(this, 0, returnIntent);
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
                stopSelf();
            }
            //ToDo:Remove returnIntent setAction where applicable
//            returnIntent.setAction(Constants.DEVICE_LAST_STEP);
            returnIntent.putExtra(Constants.DEVICE_STEP_HARDWARE, true);
            returnIntent.putExtra(Constants.LAST_STEP, timestamp);
            try {
                pendingIntent.send(this, 0, returnIntent);
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        } else if (action.equals(Constants.WEAR_LAST_STEP)) {
            LocalBroadcastManager.getInstance(this).registerReceiver(wearLastStepResults,
                    new IntentFilter(Constants.WEAR_LAST_STEP_RESULTS));
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .build();
            mGoogleApiClient.connect();

            wearResultsTimoutHandler = new Handler();
            wearResultsTimoutHandler.postDelayed(wearResultsTimout, 9000);
        } else if (action.equals(Constants.START_DEVICE_STEP_COUNTER)) {
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)) {
                Intent intentStepCounter = new Intent(this, StandDtectorStepCounter.class);
                startService(intentStepCounter);
            }
            stopSelf();
        } else if (action.equals(Constants.STOP_DEVICE_STEP_COUNTER)) {
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)) {
                Intent intentStepCounter = new Intent(this, StandDtectorStepCounter.class);
                intentStepCounter.setAction(Constants.STOP_DEVICE_STEP_COUNTER);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intentStepCounter);
            }
            stopSelf();
        } else if (action.equals(Constants.CALIBRATE)) {
            InitializeStandSensors();
            CalibrateSensor();
        } else if (action.equals(Constants.STANDDTECTOR_START)) {
            InitializeStandSensors();
            StartSensor(extras.getLong("MILLISECONDS", 0));
            LocalBroadcastManager.getInstance(this).registerReceiver(standDtectorTMStop,
                    new IntentFilter(Constants.STANDDTECTOR_STOP));
        } else if (action.equals("STOP")) {
            StopSensor();
            stopSelf();
        }
    }

    private void InitializeStandSensors() {
        vibration = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR) != null) {
            mRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
        } else {
            mRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        }

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
            mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        } else {
            bPocketDetected = true;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Time tNow = new Time();
        tNow.set(System.currentTimeMillis());
        if (Time.compare(tSensorEnd, tNow) < 0) {
            StopSensor();
            if (!bCalibrate) {
                returnIntent.setAction(Constants.STOOD_RESULTS);
                returnIntent.putExtra(Constants.STAND_DETECTOR_RESULT, "EXPIRED");
            } else {
                long[] pattern = {0, 250, 250, 250, 250, 250, 250, 250};
                vibration.vibrate(pattern, -1);
                returnIntent.putExtra("Results", "EXPIRED");
            }
            try {
                pendingIntent.send(this, 0, returnIntent);
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
            stopSelf();
        } else if (event.sensor == mLightSensor) {
            mLux = event.values[0];
            if (mLux <= 3f) {
                bPocketDetected = true;
            } else {
                bPocketDetected = false;
            }
        } else if (event.sensor == mRotationSensor) {
            if (bCalibrate) {
                tNow.set(System.currentTimeMillis());
                if (!bStartCalibration) {
                    if (tCalibrationReady.year == 1969) {
                        tCalibrationReady.set(System.currentTimeMillis() + 5000);
                    } else if (Time.compare(tCalibrationReady, tNow) < 0) {
                        if (bPocketDetected) {
                            bStartCalibration = true;
                        } else {
                            tCalibrationReady.set(System.currentTimeMillis() + 5000);
                        }
                    }
                } else {
                    if (PreCalRotXList.size() < 20) {
                        PreCalRotXList.add(event.values[0]);
                        PreCalRotYList.add(event.values[1]);
                        PreCalRotZList.add(event.values[2]);
                    }
                    //ToDo: This year comparison sucks
                    else if (tCalibrationStart.year == 1969) {
                        PreCalRotXAverage = calculateAverage(PreCalRotXList);
                        PreCalRotYAverage = calculateAverage(PreCalRotYList);
                        PreCalRotZAverage = calculateAverage(PreCalRotZList);

                        vibration.vibrate(500);

                        tCalibrationStart.set(System.currentTimeMillis() + 5000);
                    } else {
                        if (event.values[0] < RotXMinMax[0]) RotXMinMax[0] = event.values[0];
                        else if (event.values[0] > RotXMinMax[1]) RotXMinMax[1] = event.values[0];
                        if (event.values[1] < RotYMinMax[0]) RotYMinMax[0] = event.values[1];
                        else if (event.values[1] > RotYMinMax[1]) RotYMinMax[1] = event.values[1];
                        if (event.values[2] < RotZMinMax[0]) RotZMinMax[0] = event.values[2];
                        else if (event.values[2] > RotZMinMax[1]) RotZMinMax[1] = event.values[2];

                        if (Time.compare(tCalibrationStart, tNow) < 0) {
                            float[] differences = new float[6];
                            differences[0] = PreCalRotXAverage - RotXMinMax[0];
                            differences[1] = RotXMinMax[1] - PreCalRotXAverage;

                            differences[2] = PreCalRotYAverage - RotYMinMax[0];
                            differences[3] = RotYMinMax[1] - PreCalRotYAverage;

                            differences[4] = PreCalRotZAverage - RotZMinMax[0];
                            differences[5] = RotZMinMax[1] - PreCalRotZAverage;

                            LargestVariation = FindLargestValue(differences);

                            bCalibrate = false;
                            long[] pattern = {0, 250, 250, 250, 250, 250, 250, 250};
                            vibration.vibrate(pattern, -1);
                            StopSensor();
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putFloat("CALIBRATEDVARIATION", LargestVariation * .5f);
                            editor.commit();
                            returnIntent.putExtra("Results", "Success");
                            try {
                                pendingIntent.send(this, 0, returnIntent);
                            } catch (PendingIntent.CanceledException e) {
                                e.printStackTrace();
                            }
                            stopSelf();
                        }
                    }
                }
            } else if (bPocketDetected) {
                RotX = event.values[0];
                RotY = event.values[1];
                RotZ = event.values[2];

                RotXList.add(event.values[0]);
                RotYList.add(event.values[1]);
                RotZList.add(event.values[2]);

                if (RotXList.size() > 5) {
                    RotXListOld.add(RotXList.get(0));
                    RotXList.remove(0);
                    if (RotXListOld.size() > 20) RotXListOld.remove(0);
                }
                if (RotYList.size() > 5) {
                    RotYListOld.add(RotYList.get(0));
                    RotYList.remove(0);
                    if (RotYListOld.size() > 20) RotYListOld.remove(0);
                }
                if (RotZList.size() > 5) {
                    RotZListOld.add(RotZList.get(0));
                    RotZList.remove(0);
                    if (RotZListOld.size() > 20) RotZListOld.remove(0);
                }

                RotXAverage = calculateAverage(RotXList);
                RotYAverage = calculateAverage(RotYList);
                RotZAverage = calculateAverage(RotZList);

                RotXAverageOld = calculateAverage(RotXListOld);
                RotYAverageOld = calculateAverage(RotYListOld);
                RotZAverageOld = calculateAverage(RotZListOld);

                if (RotXListOld.size() >= 10) {
                    if (RotXAverage > RotXAverageOld + flCalibratedVariation ||
                            RotXAverage < RotXAverageOld - flCalibratedVariation) {
                        bDetectedStand = true;
                    }
                    if (RotYAverage > RotYAverageOld + flCalibratedVariation ||
                            RotYAverage < RotYAverageOld - flCalibratedVariation) {
                        bDetectedStand = true;
                    }
                    if (RotZAverage > RotZAverageOld + flCalibratedVariation ||
                            RotZAverage < RotZAverageOld - flCalibratedVariation) {
                        bDetectedStand = true;
                    }

                    if (bDetectedStand) {
                        StopSensor();
                        try {
                            returnIntent.setAction(Constants.STOOD_RESULTS);
                            returnIntent.putExtra(Constants.STAND_DETECTOR_RESULT,
                                    Constants.STAND_DETECTED);
                            pendingIntent.send(this, 0, returnIntent);
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                        stopSelf();
                    }
                }
            } else {
                try {
                    StopSensor();
                    returnIntent.setAction(Constants.STOOD_RESULTS);
                    returnIntent.putExtra(Constants.STAND_DETECTOR_RESULT, "FAILED_POCKET");
                    pendingIntent.send(this, 0, returnIntent);
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
                stopSelf();
            }
        }
    }

    private float calculateAverage(List<Float> flList) {
        float sum = 0;
        if (!flList.isEmpty()) {
            for (Float flItem : flList) {
                sum += flItem;
            }
            return sum / flList.size();
        }
        return sum;
    }

    private float FindLargestValue(float[] flArray) {

        float maxVal = 0f;
        for (Float flVal : flArray) {
            if (flVal > maxVal) maxVal = flVal;
        }

        return maxVal;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void StartSensor(long milliseconds) {
        flCalibratedVariation = sharedPref.getFloat("CALIBRATEDVARIATION", .35f);
        bDetectedStand = false;

        RotXList = new ArrayList<Float>();
        RotYList = new ArrayList<Float>();
        RotZList = new ArrayList<Float>();

        PreCalRotXList = new ArrayList<Float>();
        PreCalRotYList = new ArrayList<Float>();
        PreCalRotZList = new ArrayList<Float>();

        RotXListOld = new ArrayList<Float>();
        RotYListOld = new ArrayList<Float>();
        RotZListOld = new ArrayList<Float>();

        tSensorEnd.set(System.currentTimeMillis() + milliseconds);
        mSensorManager.registerListener(this, mRotationSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mLightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void StopSensor() {
        mSensorManager.unregisterListener(this, mRotationSensor);
        mSensorManager.unregisterListener(this, mLightSensor);
    }

    public void CalibrateSensor() {
        PreCalRotXList = new ArrayList<Float>();
        PreCalRotYList = new ArrayList<Float>();
        PreCalRotZList = new ArrayList<Float>();
        tCalibrationStart.set(0);
        bStartCalibration = false;
        bCalibrate = true;
        StartSensor(15000);
    }

    @Override
    public void onConnected(Bundle bundle) {
        sendMessage(PATH_GET_STEP, "");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private void sendMessage(final String path, final String text) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                for (Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, node.getId(), path, text.getBytes()).await();
                }
            }
        }).start();
    }

    private Runnable wearResultsTimout = new Runnable() {

        public void run() {
            //wear results timeout
            returnIntent.putExtra(Constants.WEAR_STEP_HARDWARE, false);
            try {
                pendingIntent.send(StandDtectorTM.this, 0, returnIntent);
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
            stopSelf();
        }
    };

    private BroadcastReceiver wearLastStepResults = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.WEAR_LAST_STEP_RESULTS)) {
                wearResultsTimoutHandler.removeCallbacks(wearResultsTimout);
                Bundle extras = intent.getExtras();
                long timestamp = extras.getLong("timestamp", -1);
                Log.d("WearLastStepResults", "Event Mills: " + timestamp);
                Time tNow = new Time();
                tNow.set(System.currentTimeMillis());
                //determine time(ms) since last step
                if (timestamp > 86400000) {
                    timestamp = (tNow.toMillis(false) - timestamp);
                }

                if (timestamp <= 0) {
                    returnIntent.putExtra(Constants.WEAR_STEP_HARDWARE, false);
                    try {
                        pendingIntent.send(StandDtectorTM.this, 0, returnIntent);
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                    stopSelf();
                }
//                returnIntent.setAction(Constants.WEAR_LAST_STEP);
                returnIntent.putExtra(Constants.WEAR_STEP_HARDWARE, true);
                returnIntent.putExtra("Wear_Last_Step", timestamp);
                try {
                    pendingIntent.send(StandDtectorTM.this, 0, returnIntent);
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
                LocalBroadcastManager.getInstance(StandDtectorTM.this).unregisterReceiver(wearLastStepResults);
                stopSelf();
            }
        }
    };

    private BroadcastReceiver standDtectorTMStop = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.STANDDTECTOR_STOP)) {
                StopSensor();
                LocalBroadcastManager.getInstance(StandDtectorTM.this).unregisterReceiver(standDtectorTMStop);
                stopSelf();
            }
        }
    };

}
