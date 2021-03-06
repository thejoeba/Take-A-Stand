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

import com.Application;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.sean.takeastand.R;
import com.sean.takeastand.storage.FixedAlarmSchedule;
import com.sean.takeastand.storage.ScheduleDatabaseAdapter;
import com.sean.takeastand.util.Constants;
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
                    sendAnalyticsEvent(context, "BootReceiver: Beginning Schedule");
                    Toast.makeText(context, context.getString(R.string.boot_schedule_running),
                            Toast.LENGTH_SHORT).show();
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
        Utils.getBootFitSync(context);
        int currentStatus = Utils.getImageStatus(context);
        if(currentStatus == Constants.NON_SCHEDULE_ALARM_RUNNING ||
                currentStatus == Constants.NON_SCHEDULE_TIME_TO_STAND ||
                currentStatus == Constants.NON_SCHEDULE_STOOD_UP ||
                currentStatus == Constants.NON_SCHEDULE_PAUSED) {
            new UnscheduledRepeatingAlarm(context).setRepeatingAlarm();
        }
    }

    private void sendAnalyticsEvent(Context context, String action){
        Tracker t = ((Application)context.getApplicationContext()).getTracker(
                Application.TrackerName.APP_TRACKER);
        t.enableAdvertisingIdCollection(true);
        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder()
                .setCategory(Constants.ALARM_PROCESS_EVENT)
                .setAction(action)
                .build());
    }
}

