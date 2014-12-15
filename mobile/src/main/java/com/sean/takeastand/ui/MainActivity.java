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

package com.sean.takeastand.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.TextView;

import com.Application;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.heckbot.standdtector.MyBroadcastReceiver;
import com.heckbot.standdtector.StandDtectorTM;
import com.sean.takeastand.R;
import com.sean.takeastand.alarmprocess.ScheduledRepeatingAlarm;
import com.sean.takeastand.alarmprocess.UnscheduledRepeatingAlarm;
import com.sean.takeastand.storage.FixedAlarmSchedule;
import com.sean.takeastand.storage.ScheduleDatabaseAdapter;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private ArrayList<String> mNavDrawerOptions;
    private ArrayAdapter mListAdapter;
    private MenuItem mPausePlay;
    private int[] pauseTimes= new int[] {5, 10, 15, 20, 25, 30, 45, 60, 75, 90, 105, 120, 135, 150,
                        165, 180};

    @Override
    protected void onCreate(Bundle paramBundle)
    {
        super.onCreate(paramBundle);
        //deleteDatabase("alarms_database");
        //Utils.setImageStatus(this, Constants.NO_ALARM_RUNNING);
        mNavDrawerOptions = new ArrayList<String>();
        //Find out how to initialize an arraylist; then update the arraylist when vibrate status changes
        //or standdtector status changes
        mNavDrawerOptions.add(getString(R.string.default_notification));
        SharedPreferences sharedPreferences =
                getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 1);
        boolean bStandDetector = (sharedPreferences.getBoolean(Constants.STAND_DETECTOR, false));
        if(bStandDetector){
            mNavDrawerOptions.add(getString(R.string.stand_detector_on));
        } else {
            mNavDrawerOptions.add(getString(R.string.stand_detector_off));
        }
        mNavDrawerOptions.add(getString(R.string.calibrate_detector));
        mNavDrawerOptions.add(getString(R.string.science_app));
        setContentView(R.layout.activity_main);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_closed) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                if(getActionBar() != null){
                    getActionBar().setTitle(getString(R.string.app_name));
                }
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if(getActionBar() != null){
                    getActionBar().setTitle(getString(R.string.settings));
                }
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerList = (ListView)findViewById(R.id.left_drawer);
        mListAdapter = new ArrayAdapter(this, R.layout.drawer_list_item, mNavDrawerOptions);
        mDrawerList.setAdapter(mListAdapter);
        mDrawerList.setOnItemClickListener(drawerClickListener);
        //mDrawerList.setOnItemClickListener();
        //Navigation Drawer icon won't display without this
        if(getActionBar() != null){
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        //Styling
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        ((Application) getApplication()).getTracker(Application.TrackerName.APP_TRACKER);

    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    private AdapterView.OnItemClickListener drawerClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            switch (position){
                case 0:
                    Intent intentNotification =
                            new Intent(MainActivity.this, RemindersActivity.class);
                    startActivity(intentNotification);
                    break;
                case 1:
                    toggleStandDetector();
                    break;
                case 2:
                    calibrateStandDetector();
                    break;
                case 3:
                    Intent intentScience = new Intent(MainActivity.this, ScienceActivity.class);
                    startActivity(intentScience);
                    break;
            }
        }


    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch(item.getItemId()){
            case R.id.schedules:
                Intent intent = new Intent(this, ScheduleListActivity.class);
                startActivity(intent);
                break;
            case R.id.pauseplay:
                togglePausePlay();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        Intent intent = new Intent(Constants.MAIN_ACTIVITY_VISIBILITY_STATUS);
        intent.putExtra("Visible", false);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceivers();
    }

    @Override
    protected void onResume() {
        registerReceivers();
        Intent intent = new Intent(Constants.MAIN_ACTIVITY_VISIBILITY_STATUS);
        intent.putExtra("Visible", true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        invalidateOptionsMenu();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        mPausePlay = menu.findItem(R.id.pauseplay);
        updatePausePlayIcon();
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        mPausePlay = menu.findItem(R.id.pauseplay);
        if(drawerOpen){
            menu.findItem(R.id.schedules).setVisible(false);
            mPausePlay.setVisible(false);
        } else {
            menu.findItem(R.id.schedules).setVisible(true);
            updatePausePlayIcon();
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    private void registerReceivers(){
        LocalBroadcastManager.getInstance(this).registerReceiver(visibilityReceiver,
                new IntentFilter("Visible"));
        LocalBroadcastManager.getInstance(this).registerReceiver(updateActionBarReceiver,
                new IntentFilter(Constants.UPDATE_ACTION_BAR));
    }

    private void unregisterReceivers(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(visibilityReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateActionBarReceiver);
    }

    private BroadcastReceiver visibilityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent newIntent = new Intent(Constants.MAIN_ACTIVITY_VISIBILITY_STATUS);
            newIntent.putExtra("Visible", true);
            LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(newIntent);
        }
    };

    private BroadcastReceiver updateActionBarReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received intent");
            invalidateOptionsMenu();
        }
    };

    private void toggleStandDetector() {
        // shared preferences declared on create
        // skip declaring boolean and just drop it into the editor
        SharedPreferences sharedPreferences =
                getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);
        boolean bStandDetector = !(sharedPreferences.getBoolean(Constants.STAND_DETECTOR, false));
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.STAND_DETECTOR, bStandDetector);
        editor.commit();
        setStandDetectorMenuText();
    }

    private void setStandDetectorMenuText(){
        SharedPreferences sharedPreferences =
                getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);
        boolean bStandDetector = (sharedPreferences.getBoolean(Constants.STAND_DETECTOR, false));
        int standDetectorPosition = 1;
        if(bStandDetector){
            mNavDrawerOptions.remove(standDetectorPosition);
            mNavDrawerOptions.add(standDetectorPosition, "StandDtector™: ON");
        } else {
            mNavDrawerOptions.remove(standDetectorPosition);
            mNavDrawerOptions.add(standDetectorPosition, "StandDtector™: OFF");
        }
        mListAdapter.notifyDataSetChanged();
    }

    private void calibrateStandDetector() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.calibration))
                .setMessage(getString(R.string.calibration_instructions))
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent calibrationIntent = new Intent(MainActivity.this, StandDtectorTM.class);
                        calibrationIntent.setAction("CALIBRATE");
                        Intent intent = new Intent(MainActivity.this, MyBroadcastReceiver.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this,
                                0, intent, PendingIntent.FLAG_ONE_SHOT);
                        calibrationIntent.putExtra("pendingIntent", pendingIntent);
                        startService(calibrationIntent);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void togglePausePlay(){
        int status = Utils.getImageStatus(this);
        if(status == Constants.NON_SCHEDULE_ALARM_RUNNING || status == Constants.NON_SCHEDULE_STOOD_UP ||
                status == Constants.NON_SCHEDULE_TIME_TO_STAND){
            mPausePlay.setIcon(getResources().getDrawable(R.drawable.ic_action_play));
            showPauseSettingsDialog();
        } else if (status == Constants.SCHEDULE_RUNNING || status == Constants.SCHEDULE_STOOD_UP ||
                status == Constants.SCHEDULE_TIME_TO_STAND){
            mPausePlay.setIcon(getResources().getDrawable(R.drawable.ic_action_play));
            showPauseSettingsDialog();
        } else if (status == Constants.NON_SCHEDULE_PAUSED ){
            unPauseUnscheduled();
            mPausePlay.setIcon(getResources().getDrawable(R.drawable.ic_action_pause));
        } else if (status == Constants.SCHEDULE_PAUSED){
            unPauseScheduled();
            mPausePlay.setIcon(getResources().getDrawable(R.drawable.ic_action_pause));
        }
    }

    private void showPauseSettingsDialog(){
        LayoutInflater inflater = getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = inflater.inflate(R.layout.dialog_pause, null);
        TextView title = new TextView(this);
        title.setPadding(50, 50, 50, 50);
        title.setTextSize(22);
        title.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
        title.setText(getResources().getString(R.string.select_pause_type));
        builder.setCustomTitle(title);
        int minValue = 5;
        int maxValue = 180;
        int step = 5;
        String[] valueSet = new String[pauseTimes.length];
        for(int i = 0; i < pauseTimes.length; i++){
            valueSet[i] = Integer.toString(pauseTimes[i]);
        }
        builder.setView(dialogView);
        final NumberPicker npPause = (NumberPicker)dialogView.findViewById(R.id.pauseNumberPicker);
        npPause.setDisplayedValues(valueSet);
        npPause.setMinValue(0);
        npPause.setMaxValue(valueSet.length - 1);
        npPause.setWrapSelectorWheel(false);
        int initialValue = Utils.getDefaultPauseAmount(this);
        for(int i = 0; i < pauseTimes.length; i++){
            if (initialValue == pauseTimes[i]){
                initialValue = i;
            }
        }
        npPause.setValue(initialValue);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //getValue returns the index number, so need to do some math to correct correct
                //actual value
                setDefaultPauseAmount(pauseTimes[npPause.getValue()]);
                int currentStatus = Utils.getImageStatus(MainActivity.this);
                if(currentStatus == Constants.NON_SCHEDULE_ALARM_RUNNING ||
                        currentStatus == Constants.NON_SCHEDULE_STOOD_UP ||
                        currentStatus == Constants.NON_SCHEDULE_TIME_TO_STAND){
                    pauseUnscheduled();
                } else{
                    pauseSchedule();
                }
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mPausePlay.setIcon(getResources().getDrawable(R.drawable.ic_action_pause));
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void updatePausePlayIcon(){
        if(mPausePlay != null){
            int currentImageStatus = Utils.getImageStatus(this);
            if(currentImageStatus != Constants.NO_ALARM_RUNNING &&
                    currentImageStatus != Constants.NON_SCHEDULE_PAUSED &&
                    currentImageStatus != Constants.SCHEDULE_PAUSED){
                mPausePlay.setVisible(true);
                mPausePlay.setIcon(getResources().getDrawable(R.drawable.ic_action_pause));
            } else if (currentImageStatus == Constants.SCHEDULE_PAUSED ||
                    currentImageStatus == Constants.NON_SCHEDULE_PAUSED) {
                mPausePlay.setVisible(true);
                mPausePlay.setIcon(getResources().getDrawable(R.drawable.ic_action_play));
            } else {
                mPausePlay.setVisible(false);
            }
            invalidateOptionsMenu();
        } else {
            Log.i(TAG, "null");
        }
    }

    private void pauseUnscheduled(){
        new UnscheduledRepeatingAlarm(this).pause();
    }

    private void pauseSchedule(){
        int currentRunningAlarmUID = Utils.getRunningScheduledAlarm(this);
        ScheduleDatabaseAdapter scheduleDatabaseAdapter = new ScheduleDatabaseAdapter(this);
        FixedAlarmSchedule currentAlarmSchedule =
                new FixedAlarmSchedule(
                        scheduleDatabaseAdapter.getSpecificAlarmSchedule(currentRunningAlarmUID));
        new ScheduledRepeatingAlarm(this,currentAlarmSchedule).pause();
    }

    private void unPauseUnscheduled(){
        new UnscheduledRepeatingAlarm(this).unpause();
    }

    private void unPauseScheduled(){
        int currentRunningAlarmUID = Utils.getRunningScheduledAlarm(this);
        ScheduleDatabaseAdapter scheduleDatabaseAdapter = new ScheduleDatabaseAdapter(this);
        FixedAlarmSchedule currentAlarmSchedule =
                new FixedAlarmSchedule(
                        scheduleDatabaseAdapter.getSpecificAlarmSchedule(currentRunningAlarmUID));
        new ScheduledRepeatingAlarm(this,currentAlarmSchedule).unpause();
    }

    private void setDefaultPauseAmount(int pauseAmount){
        SharedPreferences sharedPreferences =
                getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.PAUSE_TIME, pauseAmount);
        editor.commit();
    }

    //For Calligraphy font library class
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new CalligraphyContextWrapper(newBase));
    }


}
