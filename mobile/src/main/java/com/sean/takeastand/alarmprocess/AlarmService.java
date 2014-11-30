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
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.sean.takeastand.R;
import com.sean.takeastand.storage.FixedAlarmSchedule;
import com.sean.takeastand.ui.MainActivity;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;

import java.util.Random;

/*This class is started by the AlarmReceiver once it receives an intent that it is time for the
user to stand.  This class takes care of notifying the user and waiting for their response.  It
creates a notification that indicates that it is time to stand up and that has two buttons for
the user to choose “stood” or “delay.” It updates the notification every minute to let users whose
device may be on silent know how long ago they were supposed to have stood up. Periodically this
will vibrate or make a sound.  This class notifies the ui classes, to update to reflect the current status.   */

/**
 * Created by Sean on 2014-09-18.
 */
public class AlarmService extends Service  {

    private static final String TAG = "AlarmService";


    private Handler mHandler;
    private FixedAlarmSchedule mCurrentAlarmSchedule;
    private int mNotifTimePassed = 0;
    long[] mVibrationPattern = {(long)200, (long)300, (long)200, (long)300, (long)200, (long)300};
    private boolean mainActivityVisible;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "AlarmService started");
        mainActivityVisible = false;
        registerReceivers();
        mHandler = new Handler();
        checkMainActivityVisible();
        sendNotification();
        int oneMinuteMillis = 60000;
        mHandler.postDelayed(oneMinuteForNotificationResponse, oneMinuteMillis);
        if(intent.hasExtra(Constants.ALARM_SCHEDULE)){
            mCurrentAlarmSchedule = intent.getParcelableExtra(Constants.ALARM_SCHEDULE);
        }
        if(mCurrentAlarmSchedule == null){
            Utils.setImageStatus(this, Constants.NON_SCHEDULE_TIME_TO_STAND);
        } else {
            Utils.setImageStatus(this, Constants.SCHEDULE_TIME_TO_STAND);
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceivers();
        Log.i(TAG, "AlarmService destroyed");
    }

    private void checkMainActivityVisible(){
        Intent intent = new Intent("Visible");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void registerReceivers(){
        getApplicationContext().registerReceiver(stoodUpReceiver,
                new IntentFilter(Constants.USER_STOOD));
        getApplicationContext().registerReceiver(delayAlarmReceiver,
                new IntentFilter(Constants.USER_DELAYED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mainVisibilityReceiver,
                new IntentFilter(Constants.MAIN_ACTIVITY_VISIBILITY_STATUS));
        LocalBroadcastManager.getInstance(this).registerReceiver(endAlarmService,
                new IntentFilter(Constants.END_ALARM_SERVICE));
        LocalBroadcastManager.getInstance(this).registerReceiver(deletedAlarm,
                new IntentFilter(Constants.ALARM_SCHEDULE_DELETED));
    }

    private void unregisterReceivers(){
        getApplicationContext().unregisterReceiver(stoodUpReceiver);
        getApplicationContext().unregisterReceiver(delayAlarmReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mainVisibilityReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(endAlarmService);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(deletedAlarm);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private BroadcastReceiver mainVisibilityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mainActivityVisible = intent.getBooleanExtra("Visible", false);
        }
    };

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
                Utils.setImageStatus(getApplicationContext(), Constants.NON_SCHEDULE_STOOD_UP);
            } else {
                Utils.setImageStatus(getApplicationContext(), Constants.SCHEDULE_STOOD_UP);
            }
        }
    };

    private BroadcastReceiver delayAlarmReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "delayAlarmReceiver");
            cancelNotification();
            delayAlarm(getApplicationContext());
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
            int oneMinuteMillis = 60000;
            mHandler.postDelayed(oneMinuteForNotificationResponse, oneMinuteMillis);
        }
    };

    private Runnable stoodUp = new Runnable() {
        @Override
        public void run() {
            setStoodUpAlarm(getApplicationContext());
            AlarmService.this.stopSelf();
        }
    };

    private Runnable changeImage= new Runnable() {
        @Override
        public void run() {
            if(mCurrentAlarmSchedule==null){
                Utils.setImageStatus(getApplicationContext(), Constants.NON_SCHEDULE_ALARM_RUNNING);
            } else {
                Utils.setImageStatus(getApplicationContext(), Constants.SCHEDULE_RUNNING);
            }
        }
    };

    private void setStoodUpAlarm(Context context){
        RepeatingAlarm repeatingAlarm;
        if(mCurrentAlarmSchedule == null){
            repeatingAlarm = new UnscheduledRepeatingAlarm(context);
        } else {
            repeatingAlarm = new ScheduledRepeatingAlarm(context, mCurrentAlarmSchedule);
        }
        repeatingAlarm.setRepeatingAlarm();
    }

    private void delayAlarm(Context context){
        RepeatingAlarm repeatingAlarm;
        if(mCurrentAlarmSchedule == null){
            repeatingAlarm = new UnscheduledRepeatingAlarm(context);
            Utils.setImageStatus(context, Constants.NON_SCHEDULE_ALARM_RUNNING);
        } else {
            repeatingAlarm = new ScheduledRepeatingAlarm(context, mCurrentAlarmSchedule);
            Utils.setImageStatus(context, Constants.SCHEDULE_RUNNING);
        }
        repeatingAlarm.delayAlarm();
    }

    private void showPraise(){
        String praise = praiseForUser();
        if(mainActivityVisible){
            Intent intent = new Intent(Constants.PRAISE_FOR_USER);
            intent.putExtra("Praise", praise);
            LocalBroadcastManager.getInstance(this).sendBroadcastSync(intent);
        } else {
            Toast.makeText(this, praise, Toast.LENGTH_LONG).show();
        }
    }

    private String praiseForUser(){
        String[] praise = getResources().getStringArray(R.array.praise);
        Random random = new Random(System.currentTimeMillis());
        int randomNumber = random.nextInt(praise.length);
        return praise[randomNumber];
    }

    private void sendNotification(){
        NotificationManager notificationManager = (NotificationManager)this.getSystemService(
                Context.NOTIFICATION_SERVICE);
        PendingIntent[] pendingIntents = makeNotificationIntents();
        RemoteViews rvRibbon = new RemoteViews(getPackageName(),R.layout.stand_notification);
        rvRibbon.setOnClickPendingIntent(R.id.btnStood, pendingIntents[1]);
        rvRibbon.setOnClickPendingIntent(R.id.btnDelay, pendingIntents[2]);
        Notification.Builder alarmNotificationBuilder =  new Notification.Builder(this);
        alarmNotificationBuilder.setContent(rvRibbon);
        alarmNotificationBuilder
                .setContentIntent(pendingIntents[0])
                .setSmallIcon(R.drawable.ic_notification_small)
                .setAutoCancel(false)
                .setOngoing(true)
                .setTicker(getString(R.string.stand_up_time_low));

        //Purpose of below is to figure out what type of user alert to give with the notification
        //If scheduled, check settings for that schedule
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
                        Utils.getVibrateOverride(this)) {
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(mVibrationPattern, -1);
                }
            }
            if(alertType[2] == 1){
                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                alarmNotificationBuilder.setSound(soundUri);
            }
        } else {
            int[] alertType = Utils.getDefaultAlertType(this);
            if((alertType[0]) == 1){
                alarmNotificationBuilder.setLights(238154000, 1000, 4000);
            }
            if(alertType[1] == 1){
                alarmNotificationBuilder.setVibrate(mVibrationPattern);
                AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                if(audioManager.getMode() == AudioManager.RINGER_MODE_SILENT &&
                        Utils.getVibrateOverride(this)) {
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

    private void updateNotification(){
        mNotifTimePassed ++;
        Log.i(TAG, "time since first notification: " + mNotifTimePassed + setMinutes(mNotifTimePassed));
        NotificationManager notificationManager = (NotificationManager)this.getSystemService(
                Context.NOTIFICATION_SERVICE);
        PendingIntent[] pendingIntents = makeNotificationIntents();
        RemoteViews rvRibbon = new RemoteViews(getPackageName(),R.layout.stand_notification);
        rvRibbon.setOnClickPendingIntent(R.id.btnStood, pendingIntents[1]);
        rvRibbon.setOnClickPendingIntent(R.id.btnDelay, pendingIntents[2]);
        rvRibbon.setTextViewText(R.id.stand_up_minutes, mNotifTimePassed +
                setMinutes(mNotifTimePassed));
        rvRibbon.setTextViewText(R.id.topTextView, getString(R.string.stand_up_time_up));
        Notification.Builder alarmNotificationBuilder =  new Notification.Builder(this);
        alarmNotificationBuilder.setContent(rvRibbon);
        alarmNotificationBuilder
                .setContentIntent(pendingIntents[0])
                .setSmallIcon(R.drawable.ic_notification_small)
                .setAutoCancel(false)
                .setOngoing(true)
                .setTicker(getString(R.string.stand_up_time_low));
        if(mCurrentAlarmSchedule != null){
            int[] alertType = mCurrentAlarmSchedule.getAlertType();
            if((alertType[0]) == 1){
                alarmNotificationBuilder.setLights(238154000, 1000, 4000);
            }
            if(alertType[1] == 1 && mNotifTimePassed % (Utils.getDefaultDelay(this)) == 0){
                alarmNotificationBuilder.setVibrate(mVibrationPattern);
                AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                if(audioManager.getMode() == AudioManager.RINGER_MODE_SILENT &&
                        Utils.getVibrateOverride(this)) {
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(mVibrationPattern, -1);
                }
            }
            if(alertType[2] == 1 && mNotifTimePassed % (Utils.getDefaultDelay(this)) == 0){
                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                alarmNotificationBuilder.setSound(soundUri);
            }
        } else {
            int[] alertType = Utils.getDefaultAlertType(this);
            if((alertType[0]) == 1){
                alarmNotificationBuilder.setLights(238154000, 1000, 4000);
            }
            if(alertType[1] == 1 && mNotifTimePassed % (Utils.getDefaultDelay(this)) == 0){
                alarmNotificationBuilder.setVibrate(mVibrationPattern);
                AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                if(audioManager.getMode() == AudioManager.RINGER_MODE_SILENT &&
                        Utils.getVibrateOverride(this)) {
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(mVibrationPattern, -1);
                }
            }
            if(alertType[2] == 1 && mNotifTimePassed % (Utils.getDefaultDelay(this)) == 0){
                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                alarmNotificationBuilder.setSound(soundUri);
            }
        }
        Notification alarmNotification = alarmNotificationBuilder.build();
        notificationManager.notify(R.integer.AlarmNotificationID, alarmNotification);
    }

    private PendingIntent[] makeNotificationIntents(){
        // Creates an explicit intent for an Activity in your app
        Intent launchActivityIntent = new Intent(this, MainActivity.class);
        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(launchActivityIntent);
        PendingIntent launchActivityPendingIntent = PendingIntent.getActivity(this, 0,
                launchActivityIntent, 0);
        Intent stoodUpIntent = new Intent(Constants.USER_STOOD);
        PendingIntent stoodUpPendingIntent = PendingIntent.getBroadcast(this, 0,
                stoodUpIntent, 0);
        Intent delayAlarmIntent = new Intent(Constants.USER_DELAYED);
        PendingIntent delayAlarmPendingIntent = PendingIntent.getBroadcast(this, 0,
                delayAlarmIntent, 0);
        PendingIntent [] pendingIntents =
                {launchActivityPendingIntent, stoodUpPendingIntent, delayAlarmPendingIntent};
        return pendingIntents;
    }

    private String setMinutes(int minutes){
        if(minutes > 1 ){
            return getString(R.string.minutes_ago);
        } else {
            return getString(R.string.minute_ago);
        }
    }

    private void cancelNotification(){
        Log.i(TAG, "Notification removed");
        NotificationManager notificationManager = (NotificationManager)this.getSystemService(
                Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(R.integer.AlarmNotificationID);
    }


}
