package com.sean.takeastand.alarmprocess;

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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.sean.takeastand.storage.FixedAlarmSchedule;
import com.sean.takeastand.storage.ScheduleDatabaseAdapter;
import com.sean.takeastand.util.Utils;

import java.util.ArrayList;
import java.util.Calendar;
/*
   This receiver is registered for device boots.  If the device is powered on, it
   will receive an intent and check to see if any schedules should be running.  This class is very
   similar to StartScheduleReceiver.
 */
/**
 * Created by Sean on 2014-11-10.
 */
public class BootReceiver extends BroadcastReceiver
{
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        ArrayList<FixedAlarmSchedule> fixedAlarmSchedules =
                new ScheduleDatabaseAdapter(context).getFixedAlarmSchedules();
        if(!fixedAlarmSchedules.isEmpty()){
            FixedAlarmSchedule todayAlarm = Utils.findTodaysSchedule(fixedAlarmSchedules);
            if(!(todayAlarm.getUID()== -100) && todayAlarm.getActivated()){
                Calendar rightNow = Calendar.getInstance();
                Calendar startTime = todayAlarm.getStartTime();
                Calendar endTime = todayAlarm.getEndTime();
                if(rightNow.after(startTime) && rightNow.before(endTime)){
                    new ScheduledRepeatingAlarm(context, todayAlarm).setRepeatingAlarm();
                    Toast.makeText(context, "Take A Stand Schedule is Running", Toast.LENGTH_SHORT).show();
                } else {
                    Log.i(TAG, "Today’s alarm has either already run or will run later.");
                }
            } else {
                Log.i(TAG, "There is no alarm with this UID for today in the database or it is not activated.");
            }
        } else {
            Log.i(TAG, "There are no alarms in the database." +
                    " There should not be an alarm set in AlarmManager");
        }

    }
}

