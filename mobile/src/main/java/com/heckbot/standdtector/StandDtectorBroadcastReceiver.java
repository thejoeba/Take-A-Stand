package com.heckbot.standdtector;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by Joey on 11/23/2014.
 */
public class StandDtectorBroadcastReceiver extends android.content.BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //ToDo: Remove this class, it's only used to accept calibration results, and vibrate when received. move to settings activity.
        Log.d("MyBroadcastReceiver", "Intent Received");
        String action = intent.getAction();
        if (action.equals("CALIBRATION_FINISHED")) {
            Log.d("MyBroadcastReceiver", action);
        }
        else if (action.equals("STOOD_RESULTS")) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String result = extras.getString("RESULT");
                if(result.equals("STAND_DETECTED")){
                    Vibrator v = (Vibrator)context.getSystemService(context.VIBRATOR_SERVICE);
                    v.vibrate(250);
                }
                Log.d("MyBroadcastReceiver", result);
            }
        }
        else if (action.equals("LastStep")) {
            Log.d("MyBroadcastReceiver", action);
        }
    }
}