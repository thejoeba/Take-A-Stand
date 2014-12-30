package com.heckbot.standdtector;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by carlos on 7/15/14.
 */
public class WearResultsListenerService extends WearableListenerService {
    public static final String PATH_REPLY_STEP = "/replystep";
    public static final String PATH_REPLY_FAILED = "/replyfailed";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d("onMessageReceived", "Message Received");
        Intent stepDetectorIntent = new Intent(this, com.heckbot.standdtector.StandDtectorTM.class);
        stepDetectorIntent.setAction(Constants.WEAR_LAST_STEP_RESULTS);
        stepDetectorIntent.putExtra("WearResults", true);

        if (messageEvent.getPath().equals(PATH_REPLY_STEP)) {
            Long lData = Long.parseLong(new String(messageEvent.getData()));
            Log.d("onMessageReceived", "Last step: " + lData);
            stepDetectorIntent.putExtra("timestamp", lData);
        } else if (messageEvent.getPath().equals(PATH_REPLY_FAILED)) {
            Log.d("onMessageReceived", new String(messageEvent.getData()));
            stepDetectorIntent.putExtra("timestamp", -1);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(stepDetectorIntent);
        stopSelf();
    }
}