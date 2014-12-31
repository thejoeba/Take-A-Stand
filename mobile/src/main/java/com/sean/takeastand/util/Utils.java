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

package com.sean.takeastand.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateFormat;
import android.util.Log;

import com.heckbot.standdtector.StandDtectorTM;
import com.sean.takeastand.R;
import com.sean.takeastand.storage.AlarmSchedule;
import com.sean.takeastand.storage.FixedAlarmSchedule;
import com.sean.takeastand.storage.GoogleFitService;
import com.sean.takeastand.storage.ScheduleDatabaseAdapter;
import com.sean.takeastand.storage.StoodLogsAdapter;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Sean on 2014-10-04.
 * This class contains commonly used methods.
 */
public final class Utils {

    private static String TAG = "Utils ";

    //Start time and end time are stored in the database as a string.
    //This method converts the string to a Calendar object
    //Used by ScheduleEditor, ScheduleDatabaseAdapter, scheduleListAdapter, and within Utils
    public static Calendar convertToCalendarTime(String time, Context context){
        Calendar calendar = Calendar.getInstance();
        calendar = setCalendarTime(calendar, readHourFromString(time, context),
                readMinutesFromString(time, context));
        return calendar;
    }

    //Converts the Calendar object to a string for storage in the database
    //Used by TimePickerFragment, ScheduleEditor, and within Utils
    public static String calendarToTimeString(Calendar calendar){
        String time = Integer.toString(calendar.get(Calendar.HOUR_OF_DAY));
        time += ":";
        //In order to account for the zero that precedes minutes less than 10
        if(calendar.get(Calendar.MINUTE)<10){
            time+="0";
        }
        time += Integer.toString(calendar.get(Calendar.MINUTE));
        return time;
    }

    //Used by TimePickerFragment and within Utils
    public static int readHourFromString(String alarmTime, Context context){
        boolean twelveHourClock = false;
        boolean pm = false;
        if(alarmTime.contains(context.getString(R.string.AM))){
            alarmTime = alarmTime.substring(0, alarmTime.length() - 3);
            twelveHourClock = true;
        }
        if(alarmTime.contains(context.getString(R.string.PM))){
            alarmTime = alarmTime.substring(0, alarmTime.length() - 3);
            twelveHourClock = true;
            pm = true;
        }
        String hour = alarmTime.substring(0, alarmTime.indexOf(":"));
        if(alarmTime.length() == 5 || alarmTime.length() == 4){
            if(twelveHourClock && pm){
                if(hour.equals("12")){
                    //Noon
                    return 12;
                } else{
                    //Afternoon
                    return (Integer.valueOf(hour) + 12);
                }
            } else if (twelveHourClock && !pm && hour.equals("12")){
                //12 am
                return  0;
            } else {
                //Morning
                return Integer.valueOf(hour);
            }
        } else {
            Log.e(TAG, "alarmTime string is " + Integer.toString(alarmTime.length())
                    + " characters long, not 4 or 5.");
            return 12;
        }
    }

    //Used by TimePickerFragment and within Utils
    public static int readMinutesFromString(String alarmTime, Context context){
        if(alarmTime.contains(context.getString(R.string.AM)) ||
                alarmTime.contains(context.getString(R.string.PM)) ){
            alarmTime = alarmTime.substring(0, alarmTime.length() - 3);
        }
        if(alarmTime.length() == 5){
            String time = alarmTime.substring(alarmTime.indexOf(":") + 1, 5);
            return Integer.valueOf(time);
        } else if (alarmTime.length() == 4) {
            String time = alarmTime.substring(alarmTime.indexOf(":") + 1, 4);
            return Integer.valueOf(time);
        } else {
            Log.e(TAG, "alarmTime string is " + Integer.toString(alarmTime.length()) +
                    " characters long, not 4 or 5.");
            return 0;
        }
    }

    //Can't store booleans in a SQL database;
    //Retrieval method for database
    //Used by the ScheduleDatabaseAdapter and ScheduleListAdapter
    public static boolean convertIntToBoolean(int value){
        return (value == 1);
    }

