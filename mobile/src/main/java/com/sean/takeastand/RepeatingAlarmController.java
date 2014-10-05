package com.sean.takeastand;

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

public class RepeatingAlarmController
{
    private static final String TAG = "RepeatingAlarmController";
    double mAlarmPeriodMinutes = 0.15;
    Context mContext;
    AlarmSchedule mCurrentAlarmSchedule;
    private static final int REPEATING_ALARM_ID = 987654321;
    private static boolean alarmSet;
    /*
    Once done testing, convert all doubles to longs or int
     */
    /*
    Create an app level variable that says the next alarm date (time and date).  Only two
    classes change it.  This NextScheduledAlarmSetter and this class.
     */

    public RepeatingAlarmController(Context context){
        mContext = context;
    }


    public RepeatingAlarmController(Context context, AlarmSchedule alarmSchedule)
    {
        mContext = context;
        mCurrentAlarmSchedule = alarmSchedule;
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

    public void setFiveMinuteAlarm()
    {
        double alarmTimeInMillis = 0.25D * Constants.secondsInMinute * Constants.millisecondsInSecond;
        Long triggerTime = SystemClock.elapsedRealtime() + (long)alarmTimeInMillis;
        Log.i(TAG, "alarm time: " + triggerTime + "  current time: " +
                SystemClock.elapsedRealtime());
        PendingIntent pendingIntent = createPendingIntent(mContext, mCurrentAlarmSchedule);
        AlarmManager am = ((AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE));
        am.set(AlarmManager.ELAPSED_REALTIME, triggerTime,pendingIntent);
        Log.i(TAG, "Five Minute Alarm set");
        Toast.makeText(mContext, "Set Five Minute Alarm",
                Toast.LENGTH_LONG).show();
        alarmSet = true;
    }

    public void setOneMinuteAlarm()
    {
        double alarmTimeInMillis = 0.166D * Constants.secondsInMinute  * Constants.millisecondsInSecond;
        Long triggerTime = SystemClock.elapsedRealtime() + (long)alarmTimeInMillis;
        Log.i(TAG, "alarm time: " + triggerTime + "  current time: " +
                SystemClock.elapsedRealtime());
        PendingIntent pendingIntent = createPendingIntent(mContext, mCurrentAlarmSchedule);
        AlarmManager am = ((AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE));
        am.set(AlarmManager.ELAPSED_REALTIME, triggerTime, pendingIntent);
        Log.i(TAG, "One Minute Alarm set");
        Toast.makeText(mContext, "Set One Minute Alarm",
                Toast.LENGTH_LONG).show();
        alarmSet = true;
    }

    public void setNewRepeatingAlarm()
    {
        double alarmTimeInMillis = mAlarmPeriodMinutes * Constants.secondsInMinute  * Constants.millisecondsInSecond;
        Long triggerTime = SystemClock.elapsedRealtime() + (long)alarmTimeInMillis;
        Log.i(TAG, "alarm time: " + triggerTime + "  current time: " +
                SystemClock.elapsedRealtime());
        PendingIntent pendingIntent = createPendingIntent(mContext, mCurrentAlarmSchedule);
        AlarmManager am = ((AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE));
        am.set(AlarmManager.ELAPSED_REALTIME, triggerTime, pendingIntent);
        alarmSet = true;
        Log.i(TAG, "Stood Alarm set");
        Toast.makeText(mContext, "Set New Alarm",
                Toast.LENGTH_LONG).show();
    }

    private PendingIntent createPendingIntent(Context context, AlarmSchedule alarmSchedule){
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(Constants.ALARM_SCHEDULE, (Parcelable)alarmSchedule);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REPEATING_ALARM_ID, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        return pendingIntent;
    }

    public boolean isAlarmSet(){
        return alarmSet;
    }

}
