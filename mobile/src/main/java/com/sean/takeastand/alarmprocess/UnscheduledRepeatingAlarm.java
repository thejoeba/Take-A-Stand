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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;

import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;

import java.util.Calendar;

/* This class is responsible for setting the next repeating alarm for alarms that are unscheduled.
These begin when the user taps on the start button that is in the MainImageButtonFragment.  It sets the
alarm by setting an inexact alarm in the future based on the user defined time period
(“the default frequency” that the user can customize in the settings file) and uses the Android
System’s AlarmManager class to do so.  This class is also responsible for setting the delay alarm,
when the user is not quite ready to stand up; for implementing the “break” element, for users who
need to stop receiving stand up notifications for a certain amount of time; and for the canceling
of the alarm, which is used at different points throughout the app. */

/**
 * Created by Sean on 2014-10-11.
 */
public class UnscheduledRepeatingAlarm implements RepeatingAlarm {

    //private static final String TAG = "UnscheduledRepeatingAlarm";

    private Context mContext;
    private static final int REPEATING_ALARM_ID = 987654321;
    private static final int PAUSE_ALARM_ID = 123456789;


    //For unscheduled alarms use this constructor
    public UnscheduledRepeatingAlarm(Context context) {
        mContext = context;
    }


    /*
       Once done testing, convert all doubles to longs
        */
    @Override
    public void setRepeatingAlarm() {
        double alarmPeriodMinutes = .2;//Utils.getDefaultFrequency(mContext);
        double alarmTimeInMillis = alarmPeriodMinutes * Constants.secondsInMinute * Constants.millisecondsInSecond;
        long triggerTime = SystemClock.elapsedRealtime() + (long) alarmTimeInMillis;
        Calendar nextAlarmTime = Calendar.getInstance();
        nextAlarmTime.add(Calendar.MILLISECOND, (int) alarmTimeInMillis);
        Utils.setNextAlarmTimeString(nextAlarmTime, mContext);
        setAlarm(triggerTime);
    }

    @Override
    public void delayAlarm() {
        long delayTime = Utils.getNotificationReminderFrequency(mContext);
        long delayTimeInMillis = delayTime * Constants.secondsInMinute * Constants.millisecondsInSecond;
        long triggerTime = SystemClock.elapsedRealtime() + delayTimeInMillis;
        Calendar nextAlarmTime = Calendar.getInstance();
        nextAlarmTime.add(Calendar.MILLISECOND, (int) delayTimeInMillis);
        Utils.setNextAlarmTimeString(nextAlarmTime, mContext);
        setAlarm(triggerTime);
    }

    @Override
    public void postponeAlarm(long delayTimeInMillis) {
        long triggerTime = SystemClock.elapsedRealtime() + delayTimeInMillis;
        Calendar nextAlarmTime = Calendar.getInstance();
        nextAlarmTime.add(Calendar.MILLISECOND, (int) delayTimeInMillis);
        Utils.setNextAlarmTimeString(nextAlarmTime, mContext);
        setAlarm(triggerTime);
    }

    @Override
    public void cancelAlarm() {
        PendingIntent pendingIntent = createPendingIntent(mContext);
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
        endAlarmService();
        Utils.setImageStatus(mContext, Constants.NO_ALARM_RUNNING);
    }

    @Override
    public void pause() {
        //Cancel previous
        PendingIntent pendingIntent = createPendingIntent(mContext);
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
        endAlarmService();
        int totalPauseTime = Utils.getDefaultPauseAmount(mContext);
        long delayTimeInMillis = totalPauseTime * Constants.secondsInMinute * Constants.millisecondsInSecond;
        long triggerTime = SystemClock.elapsedRealtime() + delayTimeInMillis;
        PendingIntent pausePendingIntent = createPausePendingIntent(mContext);
        am.set(AlarmManager.ELAPSED_REALTIME, triggerTime, pausePendingIntent);
        Calendar pausedUntilTime = Calendar.getInstance();
        pausedUntilTime.add(Calendar.MINUTE, Utils.getDefaultPauseAmount(mContext));
        Utils.setPausedTime(pausedUntilTime, mContext);
        Utils.setImageStatus(mContext, Constants.NON_SCHEDULE_PAUSED);
    }

    @Override
    public void unpause() {
        PendingIntent pendingIntent = createPausePendingIntent(mContext);
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
        setRepeatingAlarm();
        Utils.setImageStatus(mContext, Constants.NON_SCHEDULE_ALARM_RUNNING);
    }


    private void setAlarm(long triggerTime) {
        PendingIntent pendingIntent = createPendingIntent(mContext);
        AlarmManager am = ((AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE));
        am.set(AlarmManager.ELAPSED_REALTIME, triggerTime, pendingIntent);
    }

    private PendingIntent createPendingIntent(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        return PendingIntent.getBroadcast(context, REPEATING_ALARM_ID, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private PendingIntent createPausePendingIntent(Context context) {
        Intent intent = new Intent(context, EndPauseReceiver.class);
        return PendingIntent.getBroadcast(context, PAUSE_ALARM_ID, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private void endAlarmService() {
        Intent intent = new Intent(Constants.END_ALARM_SERVICE);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

}
