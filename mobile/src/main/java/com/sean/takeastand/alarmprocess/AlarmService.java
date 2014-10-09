package com.sean.takeastand.alarmprocess;

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
import android.widget.Toast;

import com.sean.takeastand.util.Constants;
import com.sean.takeastand.R;
import com.sean.takeastand.storage.AlarmSchedule;

/**
 * Created by Sean on 2014-09-18.
 */
public class AlarmService extends Service{

    private static final String TAG = "AlarmService";
    //Change to 60000 after testing
    private final long oneMinuteMillis = 60000;
    private final long fifteenSecondsMillis = 15000;
    private Handler mHandler;
    private Context mContext;
    private AlarmSchedule mCurrentAlarmSchedule;


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
        mHandler.postDelayed(oneMinuteForNotificationResponse, oneMinuteMillis);
        mCurrentAlarmSchedule = intent.getParcelableExtra(Constants.ALARM_SCHEDULE);
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
        LocalBroadcastManager.getInstance(AlarmService.this).registerReceiver(deletedAlarm,
                new IntentFilter(Constants.ALARM_SCHEDULE_DELETED));
    }

    private void unregisterReceivers(){
        getApplicationContext().unregisterReceiver(stoodUpReceiver);
        getApplicationContext().unregisterReceiver(oneMinuteReceiver);
        getApplicationContext().unregisterReceiver(fiveMinuteReceiver);
        LocalBroadcastManager.getInstance(AlarmService.this).unregisterReceiver(endAlarmService);
        LocalBroadcastManager.getInstance(AlarmService.this).unregisterReceiver(deletedAlarm);
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
            Toast.makeText(mContext, "Good job!", Toast.LENGTH_SHORT);
            mHandler.removeCallbacks(oneMinuteForNotificationResponse);
            mHandler.postDelayed(stoodUp, fifteenSecondsMillis);
        }
    };

    private BroadcastReceiver oneMinuteReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "oneMinuteReceiver");
            cancelNotification();
            setOneMinuteAlarm(mContext);
            mHandler.removeCallbacks(oneMinuteForNotificationResponse);
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
            mHandler.removeCallbacks(oneMinuteForNotificationResponse);
            //End service
            AlarmService.this.stopSelf();
        }
    };

    private BroadcastReceiver endAlarmService = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "User switched off the repeating alarm");
            mHandler.removeCallbacks(oneMinuteForNotificationResponse);
            cancelNotification();
            //End this service
            AlarmService.this.stopSelf();
        }
    };

    private BroadcastReceiver deletedAlarm = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "User deleted an Alarm Schedule.  Checking to see if that is the currently" +
                    " alarm notification.");
            int currentAlarmUID = -1;
            if(mCurrentAlarmSchedule!=null){
                currentAlarmUID = mCurrentAlarmSchedule.getUID();
            }
            int deletedAlarmUID = intent.getIntExtra("UID" , -2);
            if(deletedAlarmUID == currentAlarmUID){
                mHandler.removeCallbacks(oneMinuteForNotificationResponse);
                cancelNotification();
                //End this service
                AlarmService.this.stopSelf();
            }
        }
    };

    private Runnable oneMinuteForNotificationResponse = new Runnable() {

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
        RepeatingAlarmController repeatingAlarmController =
                new RepeatingAlarmController(context, mCurrentAlarmSchedule);
        repeatingAlarmController.setOneMinuteAlarm();
    }

    private void setFiveMinuteAlarm(Context context){
        RepeatingAlarmController repeatingAlarmController =
                new RepeatingAlarmController(context, mCurrentAlarmSchedule);
        repeatingAlarmController.setFiveMinuteAlarm();
    }

    private void setStoodUpAlarm(Context context){
        RepeatingAlarmController repeatingAlarmController =
                new RepeatingAlarmController(context, mCurrentAlarmSchedule);
        repeatingAlarmController.setNewScheduledRepeatingAlarm();
    }
}
