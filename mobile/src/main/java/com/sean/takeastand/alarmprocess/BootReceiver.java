package com.sean.takeastand.alarmprocess;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.sean.takeastand.storage.FixedAlarmSchedule;
import com.sean.takeastand.storage.ScheduleDatabaseAdapter;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Sean on 2014-11-10.
 */
public class BootReceiver extends BroadcastReceiver
{
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Toast.makeText(context, "Boot Receiver", Toast.LENGTH_SHORT);
        Log.i(TAG, "BootReceiver has received an intent");
        ArrayList<FixedAlarmSchedule> fixedAlarmSchedules =
                new ScheduleDatabaseAdapter(context).getFixedAlarmSchedules();
        if(!fixedAlarmSchedules.isEmpty()){
            FixedAlarmSchedule todayAlarm = findIfAlarmToday(Utils.getTodayWeekday(),
                    fixedAlarmSchedules, intent.getIntExtra(Constants.ALARM_UID, 0));
            if(!(todayAlarm.getUID()== -100) && todayAlarm.getActivated()){
                Calendar rightNow = Calendar.getInstance();
                Calendar startTime = todayAlarm.getStartTime();
                Calendar endTime = todayAlarm.getEndTime();
                if(rightNow.after(startTime) && rightNow.before(endTime)){
                    new ScheduledRepeatingAlarm(context, todayAlarm).setRepeatingAlarm();
                    Toast.makeText(context, "Take A Stand Schedule Now Running", Toast.LENGTH_SHORT);
                } else {
                    Log.i(TAG, "Today's alarm has either ran or will later.");
                }
            } else {
                Log.i(TAG, "There is no alarm with this UID for today in the database or it is not activated.");
            }
        } else {
            Log.i(TAG, "There are no alarms in the database." +
                    " There should not be an alarm set in AlarmManager");
        }

    }

    /*
    If there is an alarmSchedule for today's weekday, return it. If not
    return null.
     */
    private FixedAlarmSchedule findIfAlarmToday(int day, ArrayList<FixedAlarmSchedule> fixedAlarmSchedules,
                                                int UID){

        switch(day){
            case 1:
                for(FixedAlarmSchedule alarmSchedule : fixedAlarmSchedules){
                    //If alarmSchedule i has an alarm for Sunday, get
                    //this alarmSchedule to be used for today.
                    if(alarmSchedule.getUID()==UID && alarmSchedule.getSunday()){
                        return alarmSchedule;
                    }
                }
                break;
            case 2:
                for(FixedAlarmSchedule alarmSchedule : fixedAlarmSchedules) {
                    if (alarmSchedule.getUID()==UID && alarmSchedule.getMonday()) {
                        return alarmSchedule;
                    }
                }
                break;
            case 3:
                for(FixedAlarmSchedule alarmSchedule : fixedAlarmSchedules) {
                    if (alarmSchedule.getUID()==UID && alarmSchedule.getTuesday()) {
                        return alarmSchedule;
                    }
                }
                break;
            case 4:
                for(FixedAlarmSchedule alarmSchedule : fixedAlarmSchedules) {
                    if (alarmSchedule.getUID()==UID && alarmSchedule.getWednesday()) {
                        return alarmSchedule;
                    }
                }
                break;
            case 5:
                for(FixedAlarmSchedule alarmSchedule : fixedAlarmSchedules) {
                    if (alarmSchedule.getUID()==UID && alarmSchedule.getThursday()) {
                        return alarmSchedule;
                    }
                }
                break;
            case 6:
                for(FixedAlarmSchedule alarmSchedule : fixedAlarmSchedules) {
                    if (alarmSchedule.getUID()==UID && alarmSchedule.getFriday()) {
                        return alarmSchedule;
                    }
                }
                break;
            case 7:
                for(FixedAlarmSchedule alarmSchedule : fixedAlarmSchedules) {
                    if (alarmSchedule.getUID()==UID && alarmSchedule.getSaturday()) {
                        return alarmSchedule;
                    }
                }
                break;
        }
        //Return a dummy alarmSchedule with a UID of -100 which signals alarm was not found
        return new FixedAlarmSchedule(-100, false, null, null, null, 0, "", false,
                false, false, false, false, false, false);

    }

}

