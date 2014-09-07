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
        this.mContext = context;
    }

    private void cancelAlarm()
    {
        Intent localIntent = new Intent(this.mContext, StartDayReceiver.class);
        PendingIntent localPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, localIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        ((AlarmManager)this.mContext.getSystemService(Context.ALARM_SERVICE)).cancel(localPendingIntent);
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

    private void setAlarm(Date date)
    {
        long l = System.currentTimeMillis() + (date.getTime() - System.currentTimeMillis());
        Intent localIntent = new Intent(this.mContext, StartDayReceiver.class);
        PendingIntent localPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, localIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        ((AlarmManager)this.mContext.getSystemService(Context.ALARM_SERVICE)).set(0, l, localPendingIntent);
    }

    protected void deleteAlarm(int activated, int rowID)
    {
        this.alarmsDatabaseAdapter.deleteAlarm(rowID);
        if (activated == 1) {
            cancelAlarm();
        }
    }

    protected void editAlarm(int activated, String startTime, String endTime, int frequency, String title, String alarmType, int sunday, int monday, int tuesday, int wednesday, int thursday, int friday, int saturday, int rowID)
    {
        this.alarmsDatabaseAdapter.editAlarm(activated, startTime, endTime, frequency, title, alarmType, sunday, monday, tuesday, wednesday, thursday, friday, saturday, rowID);
        if (activated == 1)
        {
            new NextDayAlarmSetter().setNextDayAlarm(this.mContext, sunday, monday, tuesday, wednesday, thursday, friday, saturday);
            return;
        }
        cancelAlarm();
    }

    protected void newAlarm(int activated, String startTime, String endTime, int frequency, String title, String alarmType, int sunday, int monday, int tuesday, int wednesday, int thursday, int friday, int saturday)
    {
        this.alarmsDatabaseAdapter.newAlarm(activated, startTime, endTime, frequency, title, alarmType, sunday, monday, tuesday, wednesday, thursday, friday, saturday);
        if (activated == 1)
        {
            /*Date localDate = closestDay(sunday, monday, tuesday, wednesday, thursday, friday, saturday);
            if (localDate != null) {
                setAlarm(localDate);
            }*/
        }
    }
}
