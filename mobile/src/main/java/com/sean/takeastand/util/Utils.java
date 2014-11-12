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

import com.sean.takeastand.storage.AlarmSchedule;

import java.util.Arrays;
import java.util.Calendar;

/**
 * Created by Sean on 2014-10-04.
 * This class includes commonly used methods.
 */
public final class Utils {

    private static String TAG = "Utils ";

    /* Start time and end time are stored in the database as a string.
       This method converts the string to a Calendar object */
    public static Calendar convertToCalendarTime(String time){
        Calendar calendar = Calendar.getInstance();
        calendar = setCalendarTime(calendar, readHourFromString(time), readMinutesFromString(time));
        return calendar;
    }

    //Converts the Calendar object to a string for storage in the database
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

    //Used by other Utils methods and for setting the starting time for the time picker
    public static int readHourFromString(String alarmTime){
        boolean twelveHourClock = false;
        boolean pm = false;
        if(alarmTime.contains("am")){
            alarmTime = alarmTime.substring(0, alarmTime.length() - 3);
            twelveHourClock = true;
        }
        if(alarmTime.contains("pm")){
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

    //Used by other Utils methods and for setting the starting time for the time picker
    public static int readMinutesFromString(String alarmTime){
        if(alarmTime.contains("am")){
            alarmTime = alarmTime.substring(0, alarmTime.length() - 3);
        }
        if(alarmTime.contains("pm")){
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
            return 00;
        }
    }

    private static Calendar setCalendarTime(Calendar calendar, int hour, int minute){
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        return calendar;
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

    public static int getTodayWeekdayNum(){
        Calendar today = Calendar.getInstance();
        return today.get(Calendar.DAY_OF_WEEK);
    }

    //This method takes a string in 24-hour format.  If the user is using a 12-hour clock,
    //it formats the string accordingly, other it returns the 24-hour string back
    public static String getFormattedTimeString(String time, Context context){
        if (!DateFormat.is24HourFormat(context))
        {
            int hour = readHourFromString(time);
            String minutes = correctMinuteFormat(Integer.toString((readMinutesFromString(time))));
            if(hour >= 12)
            {
                if(hour == 12){
                    //Noon
                    return "12:" + minutes + " pm";
                } else {
                    //Afternoon
                    return Integer.toString(hour - 12) + ":" + minutes + " pm";
                }
            } else {
                if( hour == 0){
                    //12 am
                    return "12:" + minutes + " am";
                } else {
                    //Morning, string is ready to go after adding am
                    return time + " am";
                }
            }
        }
        else {
            //Device clock is 24-hour
            return time;
        }
    }

    public static String getFormattedCalendarTime(Calendar calendar, Context context){
        String calendarTime = calendarToTimeString(calendar);
        String formattedForAmPm = getFormattedTimeString(calendarTime, context);
        return formattedForAmPm;
    }


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

    public static void setRunningScheduledAlarm(Context context, int uid){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Constants.EVENT_SHARED_PREFERENCES, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.CURRENT_RUNNING_SCHEDULED_ALARM, uid);
        editor.commit();
    }

    public static int getRunningScheduledAlarm(Context context){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Constants.EVENT_SHARED_PREFERENCES, 0);
        return sharedPreferences.getInt(Constants.CURRENT_RUNNING_SCHEDULED_ALARM, -1);
    }

    public static void setCurrentMainActivityImage(Context context, int imageStatus){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Constants.EVENT_SHARED_PREFERENCES, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.MAIN_IMAGE_STATUS, imageStatus);
        editor.commit();
        Intent intent = new Intent(Constants.INTENT_MAIN_IMAGE);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static int getCurrentImageStatus(Context context){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Constants.EVENT_SHARED_PREFERENCES, 0);
        return sharedPreferences.getInt(Constants.MAIN_IMAGE_STATUS, 1);
    }

    public static String correctMinuteFormat(String minute){
        if(minute.length() == 1){
            minute = "0" + minute;
        }
        return minute;
    }

    //Used to convert alertType into String for SQL database storage
    //Method used by ScheduleDatabaseAdapter, MainActivity, and within Utils class
    public static String convertIntArrayToString(int[] array){
        if(array.length == 3){
            String intArrayString = "";
            for (int i = 0; i < array.length; i++){
                intArrayString += array[i] + "-";
            }
            Log.i(TAG, intArrayString);
            return intArrayString;
        } else {
            return "";
        }

    }

    //Used by ScheduleDatabaseAdapter and locally within Utils
    public static int[] convertStringToIntArray(String string){
        if(string.length()==6){
            int[] intArray = new int[3];
            int arrayIndex = 0;
            for (int i = 0; i < string.length(); i++) {
                if(Character.isDigit(string.charAt(i))){
                    intArray[arrayIndex] = string.charAt(i) - '0';
                    arrayIndex++ ;
                }
            }
            Log.i(TAG, Arrays.toString(intArray));
            return intArray;
        } else {
            return null;
        }
    }

    //Used by the AlarmService, ScheduleListAdapter, MainActivity, ScheduleListActivity
    public static int[] getDefaultAlertType(Context context){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);
        String alertType = sharedPreferences.getString(Constants.USER_ALERT_TYPE, "1-1-0");
        Log.i(TAG, alertType);
        return convertStringToIntArray(alertType);
    }

    //Used by UnscheduledRepeatingAlarm, ScheduleListAdapter, MainActivity, ScheduleListActivity
    public static int getDefaultFrequency(Context context){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);
        int frequency = sharedPreferences.getInt(Constants.USER_FREQUENCY, 20);
        Log.i(TAG, "Default frequency: " + frequency);
        return frequency;
    }

    //Used by MainActivity, ScheduledRepeatingAlarm, UnscheduledRepeatingAlarm
    public static int getDefaultDelay(Context context){
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
}
