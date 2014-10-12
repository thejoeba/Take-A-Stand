package com.sean.takeastand.alarmprocess;

/**
 * Created by Sean on 2014-09-03.
 */
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.sean.takeastand.util.Constants;
import com.sean.takeastand.storage.AlarmSchedule;
import com.sean.takeastand.util.Utils;

public interface RepeatingAlarm
{
    /*
    This class in the future should be divided into scheduledRepeatingAlarmController and
    unscheduledRepeatingAlarmController
     */
    public void setRepeatingAlarm();

    public void setShortBreakAlarm();

    public void setLongBreakAlarm();

    public void cancelAlarm();

}
