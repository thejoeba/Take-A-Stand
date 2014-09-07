package com.sean.takeastand;

/**
 * Created by Sean on 2014-09-03.
 */
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class RepeatingAlarmController
{
    private static final String TAG = "RepeatingAlarmExecutive";
    double mAlarmPeriodMinutes = 0.5D;
    Context mContext;
    /*
    Once done testing, convert all doubles to longs or int
     */
    /*
    Create an app level variable that says the next alarm date (time and date).  Only two
    classes change it.  This NextScheduledAlarmSetter and this class.
     */

    public RepeatingAlarmController(Context context)
    {
        this.mContext = context;
    }

    private void endService(Context context)
    {
        Intent localIntent = new Intent("KillService");
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
    }

    private void startService(Context context)
    {
        Log.i(TAG, "onClick");
        context.startService(new Intent(context, StepService.class));
    }

    public void cancelAlarm()
    {
        Intent localIntent = new Intent(this.mContext, AlarmReceiver.class);
        PendingIntent localPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, localIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        ((AlarmManager)this.mContext.getSystemService(Context.ALARM_SERVICE).cancel(localPendingIntent));
        Log.i(TAG, "Alarm canceled");
    }

    public void setFiveMinuteAlarm()
    {
        double d = 0.333D * 60 * 1000;
        Long triggerTime = Double.doubleToLongBits(SystemClock.elapsedRealtime() + d);
        Log.i(TAG, "alarm time: " + triggerTime);
        Intent localIntent = new Intent(this.mContext, AlarmReceiver.class);
        PendingIntent localPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, localIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        ((AlarmManager)this.mContext.getSystemService(Context.ALARM_SERVICE)).set(2, triggerTime,
                localPendingIntent);
        Log.i(TAG, "Five Minute Alarm set");
    }

    public void setOneMinuteAlarm()
    {
        double d = 0.166D * 60 * 1000;
        Long triggerTime = Double.doubleToLongBits(SystemClock.elapsedRealtime() + d);
        Log.i(TAG, "alarm time: " + triggerTime);
        Intent localIntent = new Intent(this.mContext, AlarmReceiver.class);
        PendingIntent localPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, localIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        ((AlarmManager)this.mContext.getSystemService(Context.ALARM_SERVICE)).set(2, triggerTime,
                localPendingIntent);
        Log.i(TAG, "One Minute Alarm set");
    }

    public void setNewAlarm()
    {
        double d = this.mAlarmPeriodMinutes * 60 * 1000;
        Long triggerTime = Double.doubleToLongBits(SystemClock.elapsedRealtime() + d);
        Log.i(TAG, "alarm time: " + triggerTime);
        Intent localIntent = new Intent(this.mContext, AlarmReceiver.class);
        PendingIntent localPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, localIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        ((AlarmManager)this.mContext.getSystemService(Context.ALARM_SERVICE)).set(2, triggerTime,
                localPendingIntent);
        Log.i(TAG, "Stood Alarm set");
    }
}
