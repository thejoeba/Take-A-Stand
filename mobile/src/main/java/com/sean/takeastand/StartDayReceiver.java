package com.sean.takeastand;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Sean on 2014-09-03.
 */
public class StartDayReceiver extends BroadcastReceiver
{
    private void setFirstAlarm(Context context)
    {
        new RepeatingAlarmController(context).setNewAlarm();
    }

    public void onReceive(Context context, Intent intent)
    {
        setFirstAlarm(context);
    }
}
