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
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.sean.takeastand.alarmprocess.ScheduledRepeatingAlarm;
import com.sean.takeastand.alarmprocess.StartScheduleReceiver;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;

import java.util.Calendar;
/* This class is responsible for coordinating the creation, editing, and deletion of scheduled
    alarms.  For example, if a new alarm is created, then this class will make sure that the system
    daily repeating alarm that will be sent to the StartScheduleReceiver is set, that a
    new alarm is saved in the SQLite database and that if the alarm is today and
    the start time has passed but the end time has not, to start a ScheduledRepeatingAlarm
    right away.  In addition to new alarms, it also manages the editing of alarms, and the
    deletion of alarms.

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

    public void newAlarm(boolean activated,  boolean led, boolean vibrate, boolean sound,
                            String startTime, String endTime, int frequency,
                            String title, boolean sunday, boolean monday, boolean tuesday,
                            boolean wednesday, boolean thursday, boolean friday, boolean saturday)
    {
       scheduleDatabaseAdapter.newAlarm(activated, led, vibrate, sound, startTime, endTime, frequency,
               title, sunday, monday, tuesday, wednesday, thursday, friday, saturday);
        if (activated)
        {
            int UID = new ScheduleDatabaseAdapter(mContext).getLastRowID();
            setDailyRepeatingAlarm(UID, startTime);
            //If new alarm is scheduled for today
            if(Utils.isTodayActivated(sunday, monday, tuesday, wednesday, thursday, friday, saturday)){
                if(setRepeatingAlarmNow(startTime, endTime)){
                    //Because we already now activated is true, just put true
                    FixedAlarmSchedule newAlarmSchedule = new FixedAlarmSchedule(UID, true, led,
                            vibrate, sound, Utils.convertToCalendarTime(startTime, mContext),
                            Utils.convertToCalendarTime(endTime, mContext),
                            frequency, title, sunday, monday, tuesday, wednesday, thursday, friday,
                            saturday);
                    new ScheduledRepeatingAlarm(mContext, newAlarmSchedule).setRepeatingAlarm();
                    Utils.setImageStatus(mContext, Constants.SCHEDULE_RUNNING);
                    Toast.makeText(mContext, "New schedule is now running", Toast.LENGTH_SHORT).show();
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
        scheduleDatabaseAdapter.updateActivated(UID, alarmSchedule.getActivated());
        FixedAlarmSchedule fixedAlarmSchedule = new FixedAlarmSchedule(alarmSchedule);
        if(alarmSchedule.getActivated()){
            Calendar startTime = alarmSchedule.getStartTime();
            Calendar endTime = alarmSchedule.getEndTime();
            setDailyRepeatingAlarm(UID, Utils.calendarToTimeString(startTime));
            Calendar rightNow = Calendar.getInstance();
            boolean today = Utils.isTodayActivated(alarmSchedule);
            //Check to see if need to start a repeating alarm now
            if(today && startTime.before(rightNow)&&endTime.after(rightNow)) {
                new ScheduledRepeatingAlarm(mContext, fixedAlarmSchedule).setRepeatingAlarm();
                Toast.makeText(mContext, "Schedule is running right now", Toast.LENGTH_SHORT).show();
                Utils.setImageStatus(mContext, Constants.SCHEDULE_RUNNING);
            } else {
                Toast.makeText(mContext, "Schedule activated", Toast.LENGTH_SHORT).show();
            }
        } else {
            cancelDailyRepeatingAlarm(UID);
            if(UID == Utils.getRunningScheduledAlarm(mContext)){
                new ScheduledRepeatingAlarm(mContext, fixedAlarmSchedule).cancelAlarm();
                Toast.makeText(mContext, "Schedule has ended", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "Schedule turned off", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void editAlertType(AlarmSchedule alarmSchedule){
        boolean[] alertTypes = alarmSchedule.getAlertType();
        scheduleDatabaseAdapter.updateAlertType(alarmSchedule.getUID(), alertTypes[0],
                alertTypes[1], alertTypes[2] );
        if(alarmSchedule.getUID() == Utils.getRunningScheduledAlarm(mContext)
                && alarmSchedule.getActivated()){
            ScheduledRepeatingAlarm scheduledRepeatingAlarm = new ScheduledRepeatingAlarm(mContext,
                    new FixedAlarmSchedule(alarmSchedule));
            scheduledRepeatingAlarm.updateAlarm();
            Utils.setImageStatus(mContext, Constants.SCHEDULE_RUNNING);
            Toast.makeText(mContext, "Current schedule updated",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void editStartTime(boolean alarmToday, AlarmSchedule alarmSchedule){
        int UID = alarmSchedule.getUID();
        Calendar startTime = alarmSchedule.getStartTime();
        cancelDailyRepeatingAlarm(UID);
        setDailyRepeatingAlarm(UID, Utils.calendarToTimeString(startTime));
        scheduleDatabaseAdapter.updateStartTime(UID, Utils.calendarToTimeString(startTime));
        if(alarmToday && alarmSchedule.getActivated()){
            FixedAlarmSchedule fixedAlarmSchedule = new FixedAlarmSchedule(alarmSchedule);
            Calendar endTime = alarmSchedule.getEndTime();
            Calendar rightNow = Calendar.getInstance();
            if(startTime.after(rightNow)){
                if(UID == Utils.getRunningScheduledAlarm(mContext)){
                    new ScheduledRepeatingAlarm(mContext, fixedAlarmSchedule).cancelAlarm();

                }
                Toast.makeText(mContext, "Schedule will begin later today",
                        Toast.LENGTH_SHORT).show();
            } else if (startTime.before(rightNow) && endTime.after(rightNow)){
                Utils.setImageStatus(mContext, Constants.SCHEDULE_RUNNING);
                ScheduledRepeatingAlarm scheduledRepeatingAlarm =
                        new ScheduledRepeatingAlarm(mContext, fixedAlarmSchedule);
                scheduledRepeatingAlarm.updateAlarm();
                Toast.makeText(mContext, "Schedule updated and running now",
                        Toast.LENGTH_SHORT).show();
            }
        }

    }

    public void editEndTime(boolean alarmToday, AlarmSchedule alarmSchedule){
        int UID = alarmSchedule.getUID();
        Calendar endTime = alarmSchedule.getEndTime();
        scheduleDatabaseAdapter.updateEndTime(UID, Utils.calendarToTimeString(endTime));
        if(alarmToday && alarmSchedule.getActivated()){
            Calendar startTime = alarmSchedule.getStartTime();
            Calendar rightNow = Calendar.getInstance();
            FixedAlarmSchedule fixedAlarmSchedule = new FixedAlarmSchedule(alarmSchedule);
            if(endTime.before(rightNow)){
                if(UID == Utils.getRunningScheduledAlarm(mContext)){
                    new ScheduledRepeatingAlarm(mContext, fixedAlarmSchedule).cancelAlarm();
                    Toast.makeText(mContext, "Current schedule is now over",
                            Toast.LENGTH_SHORT).show();
                }
            } else if (endTime.after(rightNow) && startTime.before(rightNow)){
                Utils.setImageStatus(mContext, Constants.SCHEDULE_RUNNING);
                ScheduledRepeatingAlarm scheduledRepeatingAlarm =
                        new ScheduledRepeatingAlarm(mContext, fixedAlarmSchedule);
                scheduledRepeatingAlarm.updateAlarm();
                Toast.makeText(mContext, "Current schedule updated", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void editFrequency(AlarmSchedule alarmSchedule){
        scheduleDatabaseAdapter.updateFrequency(alarmSchedule.getUID(), alarmSchedule.getFrequency());
        if(alarmSchedule.getUID() == Utils.getRunningScheduledAlarm(mContext)){
            FixedAlarmSchedule fixedAlarmSchedule = new FixedAlarmSchedule(alarmSchedule);
            ScheduledRepeatingAlarm scheduledRepeatingAlarm =
                    new ScheduledRepeatingAlarm(mContext, fixedAlarmSchedule);
            scheduledRepeatingAlarm.cancelAlarm();
            scheduledRepeatingAlarm.setRepeatingAlarm();
            Utils.setImageStatus(mContext, Constants.SCHEDULE_RUNNING);
            Toast.makeText(mContext, "Current schedule updated",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void editDays(int weekday, boolean activated, AlarmSchedule alarmSchedule){
        if(weekday == Utils.getTodayWeekdayNum()){
            FixedAlarmSchedule fixedAlarmSchedule = new FixedAlarmSchedule(alarmSchedule);
            if(activated){
                Calendar rightNow = Calendar.getInstance();
                if(alarmSchedule.getStartTime().before(rightNow) &&
                        alarmSchedule.getEndTime().after(rightNow)){
                    ScheduledRepeatingAlarm scheduledRepeatingAlarm =
                            new ScheduledRepeatingAlarm(mContext, fixedAlarmSchedule);
                    scheduledRepeatingAlarm.updateAlarm();
                    Utils.setImageStatus(mContext, Constants.SCHEDULE_RUNNING);
                    Toast.makeText(mContext, "Schedule now running",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                if(alarmSchedule.getUID() == Utils.getRunningScheduledAlarm(mContext)){
                    ScheduledRepeatingAlarm scheduledRepeatingAlarm =
                            new ScheduledRepeatingAlarm(mContext, fixedAlarmSchedule);
                    scheduledRepeatingAlarm.cancelAlarm();
                    Toast.makeText(mContext, "Today's schedule ended",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
        int UID = alarmSchedule.getUID();
        switch (weekday){
            case 1:
                scheduleDatabaseAdapter.updateSunday(UID, activated);
                break;
            case 2:
                scheduleDatabaseAdapter.updateMonday(UID, activated);
                break;
            case 3:
                scheduleDatabaseAdapter.updateTuesday(UID, activated);
                break;
            case 4:
                scheduleDatabaseAdapter.updateWednesday(UID, activated);
                break;
            case 5:
                scheduleDatabaseAdapter.updateThursday(UID, activated);
                break;
            case 6:
                scheduleDatabaseAdapter.updateFriday(UID, activated);
                break;
            case 7:
                scheduleDatabaseAdapter.updateSaturday(UID, activated);
                break;
            default:
                Log.i(TAG, "Weekday does not fall between 1 and 7");
        }

    }

    public void editTitle(int UID, String title){
        scheduleDatabaseAdapter.updateTitle(UID, title);
        //No toast because user can see that it has changed, which is managed in the adapter class
    }

    public void deleteAlarm(AlarmSchedule alarmSchedule){
        int deletedAlarmUID = alarmSchedule.getUID();
        cancelDailyRepeatingAlarm(deletedAlarmUID);
        scheduleDatabaseAdapter.deleteAlarm(deletedAlarmUID);
        int currentlyRunningAlarm = Utils.getRunningScheduledAlarm(mContext);
        if(deletedAlarmUID == currentlyRunningAlarm){
            FixedAlarmSchedule fixedAlarmSchedule = new FixedAlarmSchedule(alarmSchedule);
            new ScheduledRepeatingAlarm(mContext, fixedAlarmSchedule).cancelAlarm();
            Toast.makeText(mContext, "Currently running scheduled deleted", Toast.LENGTH_LONG).show();
        }
        //Service needs to cancel any running alarms and notifications it is currently managing
        Intent informServiceOfDeletion = new Intent(Constants.ALARM_SCHEDULE_DELETED);
        informServiceOfDeletion.putExtra("UID", deletedAlarmUID);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(informServiceOfDeletion);
    }

    private void setDailyRepeatingAlarm(int UID, String time){
        Intent intent = new Intent(mContext, StartScheduleReceiver.class);
        intent.putExtra(Constants.ALARM_UID, UID);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(mContext, UID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        ((AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE))
                .setRepeating(AlarmManager.RTC_WAKEUP, nextAlarmTime(time).getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY, pendingIntent);
        Log.i(TAG, "Set Daily Repeating alarm for " + Long.toString(nextAlarmTime(time).getTimeInMillis()) +
                " current time " + Long.toString(System.currentTimeMillis()));
    }

    private void cancelDailyRepeatingAlarm(int UID){
        Intent intent = new Intent(mContext, StartScheduleReceiver.class);
        intent.putExtra(Constants.ALARM_UID, UID);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(mContext, UID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        Log.i(TAG, "Canceled a daily repeating alarm");
    }

    private boolean setRepeatingAlarmNow(String startTime, String endTime){
        //Set repeating alarm if in between start and end time
        Calendar rightNow = Calendar.getInstance();
        Calendar startTimeDate = Utils.convertToCalendarTime(startTime, mContext);
        Calendar endTimeDate = Utils.convertToCalendarTime(endTime, mContext);
        if(startTimeDate.before(rightNow)&&endTimeDate.after(rightNow)){
            Log.i(TAG, "New alarm is within current day's timeframe.  Starting RepeatingAlarm.");
            return true;
        } else {
            Log.i(TAG, "New alarm's repeating timeframe has either not begun or has already passed.");
            return false;
        }
    }

    private Calendar nextAlarmTime(String time){
        Calendar alarmTime = Utils.convertToCalendarTime(time, mContext);
        Calendar rightNow = Calendar.getInstance();
        if(alarmTime.after(rightNow)){
            return alarmTime;
        } else {
            //Alarm time was earlier today
            alarmTime.add(Calendar.DATE,1);
            return alarmTime;
        }
    }
}
