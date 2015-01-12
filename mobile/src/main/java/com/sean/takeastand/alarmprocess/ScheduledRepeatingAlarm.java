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
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.sean.takeastand.R;
import com.sean.takeastand.storage.FixedAlarmSchedule;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;

import java.util.Calendar;

/* This class is responsible for setting the next repeating alarm for alarm schedules.  It
does this by setting an inexact alarm in the future based on the user defined time period
(“the frequency” in the alarm schedule) and uses the Android System’s AlarmManager class to do so.
This class is also responsible for setting the delay alarm, when it is time to stand up and
the user is not quite ready to stand up; for implementing the “break” element, for
users who need to stop receiving stand up notifications for a certain amount of time; and
for the canceling of the alarm, which is used at different points throughout the app.
Before setting an alarm, it checks to see if the next alarm would be past the schedules end time.*/

/**
 * Created by Sean on 2014-10-11.
 */
public class ScheduledRepeatingAlarm implements RepeatingAlarm {

    private static final String TAG = "ScheduledRepeatingAlarm";
    private Context mContext;
    private FixedAlarmSchedule mCurrentAlarmSchedule;
    private static final int REPEATING_ALARM_ID = 987654321;
    private static final int PAUSE_ALARM_ID = 123456789;

    public ScheduledRepeatingAlarm(Context context, FixedAlarmSchedule alarmSchedule) {
        mContext = context;
        mCurrentAlarmSchedule = alarmSchedule;
    }

    /*
       Once done testing, convert all doubles to longs
        */
    @Override
    public void setRepeatingAlarm() {
        int alarmPeriodMinutes = mCurrentAlarmSchedule.getFrequency();
        long alarmTimeInMillis = alarmPeriodMinutes * Constants.secondsInMinute *
                Constants.millisecondsInSecond;
        long triggerTime = SystemClock.elapsedRealtime() + alarmTimeInMillis;
        Calendar nextAlarmTime = Calendar.getInstance();
        nextAlarmTime.add(Calendar.MILLISECOND, (int) alarmTimeInMillis);
        Utils.setNextAlarmTimeString(nextAlarmTime, mContext);
        setNextAlarmTimeMillis(nextAlarmTime);
        if (mCurrentAlarmSchedule.getUID() != Utils.getRunningScheduledAlarm(mContext)) {
            //Expensive to constantly access database and use for statement, only do if necessary
            Utils.setScheduleTitle(mCurrentAlarmSchedule.getTitle(), mContext,
                    mCurrentAlarmSchedule.getUID());
        }
        Utils.setRunningScheduledAlarm(mContext, mCurrentAlarmSchedule.getUID());
        setAlarm(triggerTime);
        Log.i(TAG, "Alarm set");
    }

    public void updateAlarm() {
        cancelAlarm();
        long alarmTimeInMillis = getNextAlarmTimeMillis();
        if (alarmTimeInMillis != -1) {
            Calendar alarmTime = Calendar.getInstance();
            alarmTime.setTimeInMillis(alarmTimeInMillis);
            Utils.setNextAlarmTimeString(alarmTime, mContext);
            Utils.setRunningScheduledAlarm(mContext, mCurrentAlarmSchedule.getUID());
            setAlarm(alarmTimeInMillis);
        } else {
            setRepeatingAlarm();
        }
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
        PendingIntent pendingIntent = createPendingIntent(mContext, mCurrentAlarmSchedule);
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
        endAlarmService();
        Utils.setImageStatus(mContext, Constants.NO_ALARM_RUNNING);
        Utils.setRunningScheduledAlarm(mContext, -1);
    }

    @Override
    public void pause() {
        //Cancel previous
        PendingIntent pendingIntent = createPendingIntent(mContext, mCurrentAlarmSchedule);
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
        endAlarmService();
        int totalPauseTime = Utils.getDefaultPauseAmount(mContext);
        long delayTimeInMillis = totalPauseTime * Constants.secondsInMinute * Constants.millisecondsInSecond;
        long triggerTime = SystemClock.elapsedRealtime() + delayTimeInMillis;
        PendingIntent pausePendingIntent = createPausePendingIntent(mContext, mCurrentAlarmSchedule);
        am.set(AlarmManager.ELAPSED_REALTIME, triggerTime, pausePendingIntent);
        Calendar pausedUntilTime = Calendar.getInstance();
        pausedUntilTime.add(Calendar.MINUTE, Utils.getDefaultPauseAmount(mContext));
        Utils.setPausedTime(pausedUntilTime, mContext);
        Utils.setImageStatus(mContext, Constants.SCHEDULE_PAUSED);
    }

    @Override
    public void unpause() {
        PendingIntent pendingIntent = createPausePendingIntent(mContext, mCurrentAlarmSchedule);
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
        setRepeatingAlarm();
        Utils.setImageStatus(mContext, Constants.SCHEDULE_RUNNING);
    }

    private void setAlarm(long triggerTime) {
        Calendar nextAlarmTime = Calendar.getInstance();
        nextAlarmTime.add(Calendar.MINUTE, mCurrentAlarmSchedule.getFrequency());
        if ((mCurrentAlarmSchedule.getEndTime()).before(nextAlarmTime)) {
            Utils.setRunningScheduledAlarm(mContext, -1);
            Log.i(TAG, mContext.getString(R.string.alarm_day_over));
            Toast.makeText(mContext, "Scheduled reminders over", Toast.LENGTH_LONG).show();
            Utils.setImageStatus(mContext, Constants.NO_ALARM_RUNNING);
            endAlarmService();
            cancelAlarm();
        } else {
            PendingIntent pendingIntent = createPendingIntent(mContext, mCurrentAlarmSchedule);
            AlarmManager am = ((AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE));
            am.set(AlarmManager.ELAPSED_REALTIME, triggerTime, pendingIntent);
            Utils.setRunningScheduledAlarm(mContext, mCurrentAlarmSchedule.getUID());
        }
    }

    private PendingIntent createPendingIntent(Context context, FixedAlarmSchedule alarmSchedule) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(Constants.ALARM_SCHEDULE, alarmSchedule);
        return PendingIntent.getBroadcast(context, REPEATING_ALARM_ID, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private PendingIntent createPausePendingIntent(Context context, FixedAlarmSchedule alarmSchedule) {
        Intent intent = new Intent(context, EndPauseReceiver.class);
        intent.putExtra(Constants.ALARM_SCHEDULE, alarmSchedule);
        return PendingIntent.getBroadcast(context, PAUSE_ALARM_ID, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private void endAlarmService() {
        Intent intent = new Intent(Constants.END_ALARM_SERVICE);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void setNextAlarmTimeMillis(Calendar calendar) {
        long nextAlarmTime = calendar.getTimeInMillis();
        SharedPreferences sharedPreferences =
                mContext.getSharedPreferences(Constants.EVENT_SHARED_PREFERENCES, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(Constants.NEXT_ALARM_TIME_MILLIS, nextAlarmTime);
        editor.commit();
    }

    private long getNextAlarmTimeMillis() {
        SharedPreferences sharedPreferences =
                mContext.getSharedPreferences(Constants.EVENT_SHARED_PREFERENCES, 0);
        return sharedPreferences.getLong(Constants.NEXT_ALARM_TIME_MILLIS, -1);
    }
}
