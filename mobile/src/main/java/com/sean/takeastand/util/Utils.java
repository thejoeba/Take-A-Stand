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
import android.util.Log;

import java.util.Calendar;

/**
 * Created by Sean on 2014-10-04.
 * This class includes commonly used methods.
 */
public final class Utils {

    private static String TAG = "Utils ";


    public static long calendarToRTCMillis(Calendar cal){
        return cal.getTimeInMillis();
    }

    public static Calendar convertToCalendarTime(String time){
        Calendar calendar = Calendar.getInstance();
        calendar = setCalendarTime(calendar, readHourFromString(time), readMinutesFromString(time));
        return calendar;
    }

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

    public static int readHourFromString(String alarmTime){
        if(alarmTime.length()==5 || alarmTime.length()==4){
            String time = alarmTime.substring(0, alarmTime.indexOf(":"));
            return Integer.valueOf(time);
        } else {
            Log.i(TAG, "alarmTime string is " + Integer.toString(alarmTime.length())
                    + " characters long, not 4 or 5.");
            return 24;
        }
    }

    public static int readMinutesFromString(String alarmTime){
        if(alarmTime.length()==5){
            String time = alarmTime.substring(alarmTime.indexOf(":") + 1, 5);
            return Integer.valueOf(time);
        } else if (alarmTime.length()==4) {
            String time = alarmTime.substring(alarmTime.indexOf(":") + 1, 4);
            return Integer.valueOf(time);
        } else {
            Log.i(TAG, "alarmTime string is " + Integer.toString(alarmTime.length()) +
                    " characters long, not 4 or 5.");
            return 61;
        }

    }

    private static Calendar setCalendarTime(Calendar calendar, int hour, int minute){
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        return calendar;
    }

    public static boolean convertIntToBoolean(int value){
        return (value == 1);
    }

    public static int convertBooleanToInt(boolean bool){
        if(bool){
            return 1;
        } else {
            return 0;
        }
    }

    public static int getTodayWeekday(){
        Calendar today = Calendar.getInstance();
        return today.get(Calendar.DAY_OF_WEEK);
    }

    public static boolean isTodayActivated(boolean sunday, boolean monday, boolean tuesday,
                                           boolean wednesday, boolean thursday, boolean friday,
                                           boolean saturday){
        switch(getTodayWeekday()){
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
        notifyImageUpdate(context);
    }

    private static void notifyImageUpdate(Context context){
        Log.i(TAG, "SendingIntent");
        Intent intent = new Intent(Constants.INTENT_MAIN_IMAGE);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static int getCurrentImageStatus(Context context){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Constants.EVENT_SHARED_PREFERENCES, 0);
        return sharedPreferences.getInt(Constants.MAIN_IMAGE_STATUS, 1);
    }

    public static String correctMinuteFormat(String minute){
        if(minute.length()==1){
            minute = "0" + minute;
        }
        return minute;
    }

    public static String convertIntArrayToString(int[] array){
        if(array.length==4){
            String intArrayString = "";
            for (int i = 0; i < array.length; i++){
                intArrayString += i + "-";
            }
            return intArrayString;
        } else {
            return "";
        }

    }

    public static int[] convertStringToIntArray(String string){
        if(string.length()==8){
            int[] intArray = new int[8];
            for (int i = 0; i < string.length(); i++) {
                if((string.charAt(i) >= '0') && (string.charAt(i) <= '9'))
                intArray[i] = Character.digit(string.charAt(i), 10);
            }
            return intArray;
        } else {
            return null;
        }
    }

}
