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
import android.util.Log;

import com.sean.takeastand.storage.FixedAlarmSchedule;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;

import java.util.Calendar;

/* This class is responsible for setting the next repeating alarm that is part of a schedule.  It
does this by setting an inexact alarm in the future based on the user defined time period
(“the frequency” in the alarm schedule) and uses the Android System’s AlarmManager class to do so.
This class is also responsible for setting the delay alarm, when the user is not quite ready to
stand up; for implementing the “break” element, for users who need to stop receiving stand up
notifications for a certain amount of time; and for the canceling of the alarm, which used at
different points throughout the app. */

/**
 * Created by Sean on 2014-10-11.
 */
public class ScheduledRepeatingAlarm implements RepeatingAlarm {

    private static final String TAG = "ScheduledRepeatingAlarm";
    private Context mContext;
    private FixedAlarmSchedule mCurrentAlarmSchedule;
    private static final int REPEATING_ALARM_ID = 987654321;


    //For scheduled alarms use this constructor
    public ScheduledRepeatingAlarm(Context context, FixedAlarmSchedule alarmSchedule)
    {
        mContext = context;
        mCurrentAlarmSchedule = alarmSchedule;
    }

    /*
       Once done testing, convert all doubles to longs
        */
    @Override
    public void setRepeatingAlarm() {
        double alarmPeriodMinutes = mCurrentAlarmSchedule.getFrequency();
        double alarmTimeInMillis = alarmPeriodMinutes * Constants.secondsInMinute  *
                Constants.millisecondsInSecond;
        long triggerTime = SystemClock.elapsedRealtime() + (long)alarmTimeInMillis;
        Calendar nextAlarmTime = Calendar.getInstance();
        nextAlarmTime.add(Calendar.MILLISECOND, (int)alarmTimeInMillis);
        Utils.nextAlarmTimeString(nextAlarmTime, mContext);
        Utils.nextAlarmTimeMillis(nextAlarmTime, mContext);
        Utils.setRunningScheduledAlarm(mContext, mCurrentAlarmSchedule.getUID());
        setAlarm(triggerTime);
        Log.i(TAG, "Alarm set");
    }

    public void updateAlarm() {
        cancelAlarm();
        if(Utils.getNextAlarmTimeMillis(mContext) != -1){
            Log.i(TAG, "updateAlarm");
            Calendar nextAlarmTime = Calendar.getInstance();
            nextAlarmTime.setTimeInMillis(Utils.getNextAlarmTimeMillis(mContext));
            Utils.nextAlarmTimeString(nextAlarmTime, mContext);
            Utils.nextAlarmTimeMillis(nextAlarmTime, mContext);
            Utils.setRunningScheduledAlarm(mContext, mCurrentAlarmSchedule.getUID());
            setAlarm(Utils.getNextAlarmTimeMillis(mContext));
        } else {
            setRepeatingAlarm();
        }
    }

    @Override
    public void delayAlarm() {
        long delayTime = Utils.getDefaultDelay(mContext);
        long delayTimeInMillis = delayTime * Constants.secondsInMinute * Constants.millisecondsInSecond;
        long triggerTime = SystemClock.elapsedRealtime() + delayTimeInMillis;
        Calendar nextAlarmTime = Calendar.getInstance();
        nextAlarmTime.add(Calendar.MILLISECOND, (int)delayTimeInMillis);
        Utils.nextAlarmTimeString(nextAlarmTime, mContext);
        setAlarm(triggerTime);
    }

    @Override
    public void cancelAlarm()
    {
        PendingIntent pendingIntent = createPendingIntent(mContext, mCurrentAlarmSchedule);
        AlarmManager am = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
        endAlarmService();
        Log.i(TAG, "Alarm canceled");
        Utils.setCurrentMainActivityImage(mContext, Constants.NO_ALARM_RUNNING);
        Utils.setRunningScheduledAlarm(mContext, -1);
    }

    @Override
    public void takeBreak() {

    }

    private void setAlarm(long triggerTime){
        Calendar nextAlarmTime = Calendar.getInstance();
        nextAlarmTime.add(Calendar.MINUTE, mCurrentAlarmSchedule.getFrequency());
        if((mCurrentAlarmSchedule.getEndTime()).before(nextAlarmTime)){
            Utils.setRunningScheduledAlarm(mContext, -1);
            Log.i(TAG, "Alarm day is over.");
            Utils.setCurrentMainActivityImage(mContext, Constants.NO_ALARM_RUNNING);
            endAlarmService();
            cancelAlarm();
        } else {
            PendingIntent pendingIntent = createPendingIntent(mContext, mCurrentAlarmSchedule);
            AlarmManager am = ((AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE));
            am.set(AlarmManager.ELAPSED_REALTIME, triggerTime, pendingIntent);
            Utils.setRunningScheduledAlarm(mContext, mCurrentAlarmSchedule.getUID());
        }
    }

    private PendingIntent createPendingIntent(Context context, FixedAlarmSchedule alarmSchedule){
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(Constants.ALARM_SCHEDULE, alarmSchedule);
        return PendingIntent.getBroadcast(context, REPEATING_ALARM_ID, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private void endAlarmService(){
        Intent intent = new Intent(Constants.END_ALARM_SERVICE);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }
}