    //Convert for storage in database
    //Used by the ScheduleDatabaseAdapter and ScheduleListAdapter
    public static int convertBooleanToInt(boolean bool){
       return bool ? 1 : 0;
    }

    //Used by BootReceiver, StartScheduleReceiver, ScheduleEditor, and within Utils
    public static int getTodayWeekdayNum(){
        Calendar today = Calendar.getInstance();
        return today.get(Calendar.DAY_OF_WEEK);
    }

    //This method takes a string in 24-hour format.  If the user is using a 12-hour clock,
    //it formats the string accordingly, other it returns the 24-hour string back
    //Used by ScheduleListAdapter and within Utils
    public static String getFormattedTimeString(String time, Context context){
        if (!DateFormat.is24HourFormat(context))
        {
            int hour = readHourFromString(time, context);
            String minutes = correctMinuteFormat(Integer.toString((readMinutesFromString(time, context))));
            if(hour >= 12)
            {
                if(hour == 12){
                    //Noon
                    return "12:" + minutes + " " + context.getString(R.string.PM);
                } else {
                    //Afternoon
                    return Integer.toString(hour - 12) + ":" + minutes + " " +
                            context.getString(R.string.PM);
                }
            } else {
                if( hour == 0){
                    //12 am
                    return "12:" + minutes + " " + context.getString(R.string.AM);
                } else {
                    //Morning, string is ready to go after adding am
                    return time + " " + context.getString(R.string.AM);
                }
            }
        }
        else {
            //Device clock is 24-hour
            return time;
        }
    }

    //Similar to getFormattedTimeString but takes a calendar object as its argument.
    //Used by ScheduleListAdapter and within Utils.”
    public static String getFormattedCalendarTime(Calendar calendar, Context context){
        String calendarTime = calendarToTimeString(calendar);
        return getFormattedTimeString(calendarTime, context);
    }

    //Used by ScheduleEditor
    public static boolean isTodayActivated(boolean sunday, boolean monday, boolean tuesday,
                                           boolean wednesday, boolean thursday, boolean friday,
                                           boolean saturday){
        switch(getTodayWeekdayNum()){
            case 1:
                return sunday;
            case 2:
                return monday;
            case 3:
                return tuesday;
            case 4:
                return wednesday;
            case 5:
                return thursday;
            case 6:
                return friday;
            case 7:
                return saturday;
            default:
                return false;
        }
    }

    //Used by ScheduleListAdapter and ScheduleEditor
    public static boolean isTodayActivated(AlarmSchedule alarmSchedule){
        switch(getTodayWeekdayNum()){
            case 1:
                return alarmSchedule.getSunday();
            case 2:
                return alarmSchedule.getMonday();
            case 3:
                return alarmSchedule.getTuesday();
            case 4:
                return alarmSchedule.getWednesday();
            case 5:
                return alarmSchedule.getThursday();
            case 6:
                return alarmSchedule.getFriday();
            case 7:
                return alarmSchedule.getSaturday();
            default:
                return false;
        }
    }

