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
import android.util.Log;
import android.widget.Toast;

import com.sean.takeastand.storage.AlarmSchedule;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;

/**
 * Created by Sean on 2014-10-11.
 */
public class ScheduledRepeatingAlarm implements RepeatingAlarm {

    private static final String TAG = "ScheduledRepeatingAlarm";
    private Context mContext;
    private AlarmSchedule mCurrentAlarmSchedule;
    private static final int REPEATING_ALARM_ID = 987654321;
    /*
    Once done testing, convert all doubles to longs
     */

    //For scheduled alarms use this constructor
    public ScheduledRepeatingAlarm(Context context, AlarmSchedule alarmSchedule)
    {
        mContext = context;
        mCurrentAlarmSchedule = alarmSchedule;
    }

    @Override
    public void setRepeatingAlarm() {
        //In future will check mAlarmSchedule.alarmType() and set alarm accordingly
        double alarmPeriodMinutes = .5;  //In future will check mAlarmSchedule.getFrequency() and set
        double alarmTimeInMillis = alarmPeriodMinutes * Constants.secondsInMinute  *
                Constants.millisecondsInSecond;
        long triggerTime = SystemClock.elapsedRealtime() + (long)alarmTimeInMillis;
        Log.i(TAG, "alarm time: " + triggerTime + "  current time: " + SystemClock.elapsedRealtime());
        setAlarm(triggerTime);
        //The purpose of this is to have a way of keeping track of which scheduled alarm is running
        //It helps when cancelling a deleted alarm
        Utils.setRunningScheduledAlarm(mContext, mCurrentAlarmSchedule.getUID());
        updateStatusImage();
        Log.i(TAG, "New Scheduled Repeating Alarm Set");
        Toast.makeText(mContext, "Set New Alarm", Toast.LENGTH_LONG).show();
    }

    @Override
    public void setShortBreakAlarm() {
        long alarmTimeInMillis = 1 * Constants.secondsInMinute  * Constants.millisecondsInSecond;
        long triggerTime = SystemClock.elapsedRealtime() + alarmTimeInMillis;
        Log.i(TAG, "alarm time: " + triggerTime + "  current time: " +
                SystemClock.elapsedRealtime());
        setAlarm(triggerTime);
        Log.i(TAG, "Short Break Alarm set");
        Toast.makeText(mContext, "Set Short Break Alarm", Toast.LENGTH_LONG).show();
    }

    @Override
    public void setLongBreakAlarm() {
        long alarmTimeInMillis = 1 * Constants.secondsInMinute * Constants.millisecondsInSecond;
        long triggerTime = SystemClock.elapsedRealtime() + alarmTimeInMillis;
        Log.i(TAG, "alarm time: " + triggerTime + "  current time: " +
                SystemClock.elapsedRealtime());
        setAlarm(triggerTime);
        Log.i(TAG, "Long Break Alarm set");
        Toast.makeText(mContext, "Set Long Break Alarm", Toast.LENGTH_LONG).show();
    }

    @Override
    public void cancelAlarm()
    {
        PendingIntent pendingIntent = createPendingIntent(mContext, mCurrentAlarmSchedule);
        AlarmManager am = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
        Log.i(TAG, "Alarm canceled");
        Toast.makeText(mContext, "Alarm Canceled", Toast.LENGTH_LONG).show();
        Utils.setCurrentMainActivityImage(mContext, Constants.NO_ALARM_RUNNING);
    }

    private void setAlarm(long triggerTime){
        PendingIntent pendingIntent = createPendingIntent(mContext, mCurrentAlarmSchedule);
        AlarmManager am = ((AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE));
        am.set(AlarmManager.ELAPSED_REALTIME, triggerTime, pendingIntent);
    }

    private PendingIntent createPendingIntent(Context context, AlarmSchedule alarmSchedule){
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(Constants.ALARM_SCHEDULE, alarmSchedule);
        return PendingIntent.getBroadcast(context, REPEATING_ALARM_ID, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private void updateStatusImage(){
        if(Utils.getCurrentImageStatus(mContext) == Constants.NO_ALARM_RUNNING ||
                Utils.getCurrentImageStatus(mContext) == Constants.NON_SCHEDULE_ALARM_RUNNING){
            Utils.setCurrentMainActivityImage(mContext, Constants.SCHEDULE_RUNNING);
        }
    }
}
