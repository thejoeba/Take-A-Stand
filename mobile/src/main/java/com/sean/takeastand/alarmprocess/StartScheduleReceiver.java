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

import com.Application;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.heckbot.standdtector.StandDtectorTM;
import com.sean.takeastand.storage.FixedAlarmSchedule;
import com.sean.takeastand.storage.ScheduleDatabaseAdapter;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;

import java.util.ArrayList;

/* When the user creates a new schedule, a system daily repeating alarm is set in Android’s
AlarmManager class.  When the daily repeating alarm goes off, an intent is sent to this class which
then starts the relevant alarm schedule.  */

/**
 * Created by Sean on 2014-09-03.
 */
public class StartScheduleReceiver extends BroadcastReceiver {

    private static final String TAG = "StartScheduleReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "StartScheduleReceiver has received an intent");
        ArrayList<FixedAlarmSchedule> fixedAlarmSchedules =
                new ScheduleDatabaseAdapter(context).getFixedAlarmSchedules();
        if (!fixedAlarmSchedules.isEmpty()) {
            FixedAlarmSchedule todayAlarm = Utils.findTodaysSchedule(fixedAlarmSchedules);
            if (!(todayAlarm.getUID() == -100)) {
                if (todayAlarm.getActivated()) {
                    new ScheduledRepeatingAlarm(context, todayAlarm).setRepeatingAlarm();
                    sendAnalyticsEvent(context, "StartScheduleReceiver: Beginning Schedule");
                    Utils.setImageStatus(context, Constants.SCHEDULE_RUNNING);
                    if (context.getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0).getBoolean(Constants.DEVICE_STEP_DETECTOR_ENABLED, false)) {
                        Intent startStepCounterIntent = new Intent(context, StandDtectorTM.class);
                        startStepCounterIntent.setAction("StartDeviceStepCounter");
                        context.startService(startStepCounterIntent);
                    }

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

    private void sendAnalyticsEvent(Context context, String action) {
        Tracker t = ((Application) context.getApplicationContext()).getTracker(
                Application.TrackerName.APP_TRACKER);
        t.enableAdvertisingIdCollection(true);
        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder()
                .setCategory(Constants.ALARM_PROCESS_EVENT)
                .setAction(action)
                .build());
    }
}
