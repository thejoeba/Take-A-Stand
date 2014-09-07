package com.sean.takeastand;

import android.app.ActionBar;
import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class Main_Activity extends ActionBarActivity {
    private static final String TAG = "MainActivity";
    private static boolean isVisible;
    protected final String PREFS = "User Preferences";
    private BroadcastReceiver finishReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context paramAnonymousContext, Intent paramAnonymousIntent)
        {
            MainActivity.this.finish();
            Log.i(TAG, "finish activity");
        }
    };
    Handler mHandler;

    public static boolean isMainActivityVisible()
    {
        Log.i(TAG, "isVisible " + Boolean.toString(isVisible));
        return isVisible;
    }

    private void registerReceivers()
    {
        LocalBroadcastManager.getInstance(this).registerReceiver(this.finishReceiver, new IntentFilter("finish"));
    }

    private void setLayout()
    {
        setContentView(2130903066);
    }

    private void unregisterReceivers()
    {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.finishReceiver);
    }

    protected void onCreate(Bundle paramBundle)
    {
        super.onCreate(paramBundle);
        setLayout();
        this.mHandler = new Handler();
        registerReceivers();
    }

    public boolean onCreateOptionsMenu(Menu paramMenu)
    {
        getMenuInflater().inflate(2131492864, paramMenu);
        return true;
    }

    protected void onDestroy()
    {
        unregisterReceivers();
        super.onDestroy();
    }

    public boolean onOptionsItemSelected(MenuItem paramMenuItem)
    {
        int i = paramMenuItem.getItemId();
        switch (paramMenuItem.getItemId())
        {
            case 2131034218://Schedule
                //Start New Schedule List Activity

        }
        return super.onOptionsItemSelected(paramMenuItem);
    }

    protected void onPause()
    {
        isVisible = false;
        super.onPause();
    }

    protected void onResume()
    {
        isVisible = true;
        super.onResume();
    }
}
