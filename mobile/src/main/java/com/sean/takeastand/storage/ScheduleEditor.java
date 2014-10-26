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

package com.sean.takeastand.storage;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.sean.takeastand.alarmprocess.ScheduledRepeatingAlarm;
import com.sean.takeastand.alarmprocess.StartScheduleReceiver;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;

import java.util.Calendar;
/* This class is responsible for coordinating the creation, editing, and deletion of scheduled
    alarms.  For example, if a new alarm is created, then this class will make sure that the daily
    repeating alarm that is received by the StartScheduleReceiver is set, that a new alarm is saved
    in the SQLite database and that if the alarm happens to be today and the start time has passed
    but the end time has not, to start a ScheduledRepeatingAlarm right away.  In addition to new
    alarms, it also manages the editing of alarms, and the deletion of alarms.

    Created by Sean on 2014-09-03.
 */
public class ScheduleEditor {

    private static final String TAG = "ScheduleEditor";
    private ScheduleDatabaseAdapter scheduleDatabaseAdapter;
    private Context mContext;



    public ScheduleEditor(Context context)
    {
        mContext = context;
        scheduleDatabaseAdapter = new ScheduleDatabaseAdapter(context);
    }

    /*
    In the future, need to edit alarm schedule to check if end time is after midnight
    and set if starttime has started and is today, and endtime is within same time time period
    even if next day, perhaps for nighttime office workers
     */


    public void newAlarm(boolean activated,  int[] alarmType, String startTime, String endTime, int frequency,
                            String title, boolean sunday, boolean monday, boolean tuesday,
                            boolean wednesday, boolean thursday, boolean friday, boolean saturday)
    {
       scheduleDatabaseAdapter.newAlarm(activated, alarmType, startTime, endTime, frequency, title,
                sunday, monday, tuesday, wednesday, thursday, friday, saturday);
        if (activated)
        {
            int UID = new ScheduleDatabaseAdapter(mContext).getLastRowID();
            setDailyRepeatingAlarm(UID, startTime);
            //If new alarm is meant to run this day
            if(Utils.isTodayActivated(sunday, monday, tuesday, wednesday, thursday, friday, saturday)){
                if(checkToSetRepeatingAlarm(startTime, endTime)){
                    //Because we are within an if statement where activated is true, put true in place
                    //of activated
                    FixedAlarmSchedule newAlarmSchedule = new FixedAlarmSchedule(UID, true, alarmType,
                            Utils.convertToCalendarTime(startTime), Utils.convertToCalendarTime(endTime),
                            frequency, title, sunday, monday, tuesday, wednesday, thursday, friday,
                            saturday);
                    new ScheduledRepeatingAlarm(mContext, newAlarmSchedule).setRepeatingAlarm();
                }
            } else {
                Log.i(TAG, "New alarm is not activated for today.  Not beginning repeatingAlarm.");
            }
        } else {
            Log.i(TAG, "Alarm is not activated");
        }
    }