    //Used by ScheduledRepeatingAlarm and AlarmReceiver
    public static void setRunningScheduledAlarm(Context context, int uid){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Constants.EVENT_SHARED_PREFERENCES, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.CURRENT_RUNNING_SCHEDULED_ALARM, uid);
        editor.commit();
    }

    //Used by ScheduleEditor
    public static int getRunningScheduledAlarm(Context context){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Constants.EVENT_SHARED_PREFERENCES, 0);
        return sharedPreferences.getInt(Constants.CURRENT_RUNNING_SCHEDULED_ALARM, -1);
    }

    //Used by AlarmService, ScheduleEditor, ScheduledRepeatingAlarm, UnscheduledRepeatingAlarm,
    //ImageStatusFragment, AlarmReceiver, StartScheduleReceiver, and MainActivity
    public static void setImageStatus(Context context, int imageStatus){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Constants.EVENT_SHARED_PREFERENCES, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.MAIN_IMAGE_STATUS, imageStatus);
        editor.commit();
        Intent intent = new Intent(Constants.INTENT_MAIN_IMAGE);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    //Used by AlarmFragment and ImageStatusFragment. This method is used to find out
    //which images to display and which onClickListeners to set.
    public static int getImageStatus(Context context){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Constants.EVENT_SHARED_PREFERENCES, 0);
        return sharedPreferences.getInt(Constants.MAIN_IMAGE_STATUS, 1);
    }

    //Used by TimePickerFragment and within Utils
    public static String correctMinuteFormat(String minute){
        if(minute.length() == 1){
            minute = "0" + minute;
        }
        return minute;
    }

    //Used by the AlarmService, ScheduleListAdapter, MainActivity, ScheduleListActivity
    public static boolean[] getDefaultAlertType(Context context){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);
        boolean led = sharedPreferences.getBoolean(Constants.USER_ALERT_LED, true);
        boolean vibrate = sharedPreferences.getBoolean(Constants.USER_ALERT_VIBRATE, true);
        boolean sound = sharedPreferences.getBoolean(Constants.USER_ALERT_SOUND, false);
        return new boolean[]{led, vibrate, sound};
    }

    //Used by MainActivity and AlarmService
    public static boolean getRepeatAlerts(Context context){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);
        return sharedPreferences.getBoolean(Constants.USER_ALERT_FREQUENCY, true);
    }

    //Used by UnscheduledRepeatingAlarm, ScheduleListAdapter, MainActivity, ScheduleListActivity
    public static int getDefaultFrequency(Context context){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);
        int frequency = sharedPreferences.getInt(Constants.USER_FREQUENCY, 20);
        Log.i(TAG, "Default frequency: " + frequency);
        return frequency;
    }

    //Used by NotificationsActivity, ScheduledRepeatingAlarm, UnscheduledRepeatingAlarm
    public static int getNotificationReminderFrequency(Context context){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);
        int delay = sharedPreferences.getInt(Constants.USER_DELAY, 5);
        Log.i(TAG, "Default delay: " + delay);
        return delay;
    }

    //Used by AlarmService and MainActivity
    public static boolean getVibrateOverride(Context context){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);
        return sharedPreferences.getBoolean(Constants.VIBRATE_SILENT, true);
    }

    //The AlarmFragment uses the data stored in here to set it's text for the next
    //alarm time; this is used by ScheduledRepeatingAlarm and UnscheduledRepeatingAlarm
    public static void setNextAlarmTimeString(Calendar calendar, Context context){
        String nextAlarmTime = getFormattedCalendarTime(calendar, context);
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Constants.EVENT_SHARED_PREFERENCES, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.NEXT_ALARM_TIME_STRING, nextAlarmTime);
        editor.commit();
    }

    //Used by the AlarmFragment and ScheduledRepeatingAlarm
    public static String getNextAlarmTimeString(Context context){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Constants.EVENT_SHARED_PREFERENCES, 0);
        return sharedPreferences.getString(Constants.NEXT_ALARM_TIME_STRING, "");
    }

    //Used by the ScheduledRepeatingAlarm and UnscheduledRepeatingAlarm
    public static void setPausedTime(Calendar calendar, Context context){
        String pausedUntilTime = getFormattedCalendarTime(calendar, context);
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Constants.EVENT_SHARED_PREFERENCES, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.PAUSED_UNTIL_TIME, pausedUntilTime);
        editor.commit();
    }

    //Used by the AlarmFragment and ScheduledRepeatingAlarm
    public static String getPausedTime(Context context){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Constants.EVENT_SHARED_PREFERENCES, 0);
        return sharedPreferences.getString(Constants.PAUSED_UNTIL_TIME, "");
    }

    /*
   If there is an alarmSchedule for today's weekday, return it. If not
   return null.
    */
    public static FixedAlarmSchedule findTodaysSchedule(ArrayList<FixedAlarmSchedule> fixedAlarmSchedules){

        switch(getTodayWeekdayNum()){
            case 1:
                for(FixedAlarmSchedule alarmSchedule : fixedAlarmSchedules){
                    //If alarmSchedule i has an alarm for Sunday, get
                    //this alarmSchedule to be used for today.
                    if(alarmSchedule.getSunday()){
                        return alarmSchedule;
                    }
                }
                break;
            case 2:
                for(FixedAlarmSchedule alarmSchedule : fixedAlarmSchedules) {
                    if (alarmSchedule.getMonday()) {
                        return alarmSchedule;
                    }
                }
                break;
            case 3:
                for(FixedAlarmSchedule alarmSchedule : fixedAlarmSchedules) {
                    if (alarmSchedule.getTuesday()) {
                        return alarmSchedule;
                    }
                }
                break;
            case 4:
                for(FixedAlarmSchedule alarmSchedule : fixedAlarmSchedules) {
                    if (alarmSchedule.getWednesday()) {
                        return alarmSchedule;
                    }
                }
                break;
            case 5:
                for(FixedAlarmSchedule alarmSchedule : fixedAlarmSchedules) {
                    if (alarmSchedule.getThursday()) {
                        return alarmSchedule;
                    }
                }
                break;
            case 6:
                for(FixedAlarmSchedule alarmSchedule : fixedAlarmSchedules) {
                    if (alarmSchedule.getFriday()) {
                        return alarmSchedule;
                    }
                }
                break;
            case 7:
                for(FixedAlarmSchedule alarmSchedule : fixedAlarmSchedules) {
                    if (alarmSchedule.getSaturday()) {
                        return alarmSchedule;
                    }
                }
                break;
        }
        //Return a dummy alarmSchedule with a UID of -100 which signals alarm was not found
        return new FixedAlarmSchedule(-100, false, false, false, false, null, null, 0, "", false,
                false, false, false, false, false, false);

    }

    //Called by ScheduledRepeatingAlarm and ScheduleListAdapter
    public static void setScheduleTitle(String title, Context context, int UID){
        if(title.equals("")){
            ArrayList<FixedAlarmSchedule> fixedAlarmSchedules =
                    new ScheduleDatabaseAdapter(context).getFixedAlarmSchedules();
            int schedulePosition =  1;
            for (int i = 0; i < fixedAlarmSchedules.size(); i++)
            {
                if (UID == fixedAlarmSchedules.get(i).getUID())
                {
                    schedulePosition += fixedAlarmSchedules.indexOf(fixedAlarmSchedules.get(i));
                }
            }
            title = context.getString(R.string.schedule) + schedulePosition;
        }
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Constants.EVENT_SHARED_PREFERENCES, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.CURRENT_SCHEDULED_ALARM_TITLE, title);
        editor.commit();
    }

    //Used by MainActivity, ScheduledRepeatingAlarm, and UnscheduledRepeatingAlarm
    public static int getDefaultPauseAmount(Context context){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);
        return sharedPreferences.getInt(Constants.PAUSE_TIME, 30);
    }

    private static Calendar setCalendarTime(Calendar calendar, int hour, int minute){
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        return calendar;
    }

    public static void startSession(Context context, Integer sessionType){
        new StoodLogsAdapter(context).newSession(sessionType);
        if (context.getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0).getBoolean(Constants.DEVICE_STEP_DETECTOR_ENABLED, false)) {
            Intent startStepCounterIntent = new Intent(context, StandDtectorTM.class);
            startStepCounterIntent.setAction(com.heckbot.standdtector.Constants.START_DEVICE_STEP_COUNTER);
            context.startService(startStepCounterIntent);
        }
    }

    public static void endSession(Context context){
        Intent stopStepCounterIntent = new Intent(context, StandDtectorTM.class);
        stopStepCounterIntent.setAction(com.heckbot.standdtector.Constants.STOP_DEVICE_STEP_COUNTER);
        context.startService(stopStepCounterIntent);
        syncFit(context);
    }

    public static void syncFit (Context context) {
        if (context.getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0).getBoolean(Constants.GOOGLE_FIT_ENABLED, false)) {
            Intent insertIntent = new Intent(context, GoogleFitService.class);
            insertIntent.setAction("InsertData");
            context.startService(insertIntent);
        }
    }
    public static void getOldestFitSession (Context context) {
        if (context.getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0).getBoolean(Constants.GOOGLE_FIT_AUTHORIZED, false)) {
            Intent insertIntent = new Intent(context, GoogleFitService.class);
            insertIntent.setAction("GetOldestSession");
            context.startService(insertIntent);
        }
    }
}
