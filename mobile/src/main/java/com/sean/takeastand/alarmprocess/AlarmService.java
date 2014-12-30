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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.heckbot.standdtector.StandDtectorTM;
import com.sean.takeastand.R;
import com.sean.takeastand.storage.FixedAlarmSchedule;
import com.sean.takeastand.storage.StoodLogsAdapter;
import com.sean.takeastand.ui.MainActivity;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;

import java.util.Calendar;
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
public class AlarmService extends Service {

    private static final String TAG = "AlarmService";


    private Handler mHandler;
    private FixedAlarmSchedule mCurrentAlarmSchedule;
    //private int mNotifTimePassed = 0;
    long[] mVibrationPattern = {(long) 200, (long) 300, (long) 200, (long) 300, (long) 200, (long) 300};
    private boolean mainActivityVisible;
    private boolean bStepCounterHandled = false;
    private boolean bWearStepCounterHandled = false;
    long lDeviceLastStep = -1;
    boolean bRepeatingAlarmStepCheck = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "AlarmService started");
        mainActivityVisible = false;
        registerReceivers();

        if (!UseLastStepCounters(intent)) {
            Log.d("onStartCommand", "No Detectors Enabled");
            beginStandNotifications(intent);
        }
        return START_REDELIVER_INTENT;
    }

    private boolean UseLastStepCounters(Intent intent) {
        if (getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0).getBoolean(Constants.DEVICE_STEP_DETECTOR_ENABLED, false)) {
            Log.d("UseLastStepCounters", "Step Detector Enabled");
            GetDeviceLastStep(intent);
            return true;
        } else if (getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0).getBoolean(Constants.WEAR_STEP_DETECTOR_ENABLED, false)) {
            Log.d("UseLastStepCounters", "Wear Detector Enabled");
            GetWearLastStep(intent);
            return true;
        }
        return false;
    }

    private void GetDeviceLastStep(Intent intent) {
        Intent stepDetectorIntent = new Intent(this, StandDtectorTM.class);
        stepDetectorIntent.setAction(com.heckbot.standdtector.Constants.DEVICE_LAST_STEP);
        Intent returnIntent = new Intent(com.heckbot.standdtector.Constants.DEVICE_LAST_STEP);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, returnIntent, PendingIntent.FLAG_ONE_SHOT);
        stepDetectorIntent.putExtra("pendingIntent", pendingIntent);
        startService(stepDetectorIntent);
        //error handling if no step detector results are returned.
        mHandler = new Handler();
        int tenSecondMillis = 10000;

        Runnable lastStepReceiverTimeout = new StepCounterTimeoutRunnable(intent);
        mHandler.postDelayed(lastStepReceiverTimeout, tenSecondMillis);
    }

    private void GetWearLastStep(Intent intent) {
        Intent wearStepDetectorIntent = new Intent(this, StandDtectorTM.class);
        wearStepDetectorIntent.setAction(com.heckbot.standdtector.Constants.WEAR_LAST_STEP);
        Intent returnIntent = new Intent(com.heckbot.standdtector.Constants.WEAR_LAST_STEP);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, returnIntent, PendingIntent.FLAG_ONE_SHOT);
        wearStepDetectorIntent.putExtra("pendingIntent", pendingIntent);
        startService(wearStepDetectorIntent);
        //error handling if no step detector results are returned.
        mHandler = new Handler();
        int tenSecondMillis = 10000;

        Runnable lastWearStepReceiverTimeout = new WearStepCounterTimeoutRunnable(intent);
        mHandler.postDelayed(lastWearStepReceiverTimeout, tenSecondMillis);
    }

    private void beginStandNotifications(Intent intent) {
        mainActivityVisible = false;
        mHandler = new Handler();
        checkMainActivityVisible();
        sendNotification();
        int defaultReminderTime = Utils.getNotificationReminderFrequency(AlarmService.this) *
                Constants.secondsInMinute * Constants.millisecondsInSecond;
        mHandler.postDelayed(timeToUpdateNotification, defaultReminderTime);
        if (intent.hasExtra(Constants.ALARM_SCHEDULE)) {
            mCurrentAlarmSchedule = intent.getParcelableExtra(Constants.ALARM_SCHEDULE);
        }
        if (mCurrentAlarmSchedule == null) {
            Utils.setImageStatus(this, Constants.NON_SCHEDULE_TIME_TO_STAND);
        } else {
            Utils.setImageStatus(this, Constants.SCHEDULE_TIME_TO_STAND);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceivers();
        Log.i(TAG, "AlarmService destroyed");
    }

    private void checkMainActivityVisible() {
        Intent intent = new Intent("Visible");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void registerReceivers() {
        getApplicationContext().registerReceiver(stoodUpReceiver,
                new IntentFilter(com.heckbot.standdtector.Constants.STOOD_RESULTS));
        getApplicationContext().registerReceiver(deviceLastStepReceiver,
                new IntentFilter(com.heckbot.standdtector.Constants.DEVICE_LAST_STEP));
        getApplicationContext().registerReceiver(wearLastStepReceiver,
                new IntentFilter(com.heckbot.standdtector.Constants.WEAR_LAST_STEP));
        LocalBroadcastManager.getInstance(this).registerReceiver(mainVisibilityReceiver,
                new IntentFilter(Constants.MAIN_ACTIVITY_VISIBILITY_STATUS));
        LocalBroadcastManager.getInstance(this).registerReceiver(endAlarmService,
                new IntentFilter(Constants.END_ALARM_SERVICE));
        LocalBroadcastManager.getInstance(this).registerReceiver(deletedAlarm,
                new IntentFilter(Constants.ALARM_SCHEDULE_DELETED));
    }

    private void unregisterReceivers() {
        getApplicationContext().unregisterReceiver(stoodUpReceiver);
        getApplicationContext().unregisterReceiver(deviceLastStepReceiver);
        getApplicationContext().unregisterReceiver(wearLastStepReceiver);
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

    private BroadcastReceiver stoodUpReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "stoodUpReceiver");
            if (intent.hasExtra(com.heckbot.standdtector.Constants.STAND_DETECTOR_RESULT)) {
                String result = intent.getStringExtra(com.heckbot.standdtector.Constants.STAND_DETECTOR_RESULT);
                if (result.equals(com.heckbot.standdtector.Constants.STAND_DETECTED)) {
                    Vibrator v = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
                    long[] pattern = {0, 250, 250, 250, 250, 250, 250, 250};
                    v.vibrate(pattern, -1);
                    recordStand(Constants.STOOD_AFTER, Calendar.getInstance());
                }
                // Expired or not in pocket
                else {
                    return;
                }
            }
            //0 signifies stand recorded incorrectly, for whatever reason
            if (intent.hasExtra(Constants.STOOD_METHOD)) {
                recordStand(intent.getIntExtra(Constants.STOOD_METHOD, 0), Calendar.getInstance());
            } else {
                recordStand(0, Calendar.getInstance());
            }
            cancelNotification();
            showPraise();
            mHandler.removeCallbacks(timeToUpdateNotification);
            long fiveSeconds = 5 * Constants.millisecondsInSecond;
            mHandler.postDelayed(changeImage, fiveSeconds);
            long threeSeconds = 3 * Constants.millisecondsInSecond;
            mHandler.postDelayed(stoodUp, threeSeconds);
            if (mCurrentAlarmSchedule == null) {
                Utils.setImageStatus(getApplicationContext(), Constants.NON_SCHEDULE_STOOD_UP);
            } else {
                Utils.setImageStatus(getApplicationContext(), Constants.SCHEDULE_STOOD_UP);
            }
        }
    };

    private BroadcastReceiver endAlarmService = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "User switched off the repeating alarm");
            mHandler.removeCallbacks(timeToUpdateNotification);
            mHandler.removeCallbacks(stoodUp);
            mHandler.removeCallbacks(changeImage);
            cancelNotification();
            //End this service
            AlarmService.this.stopSelf();
        }
    };

    private BroadcastReceiver deviceLastStepReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Step Data Received");
            if (!bStepCounterHandled) {
                bStepCounterHandled = true;
                Bundle extras = intent.getExtras();
                boolean bHasStepHardware = extras.getBoolean(com.heckbot.standdtector.Constants.DEVICE_STEP_HARDWARE);
                long lDefaultFrequencyMilliseconds = Utils.getDefaultFrequency(getApplicationContext()) * 60000;
                if (bHasStepHardware) {
                    lDeviceLastStep = extras.getLong(com.heckbot.standdtector.Constants.LAST_STEP);
                    if (lDeviceLastStep < (lDefaultFrequencyMilliseconds * .9)) {
                        Log.d(TAG, "Last step less than default frequency");
                    } else {
                        lDeviceLastStep = -1;
                    }
                }
                if (getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0).getBoolean(Constants.WEAR_STEP_DETECTOR_ENABLED, false)) {
                    Log.d("deviceLastStepReceiver", "Device Step Processed, getting Wear Step");
                    GetWearLastStep(intent);
                } else if (lDeviceLastStep < 0) {
                    Log.d("deviceLastStepReceiver", "Last Step < 0");
                    if (bRepeatingAlarmStepCheck) {
                        updateNotification();
                    } else {
                        beginStandNotifications(intent);
                    }
                } else {
                    Log.d("deviceLastStepReceiver", "Device Step Processed, postponing reminder");
                    //if notification path
                    int strStepDetectionMethod;
                    if (bRepeatingAlarmStepCheck) {
                        bRepeatingAlarmStepCheck = false;
                        cancelNotification();
                        mHandler.removeCallbacks(timeToUpdateNotification);
                        long fiveSeconds = 5 * Constants.millisecondsInSecond;
                        mHandler.postDelayed(changeImage, fiveSeconds);
                        long threeSeconds = 3 * Constants.millisecondsInSecond;
                        if (mCurrentAlarmSchedule == null) {
                            Utils.setImageStatus(getApplicationContext(), Constants.NON_SCHEDULE_STOOD_UP);
                        } else {
                            Utils.setImageStatus(getApplicationContext(), Constants.SCHEDULE_STOOD_UP);
                        }
                        strStepDetectionMethod = Constants.STEP_DETECTED_AFTER_DEVICE;
                    } else {
                        strStepDetectionMethod = Constants.STEP_DETECTED_BEFORE_DEVICE;
                    }
                    postponeAlarm(getApplicationContext(), lDefaultFrequencyMilliseconds - lDeviceLastStep);
                    Calendar lastStepTime = Calendar.getInstance();
                    lastStepTime.add(Calendar.MILLISECOND, (int) -(lDeviceLastStep));
                    recordStand(strStepDetectionMethod, lastStepTime);
                    stopSelf();
                }
            }
        }
    };

    private BroadcastReceiver wearLastStepReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Wear Step Data Received");
            if (!bWearStepCounterHandled) {
                bWearStepCounterHandled = true;
                Bundle extras = intent.getExtras();
                boolean bHasWearStepHardware = extras.getBoolean(com.heckbot.standdtector.Constants.WEAR_STEP_HARDWARE);
                long lDefaultFrequencyMilliseconds = Utils.getDefaultFrequency(getApplicationContext()) * 60000;
                long lWearLastStep = -1;
                if (bHasWearStepHardware) {
                    lWearLastStep = extras.getLong("Wear_Last_Step");
                    if (lWearLastStep < (lDefaultFrequencyMilliseconds * .9)) {
                        Log.d(TAG, "Last wear step less than default frequency");
                    } else {
                        lWearLastStep = -1;
                    }
                }
                if (lWearLastStep < 0 && lDeviceLastStep < 0) {
                    beginStandNotifications(intent);
                } else {
                    int stepType;
                    long lLastStep;
                    if (bRepeatingAlarmStepCheck) {
                        bRepeatingAlarmStepCheck = false;
                        cancelNotification();
                        mHandler.removeCallbacks(timeToUpdateNotification);
                        long fiveSeconds = 5 * Constants.millisecondsInSecond;
                        mHandler.postDelayed(changeImage, fiveSeconds);
                        if (mCurrentAlarmSchedule == null) {
                            Utils.setImageStatus(getApplicationContext(), Constants.NON_SCHEDULE_STOOD_UP);
                        } else {
                            Utils.setImageStatus(getApplicationContext(), Constants.SCHEDULE_STOOD_UP);
                        }
                        if (lDeviceLastStep >= lWearLastStep) {
                            lLastStep = lDeviceLastStep;
                            stepType = Constants.STEP_DETECTED_AFTER_DEVICE;
                        } else {
                            lLastStep = lWearLastStep;
                            stepType = Constants.STEP_DETECTED_AFTER_WEAR;
                        }
                    } else {
                        if (lDeviceLastStep >= lWearLastStep) {
                            lLastStep = lDeviceLastStep;
                            stepType = Constants.STEP_DETECTED_BEFORE_DEVICE;
                        } else {
                            lLastStep = lWearLastStep;
                            stepType = Constants.STEP_DETECTED_BEFORE_WEAR;
                        }
                    }

                    postponeAlarm(getApplicationContext(), lDefaultFrequencyMilliseconds - lWearLastStep);
                    Calendar lastStepTime = Calendar.getInstance();
                    lastStepTime.add(Calendar.MILLISECOND, (int) -(lLastStep));
                    recordStand(stepType, lastStepTime);
                    stopSelf();
                }
            }
        }
    };

    private BroadcastReceiver deletedAlarm = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "User deleted an Alarm Schedule.  Checking to see if that is the currently" +
                    " alarm notification.");
            int currentAlarmUID = -1;
            if (mCurrentAlarmSchedule != null) {
                currentAlarmUID = mCurrentAlarmSchedule.getUID();
            }
            int deletedAlarmUID = intent.getIntExtra("UID", -2);
            if (deletedAlarmUID == currentAlarmUID) {
                mHandler.removeCallbacks(timeToUpdateNotification);
                cancelNotification();
                //End this service
                AlarmService.this.stopSelf();
            }
        }
    };

    private Runnable timeToUpdateNotification = new Runnable() {
        //ToDo: Note: If user dismisses notification, it comes back every minute
        public void run() {
            updateNotification();
            int defaultReminderTime = Utils.getNotificationReminderFrequency(AlarmService.this) *
                    Constants.secondsInMinute * Constants.millisecondsInSecond;
            mHandler.postDelayed(timeToUpdateNotification, defaultReminderTime);
        }
    };

    private class StepCounterTimeoutRunnable implements Runnable {
        private final Intent intent;

        StepCounterTimeoutRunnable(final Intent intent) {
            this.intent = intent;
        }

        public void run() {
            if (!bStepCounterHandled) {
                bStepCounterHandled = true;
                Intent stopStepDetectorIntent = new Intent(com.heckbot.standdtector.Constants.STANDDTECTOR_STOP);
                LocalBroadcastManager.getInstance(AlarmService.this).sendBroadcast(stopStepDetectorIntent);
                Log.i(TAG, "Step Data Timeout");

                if (getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0).getBoolean(Constants.WEAR_STEP_DETECTOR_ENABLED, false)) {
                    GetWearLastStep(intent);
                } else {
                    beginStandNotifications(intent);
                }
            }
        }
    }

    private class WearStepCounterTimeoutRunnable implements Runnable {
        private final Intent intent;

        WearStepCounterTimeoutRunnable(final Intent intent) {
            this.intent = intent;
        }

        public void run() {
            if (!bWearStepCounterHandled) {
                bWearStepCounterHandled = true;
                Intent stopStepDetectorIntent = new Intent(com.heckbot.standdtector.Constants.STANDDTECTOR_STOP);
                LocalBroadcastManager.getInstance(AlarmService.this).sendBroadcast(stopStepDetectorIntent);
                Log.i(TAG, "Wear Step Data Timeout");

                if (lDeviceLastStep < 0) {
                    Log.d("WearStepCounterTimeoutRunnable", "Wear Timeout, no device step");
                    beginStandNotifications(intent);
                } else {
                    Log.d("WearStepCounterTimeoutRunnable", "Wear Timeout, Device Step Processed, postponing reminder");
                    long lDefaultFrequencyMilliseconds = Utils.getDefaultFrequency(getApplicationContext()) * 60000;
                    //if wear step detector timed out, and valid device steps returned, postpone and finish
                    postponeAlarm(getApplicationContext(), lDefaultFrequencyMilliseconds - lDeviceLastStep);
                    Calendar lastStepTime = Calendar.getInstance();
                    lastStepTime.add(Calendar.MILLISECOND, (int) -(lDeviceLastStep));
                    recordStand(Constants.STEP_DETECTED_BEFORE_DEVICE, lastStepTime);
                    stopSelf();
                }
            }
        }
    }

    private Runnable stoodUp = new Runnable() {
        @Override
        public void run() {
            setStoodUpAlarm(getApplicationContext());
            StoodLogsAdapter stoodLogsAdapter = new StoodLogsAdapter(AlarmService.this);
            stoodLogsAdapter.getLastRow();
            AlarmService.this.stopSelf();
        }
    };

    private Runnable changeImage = new Runnable() {
        @Override
        public void run() {
            if (mCurrentAlarmSchedule == null) {
                Utils.setImageStatus(getApplicationContext(), Constants.NON_SCHEDULE_ALARM_RUNNING);
            } else {
                Utils.setImageStatus(getApplicationContext(), Constants.SCHEDULE_RUNNING);
            }
        }
    };

    private void setStoodUpAlarm(Context context) {
        RepeatingAlarm repeatingAlarm;
        if (mCurrentAlarmSchedule == null) {
            repeatingAlarm = new UnscheduledRepeatingAlarm(context);
        } else {
            repeatingAlarm = new ScheduledRepeatingAlarm(context, mCurrentAlarmSchedule);
        }
        repeatingAlarm.setRepeatingAlarm();
    }

    private void postponeAlarm(Context context, long milliseconds) {
        RepeatingAlarm repeatingAlarm;
        if (mCurrentAlarmSchedule == null) {
            repeatingAlarm = new UnscheduledRepeatingAlarm(context);
            Utils.setImageStatus(context, Constants.NON_SCHEDULE_ALARM_RUNNING);
        } else {
            repeatingAlarm = new ScheduledRepeatingAlarm(context, mCurrentAlarmSchedule);
            Utils.setImageStatus(context, Constants.SCHEDULE_RUNNING);
        }
        repeatingAlarm.postponeAlarm(milliseconds);
        Intent intent = new Intent(Constants.UPDATE_NEXT_ALARM_TIME);
        LocalBroadcastManager.getInstance(this).sendBroadcastSync(intent);
    }

    private void showPraise() {
        String praise = praiseForUser();
        if (mainActivityVisible) {
            Intent intent = new Intent(Constants.PRAISE_FOR_USER);
            intent.putExtra("Praise", praise);
            LocalBroadcastManager.getInstance(this).sendBroadcastSync(intent);
        } else {
            Toast.makeText(this, praise, Toast.LENGTH_LONG).show();
        }
    }

    private String praiseForUser() {
        String[] praise = getResources().getStringArray(R.array.praise);
        Random random = new Random(System.currentTimeMillis());
        int randomNumber = random.nextInt(praise.length);
        return praise[randomNumber];
    }

    private void sendNotification() {
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(
                Context.NOTIFICATION_SERVICE);
        PendingIntent[] pendingIntents = makeNotificationIntents();
        RemoteViews rvRibbon = new RemoteViews(getPackageName(), R.layout.stand_notification);
        rvRibbon.setOnClickPendingIntent(R.id.btnStood, pendingIntents[1]);
        NotificationCompat.Builder alarmNotificationBuilder = new NotificationCompat.Builder(this);
        alarmNotificationBuilder
                .setContent(rvRibbon)
                .setContentIntent(pendingIntents[0])
                .setAutoCancel(false)
                .setTicker(getString(R.string.stand_up_time_low))
                .setSmallIcon(R.drawable.ic_notification_small)
                .setContentTitle("Take A Stand ✔")
                .setContentText("Mark Stood")
                .extend(
                        new NotificationCompat.WearableExtender()
                                .addAction(new NotificationCompat.Action.Builder(R.drawable.ic_action_done, "Stood", pendingIntents[2]).build())
                                .setContentAction(0)
                                .setHintHideIcon(true)
//                    .setBackground(BitmapFactory.decodeResource(getResources(), R.drawable.alarm_schedule_passed))
                )
        ;

        //Purpose of below is to figure out what type of user alert to give with the notification
        //If scheduled, check settings for that schedule
        //If unscheduled, check user defaults
        if (mCurrentAlarmSchedule != null) {
            boolean[] alertType = mCurrentAlarmSchedule.getAlertType();
            if ((alertType[0])) {
                alarmNotificationBuilder.setLights(238154000, 1000, 4000);
            }
            if (alertType[1]) {
                alarmNotificationBuilder.setVibrate(mVibrationPattern);
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                if (audioManager.getMode() == AudioManager.RINGER_MODE_SILENT &&
                        Utils.getVibrateOverride(this)) {
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(mVibrationPattern, -1);
                }
            }
            if (alertType[2]) {
                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                alarmNotificationBuilder.setSound(soundUri);
            }
        } else {
            boolean[] alertType = Utils.getDefaultAlertType(this);
            if ((alertType[0])) {
                alarmNotificationBuilder.setLights(238154000, 1000, 4000);
            }
            if (alertType[1]) {
                alarmNotificationBuilder.setVibrate(mVibrationPattern);
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                if (audioManager.getMode() == AudioManager.RINGER_MODE_SILENT &&
                        Utils.getVibrateOverride(this)) {
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(mVibrationPattern, -1);
                }
            }
            if (alertType[2]) {
                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                alarmNotificationBuilder.setSound(soundUri);
            }
        }
        Notification alarmNotification = alarmNotificationBuilder.build();
        notificationManager.notify(R.integer.AlarmNotificationID, alarmNotification);

        if (getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0).getBoolean(Constants.STANDDTECTORTM_ENABLED, false)) {
            Intent standSensorIntent = new Intent(this, StandDtectorTM.class);
            standSensorIntent.setAction(com.heckbot.standdtector.Constants.STANDDTECTOR_START);
            standSensorIntent.putExtra("MILLISECONDS", (long) 60000);

            standSensorIntent.putExtra("pendingIntent", pendingIntents[1]);

            startService(standSensorIntent);
        }
    }

    private void updateNotification() {
        /*if (!bRepeatingAlarmStepCheck) {
            mNotifTimePassed++;
        }
        Log.i(TAG, "time since first notification: " + mNotifTimePassed + setMinutes(mNotifTimePassed));*/
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(
                Context.NOTIFICATION_SERVICE);
        PendingIntent[] pendingIntents = makeNotificationIntents();
        RemoteViews rvRibbon = new RemoteViews(getPackageName(), R.layout.stand_notification);
        rvRibbon.setOnClickPendingIntent(R.id.btnStood, pendingIntents[1]);
        /*rvRibbon.setTextViewText(R.id.stand_up_minutes, mNotifTimePassed +
                setMinutes(mNotifTimePassed));
        rvRibbon.setTextViewText(R.id.topTextView, getString(R.string.stand_up_time_up));*/
        NotificationCompat.Builder alarmNotificationBuilder = new NotificationCompat.Builder(this);
        alarmNotificationBuilder.setContent(rvRibbon);
        alarmNotificationBuilder
                .setContentIntent(pendingIntents[0])
                .setAutoCancel(false)
                .setTicker(getString(R.string.stand_up_time_low))
                .setSmallIcon(R.drawable.ic_notification_small)
                .setContentTitle("Take A Stand ✔")
                //.setContentText("Mark Stood\n" + mNotifTimePassed + setMinutes(mNotifTimePassed))
                .extend(
                        new NotificationCompat.WearableExtender()
                                .addAction(new NotificationCompat.Action.Builder(R.drawable.ic_action_done, "Stood", pendingIntents[1]).build())
                                .setContentAction(0)
                                .setHintHideIcon(true)
//                    .setBackground(BitmapFactory.decodeResource(getResources(), R.drawable.alarm_schedule_passed))
                )
        ;

        boolean[] alertType;
        if (mCurrentAlarmSchedule != null) {
            alertType = mCurrentAlarmSchedule.getAlertType();
        } else {
            alertType = Utils.getDefaultAlertType(this);
        }

        if ((alertType[0])) {
            alarmNotificationBuilder.setLights(238154000, 1000, 4000);
        }
        if (Utils.getRepeatAlerts(this)) {
            if (alertType[1]) {
                boolean bUseLastStepCounters = false;
                if (!bRepeatingAlarmStepCheck) {
                    bRepeatingAlarmStepCheck = true;
                    bUseLastStepCounters = UseLastStepCounters(null);
                }
                if (!bUseLastStepCounters) {
                    bRepeatingAlarmStepCheck = false;
                    alarmNotificationBuilder.setVibrate(mVibrationPattern);
                    AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    if (audioManager.getMode() == AudioManager.RINGER_MODE_SILENT &&
                            Utils.getVibrateOverride(this)) {
                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        v.vibrate(mVibrationPattern, -1);
                    }
                }
            }
            if (alertType[2]) {
                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                alarmNotificationBuilder.setSound(soundUri);
            }
        }

        Notification alarmNotification = alarmNotificationBuilder.build();
        notificationManager.notify(R.integer.AlarmNotificationID, alarmNotification);
    }

    private PendingIntent[] makeNotificationIntents() {
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
        Intent stoodUpIntent = new Intent(com.heckbot.standdtector.Constants.STOOD_RESULTS);
        stoodUpIntent.putExtra(Constants.STOOD_METHOD, Constants.TAPPED_NOTIFICATION_DEVICE);
        PendingIntent stoodUpPendingIntent = PendingIntent.getBroadcast(this, 0,
                stoodUpIntent, 0);
        Intent stoodUpWearIntent = new Intent(com.heckbot.standdtector.Constants.STOOD_RESULTS);
        stoodUpWearIntent.putExtra(Constants.STOOD_METHOD, Constants.TAPPED_NOTIFICATION_WEAR);
        PendingIntent stoodUpWearPendingIntent = PendingIntent.getBroadcast(this, 0,
                stoodUpIntent, 0);
        return new PendingIntent[]{launchActivityPendingIntent, stoodUpPendingIntent, stoodUpWearPendingIntent};
    }

    private String setMinutes(int minutes) {
        if (minutes > 1) {
            return getString(R.string.minutes_ago);
        } else {
            return getString(R.string.minute_ago);
        }
    }

    private void cancelNotification() {
        Log.i(TAG, "Notification removed");
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(
                Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(R.integer.AlarmNotificationID);
    }

    private void recordStand(int stoodMethod, Calendar timeStamp) {
        StoodLogsAdapter stoodLogsAdapter = new StoodLogsAdapter(this);
        stoodLogsAdapter.newStoodLog(stoodMethod, timeStamp);
    }

}