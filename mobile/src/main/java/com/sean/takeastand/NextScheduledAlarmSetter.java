package com.sean.takeastand;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import android.text.format.Time;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by Sean on 2014-09-06.
 */
public class NextScheduledAlarmSetter {

    /*
    Create an app level variable that says the next alarm date (time and date).  Only one class
    can change it: RepeatingAlarmController.
     */
    Context mContext;

    public NextScheduledAlarmSetter(Context context){
        mContext = context;
    }


    /*
    There is some repetition in here; definitely scope for spinning off at least one new method
     */
    public void setNextAlarm(){
        Calendar rightNow = Calendar.getInstance();
        AlarmsDatabaseAdapter alarmsDatabaseAdapter = new AlarmsDatabaseAdapter(mContext);
        String alarmDetails = alarmsDatabaseAdapter.getNextActivatedDay(rightNow.getTime());
        String[] alarmDetailsArray = alarmDetails.split("|");
        Calendar nextAlarmDate = Calendar.getInstance();
        nextAlarmDate.setTime(readWeekDayFromString(alarmDetailsArray[0]));
        nextAlarmDate.add(Calendar.HOUR_OF_DAY, readHourFromString(alarmDetailsArray[1]));
        nextAlarmDate.add(Calendar.MINUTE, readMinutesFromString(alarmDetailsArray[1]));
        nextAlarmDate.add(Calendar.SECOND, 0);
        Calendar nextAlarmEndTime = nextAlarmDate;
        nextAlarmEndTime.set(Calendar.HOUR_OF_DAY, readHourFromString(alarmDetailsArray[2]));
        nextAlarmEndTime.set(Calendar.MINUTE, readMinutesFromString(alarmDetailsArray[2]));
        nextAlarmEndTime.set(Calendar.SECOND, 0);
        if(nextAlarmDate.after(rightNow)){
            setAlarm(nextAlarmDate.getTime(), mContext);
        } else if (nextAlarmDate.before(rightNow) && nextAlarmEndTime.after(rightNow)){
            //Start the repeating alarm now
            RepeatingAlarmController repeatingAlarmController = new RepeatingAlarmController(mContext);
            repeatingAlarmController.setNewAlarm();
        } else {
            Calendar tomorrow = Calendar.getInstance();
            tomorrow.add(Calendar.DATE, 1);
            alarmDetails = alarmsDatabaseAdapter.getNextActivatedDay(tomorrow.getTime());
            String[] nextAlarmDetailsArray = alarmDetails.split("|");
            nextAlarmDate.setTime(readWeekDayFromString(nextAlarmDetailsArray[0]));
            nextAlarmDate.add(Calendar.HOUR_OF_DAY, readHourFromString(nextAlarmDetailsArray[1]));
            nextAlarmDate.add(Calendar.MINUTE, readMinutesFromString(nextAlarmDetailsArray[1]));
            nextAlarmDate.add(Calendar.SECOND, 0);
            setAlarm(nextAlarmDate.getTime(), mContext);
        }
    }

    private Date readWeekDayFromString(String weekday) {
        Calendar scheduleDay;
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        int dayOfWeek = today.get(Calendar.DAY_OF_WEEK);
        //Inspiration: http://www.coderanch.com/t/385117/java/java/date-Monday
        if(weekday.equals("sunday")){
            int days = (Calendar.SATURDAY - dayOfWeek + 1) % 7;
            today.add(Calendar.DAY_OF_YEAR, days);
            return today.getTime();
        }else if (weekday.equals("monday")){
            int days = (Calendar.SATURDAY - dayOfWeek + 2) % 7;
            today.add(Calendar.DAY_OF_YEAR, days);
            return today.getTime();
        } else if (weekday.equals("tuesday")){
            int days = (Calendar.SATURDAY - dayOfWeek + 3) % 7;
            today.add(Calendar.DAY_OF_YEAR, days);
            return today.getTime();
        } else if (weekday.equals("wednesday")){
            int days = (Calendar.SATURDAY - dayOfWeek + 4) % 7;
            today.add(Calendar.DAY_OF_YEAR, days);
            return today.getTime();
        } else if (weekday.equals("thursday")){
            int days = (Calendar.SATURDAY - dayOfWeek + 5) % 7;
            today.add(Calendar.DAY_OF_YEAR, days);
            return today.getTime();
        } else if (weekday.equals("friday")){
            int days = (Calendar.SATURDAY - dayOfWeek + 6) % 7;
            today.add(Calendar.DAY_OF_YEAR, days);
            return today.getTime();
        } else if (weekday.equals("saturday")){
            int days = (Calendar.SATURDAY - dayOfWeek + 7) % 7;
            today.add(Calendar.DAY_OF_YEAR, days);
            return today.getTime();
        } else {
            return today.getTime();
        }
    }

    private int readHourFromString(String alarmTime){
        String[] time = alarmTime.split(":");
        return Integer.valueOf(time[0]);
    }

    private int readMinutesFromString(String alarmTime){
        String[] time = alarmTime.split(":");
        return Integer.valueOf(time[1]);
    }

    private void setAlarm(Date date, Context context)
    {
        long l = System.currentTimeMillis() + (date.getTime() - System.currentTimeMillis());
        PendingIntent localPendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, StartDayReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT);
        ((AlarmManager)context.getSystemService(Context.ALARM_SERVICE)).set(0, l, localPendingIntent);
        //Update the app level variable
    }

}
