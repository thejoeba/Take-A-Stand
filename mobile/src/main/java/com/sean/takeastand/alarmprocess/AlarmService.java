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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.sean.takeastand.R;
import com.sean.takeastand.storage.AlarmSchedule;
import com.sean.takeastand.storage.FixedAlarmSchedule;
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
public class AlarmService extends Service  {

    private static final String TAG = "AlarmService";
    private final long oneMinuteMillis = 60000;

    private Handler mHandler;
    private Context mContext;
    private FixedAlarmSchedule mCurrentAlarmSchedule;
    private int mNotifTimePassed = 0;
    long[] mVibrationPattern = {(long)200, (long)200, (long)200, (long)200};
    private boolean mainActivityVisible;


    @Override
    public void onCreate() {
        super.onCreate();
        mainActivityVisible = false;
        registerReceivers();
        mHandler = new Handler();
        mContext = getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "AlarmService started");
        checkMainActivityVisible();
        sendNotification();
        mHandler.postDelayed(oneMinuteForNotificationResponse, oneMinuteMillis);
        if(intent.hasExtra(Constants.ALARM_SCHEDULE)){
            mCurrentAlarmSchedule = intent.getParcelableExtra(Constants.ALARM_SCHEDULE);
        }
        if(mCurrentAlarmSchedule == null){
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
        getApplicationContext().registerReceiver(delayAlarmReceiver,
                new IntentFilter("DelayAlarm"));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(endAlarmService,
                new IntentFilter(Constants.END_ALARM_SERVICE));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(deletedAlarm,
                new IntentFilter(Constants.ALARM_SCHEDULE_DELETED));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mainVisibilityReceiver,
                new IntentFilter("VisibilityStatus"));
    }

    private void unregisterReceivers(){
        getApplicationContext().unregisterReceiver(stoodUpReceiver);
        getApplicationContext().unregisterReceiver(delayAlarmReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(endAlarmService);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(deletedAlarm);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mainVisibilityReceiver);
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
            showPraise();
            mHandler.removeCallbacks(oneMinuteForNotificationResponse);
            long fiveSeconds = 5 * Constants.millisecondsInSecond;
            mHandler.postDelayed(changeImage, fiveSeconds);
            long threeSeconds = 3 * Constants.millisecondsInSecond;
            mHandler.postDelayed(stoodUp, threeSeconds);
            if(mCurrentAlarmSchedule == null){
                Utils.setCurrentMainActivityImage(mContext, Constants.NON_SCHEDULE_STOOD_UP);
            } else {
                Utils.setCurrentMainActivityImage(mContext, Constants.SCHEDULE_STOOD_UP);
            }
        }
    };

    private BroadcastReceiver delayAlarmReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "delayAlarmReceiver");
            cancelNotification();
            delayAlarm(mContext);
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

    private BroadcastReceiver mainVisibilityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mainActivityVisible = intent.getBooleanExtra("Visible", false);
            Log.i(TAG, "Main activity visibility: " + mainActivityVisible);
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
            setStoodUpAlarm(mContext);
            AlarmService.this.stopSelf();
            Log.i(TAG, "setting new alarm");

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
            Log.i(TAG, "changing from stood up image");
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
        PendingIntent[] pendingIntents = makeNotificationIntents();
        Bitmap largeIcon = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.ic_notification);
        Notification.Builder alarmNotificationBuilder =  new Notification.Builder(mContext);
        alarmNotificationBuilder
                .setContentTitle("Take A Stand")
                .setStyle(new Notification.InboxStyle()
                        .addLine("Time to Stand Up"))
                .setContentText("Time to stand up")
                .setContentIntent(pendingIntents[0])
                .setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.ic_notification_small)
                .setAutoCancel(false)
                .setOngoing(true)
                .addAction(R.drawable.ic_action_done, "Stood", pendingIntents[1])
                .addAction(R.drawable.ic_action_time, "Delay", pendingIntents[2])
                .setTicker("Time to stand up");
        //Purpose of below is to figure out what type of user alert to give with the notification
        //If scheduled, check schedule preferences
        //If unscheduled, check user defaults
        if(mCurrentAlarmSchedule!=null){
            int[] alertType = mCurrentAlarmSchedule.getAlertType();
            if((alertType[0]) == 1){
                alarmNotificationBuilder.setLights(238154000, 1000, 4000);
            }
            if(alertType[1] == 1){
                alarmNotificationBuilder.setVibrate(mVibrationPattern);
                AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                if(audioManager.getMode() == AudioManager.RINGER_MODE_SILENT &&
                        Utils.getVibrateOverride(mContext)) {
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(mVibrationPattern, -1);
                }
            }
            if(alertType[2] == 1){
                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                alarmNotificationBuilder.setSound(soundUri);
            }
        } else {
            int[] alertType = Utils.getDefaultAlertType(mContext);
            if((alertType[0]) == 1){
                alarmNotificationBuilder.setLights(238154000, 1000, 4000);
            }
            if(alertType[1] == 1){
                alarmNotificationBuilder.setVibrate(mVibrationPattern);
                AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                if(audioManager.getMode() == AudioManager.RINGER_MODE_SILENT &&
                        Utils.getVibrateOverride(mContext)) {
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(mVibrationPattern, -1);
                }
            }
            if(alertType[2] == 1){
                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                alarmNotificationBuilder.setSound(soundUri);
            }
        }
        Notification alarmNotification = alarmNotificationBuilder.build();
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
        PendingIntent[] pendingIntents = makeNotificationIntents();
        Bitmap largeIcon = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.ic_notification);

        Notification.Builder alarmNotificationBuilder =  new Notification.Builder(mContext);
        alarmNotificationBuilder
                .setContentTitle("Take A Stand")
                .setStyle(new Notification.InboxStyle()
                        .addLine("Time to stand up: " + mNotifTimePassed +
                                setMinutes(mNotifTimePassed)))
                .setContentText("Time to stand up: " + mNotifTimePassed +
                        setMinutes(mNotifTimePassed))
                .setContentIntent(pendingIntents[0])
                .setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.ic_notification_small)
                .setAutoCancel(false)
                .setOngoing(true)
                .addAction(R.drawable.ic_action_done, "Stood", pendingIntents[1])
                .addAction(R.drawable.ic_action_time, "Delay", pendingIntents[2])
                .setTicker("Time to stand up");
        //Only keep the LED lights going on new notifications, if user has them set
        //Don't vibrate or make a sound
        if(mCurrentAlarmSchedule != null){
            int[] alertType = mCurrentAlarmSchedule.getAlertType();
            if((alertType[0]) == 1){
                alarmNotificationBuilder.setLights(238154000, 1000, 4000);
            }
            if(alertType[1] == 1 && mNotifTimePassed < 2){
                alarmNotificationBuilder.setVibrate(mVibrationPattern);
                AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                if(audioManager.getMode() == AudioManager.RINGER_MODE_SILENT &&
                        Utils.getVibrateOverride(mContext)) {
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(mVibrationPattern, -1);
                }
            }
        } else {
            int[] alertType = Utils.getDefaultAlertType(mContext);
            if((alertType[0]) == 1){
                alarmNotificationBuilder.setLights(238154000, 1000, 4000);
            }
            if(alertType[1] == 1){
                alarmNotificationBuilder.setVibrate(mVibrationPattern);
                AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                if(audioManager.getMode() == AudioManager.RINGER_MODE_SILENT &&
                        Utils.getVibrateOverride(mContext)) {
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(mVibrationPattern, -1);
                }
            }
        }
        Notification alarmNotification = alarmNotificationBuilder.build();
        notificationManager.notify(R.integer.AlarmNotificationID, alarmNotification);




    }

    private PendingIntent[] makeNotificationIntents(){
        // Creates an explicit intent for an Activity in your app
        Intent launchActivityIntent = new Intent(mContext, MainActivity.class);
        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(launchActivityIntent);
        PendingIntent launchActivityPendingIntent = PendingIntent.getActivity(mContext, 0,
                launchActivityIntent, 0);
        Intent stoodUpIntent = new Intent("StoodUp");
        PendingIntent stoodUpPendingIntent = PendingIntent.getBroadcast(mContext, 0,
                stoodUpIntent, 0);
        Intent delayAlarmIntent = new Intent("DelayAlarm");
        PendingIntent delayAlarmPendingIntent = PendingIntent.getBroadcast(mContext, 0,
                delayAlarmIntent, 0);
        PendingIntent[] pendingIntents =
                {launchActivityPendingIntent, stoodUpPendingIntent, delayAlarmPendingIntent};
        return pendingIntents;
    }

    private String setMinutes(int minutes){
        if(minutes > 1 ){
            return " minutes ago";
        } else {
            return " minute ago";
        }
    }

    private void delayAlarm(Context context){
        RepeatingAlarm repeatingAlarm;
        if(mCurrentAlarmSchedule == null){
            repeatingAlarm = new UnscheduledRepeatingAlarm(context);
            Utils.setCurrentMainActivityImage(context, Constants.NON_SCHEDULE_ALARM_RUNNING);
        } else {
            repeatingAlarm = new ScheduledRepeatingAlarm(context, mCurrentAlarmSchedule);
            Utils.setCurrentMainActivityImage(context, Constants.SCHEDULE_RUNNING);
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

    private void checkMainActivityVisible(){
        Intent intent = new Intent("Visible");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void showPraise(){
        String praise = praiseForUser();
        if(mainActivityVisible){
            Intent intent = new Intent("PraiseForUser");
            intent.putExtra("Praise", praise);
            LocalBroadcastManager.getInstance(mContext).sendBroadcastSync(intent);
        } else {
            Toast.makeText(mContext, praise, Toast.LENGTH_LONG).show();
        }
    }

    private String praiseForUser(){
        String[] praise = getResources().getStringArray(R.array.praise);
        Random random = new Random(System.currentTimeMillis());
        int randomNumber = random.nextInt(praise.length);
        return praise[randomNumber];
    }
}
