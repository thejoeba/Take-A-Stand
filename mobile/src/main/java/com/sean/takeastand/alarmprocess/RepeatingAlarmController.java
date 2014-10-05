package com.sean.takeastand.alarmprocess;

/**
 * Created by Sean on 2014-09-03.
 */
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.sean.takeastand.util.Constants;
import com.sean.takeastand.storage.AlarmSchedule;

public class RepeatingAlarmController
{
    private static final String TAG = "RepeatingAlarmController";
    private double mAlarmPeriodMinutes = 0.15;
    private Context mContext;
    private AlarmSchedule mCurrentAlarmSchedule;
    private static final int REPEATING_ALARM_ID = 987654321;
    private static boolean alarmSet;
    /*
    Once done testing, convert all doubles to longs or int
     */

    //For unscheduled alarms use this constructor
    public RepeatingAlarmController(Context context){
        mContext = context;
    }

    //For scheduled alarms use this constructor
    public RepeatingAlarmController(Context context, AlarmSchedule alarmSchedule)
    {
        mContext = context;
        mCurrentAlarmSchedule = alarmSchedule;
    }

    public boolean isAlarmSet(){
        return alarmSet;
    }

    public void setNewScheduledRepeatingAlarm()
    {
        //Will check mAlarmSchedule.getFrequency()
        //Will check mAlarmSchedule.alarmType()
        double alarmTimeInMillis = mAlarmPeriodMinutes * Constants.secondsInMinute  * Constants.millisecondsInSecond;
        Long triggerTime = SystemClock.elapsedRealtime() + (long)alarmTimeInMillis;
        Log.i(TAG, "alarm time: " + triggerTime + "  current time: " +
                SystemClock.elapsedRealtime());
        PendingIntent pendingIntent = createPendingIntent(mContext, mCurrentAlarmSchedule);
        AlarmManager am = ((AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE));
        am.set(AlarmManager.ELAPSED_REALTIME, triggerTime, pendingIntent);
        alarmSet = true;
        Log.i(TAG, "New Scheduled Repeating Alarm Set");
        Toast.makeText(mContext, "Set New Alarm",
                Toast.LENGTH_LONG).show();
    }

    public void setNonScheduleRepeatingAlarm()
    {
        //Will check sharedPreferences for alarmType and frequency
        double alarmTimeInMillis = mAlarmPeriodMinutes * Constants.secondsInMinute  * Constants.millisecondsInSecond;
        Long triggerTime = SystemClock.elapsedRealtime() + (long)alarmTimeInMillis;
        Log.i(TAG, "alarm time: " + triggerTime + "  current time: " +
                SystemClock.elapsedRealtime());
        PendingIntent pendingIntent = createNonScheduledPendingIntent(mContext);
        AlarmManager am = ((AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE));
        am.set(AlarmManager.ELAPSED_REALTIME, triggerTime, pendingIntent);
        alarmSet = true;
        Log.i(TAG, "New Non-Scheduled Alarm Set");
        Toast.makeText(mContext, "Set New Alarm",
                Toast.LENGTH_LONG).show();
    }

    public void setOneMinuteAlarm()
    {
        double alarmTimeInMillis = 0.166D * Constants.secondsInMinute  * Constants.millisecondsInSecond;
        Long triggerTime = SystemClock.elapsedRealtime() + (long)alarmTimeInMillis;
        Log.i(TAG, "alarm time: " + triggerTime + "  current time: " +
                SystemClock.elapsedRealtime());
        //If scheduled:
        //Will check mAlarmSchedule.alarmType()
        //Will set pending intent with schedule
        //If unscheduled will check shared preferences for alarm type, no schedule in intent
        //Have this method take parameter boolean scheduled and then add if statements to act accordingly
        PendingIntent pendingIntent = createPendingIntent(mContext, mCurrentAlarmSchedule);
        AlarmManager am = ((AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE));
        am.set(AlarmManager.ELAPSED_REALTIME, triggerTime, pendingIntent);
        Log.i(TAG, "One Minute Alarm set");
        Toast.makeText(mContext, "Set One Minute Alarm",
                Toast.LENGTH_LONG).show();
        alarmSet = true;
    }

    public void setFiveMinuteAlarm()
    {
        double alarmTimeInMillis = 0.25D * Constants.secondsInMinute * Constants.millisecondsInSecond;
        Long triggerTime = SystemClock.elapsedRealtime() + (long)alarmTimeInMillis;
        Log.i(TAG, "alarm time: " + triggerTime + "  current time: " +
                SystemClock.elapsedRealtime());
        //If scheduled:
        //Will check mAlarmSchedule.alarmType()
        //Will set pending intent with schedule
        //If unscheduled will check shared preferences for alarm type, no schedule in intent
        //Have this method take parameter boolean scheduled and then add if statements to act accordingly
        PendingIntent pendingIntent = createPendingIntent(mContext, mCurrentAlarmSchedule);
        AlarmManager am = ((AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE));
        am.set(AlarmManager.ELAPSED_REALTIME, triggerTime,pendingIntent);
        Log.i(TAG, "Five Minute Alarm set");
        Toast.makeText(mContext, "Set Five Minute Alarm",
                Toast.LENGTH_LONG).show();
        alarmSet = true;
    }

    private PendingIntent createPendingIntent(Context context, AlarmSchedule alarmSchedule){
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(Constants.ALARM_SCHEDULE, (Parcelable)alarmSchedule);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REPEATING_ALARM_ID, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        return pendingIntent;
    }

    private PendingIntent createNonScheduledPendingIntent(Context context){
        //Creates without the alarmSchedule
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REPEATING_ALARM_ID, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        return pendingIntent;
    }

    public void cancelAlarm()
    {
        PendingIntent pendingIntent = createPendingIntent(mContext, mCurrentAlarmSchedule);
        AlarmManager am = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
        Log.i(TAG, "Alarm canceled");
        Toast.makeText(mContext, "Alarm Canceled",
                Toast.LENGTH_LONG).show();
        alarmSet = false;
    }

}
