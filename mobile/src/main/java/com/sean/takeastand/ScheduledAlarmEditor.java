package com.sean.takeastand;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by Sean on 2014-09-03.
 */
public class ScheduledAlarmEditor {
    private static final String TAG = "ScheduledAlarmEditor";
    AlarmsDatabaseAdapter alarmsDatabaseAdapter;
    Context mContext;

    public ScheduledAlarmEditor(Context context)
    {
        mContext = context;
    }



    /*
    Create a method that checks if next activated day is today, check if any other schedules have today,
    if not set today, if yes inform user, already have alarm set for today; if no alarm set for today,
    check each day, but check each day to see if there is another active alarm scheduled for that day,
    stop searching if next active day of this alarm schedule is after another alarm schedule
     */
    /*
    Important that only days that another alarm doesn't have are possible;  Perhaps only display the
    available days, instead of all.
     */


    protected int[] checkIfDaysFree(){
        return alarmsDatabaseAdapter.getActivatedDays();
    }


    protected void editAlarm(int activated, String startTime, String endTime, int frequency,
                             String title, String alarmType, int sunday, int monday,
                             int tuesday, int wednesday, int thursday, int friday, int saturday,
                             int rowID)
    {
        alarmsDatabaseAdapter.editAlarm(activated, startTime, endTime, frequency, title,
                alarmType, sunday, monday, tuesday, wednesday, thursday, friday, saturday, rowID);
        if (activated == 1)
        {
            NextScheduledAlarmSetter nextScheduledAlarmSetter = new NextScheduledAlarmSetter(mContext);
            nextScheduledAlarmSetter.setNextAlarm();
        }
    }

    protected void newAlarm(int activated, String startTime, String endTime, int frequency,
                            String title, String alarmType, int sunday, int monday, int tuesday,
                            int wednesday, int thursday, int friday, int saturday)
    {
       alarmsDatabaseAdapter.newAlarm(activated, startTime, endTime, frequency, title,
                alarmType, sunday, monday, tuesday, wednesday, thursday, friday, saturday);
        if (activated == 1)
        {
            NextScheduledAlarmSetter nextScheduledAlarmSetter = new NextScheduledAlarmSetter(mContext);
            nextScheduledAlarmSetter.setNextAlarm();
        }
    }

    protected void deleteAlarm(int activated, int rowID)
    {
        alarmsDatabaseAdapter.deleteAlarm(rowID);
        NextScheduledAlarmSetter nextScheduledAlarmSetter = new NextScheduledAlarmSetter(mContext);
        nextScheduledAlarmSetter.setNextAlarm();
    }

}