    public void editActivated(AlarmSchedule alarmSchedule){
        int UID = alarmSchedule.getUID();
        Calendar startTime = alarmSchedule.getStartTime();
        Calendar endTime = alarmSchedule.getEndTime();
        FixedAlarmSchedule fixedAlarmSchedule = new FixedAlarmSchedule(alarmSchedule);
        if(alarmSchedule.getActivated()){
            setDailyRepeatingAlarm(UID, Utils.calendarToTimeString(startTime));
            Calendar rightNow = Calendar.getInstance();
            if(startTime.before(rightNow)&&endTime.after(rightNow)) {
                new ScheduledRepeatingAlarm(mContext, fixedAlarmSchedule).setRepeatingAlarm();
                Toast.makeText(mContext, "Schedule is running right now.",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            cancelDailyRepeatingAlarm(UID);
            if(UID == Utils.getRunningScheduledAlarm(mContext)){
                new ScheduledRepeatingAlarm(mContext, fixedAlarmSchedule).cancelAlarm();
                Toast.makeText(mContext, "Currently running scheduled has ended.",
                        Toast.LENGTH_SHORT).show();
            }
        }
        scheduleDatabaseAdapter.updateActivated(UID, alarmSchedule.getActivated());
    }

    public void editAlertType(AlarmSchedule alarmSchedule){
        if(alarmSchedule.getUID() == Utils.getRunningScheduledAlarm(mContext)){
            ScheduledRepeatingAlarm scheduledRepeatingAlarm = new ScheduledRepeatingAlarm(mContext,
                    new FixedAlarmSchedule(alarmSchedule));
            scheduledRepeatingAlarm.cancelAlarm();
            scheduledRepeatingAlarm.setRepeatingAlarm();
            Toast.makeText(mContext, "Currently running schedule updated.",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, "Changes saved.",
                    Toast.LENGTH_SHORT).show();
        }
        scheduleDatabaseAdapter.updateAlertType(alarmSchedule.getUID(),alarmSchedule.getAlertType());
    }

    public void editStartTime(boolean alarmToday, AlarmSchedule alarmSchedule){
        int UID = alarmSchedule.getUID();
        Calendar startTime = alarmSchedule.getStartTime();
        Calendar endTime = alarmSchedule.getEndTime();
        FixedAlarmSchedule fixedAlarmSchedule = new FixedAlarmSchedule(alarmSchedule);
        if(alarmToday){
            Calendar rightNow = Calendar.getInstance();
            if(startTime.after(rightNow)){
                if(UID == Utils.getRunningScheduledAlarm(mContext)){
                    new ScheduledRepeatingAlarm(mContext, fixedAlarmSchedule).cancelAlarm();
                    Toast.makeText(mContext, "Schedule set to begin later today.",
                            Toast.LENGTH_SHORT).show();
                }
            } else if (startTime.before(rightNow) && endTime.after(rightNow)){
                ScheduledRepeatingAlarm scheduledRepeatingAlarm =
                        new ScheduledRepeatingAlarm(mContext, fixedAlarmSchedule);
                scheduledRepeatingAlarm.cancelAlarm();
                scheduledRepeatingAlarm.setRepeatingAlarm();
                Toast.makeText(mContext, "Schedule updated and running now.",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(mContext, "Changes saved.",
                    Toast.LENGTH_SHORT).show();
        }
        cancelDailyRepeatingAlarm(UID);
        setDailyRepeatingAlarm(UID, Utils.calendarToTimeString(startTime));
        scheduleDatabaseAdapter.updateStartTime(UID, Utils.calendarToTimeString(startTime));
    }

    public void editEndTime(boolean alarmToday, AlarmSchedule alarmSchedule){
        int UID = alarmSchedule.getUID();
        Calendar endTime = alarmSchedule.getEndTime();
        Calendar startTime = alarmSchedule.getStartTime();
        FixedAlarmSchedule fixedAlarmSchedule = new FixedAlarmSchedule(alarmSchedule);
        if(alarmToday){
            Calendar rightNow = Calendar.getInstance();
            if(endTime.before(rightNow)){
                if(UID == Utils.getRunningScheduledAlarm(mContext)){
                    new ScheduledRepeatingAlarm(mContext, fixedAlarmSchedule).cancelAlarm();
                    Toast.makeText(mContext, "Current schedule is now over.",
                            Toast.LENGTH_SHORT).show();
                } else if (endTime.after(rightNow) && startTime.before(rightNow)){
                    ScheduledRepeatingAlarm scheduledRepeatingAlarm =
                             new ScheduledRepeatingAlarm(mContext, fixedAlarmSchedule);
                    scheduledRepeatingAlarm.cancelAlarm();
                    scheduledRepeatingAlarm.setRepeatingAlarm();
                    Toast.makeText(mContext, "Current running schedule updated.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(mContext, "Changes saved.",
                    Toast.LENGTH_SHORT).show();
        }
        scheduleDatabaseAdapter.updateEndTime(UID, Utils.calendarToTimeString(endTime));
    }

    public void editFrequency(AlarmSchedule alarmSchedule){
        FixedAlarmSchedule fixedAlarmSchedule = new FixedAlarmSchedule(alarmSchedule);
        if(alarmSchedule.getUID() == Utils.getRunningScheduledAlarm(mContext)){
            ScheduledRepeatingAlarm scheduledRepeatingAlarm =
                    new ScheduledRepeatingAlarm(mContext, fixedAlarmSchedule);
            scheduledRepeatingAlarm.cancelAlarm();
            scheduledRepeatingAlarm.setRepeatingAlarm();
            Toast.makeText(mContext, "Current running schedule updated.",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, "New frequency saved.",
                    Toast.LENGTH_SHORT).show();
        }
        scheduleDatabaseAdapter.updateFrequency(alarmSchedule.getUID(), alarmSchedule.getFrequency());
    }

    public void editDays(int weekday, boolean status, AlarmSchedule alarmSchedule){
        if(weekday == Utils.getTodayWeekday()){
            FixedAlarmSchedule fixedAlarmSchedule = new FixedAlarmSchedule(alarmSchedule);
            if(status){
                Calendar rightNow = Calendar.getInstance();
                if(alarmSchedule.getStartTime().before(rightNow) &&
                        alarmSchedule.getEndTime().after(rightNow)){
                    ScheduledRepeatingAlarm scheduledRepeatingAlarm =
                            new ScheduledRepeatingAlarm(mContext, fixedAlarmSchedule);
                    scheduledRepeatingAlarm.cancelAlarm();
                    scheduledRepeatingAlarm.setRepeatingAlarm();
                    Toast.makeText(mContext, "Today's schedule now running.",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Calendar rightNow = Calendar.getInstance();
                if(alarmSchedule.getStartTime().before(rightNow) &&
                        alarmSchedule.getEndTime().after(rightNow)){
                    ScheduledRepeatingAlarm scheduledRepeatingAlarm =
                            new ScheduledRepeatingAlarm(mContext, fixedAlarmSchedule);
                    scheduledRepeatingAlarm.cancelAlarm();
                    Toast.makeText(mContext, "Today's schedule cancelled.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(mContext, "Change Saved.",
                    Toast.LENGTH_SHORT).show();
        }
        int UID = alarmSchedule.getUID();
        switch (weekday){
            case 1:
                scheduleDatabaseAdapter.updateSunday(UID, status);
                break;
            case 2:
                scheduleDatabaseAdapter.updateMonday(UID, status);
                break;
            case 3:
                scheduleDatabaseAdapter.updateTuesday(UID, status);
                break;
            case 4:
                scheduleDatabaseAdapter.updateWednesday(UID, status);
                break;
            case 5:
                scheduleDatabaseAdapter.updateThursday(UID, status);
                break;
            case 6:
                scheduleDatabaseAdapter.updateFriday(UID, status);
                break;
            case 7:
                scheduleDatabaseAdapter.updateSaturday(UID, status);
                break;
            default:
                Log.i(TAG, "Weekday does not fall between 1 and 7");
        }

    }

    public void editTitle(int UID, String title){
        scheduleDatabaseAdapter.updateTitle(UID, title);
        //No toast because user can see that it has changed
    }

    public void deleteAlarm(AlarmSchedule alarmSchedule){
        cancelDailyRepeatingAlarm(alarmSchedule.getUID());
        scheduleDatabaseAdapter.deleteAlarm(alarmSchedule.getUID());
    }

    private void setDailyRepeatingAlarm(int UID, String time){
        Intent intent = new Intent(mContext, StartScheduleReceiver.class);
        intent.putExtra(Constants.ALARM_UID, UID);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(mContext, UID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        ((AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE))
                .setRepeating(AlarmManager.RTC_WAKEUP, Utils.calendarToRTCMillis(nextAlarmTime(time)),
                        AlarmManager.INTERVAL_DAY, pendingIntent);
        Log.i(TAG, "Set Daily Repeating alarm for " + Long.toString(Utils.calendarToRTCMillis(nextAlarmTime(time))) +
                " current time " + Long.toString(System.currentTimeMillis()));
    }

    private void cancelDailyRepeatingAlarm(int UID){
        Log.i(TAG, "Canceled a daily repeating alarm");
        Intent intent = new Intent(mContext, StartScheduleReceiver.class);
        intent.putExtra(Constants.ALARM_UID, UID);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(mContext, UID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    private Calendar nextAlarmTime(String time){
        Calendar alarmTime = Utils.convertToCalendarTime(time);
        Calendar rightNow = Calendar.getInstance();
        if(alarmTime.after(rightNow)){
            return alarmTime;
        } else {
            //Alarm time was earlier today
            alarmTime.add(Calendar.DATE,1);
            return alarmTime;
        }
    }

    private boolean checkToSetRepeatingAlarm(String startTime, String endTime){
        //Set repeating alarm if in between start and end time
        Calendar rightNow = Calendar.getInstance();
        Calendar startTimeDate = Utils.convertToCalendarTime(startTime);
        Calendar endTimeDate = Utils.convertToCalendarTime(endTime);
        if(startTimeDate.before(rightNow)&&endTimeDate.after(rightNow)){
            Log.i(TAG, "New alarm is within current day's timeframe.  Starting RepeatingAlarm.");
            return true;
        } else {
            Log.i(TAG, "New alarm's repeating timeframe has either not begun or has already passed.");
            return false;
        }
    }
}
