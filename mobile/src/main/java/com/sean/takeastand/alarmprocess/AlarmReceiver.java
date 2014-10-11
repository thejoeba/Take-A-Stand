package com.sean.takeastand.alarmprocess;

/**
 * Created by Sean on 2014-09-03.
 */
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sean.takeastand.storage.AlarmSchedule;
import com.sean.takeastand.ui.MainActivity;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.R;
import com.sean.takeastand.util.Utils;

import java.util.Calendar;

public class AlarmReceiver
        extends BroadcastReceiver
{
    private static final String TAG = "AlarmReceiver";
    private Context mContext;

    //in the future it will be possible to cancel before end of day.
    //Wherever that cancellation occurs Utils.setRunningScheduledAlarm should be called and pass -1

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.i(TAG, "AlarmReceiver received intent");
        mContext = context;
        AlarmSchedule currentAlarmSchedule = intent.getParcelableExtra(Constants.ALARM_SCHEDULE);
        //If the alarmSchedule is still running, send a notification that it is time
        //to stand up and start the service that listens for the user response.
        if(currentAlarmSchedule!=null){
            if(!hasEndTimePassed(currentAlarmSchedule.getEndTime())){
                sendNotification();
                //currentAlarmSchedule.getAlarmType if vibrate, vibrate, some kind of sound, make that
                //sound
                Intent serviceStartIntent = new Intent(mContext, AlarmService.class);
                serviceStartIntent.putExtra(Constants.ALARM_SCHEDULE, currentAlarmSchedule);
                mContext.startService(serviceStartIntent);
            } else {
                //-1 indicates that there is no currently running scheduled alarm
                Utils.setRunningScheduledAlarm(mContext, -1);
                Log.i(TAG, "Alarm day is over.");
                Utils.setCurrentMainActivityImage(mContext, Constants.NO_ALARM_RUNNING);
                Utils.notifyImageUpdate(mContext);
            }
        } else {
            Log.i(TAG, "AlarmSchedule is null");
            sendNotification();
            Intent serviceStartIntent = new Intent(mContext, AlarmService.class);
            mContext.startService(serviceStartIntent);
        }
    }

    private void sendNotification(){
        NotificationManager notificationManager = (NotificationManager)mContext.getSystemService(
                Context.NOTIFICATION_SERVICE);

        //Make intents
        Intent launchActivity = new Intent(mContext, MainActivity.class);
        PendingIntent launchActivityPendingIntent = PendingIntent.getActivity(mContext, 0,
                launchActivity, 0);
        Intent stoodUpIntent = new Intent("StoodUp");
        PendingIntent stoodUpPendingIntent = PendingIntent.getBroadcast(mContext, 0,
                stoodUpIntent, 0);
        Intent oneMinuteIntent = new Intent("OneMinute");
        PendingIntent oneMinutePendingIntent = PendingIntent.getBroadcast(mContext, 0,
                oneMinuteIntent, 0);
        Intent fiveMinuteIntent = new Intent("FiveMinute");
        PendingIntent fiveMinutePendingIntent = PendingIntent.getBroadcast(mContext, 0,
                fiveMinuteIntent, 0);

        Notification alarmNotification = new Notification.InboxStyle(
                new Notification.Builder(mContext)
                        .setContentTitle("Take A Stand")
                        .setContentText("Time to stand up")
                        .setContentIntent(launchActivityPendingIntent)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setAutoCancel(false)
                        .setOngoing(true)
                        .addAction(android.R.drawable.btn_default, "Stood Up", stoodUpPendingIntent)
                        .addAction(android.R.drawable.btn_default, "1 More Minute",
                                oneMinutePendingIntent)
                        .addAction(android.R.drawable.btn_default, "5 More Minutes",
                                fiveMinutePendingIntent)
                        .setTicker("Time to stand up"))
                .build();
        notificationManager.notify(R.integer.AlarmNotificationID, alarmNotification);
    }

    private boolean hasEndTimePassed(Calendar endTime){
        Calendar rightNow = Calendar.getInstance();
        return endTime.before(rightNow);
    }
}
