package com.heckbot.standdtector;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by carlos on 7/15/14.
 */
public class StandDtectorListenerService extends WearableListenerService {
    public static final String PATH_GET_STEP = "/getsteptime";
    public static final String PATH_REPLY_STEP = "/replystep";
    public static final String PATH_REPLY_FAILED = "/replyfailed";

    private Handler mHandler;
    private boolean bStepCounterHandled;

    String nodeId;
    GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if(messageEvent.getPath().equals(PATH_GET_STEP)) {
            nodeId = messageEvent.getSourceNodeId();
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .build();
            mGoogleApiClient.connect();

            getApplicationContext().registerReceiver(lastStepReceiver, new IntentFilter("LastStep"));

            Intent stepDetectorIntent = new Intent(this, com.heckbot.standdtector.StandDtectorJRTM.class);
            stepDetectorIntent.setAction("LastStep");
            Intent returnIntent = new Intent("LastStep");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, returnIntent, PendingIntent.FLAG_ONE_SHOT);
            stepDetectorIntent.putExtra("pendingIntent", pendingIntent);
            bStepCounterHandled = false;
            startService(stepDetectorIntent);
            //error handling if no step detector results are returned.
            mHandler = new Handler();
            int fiveSecondMillis = 5000;

            Runnable lastStepReceiverTimeout = new StepCounterRunnable();
            mHandler.postDelayed(lastStepReceiverTimeout, fiveSecondMillis);
        }
        else {
            Log.d("onMessageReceived", "Unrecognized Path: " + messageEvent.getPath());
        }
    }

    private class StepCounterRunnable implements Runnable {

        StepCounterRunnable() {
        }

        public void run() {
            if (!bStepCounterHandled) {
                bStepCounterHandled = true;
                Intent stopStepDetectorIntent = new Intent("STOP");
                startService(stopStepDetectorIntent);
                Log.i("StepCounterRunnable", "Step Data Timeout");

                replyMessage(PATH_REPLY_FAILED,  "Step Data Timeout");
            }
        }
    }

    private BroadcastReceiver lastStepReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("lastStepReceiver", "Step Data Received");
            if (!bStepCounterHandled) {
                bStepCounterHandled = true;
                Bundle extras = intent.getExtras();
                boolean bHasStepHardware = extras.getBoolean("Step_Hardware");
                if (bHasStepHardware) {
                    long lLastStep = extras.getLong("Last_Step");
                    Log.i("lastStepReceiver", "Last Step: " + lLastStep);
                    replyMessage(PATH_REPLY_STEP,  Long.toString(lLastStep));
                }
                else{
                    Log.i("lastStepReceiver", "No Step Hardware");
                    replyMessage(PATH_REPLY_FAILED,  "No Step Hardware");
                }
            }
            else{
                Log.d("lastStepReceiver", "Step Listener Expired");
            }
            getApplicationContext().unregisterReceiver(lastStepReceiver);
        }
    };

    private void replyMessage( final String path, final String text ) {
        Wearable.MessageApi.sendMessage(mGoogleApiClient, nodeId, path, text.getBytes());
        Log.d("SendMessage", "Sent path: " + path);
        mGoogleApiClient.disconnect();
        stopSelf();
    }

}