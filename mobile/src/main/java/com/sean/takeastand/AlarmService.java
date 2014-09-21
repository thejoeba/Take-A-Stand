package com.sean.takeastand;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by Sean on 2014-09-18.
 */
public class AlarmService extends Service{

    private static final String TAG = "AlarmService";
    //Change to 60000 after testing
    private final long oneMinuteMillis = 10000;
    private final long fifteenSecondsMillis = 15000;
    Handler mHandler;
    Context mContext;


    @Override
    public void onCreate() {
        super.onCreate();
        registerReceivers();
        mHandler = new Handler();
        mContext = getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "AlarmService started");
        mHandler.postDelayed(oneMinuteForNotifResponse, oneMinuteMillis);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceivers();
        Log.i(TAG, "AlarmService destroyed");
    }

    private void registerReceivers(){
        getApplicationContext().registerReceiver(stoodUpReceiver,
                new IntentFilter("StoodUp"));
        getApplicationContext().registerReceiver(oneMinuteReceiver,
                new IntentFilter("OneMinute"));
        getApplicationContext().registerReceiver(fiveMinuteReceiver,
                new IntentFilter("FiveMinute"));
        LocalBroadcastManager.getInstance(AlarmService.this).registerReceiver(endAlarmService,
                new IntentFilter("userSwitchedOffAlarm"));
    }

    private void unregisterReceivers(){
        getApplicationContext().unregisterReceiver(stoodUpReceiver);
        getApplicationContext().unregisterReceiver(oneMinuteReceiver);
        getApplicationContext().unregisterReceiver(fiveMinuteReceiver);
        LocalBroadcastManager.getInstance(AlarmService.this).unregisterReceiver(endAlarmService);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private BroadcastReceiver stoodUpReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "stoodUpReceiver");
            cancelNotification();
            mHandler.removeCallbacks(oneMinuteForNotifResponse);
            mHandler.postDelayed(stoodUp, fifteenSecondsMillis);
        }
    };

    private BroadcastReceiver oneMinuteReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "oneMinuteReceiver");
            cancelNotification();
            setOneMinuteAlarm(mContext);
            mHandler.removeCallbacks(oneMinuteForNotifResponse);
            //End service
            AlarmService.this.stopSelf();
        }
    };

    private BroadcastReceiver fiveMinuteReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "fiveMinuteReceiver");
            cancelNotification();
            setFiveMinuteAlarm(mContext);
            mHandler.removeCallbacks(oneMinuteForNotifResponse);
            //End service
            AlarmService.this.stopSelf();
        }
    };

    private BroadcastReceiver endAlarmService = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "User switched off the repeating alarm");
            mHandler.removeCallbacks(oneMinuteForNotifResponse);
            cancelNotification();
            //End this service
            AlarmService.this.stopSelf();
        }
    };

    private Runnable oneMinuteForNotifResponse = new Runnable() {

        public void run() {
            cancelNotification();
            setOneMinuteAlarm(mContext);
            //End this service
            AlarmService.this.stopSelf();
        }
    };

    private Runnable stoodUp = new Runnable() {
        @Override
        public void run() {
            setStoodUpAlarm(mContext);
            //End this service
            AlarmService.this.stopSelf();
        }
    };

    private void cancelNotification(){
        Log.i(TAG, "Notification removed");
        NotificationManager notificationManager = (NotificationManager)mContext.getSystemService(
                Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(R.integer.AlarmNotificationID);
    }

    private void setOneMinuteAlarm(Context context){
        RepeatingAlarmController repeatingAlarmController = new RepeatingAlarmController(context);
        repeatingAlarmController.setOneMinuteAlarm();
    }

    private void setFiveMinuteAlarm(Context context){
        RepeatingAlarmController repeatingAlarmController = new RepeatingAlarmController(context);
        repeatingAlarmController.setFiveMinuteAlarm();
    }

    private void setStoodUpAlarm(Context context){
        RepeatingAlarmController repeatingAlarmController = new RepeatingAlarmController(context);
        repeatingAlarmController.setNewAlarm();
    }
}
