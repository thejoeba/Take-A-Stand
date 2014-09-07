package com.sean.takeastand;

/**
 * Created by Sean on 2014-09-03.
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class AlarmReceiver
        extends BroadcastReceiver
{
    private static final String TAG = "AlarmReceiver";
    Context mContext;


    public void onReceive(Context context, Intent intent)
    {
        Log.i(TAG, "AlarmReceiver received intent");
        mContext = context;
        /*
        If makes sense create a notification class, that sends a new
        notification intent, with three button choices (two if three not possible)
         */
    }
}
