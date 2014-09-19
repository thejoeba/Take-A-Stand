package com.sean.takeastand;

/**
 * Created by Sean on 2014-09-03.
 */
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class AlarmReceiver
        extends BroadcastReceiver
{
    private static final String TAG = "AlarmReceiver";
    private static final int NTFY_ID = 1;
    Context mContext;

    public AlarmReceiver(){
       Log.i(TAG, "constructed");
    }


    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.i(TAG, "AlarmReceiver received intent");
        mContext = context;
        sendNotification();
        Intent serviceStartIntent = new Intent(mContext, AlarmService.class);
        mContext.startService(serviceStartIntent);
    }

    private void sendNotification(){
        NotificationManager notificationManager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent stoodUpIntent = new Intent("StoodUp");
        PendingIntent stoodUpPendingIntent = PendingIntent.getBroadcast(mContext, 0, stoodUpIntent, 0);
        Intent oneMinuteIntent = new Intent("OneMinute");
        PendingIntent oneMinutePendingIntent = PendingIntent.getBroadcast(mContext, 0, oneMinuteIntent, 0);
        Intent fiveMinuteIntent = new Intent("FiveMinute");
        PendingIntent fiveMinutePendingIntent = PendingIntent.getBroadcast(mContext, 0, fiveMinuteIntent, 0);

        Notification alarmNotification = new Notification.InboxStyle(
                new Notification.Builder(mContext)
                        .setContentTitle("Take A Stand")
                        .setContentText("Time to stand up")
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setAutoCancel(true)
                        .setOngoing(false)
                        //Once app done testing set ongoing to true, as will be removed by the AlarmService after a minute
                        .addAction(android.R.drawable.btn_default, "Stood Up", stoodUpPendingIntent)
                        .addAction(android.R.drawable.btn_default, "1 More Minute", oneMinutePendingIntent)
                        .addAction(android.R.drawable.btn_default, "5 More Minutes", fiveMinutePendingIntent)
                        .setTicker("Time to stand up"))
                .build();
        notificationManager.notify(NTFY_ID, alarmNotification);
    }
}
