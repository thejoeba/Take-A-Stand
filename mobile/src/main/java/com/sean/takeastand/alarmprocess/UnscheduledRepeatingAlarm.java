package com.sean.takeastand.alarmprocess;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.sean.takeastand.storage.AlarmSchedule;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;

/**
 * Created by Sean on 2014-10-11.
 */
public class UnscheduledRepeatingAlarm implements RepeatingAlarm{

    private static final String TAG = "UnscheduledRepeatingAlarm";

    private Context mContext;
    private static final int REPEATING_ALARM_ID = 987654321;


    //For unscheduled alarms use this constructor
    public UnscheduledRepeatingAlarm(Context context){
        mContext = context;
    }

    @Override
    public void setRepeatingAlarm() {
        //Will check sharedpreferences for user defaults for alarms
        double alarmPeriodMinutes = .5;  //In future will get from user sharedpreferences
        double alarmTimeInMillis = alarmPeriodMinutes * Constants.secondsInMinute  * Constants.millisecondsInSecond;
        long triggerTime = SystemClock.elapsedRealtime() + (long)alarmTimeInMillis;
        Log.i(TAG, "alarm time: " + triggerTime + "  current time: " +
                SystemClock.elapsedRealtime());
        setAlarm(triggerTime);
        updateStatusImage();
        Log.i(TAG, "New Non-Scheduled Alarm Set");
        Toast.makeText(mContext, "Set New Alarm", Toast.LENGTH_LONG).show();
    }

    @Override
    public void setShortBreakAlarm() {
        long alarmTimeInMillis = 1 * Constants.secondsInMinute  * Constants.millisecondsInSecond;
        long triggerTime = SystemClock.elapsedRealtime() + alarmTimeInMillis;
        Log.i(TAG, "alarm time: " + triggerTime + "  current time: " +
                SystemClock.elapsedRealtime());
        setAlarm(triggerTime);
        Log.i(TAG, "Short Break Alarm set");
        Toast.makeText(mContext, "Set Short Break Alarm", Toast.LENGTH_LONG).show();
    }

    @Override
    public void setLongBreakAlarm() {
        long alarmTimeInMillis = 5 * Constants.secondsInMinute * Constants.millisecondsInSecond;
        long triggerTime = SystemClock.elapsedRealtime() + (long)alarmTimeInMillis;
        Log.i(TAG, "alarm time: " + triggerTime + "  current time: " +
                SystemClock.elapsedRealtime());
        setAlarm(triggerTime);
        Log.i(TAG, "Long Break Alarm set");
        Toast.makeText(mContext, "Set Long Break Alarm", Toast.LENGTH_LONG).show();
    }

    @Override
    public void cancelAlarm() {
        PendingIntent pendingIntent = createPendingIntent(mContext);
        AlarmManager am = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
        Log.i(TAG, "Alarm canceled");
        Toast.makeText(mContext, "Alarm Canceled", Toast.LENGTH_LONG).show();
        Utils.setCurrentMainActivityImage(mContext, Constants.NO_ALARM_RUNNING);
    }


    private void setAlarm(long triggerTime){
        PendingIntent pendingIntent = createPendingIntent(mContext);
        AlarmManager am = ((AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE));
        am.set(AlarmManager.ELAPSED_REALTIME, triggerTime, pendingIntent);
    }

    private PendingIntent createPendingIntent(Context context){
        Intent intent = new Intent(context, AlarmReceiver.class);
        return PendingIntent.getBroadcast(context, REPEATING_ALARM_ID, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private void updateStatusImage(){
        if(Utils.getCurrentImageStatus(mContext) == Constants.NO_ALARM_RUNNING ||
                Utils.getCurrentImageStatus(mContext) == Constants.NON_SCHEDULE_ALARM_RUNNING){
            Utils.setCurrentMainActivityImage(mContext, Constants.SCHEDULE_RUNNING);
        }
    }
}
