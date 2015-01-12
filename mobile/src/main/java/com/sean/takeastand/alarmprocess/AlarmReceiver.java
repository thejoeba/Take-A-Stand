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

/**
 * Created by Sean on 2014-09-03.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.sean.takeastand.R;
import com.sean.takeastand.storage.FixedAlarmSchedule;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;

import java.util.Calendar;

/* This class receives the alarm intent that is set by the RepeatingAlarm class and that is sent by
 the Android system’s AlarmManager class.  This alarm notification signals that the
 application needs to notify the user that it is time to stand up.  This class starts the
 AlarmService class which takes care of managing the process of notifying the user that it is time
 to stand up and also waits for the user’s response.  The AlarmReceiver class is used for both
 unscheduled alarms and scheduled alarms.  For scheduled alarms it checks to
 see if it now past the end time for the schedule and ends the repeating alarm process, if it is. */

public class AlarmReceiver
        extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "AlarmReceiver received alarm intent");
        mContext = context;
        FixedAlarmSchedule currentAlarmSchedule = intent.getParcelableExtra(Constants.ALARM_SCHEDULE);
        if (currentAlarmSchedule != null) {
            if (!hasEndTimePassed(currentAlarmSchedule.getEndTime())) {
                Intent serviceStartIntent = new Intent(mContext, AlarmService.class);
                serviceStartIntent.putExtra(Constants.ALARM_SCHEDULE, currentAlarmSchedule);
                mContext.startService(serviceStartIntent);
            } else {
                //-1 indicates that there is no currently running scheduled alarm
                Utils.setRunningScheduledAlarm(mContext, -1);
                Log.i(TAG, context.getString(R.string.alarm_day_over));
                Utils.setImageStatus(mContext, Constants.NO_ALARM_RUNNING);
                endAlarmService();
                Utils.endSession(mContext);
            }
        } else {
            //Unscheduled alarms
            Intent serviceStartIntent = new Intent(mContext, AlarmService.class);
            mContext.startService(serviceStartIntent);
        }
    }

    private boolean hasEndTimePassed(Calendar endTime) {
        Calendar rightNow = Calendar.getInstance();
        return endTime.before(rightNow);
    }

    private void endAlarmService() {
        Intent intent = new Intent(Constants.END_ALARM_SERVICE);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }
}
