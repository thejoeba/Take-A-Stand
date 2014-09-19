package com.sean.takeastand;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

/**
 * Created by Sean on 2014-09-18.
 */
public class AlarmService extends Service{

    private static final String TAG = "AlarmService";
    private final long oneMinuteMillis = 60000;
    Handler mHandler;


    @Override
    public void onCreate() {
        Log.i(TAG, "Service started");
        super.onCreate();
        registerReceivers();
        mHandler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mHandler.postDelayed(oneMinuteForUserResponse, oneMinuteMillis);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceivers();
    }

    private void registerReceivers(){
        getApplicationContext().registerReceiver(stoodUpReceiver,
                new IntentFilter("StoodUp"));
        getApplicationContext().registerReceiver(oneMinuteReceiver,
                new IntentFilter("OneMinute"));
        getApplicationContext().registerReceiver(fiveMinuteReceiver,
                new IntentFilter("FiveMinute"));
    }

    private void unregisterReceivers(){
        getApplicationContext().unregisterReceiver(stoodUpReceiver);
        getApplicationContext().unregisterReceiver(oneMinuteReceiver);
        getApplicationContext().unregisterReceiver(fiveMinuteReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private BroadcastReceiver stoodUpReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "stoodUpReceiver");

            //Set new stood up alarm, in 30 seconds
            //End service
        }
    };

    private BroadcastReceiver oneMinuteReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "oneMinuteReceiver");
            //Set one minute alarm
            //End service
        }
    };

    private BroadcastReceiver fiveMinuteReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "fiveMinuteReceiver");
            //Set five minute alarm
            //End service
        }
    };

    private Runnable oneMinuteForUserResponse = new Runnable() {

        public void run() {
            //End notification
            //Set new alarm for one minute or five minutes
            //End service
        }
    };
}
