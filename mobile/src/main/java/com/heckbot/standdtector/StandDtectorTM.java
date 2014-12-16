package com.heckbot.standdtector;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.text.format.Time;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.sean.takeastand.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class StandDtectorTM extends IntentService implements SensorEventListener, GoogleApiClient.ConnectionCallbacks {
    private SensorManager mSensorManager;
    private Sensor mRotationSensor;
    private Sensor mLightSensor;
    private Sensor mStepCounterSensor;
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
    public String SensorMethod;

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
    boolean bWaitingWearResults = false;

    public StandDtectorTM() {
        super("StandDtectorTM");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            sharedPref = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
            String action = intent.getAction();
            Log.d("Intent", action);
            if (!intent.getBooleanExtra("WearResults", false)) {
                pendingIntent = intent.getParcelableExtra("pendingIntent");
                returnIntent = new Intent();
            }
            else if (!bWaitingWearResults) {
                // Wear results returned, but service was not expecting them
                stopSelf();
            }
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

            if (action.equals("LastStep")) {
                if (getPackageManager().hasSystemFeature(getPackageManager().FEATURE_SENSOR_STEP_COUNTER)){
                    Log.d("Step_Counter","Step Counter Available");
                    mStepCounterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
                    mSensorManager.registerListener(this, mStepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
                }
                else {
                    Log.d("Step_Counter","No Step Counter Available");
                    returnIntent.putExtra("Step_Hardware", false);
                    try {
                        pendingIntent.send(this, 0 , returnIntent);
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                    stopSelf();
                }
            }
            else if (action.equals("WearLastStep")) {
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addApi(Wearable.API)
                        .addConnectionCallbacks(this)
                        .build();
                mGoogleApiClient.connect();

                Handler mHandler = new Handler();
                mHandler.postDelayed(wearResultsTimout, 9000);
            }
            else if (action.equals("WearLastStepResults")) {
                bWaitingWearResults = false;
                long timestamp = extras.getLong("timestamp", -1);
                Log.d("WearLastStepResults", "Event Mills: " + timestamp);
                Time tNow = new Time();
                tNow.set(System.currentTimeMillis());
                //determine time(ms) since last step
                if (timestamp > 86400000) {
                    timestamp = (tNow.toMillis(false) - timestamp);
                }

                if (timestamp <= 0 ) {
                    Log.d("WearLastStepResults","No Timestamp Available");
                    returnIntent.putExtra("Wear_Step_Hardware", false);
                    try {
                        pendingIntent.send(this, 0 , returnIntent);
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                    stopSelf();
                }
                Log.d("WearLastStepResults", "Last Step: " + (timestamp / 1000) + " seconds ago");
                returnIntent.setAction("WearLastStep");
                returnIntent.putExtra("Wear_Step_Hardware", true);
                returnIntent.putExtra("Wear_Last_Step", timestamp);
                try {
                    pendingIntent.send(this, 0 , returnIntent);
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
                stopSelf();
            }
            else {
                InitializeStandSensors();
                if (action.equals("CALIBRATE")) {
                    CalibrateSensor();
                } else if (action.equals("START")) {
                    StartSensor(extras.getLong("MILLISECONDS", 0));
                } else if (action.equals("STOP")) {
                    StopSensor();
                    stopSelf();
                }
            }
        }
    }

    private void InitializeStandSensors() {
        vibration = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR) != null){
            mRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
            SensorMethod = "Geomagnetic Rotation Sensor";
        }
        else {
            mRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            SensorMethod = "Rotation Sensor";
        }

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null){
            mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        }
        else {
            bPocketDetected = true;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long lUptime = SystemClock.uptimeMillis();
        Time tNow = new Time();
        tNow.set(System.currentTimeMillis());
        if (event.sensor == mStepCounterSensor) {
            StopSensor();
            long timestamp = event.timestamp / 1000000L;
            //determine if timestamp is erroneously reporting uptime
            if ((lUptime - 2) <= timestamp && timestamp <= lUptime){
                Log.d("Step_Counter","No Timestamp Available");
                returnIntent.putExtra("Step_Hardware", false);
                try {
                    pendingIntent.send(this, 0 , returnIntent);
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
                stopSelf();
            }
            Log.d("SensorEvent", "Event Mills: " + timestamp);
            //determine time(ms) since last step
            if (timestamp > 86400000) {
                timestamp = (tNow.toMillis(false) - timestamp);
            }

            if (timestamp <= 0 ) {
                Log.d("Step_Counter","No Timestamp Available");
                returnIntent.putExtra("Step_Hardware", false);
                try {
                    pendingIntent.send(this, 0 , returnIntent);
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
                stopSelf();
            }
            Log.d("SensorEvent", "Last Step: " + (timestamp / 1000) + " seconds ago");
            returnIntent.setAction("LastStep");
            returnIntent.putExtra("Step_Hardware", true);
            returnIntent.putExtra("Last_Step", timestamp);
            try {
                pendingIntent.send(this, 0 , returnIntent);
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
        else if (Time.compare(tSensorEnd,tNow) < 0) {
            StopSensor();
            if(!bCalibrate) {
                try {
//                    Log.d("StandSensor", "Expired");
                    returnIntent.setAction(Constants.STOOD_RESULTS);
                    returnIntent.putExtra(Constants.STAND_DETECTOR_RESULT, "EXPIRED");
                    pendingIntent.send(this, 0 , returnIntent);
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
                stopSelf();
            }
        }
        else if (event.sensor == mLightSensor) {
            mLux = event.values[0];
            if (mLux <= 3f) {
                bPocketDetected = true;
            }
            else {
                bPocketDetected = false;
            }
        }
        else if (event.sensor == mRotationSensor) {
            if (bCalibrate) {
                tNow.set(System.currentTimeMillis());
                if (!bStartCalibration) {
                    if (tCalibrationReady.year == 1969) {
                        tCalibrationReady.set(System.currentTimeMillis() + 5000);
                    } else if (Time.compare(tCalibrationReady, tNow) < 0) {
                        if (bPocketDetected) {
                            bStartCalibration = true;
                        }
                        else {
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
                            try {
//                                Log.d("Calibrate","Calibrate Finished");
                                returnIntent.setAction("CALIBRATION_FINISHED");
                                pendingIntent.send(this, 0 , returnIntent);
                            } catch (PendingIntent.CanceledException e) {
                                e.printStackTrace();
                            }
                            stopSelf();
                        }
                    }
                }
            }
            else if (bPocketDetected) {
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

                    if(bDetectedStand){
                        StopSensor();
                        try {
//                            Log.d("StandSensor", "Stood");
                            returnIntent.setAction(Constants.STOOD_RESULTS);
                            returnIntent.putExtra(Constants.STAND_DETECTOR_RESULT,
                                    Constants.STAND_DETECTED);
                            pendingIntent.send(this, 0 ,returnIntent);
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                        stopSelf();
                    }
                }
            }
            else {
                try {
                    StopSensor();
                    returnIntent.setAction(Constants.STOOD_RESULTS);
                    returnIntent.putExtra(Constants.STAND_DETECTOR_RESULT, "FAILED_POCKET");
                    pendingIntent.send(this, 0 ,returnIntent);
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
                stopSelf();
            }
        }
    }

    private float calculateAverage(List <Float> flList) {
        float sum = 0;
        if(!flList.isEmpty()) {
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
        flCalibratedVariation = sharedPref.getFloat("CALIBRATEDVARIATION", 0f);
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
        mSensorManager.registerListener(this, mRotationSensor,SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mLightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void StopSensor() {
        mSensorManager.unregisterListener(this, mRotationSensor);
        mSensorManager.unregisterListener(this, mLightSensor);
        mSensorManager.unregisterListener(this, mStepCounterSensor);
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
        Log.d("onConnected", "Connected");
        bWaitingWearResults = true;
        sendMessage(PATH_GET_STEP, "" );
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private void sendMessage( final String path, final String text ) {
        new Thread( new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( mGoogleApiClient ).await();
                for(Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, node.getId(), path, text.getBytes() ).await();
                    Log.d("SendMessage", "Sent path: " + path);
                }
            }
        }).start();
    }

    private Runnable wearResultsTimout = new Runnable() {

        public void run() {
            //wear results timeout
            if (bWaitingWearResults) {
                bWaitingWearResults = false;
                Log.d("WearLastStepResults","Wear Timeout");
                returnIntent.putExtra("Wear_Step_Hardware", false);
                try {
                    pendingIntent.send(StandDtectorTM.this, 0 , returnIntent);
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
                stopSelf();
            }
        }
    };

}
