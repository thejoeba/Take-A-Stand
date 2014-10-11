package com.sean.takeastand.alarmprocess;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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

import com.sean.takeastand.ui.MainActivity;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.R;
import com.sean.takeastand.storage.AlarmSchedule;
import com.sean.takeastand.util.Utils;

/**
 * Created by Sean on 2014-09-18.
 */
public class AlarmService extends Service{

    private static final String TAG = "AlarmService";
    //Change to 60000 after testing
    private final long oneMinuteMillis = 60000;
    private final long fiveSecondsMillis = 5000;
    private final long thirtySecondsMillis = 10000;
    private Handler mHandler;
    private Context mContext;
    private AlarmSchedule mCurrentAlarmSchedule;
    private int mNotifTimePassed = 0;


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
        if(intent.hasExtra(Constants.ALARM_SCHEDULE)){
            mCurrentAlarmSchedule = intent.getParcelableExtra(Constants.ALARM_SCHEDULE);
        }
        Utils.setCurrentMainActivityImage(mContext, Constants.SCHEDULE_TIME_TO_STAND);
        Utils.notifyImageUpdate(mContext);
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
            Toast.makeText(mContext, "Good job!", Toast.LENGTH_SHORT).show();
            mHandler.removeCallbacks(oneMinuteForNotificationResponse);
            mHandler.postDelayed(stoodUp, fiveSecondsMillis);
            Utils.setCurrentMainActivityImage(mContext, Constants.SCHEDULE_STOOD_UP);
            Utils.notifyImageUpdate(mContext);
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
            updateNotification();
            mHandler.postDelayed(oneMinuteForNotificationResponse, oneMinuteMillis);
        }
    };

    private Runnable stoodUp = new Runnable() {
        @Override
        public void run() {
            AlarmService.this.stopSelf();
            setStoodUpAlarm(mContext);
            mHandler.postDelayed(changeImage, thirtySecondsMillis);
        }
    };

    private Runnable changeImage= new Runnable() {
        @Override
        public void run() {
            Utils.setCurrentMainActivityImage(mContext, Constants.SCHEDULE_RUNNING);
            Utils.notifyImageUpdate(mContext);
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

    private void updateNotification(){
        mNotifTimePassed ++;
        Log.i(TAG, "time since first notification: " + mNotifTimePassed + setMinutes(mNotifTimePassed));
        NotificationManager notificationManager = (NotificationManager)mContext.getSystemService(
                Context.NOTIFICATION_SERVICE);
        //Cancel the previous one so ticker text shows again
        notificationManager.cancel(R.integer.AlarmNotificationID);
        //Make intents
        Intent launchActivity = new Intent(mContext, MainActivity.class);
        PendingIntent launchActivityPendingIntent = PendingIntent.getActivity(mContext, 0,
                launchActivity, 0);
        Intent stoodUpIntent = new Intent("StoodUp");
        PendingIntent stoodUpPendingIntent = PendingIntent.getBroadcast(mContext, 0,
                stoodUpIntent, 0);
        Intent oneMinuteIntent = new Intent("OneMinute");
        PendingIntent oneMinutePendingIntent = PendingIntent.getBroadcast(mContext, 0,
                oneMinuteIntent, 0);
        Intent fiveMinuteIntent = new Intent("FiveMinute");
        PendingIntent fiveMinutePendingIntent = PendingIntent.getBroadcast(mContext, 0,
                fiveMinuteIntent, 0);

        Notification alarmNotification = new Notification.InboxStyle(
                new Notification.Builder(mContext)
                        .setContentTitle("Take A Stand")
                        .setContentText("Time to stand up: " + mNotifTimePassed +
                                setMinutes(mNotifTimePassed))
                        .setContentIntent(launchActivityPendingIntent)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setAutoCancel(false)
                        .setOngoing(true)
                        .addAction(android.R.drawable.btn_default, "Stood Up", stoodUpPendingIntent)
                        .addAction(android.R.drawable.btn_default, "1 More Minute",
                                oneMinutePendingIntent)
                        .addAction(android.R.drawable.btn_default, "5 More Minutes",
                                fiveMinutePendingIntent)
                        .setTicker("Time to stand up"))
                .build();
        notificationManager.notify(R.integer.AlarmNotificationID, alarmNotification);
    }

    private String setMinutes(int minutes){
        if(minutes > 1 ){
            return " minutes ago";
        } else {
            return " minute ago";
        }
    }

    private void setOneMinuteAlarm(Context context){
        RepeatingAlarmController repeatingAlarmController;
        if(mCurrentAlarmSchedule==null){
            repeatingAlarmController = new RepeatingAlarmController(context);
        } else {
            repeatingAlarmController = new RepeatingAlarmController(context, mCurrentAlarmSchedule);
        }
        repeatingAlarmController.setOneMinuteAlarm();
    }

    private void setFiveMinuteAlarm(Context context){
        RepeatingAlarmController repeatingAlarmController;
        if(mCurrentAlarmSchedule==null){
            repeatingAlarmController = new RepeatingAlarmController(context);
        } else {
            repeatingAlarmController = new RepeatingAlarmController(context, mCurrentAlarmSchedule);
        }
        repeatingAlarmController.setFiveMinuteAlarm();
    }

    private void setStoodUpAlarm(Context context){
        RepeatingAlarmController repeatingAlarmController;
        if(mCurrentAlarmSchedule==null){
            repeatingAlarmController = new RepeatingAlarmController(context);
            repeatingAlarmController.setNonScheduleRepeatingAlarm();
        } else {
            repeatingAlarmController = new RepeatingAlarmController(context, mCurrentAlarmSchedule);
            repeatingAlarmController.setNewScheduledRepeatingAlarm();
        }
    }
}
