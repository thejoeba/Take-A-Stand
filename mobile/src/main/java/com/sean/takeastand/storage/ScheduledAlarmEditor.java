package com.sean.takeastand.storage;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sean.takeastand.alarmprocess.ScheduledRepeatingAlarm;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.alarmprocess.RepeatingAlarm;
import com.sean.takeastand.alarmprocess.StartScheduleReceiver;
import com.sean.takeastand.util.Utils;

import java.util.Calendar;

/**
 * Created by Sean on 2014-09-03.
 */
public class ScheduledAlarmEditor {

    private static final String TAG = "ScheduledAlarmEditor";
    private AlarmsDatabaseAdapter alarmsDatabaseAdapter;
    private Context mContext;


    public ScheduledAlarmEditor(Context context)
    {
        mContext = context;
        alarmsDatabaseAdapter = new AlarmsDatabaseAdapter(context);
    }
    /*
    This method will be used by the activity class in order to know which checkboxes to make
    checkable
     */


    public void newAlarm(boolean activated,  String alarmType, String startTime, String endTime, int frequency,
                            String title, boolean sunday, boolean monday, boolean tuesday,
                            boolean wednesday, boolean thursday, boolean friday, boolean saturday)
    {
       alarmsDatabaseAdapter.newAlarm(activated, alarmType, startTime, endTime, frequency, title,
                sunday, monday, tuesday, wednesday, thursday, friday, saturday);
        if (activated)
        {
            int UID = new AlarmsDatabaseAdapter(mContext).getLastRowID();
            setDailyRepeatingAlarm(UID, startTime);
            //If new alarm is meant to run this day
            if(Utils.isTodayActivated(sunday, monday, tuesday, wednesday, thursday, friday, saturday)){
                if(checkToSetRepeatingAlarm(startTime, endTime)){
                    //Because we are within an if statement where activated is true, put true in place
                    //of activated
                    AlarmSchedule newAlarmSchedule = new AlarmSchedule(UID, true, alarmType,
                            Utils.convertToCalendarTime(startTime), Utils.convertToCalendarTime(endTime),
                            frequency, title, sunday, monday, tuesday, wednesday, thursday, friday,
                            saturday);
                    new ScheduledRepeatingAlarm(mContext, newAlarmSchedule).setRepeatingAlarm();
                }
            } else {
                Log.i(TAG, "New alarm is not activated for today.  Not beginning repeatingAlarm.");
            }
        } else {
            Log.i(TAG, "Alarm is not activated");
        }
    }

    public void editAlarm(boolean activated, String startTime, String endTime, int frequency,
                          String title, String alarmType, boolean sunday, boolean monday,
                          boolean tuesday, boolean wednesday, boolean thursday, boolean friday, boolean saturday,
                          int rowID){

        alarmsDatabaseAdapter.editAlarm(activated, alarmType,startTime, endTime, frequency, title,
                sunday, monday, tuesday, wednesday, thursday, friday, saturday, rowID);
        cancelDailyRepeatingAlarm(rowID);
        if (activated)
        {
            setDailyRepeatingAlarm(rowID, startTime);
            //If new alarm is meant to run this day
            if(Utils.isTodayActivated(sunday, monday, tuesday, wednesday, thursday, friday, saturday)){
                if(checkToSetRepeatingAlarm(startTime, endTime)){
                    //Because we are within an if statement where activated is true, put true in place
                    //of activated
                    AlarmSchedule newAlarmSchedule = new AlarmSchedule(rowID, true, alarmType,
                            Utils.convertToCalendarTime(startTime), Utils.convertToCalendarTime(endTime),
                            frequency, title, sunday, monday, tuesday, wednesday, thursday, friday,
                            saturday);
                    new ScheduledRepeatingAlarm(mContext, newAlarmSchedule).setRepeatingAlarm();
                }
            } else {
                Log.i(TAG, "New alarm is not activated for today.  Not beginning repeatingAlarm.");
            }
        }
    }

    public void deleteAlarm(AlarmSchedule alarmSchedule){
        cancelDailyRepeatingAlarm(alarmSchedule.getUID());
        alarmsDatabaseAdapter.deleteAlarm(alarmSchedule.getUID());
    }

    private void setDailyRepeatingAlarm(int UID, String time){
        Log.i(TAG, "Set new daily repeating alarm");
        Intent intent = new Intent(mContext, StartScheduleReceiver.class);
        intent.putExtra(Constants.ALARM_UID, UID);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(mContext, UID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        ((AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE))
                .setRepeating(AlarmManager.RTC_WAKEUP, Utils.calendarToRTCMillis(nextAlarmTime(time)),
                        AlarmManager.INTERVAL_DAY, pendingIntent);
        Log.i(TAG, "Set Daily Repeating alarm for " + Long.toString(Utils.calendarToRTCMillis(nextAlarmTime(time))) +
                " current time " + Long.toString(System.currentTimeMillis()));
    }

    private void cancelDailyRepeatingAlarm(int UID){
        Log.i(TAG, "Canceled a daily repeating alarm");
        Intent intent = new Intent(mContext, StartScheduleReceiver.class);
        intent.putExtra(Constants.ALARM_UID, UID);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(mContext, UID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    private Calendar nextAlarmTime(String time){
        Calendar alarmTime = Utils.convertToCalendarTime(time);
        Calendar rightNow = Calendar.getInstance();
        if(alarmTime.after(rightNow)){
            return alarmTime;
        } else {
            //Alarm time was earlier today
            alarmTime.add(Calendar.DATE,1);
            return alarmTime;
        }
    }

    private boolean checkToSetRepeatingAlarm(String startTime, String endTime){
        //Set repeating alarm if in between start and end time
        Calendar rightNow = Calendar.getInstance();
        Calendar startTimeDate = Utils.convertToCalendarTime(startTime);
        Calendar endTimeDate = Utils.convertToCalendarTime(endTime);
        if(startTimeDate.before(rightNow)&&endTimeDate.after(rightNow)){
            Log.i(TAG, "New alarm is within current day's timeframe.  Starting RepeatingAlarm.");
            return true;
        } else {
            Log.i(TAG, "New alarm's repeating timeframe has either not begun or has already passed.");
            return false;
        }
    }
}
