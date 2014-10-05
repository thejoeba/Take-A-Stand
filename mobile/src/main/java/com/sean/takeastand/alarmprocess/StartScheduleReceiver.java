package com.sean.takeastand.alarmprocess;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.sean.takeastand.util.Constants;
import com.sean.takeastand.storage.AlarmSchedule;
import com.sean.takeastand.storage.AlarmsDatabaseAdapter;
import com.sean.takeastand.util.Utils;

import java.util.ArrayList;

/**
 * Created by Sean on 2014-09-03.
 * AlarmSchedule Class
 */
public class StartScheduleReceiver extends BroadcastReceiver
{
    
    private static final String TAG = "StartScheduleReceiver";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.i(TAG, "StartScheduleReceiver has received an intent");
        Toast.makeText(context, "StartReceiver is starting an alarm schedule", Toast.LENGTH_LONG);
        ArrayList<AlarmSchedule> alarmSchedules =
                new AlarmsDatabaseAdapter(context).getAlarmSchedules();
        if(!alarmSchedules.isEmpty()){
            AlarmSchedule todayAlarm = findIfAlarmToday(Utils.getTodayWeekday(),
                    alarmSchedules, intent.getIntExtra(Constants.ALARM_UID, 0));
            if(!(todayAlarm.getUID()==-100)){
                if(todayAlarm.getActivated()){
                    setFirstAlarm(context, todayAlarm);
                } else {
                    Log.i(TAG, "Today's alarm is not activated.");
                }
            } else {
                Log.i(TAG, "There is no alarm with this UID for today in the database.");
            }
        } else {
            Log.i(TAG, "There are no alarms in the database." +
                    " There should not be an alarm set in AlarmManager");
        }
    }

    private void setFirstAlarm(Context context, AlarmSchedule alarmSchedule)
    {
        new RepeatingAlarmController(context, alarmSchedule).setNewScheduledRepeatingAlarm();
    }

    /*
    If there is an alarmSchedule for today's weekday, return it. If not
    return null.
     */
    private AlarmSchedule findIfAlarmToday(int day, ArrayList<AlarmSchedule> alarmSchedules, int UID){

        switch(day){
            case 1:
                for(AlarmSchedule alarmSchedule : alarmSchedules){
                    //If alarmSchedule i has an alarm for Sunday, get
                    //this alarmSchedule to be used for today.
                    if(alarmSchedule.getUID()==UID && alarmSchedule.getSunday()){
                        return alarmSchedule;
                    }
                }
                break;
            case 2:
                for(AlarmSchedule alarmSchedule : alarmSchedules) {
                    if (alarmSchedule.getUID()==UID && alarmSchedule.getMonday()) {
                        return alarmSchedule;
                    }
                }
                break;
            case 3:
                for(AlarmSchedule alarmSchedule : alarmSchedules) {
                    if (alarmSchedule.getUID()==UID && alarmSchedule.getTuesday()) {
                        return alarmSchedule;
                    }
                }
                break;
            case 4:
                for(AlarmSchedule alarmSchedule : alarmSchedules) {
                    if (alarmSchedule.getUID()==UID && alarmSchedule.getWednesday()) {
                        return alarmSchedule;
                    }
                }
                break;
            case 5:
                for(AlarmSchedule alarmSchedule : alarmSchedules) {
                    if (alarmSchedule.getUID()==UID && alarmSchedule.getThursday()) {
                        return alarmSchedule;
                    }
                }
                break;
            case 6:
                for(AlarmSchedule alarmSchedule : alarmSchedules) {
                    if (alarmSchedule.getUID()==UID && alarmSchedule.getFriday()) {
                        return alarmSchedule;
                    }
                }
                break;
            case 7:
                for(AlarmSchedule alarmSchedule : alarmSchedules) {
                    if (alarmSchedule.getUID()==UID && alarmSchedule.getSaturday()) {
                        return alarmSchedule;
                    }
                }
                break;
        }
        //Return a dummy alarmSchedule with a UID of -100 which signals alarm was not found
        return new AlarmSchedule(-100, false, "", null, null, 0, "", false,
                false, false, false, false, false, false);

    }

}
