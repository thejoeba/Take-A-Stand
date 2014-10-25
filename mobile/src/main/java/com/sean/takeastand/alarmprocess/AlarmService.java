/*
 * Copyright (C) 2014 Sean Allen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

import com.sean.takeastand.R;
import com.sean.takeastand.storage.AlarmSchedule;
import com.sean.takeastand.ui.MainActivity;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;

import java.util.Random;

/*This class is started by the AlarmReceiver once it receives an intent that it is time for the
user to stand.  This class takes care of notifying the user and waiting for their response.  It
creates a notification that indicates that it is time to stand up and that has two buttons for
the user to choose “stood” or “delay.” It updates the notification, minus any initial sound or
vibration, every minute in order for ticker text to remind the user that they need to stand up,
and for users who are away from their phone or with their phone on silent, how long ago they were
supposed to get up.  This class also notifies the ImageStatusFragment to change to the relevant
images. */

/**
 * Created by Sean on 2014-09-18.
 */
public class AlarmService extends Service{

    /*Note that more testing will have to be done to confirm that the LED light is not showing
    after 1 minute.

    Control LED lights directly with:

    android.provider.Settings.System.putInt(getContentResolver(), "notification_light_pulse", 1);
    1 - indicate on state 0 - indicate off state

    Have the AlarmService run this while a notification button has not been selected.
    Maybe use a handler that posts this thread until something cancels it.
    That way LED runs even while notification canceled and updated again each minute*/

    private static final String TAG = "AlarmService";
    //Change to 60000 after testing
    private final long oneMinuteMillis = 60000;


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
        sendNotification();
        mHandler.postDelayed(oneMinuteForNotificationResponse, oneMinuteMillis);
        if(intent.hasExtra(Constants.ALARM_SCHEDULE)){
            mCurrentAlarmSchedule = intent.getParcelableExtra(Constants.ALARM_SCHEDULE);
        }
        if(mCurrentAlarmSchedule==null){
            Utils.setCurrentMainActivityImage(mContext, Constants.NON_SCHEDULE_TIME_TO_STAND);
        } else {
            Utils.setCurrentMainActivityImage(mContext, Constants.SCHEDULE_TIME_TO_STAND);
        }
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
            Toast.makeText(mContext, praiseForUser(), Toast.LENGTH_SHORT).show();
            mHandler.removeCallbacks(oneMinuteForNotificationResponse);
            long tenSeconds = 10 * Constants.millisecondsInSecond;
            mHandler.postDelayed(stoodUp, tenSeconds);
            if(mCurrentAlarmSchedule==null){
                Utils.setCurrentMainActivityImage(mContext, Constants.NON_SCHEDULE_STOOD_UP);
            } else {
                Utils.setCurrentMainActivityImage(mContext, Constants.SCHEDULE_STOOD_UP);
            }
        }
    };

    private BroadcastReceiver oneMinuteReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "oneMinuteReceiver");
            cancelNotification();
            setShortBreakAlarm(mContext);
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
            setLongBreakAlarm(mContext);
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
            mHandler.removeCallbacks(stoodUp);
            mHandler.removeCallbacks(changeImage);
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
            long fifteenSeconds = 15 * Constants.millisecondsInSecond;
            mHandler.postDelayed(changeImage, fifteenSeconds);
        }
    };

    private Runnable changeImage= new Runnable() {
        @Override
        public void run() {
            if(mCurrentAlarmSchedule==null){
                Utils.setCurrentMainActivityImage(mContext, Constants.NON_SCHEDULE_ALARM_RUNNING);
            } else {
                Utils.setCurrentMainActivityImage(mContext, Constants.SCHEDULE_RUNNING);
            }
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

    private void sendNotification(){
        NotificationManager notificationManager = (NotificationManager)mContext.getSystemService(
                Context.NOTIFICATION_SERVICE);

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
        long[] vibrationPattern = {(long)500, (long)750};

        Notification alarmNotification = new Notification.InboxStyle(
                new Notification.Builder(mContext)
                        .setContentTitle("Take A Stand")
                        .setContentText("Time to stand up")
                        .setContentIntent(launchActivityPendingIntent)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setAutoCancel(false)
                        .setOngoing(true)
                        .addAction(R.drawable.ic_action_done, "Stood", stoodUpPendingIntent)
                        .addAction(R.drawable.ic_action_time, "1 Min",
                                oneMinutePendingIntent)
                        .addAction(R.drawable.ic_action_time, "5 Min",
                                fiveMinutePendingIntent)
                        //Both vibrate and set lights will be optional in the future.
                        .setVibrate(vibrationPattern)
                        .setLights(238154000, 1000, 4000)
                        .setTicker("Time to stand up"))
                        .addLine("Time to stand up")
                .build();
        notificationManager.notify(R.integer.AlarmNotificationID, alarmNotification);
    }

    /*
    Maybe the LED light will work if instead of cancelling the notification you just update it.
    The question is will the ticker text display if only updating it?
     */
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
                        .addAction(R.drawable.ic_action_done, "Stood", stoodUpPendingIntent)
                        .addAction(R.drawable.ic_action_time, "1 Min",
                                oneMinutePendingIntent)
                        .addAction(R.drawable.ic_action_time, "5 Min",
                                fiveMinutePendingIntent)
                        .setLights(238154000, 1000, 4000)
                        .setTicker("Time to stand up"))
                        .addLine("Time to stand up: " + mNotifTimePassed +
                                setMinutes(mNotifTimePassed))
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

    private void setShortBreakAlarm(Context context){
        RepeatingAlarm repeatingAlarm;
        if(mCurrentAlarmSchedule == null){
            repeatingAlarm = new UnscheduledRepeatingAlarm(context);
        } else {
            repeatingAlarm = new ScheduledRepeatingAlarm(context, mCurrentAlarmSchedule);
        }
        repeatingAlarm.setShortBreakAlarm();
    }

    private void setLongBreakAlarm(Context context){
        RepeatingAlarm repeatingAlarm;
        if(mCurrentAlarmSchedule == null){
            repeatingAlarm = new UnscheduledRepeatingAlarm(context);
        } else {
            repeatingAlarm = new ScheduledRepeatingAlarm(context, mCurrentAlarmSchedule);
        }
        repeatingAlarm.delayAlarm();
    }

    private void setStoodUpAlarm(Context context){
        RepeatingAlarm repeatingAlarm;
        if(mCurrentAlarmSchedule == null){
            repeatingAlarm = new UnscheduledRepeatingAlarm(context);
        } else {
            repeatingAlarm = new ScheduledRepeatingAlarm(context, mCurrentAlarmSchedule);
        }
        repeatingAlarm.setRepeatingAlarm();
    }

    private String praiseForUser(){
        String[] praise = {
                getResources().getString(R.string.praise1),
                getResources().getString(R.string.praise2),
                getResources().getString(R.string.praise3),
                getResources().getString(R.string.praise4),
                getResources().getString(R.string.praise5),
                getResources().getString(R.string.praise6)
        };
        Random random = new Random(System.currentTimeMillis());
        int randomNumber = random.nextInt(praise.length);
        return praise[randomNumber];
    }

}
