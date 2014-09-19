package com.sean.takeastand;

/**
 * Created by Sean on 2014-09-03.
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class EndDayReceiver
        extends BroadcastReceiver
{
    AlarmsDatabaseAdapter mAlarmsDatabaseAdapter;

    public void onReceive(Context context, Intent intent)
    {
        new NextScheduledAlarmSetter(context).setNextAlarm();
    }
}