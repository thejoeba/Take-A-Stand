/*
 * Copyright (C) 2014 Sean Allen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.sean.takeastand.alarmprocess;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sean.takeastand.storage.FixedAlarmSchedule;
import com.sean.takeastand.storage.ScheduleDatabaseAdapter;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;

import java.util.ArrayList;

/* When the user creates a new alarm, a daily repeating alarm is set in the Android system’s
AlarmManager class.  When that alarm goes off,  an intent is sent which is received by this class.
This class is responsible then for starting scheduled alarms.  It does that by first figuring out
which alarm is scheduled for today and checks to see that the intent it received is for that alarm
(since it most likely will be receiving multiple intents throughout the day, one intent for each
alarm schedule).  Once it receives the intent that is for today’s alarm it sets a
ScheduledRepeatingAlarm which continues repeating throughout the day until the end time. */




/**
 * Created by Sean on 2014-09-03.
 */
public class StartScheduleReceiver extends BroadcastReceiver
{
    /*This class in the future will set an EndSchedule intent for an EndScheduleReceiver class
    that cancels everything and launches a statistics page or a page indicating how much the
    user has stood today.  The reason I would like to implement this is because what if the end
    time is for 5:30 and the last repeating alarm was at 5:25. That means the image won’t be set
    to reflect the fact the alarm process has ended until 5:45.  Setting this EndSchedule intent
    allows the user to know at 5:30 that no more alarms will be occurring.
    */
    private static final String TAG = "StartScheduleReceiver";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.i(TAG, "StartScheduleReceiver has received an intent");
        ArrayList<FixedAlarmSchedule> fixedAlarmSchedules =
                new ScheduleDatabaseAdapter(context).getFixedAlarmSchedules();
        if(!fixedAlarmSchedules.isEmpty()){
            FixedAlarmSchedule todayAlarm = findIfAlarmToday(Utils.getTodayWeekdayNum(),
                    fixedAlarmSchedules, intent.getIntExtra(Constants.ALARM_UID, 0));
            if(!(todayAlarm.getUID()== -100)){
                if(todayAlarm.getActivated()){
                    new ScheduledRepeatingAlarm(context, todayAlarm).setRepeatingAlarm();
                    Utils.setCurrentMainActivityImage(context, Constants.SCHEDULE_RUNNING);
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
