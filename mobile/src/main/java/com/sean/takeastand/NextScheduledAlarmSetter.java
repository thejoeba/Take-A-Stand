package com.sean.takeastand;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.lang.reflect.Array;
import java.util.ArrayList;
import android.text.format.Time;
import java.util.Date;

/**
 * Created by Sean on 2014-09-06.
 */
public class NextScheduledAlarmSetter {

    /*
    Create an app level variable that says the next alarm date (time and date).  Only two
    classes can change it.  This one and RepeatingAlarmController.
     */
    Context mContext;

    public NextScheduledAlarmSetter(Context context){
        mContext = context;
    }

    public void scheduleNextAlarm(Time time, String startTime, String endTime){
        if(isAlarmToday(time.weekDay)){
            if(hasEndTimePassed(endTime)){
                setNextDayAlarm();
            } else {
                if(hasStartTimeBegan(startTime)){
                    //Has the start time began, should we start running the repeatingAlarm
                    RepeatingAlarmController repeatingAlarmController = new RepeatingAlarmController(mContext);
                    repeatingAlarmController.setNewAlarm();
                    return;
                } else {
                    //Set the repeatingAlarm to start later today
                    setAlarm(time, mContext);
                    return;
                }
            }
        } else{
            setNextDayAlarm();
        }
    }


    private boolean isAlarmToday(Time time){
        return false;
    }

    private boolean hasEndTimePassed(String endTime){
       return false;
    }

    private boolean hasStartTimeBegan(String startTime){
        return false;
    }

    private void setNextDayAlarm(Date date){
        Date tomorrow = new Date();
        //how to set to tomorrow?
        AlarmsDatabaseAdapter alarmsDatabaseAdapter = new AlarmsDatabaseAdapter(mContext);
        String alarmDetails = alarmsDatabaseAdapter.getNextActivatedDay();
        String[] alarmDetailsArray = alarmDetails.split("|");
        //Find the next weekday that was returned and set the start time; will mostly use position
        //0 and 1 of the array
    }

    private void cancelAllAlarms(Context context)
    {
        new RepeatingAlarmController(context).cancelAlarm();
    }

    private void setAlarm(Date date, Context context)
    {
        long l = System.currentTimeMillis() + (date.getTime() - System.currentTimeMillis());
        PendingIntent localPendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, StartDayReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT);
        ((AlarmManager)context.getSystemService(Context.ALARM_SERVICE)).set(0, l, localPendingIntent);
        //Update the app level variable
    }

    private ArrayList<Date> getNextDate(){

    }

}
