package com.sean.takeastand.alarmprocess;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sean.takeastand.storage.FixedAlarmSchedule;
import com.sean.takeastand.util.Constants;

/**
 * Created by Sean on 2014-12-10.
 */
public class EndPauseReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.hasExtra(Constants.ALARM_SCHEDULE)){
            ScheduledRepeatingAlarm scheduledRepeatingAlarm = new ScheduledRepeatingAlarm(context,
                    (FixedAlarmSchedule)intent.getParcelableExtra(Constants.ALARM_SCHEDULE));
            scheduledRepeatingAlarm.unpause();
        } else {
            UnscheduledRepeatingAlarm unscheduledRepeatingAlarm = new UnscheduledRepeatingAlarm(context);
            unscheduledRepeatingAlarm.unpause();
        }
    }
}
