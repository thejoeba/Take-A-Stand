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
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;

import com.heckbot.standdtector.MyBroadcastReceiver;
import com.heckbot.standdtector.StandDtectorTM;
import com.sean.takeastand.R;
import com.sean.takeastand.alarmprocess.ScheduledRepeatingAlarm;
import com.sean.takeastand.alarmprocess.UnscheduledRepeatingAlarm;
import com.sean.takeastand.storage.AlarmSchedule;
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

    @Override
    protected void onCreate(Bundle paramBundle)
    {
        super.onCreate(paramBundle);
        //deleteDatabase("alarms_database");
        //Utils.setImageStatus(this, Constants.NO_ALARM_RUNNING);
        mNavDrawerOptions = new ArrayList<String>();
        //Find out how to initialize an arraylist; then update the arraylist when vibrate status changes
        //or standdtector status changes
        mNavDrawerOptions.add(getString(R.string.default_frequency));
        mNavDrawerOptions.add(getString(R.string.default_notification));
        SharedPreferences sharedPreferences =
                getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);
        boolean bStandDetector = (sharedPreferences.getBoolean(Constants.STAND_DETECTOR, false));
        if(bStandDetector){
            mNavDrawerOptions.add(getString(R.string.stand_detector_on));
        } else {
            mNavDrawerOptions.add(getString(R.string.stand_detector_off));
        }
        mNavDrawerOptions.add(getString(R.string.calibrate_detector));
        mNavDrawerOptions.add(getString(R.string.pause_settings));
        mNavDrawerOptions.add(getString(R.string.science_app));

        setContentView(R.layout.activity_main);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_closed) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(getString(R.string.app_name));
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(getString(R.string.settings));
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
        getActionBar().setDisplayHomeAsUpEnabled(true);
        //Styling
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
    }

    private AdapterView.OnItemClickListener drawerClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            switch (position){
                case 0:
                    showNumberPickerDialog(Utils.getDefaultFrequency(MainActivity.this), 2 , 100,
                            getString(R.string.select_frequency_default), true);
                    break;
                case 1:
                    Intent intentNotification =
                            new Intent(MainActivity.this, NotificationsActivity.class);
                    startActivity(intentNotification);
                    break;
                case 2:
                    toggleStandDetector();
                    break;
                case 3:
                    calibrateStandDetector();
                    break;
                case 4:
                    setPauseSettings();
                    break;
                case 5:
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
        updatePausePlay();
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
            updatePausePlay();
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

    private void togglePausePlay(){
        int status = Utils.getImageStatus(this);
        if(status == Constants.NON_SCHEDULE_ALARM_RUNNING || status == Constants.NON_SCHEDULE_STOOD_UP ||
                status == Constants.NON_SCHEDULE_TIME_TO_STAND){
            new UnscheduledRepeatingAlarm(this).pause();
            mPausePlay.setIcon(getResources().getDrawable(R.drawable.ic_action_play));
        } else if (status == Constants.SCHEDULE_RUNNING || status == Constants.SCHEDULE_STOOD_UP ||
                status == Constants.SCHEDULE_TIME_TO_STAND){
            ArrayList<FixedAlarmSchedule> alarmSchedules =
                    new ScheduleDatabaseAdapter(this).getFixedAlarmSchedules();
            //ToDo: Finish off the database method getSpecificSchedule and use instead getting the
            //whole arraylist
            for(int i = 0; i < alarmSchedules.size(); i ++){
                if(alarmSchedules.get(i).getUID() == Utils.getRunningScheduledAlarm(this)){
                    new ScheduledRepeatingAlarm(this, alarmSchedules.get(i)).pause();
                    mPausePlay.setIcon(getResources().getDrawable(R.drawable.ic_action_play));
                }
            }
        } else if (status == Constants.NON_SCHEDULE_PAUSED ){
            new UnscheduledRepeatingAlarm(this).setRepeatingAlarm();
            mPausePlay.setIcon(getResources().getDrawable(R.drawable.ic_action_pause));
            Utils.setImageStatus(this, Constants.NON_SCHEDULE_ALARM_RUNNING);
        } else if (status == Constants.SCHEDULE_PAUSED){
            ArrayList<FixedAlarmSchedule> alarmSchedules =
                    new ScheduleDatabaseAdapter(this).getFixedAlarmSchedules();
            //ToDo: Finish off the database method getSpecificSchedule and use instead getting the
            //whole arraylist
            for(int i = 0; i < alarmSchedules.size(); i ++){
                if(alarmSchedules.get(i).getUID() == Utils.getRunningScheduledAlarm(this)){
                    new ScheduledRepeatingAlarm(this, alarmSchedules.get(i)).setRepeatingAlarm();
                    mPausePlay.setIcon(getResources().getDrawable(R.drawable.ic_action_pause));
                    Utils.setImageStatus(this, Constants.SCHEDULE_RUNNING);
                }
            }
        }
    }


    private void showNumberPickerDialog(int startingValue, int min, int max, String title,
                                        final boolean frequency)
    {
        LayoutInflater inflater = getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = inflater.inflate(R.layout.dialog_number_picker, null);
        builder.setView(dialogView);
        final NumberPicker numberPicker = (NumberPicker)dialogView.findViewById(R.id.numberPicker);
        numberPicker.setMaxValue(max);
        numberPicker.setMinValue(min);
        numberPicker.setValue(startingValue);
        numberPicker.setWrapSelectorWheel(false);
        builder.setMessage(title);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(frequency){
                    setDefaultFrequency(numberPicker.getValue());
                } else {
                    setDefaultAlertDelay(numberPicker.getValue());
                }
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.i(TAG, "Cancel");
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

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
        int standDetectorPosition = 2;
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

    private void setPauseSettings(){
        LayoutInflater inflater = getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = inflater.inflate(R.layout.dialog_pause, null);
        int minValue = 5;
        int maxValue = 180;
        int step = 5;
        String[] valueSet = new String[maxValue/step];
        for (int i = minValue; i <= maxValue; i += step) {
            valueSet[ (i/step) -1 ] = String.valueOf(i);
        }
        builder.setView(dialogView);
        final NumberPicker npPause = (NumberPicker)dialogView.findViewById(R.id.pauseNumberPicker);
        npPause.setDisplayedValues(valueSet);
        npPause.setMinValue(0);
        npPause.setMaxValue(valueSet.length - 1);
        npPause.setWrapSelectorWheel(false);
        npPause.setValue(Utils.getDefaultPauseAmount(this));
        RadioButton rbIndefinite = (RadioButton)dialogView.findViewById(R.id.pause_indefinite);
        RadioButton rbSetTime = (RadioButton)dialogView.findViewById(R.id.pause_set_time);
        if(Utils.getDefaultPauseType(this)){
            rbIndefinite.setChecked(true);
            npPause.setEnabled(false);
            npPause.setAlpha((float) 0.5);
        } else {
            rbSetTime.setChecked(true);
            npPause.setEnabled(true);
            npPause.setAlpha((float) 1);
        }
        rbIndefinite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                npPause.setEnabled(false);
                npPause.setAlpha((float) 0.5);
                setDefaultPauseType(true);
            }
        });
        rbSetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                npPause.setEnabled(true);
                npPause.setAlpha((float) 1);
                setDefaultPauseType(false);
            }
        });
        LinearLayout lPauseDescription = (LinearLayout)dialogView.findViewById(R.id.touch_for_pause);
        lPauseDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView txtPauseDescription = (TextView)view.findViewById(R.id.pause_description);
                ImageView imgCollapseExpand = (ImageView)view.findViewById(R.id.pause_collapse_expand);
                if(txtPauseDescription.getVisibility() == View.VISIBLE){
                    txtPauseDescription.setVisibility(View.GONE);
                    imgCollapseExpand.setImageDrawable(
                            getResources().getDrawable(R.drawable.ic_action_expand));
                } else {
                    txtPauseDescription.setVisibility(View.VISIBLE);
                    imgCollapseExpand.setImageDrawable(
                            getResources().getDrawable(R.drawable.ic_action_collapse));
                }
            }
        });
        builder.setMessage(getResources().getString(R.string.pause_settings));
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setDefaultPauseAmount(npPause.getValue());
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void setDefaultFrequency(int frequency){
        SharedPreferences sharedPreferences =
                getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.USER_FREQUENCY, frequency);
        editor.commit();
    }

    private void setDefaultAlertDelay(int delay){
        SharedPreferences sharedPreferences =
                getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.USER_DELAY, delay);
        editor.commit();
    }

    private void setDefaultPauseAmount(int pauseAmount){
        SharedPreferences sharedPreferences =
                getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.PAUSE_TIME, pauseAmount);
        editor.commit();
    }

    private void setDefaultPauseType(boolean pauseIndefinite){
        SharedPreferences sharedPreferences =
                getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.PAUSE_TYPE, pauseIndefinite);
        editor.commit();
    }

    private void updatePausePlay(){
        if(mPausePlay != null){
            int currentImageStatus = Utils.getImageStatus(this);
            if(currentImageStatus != Constants.NO_ALARM_RUNNING){
                Log.i(TAG, "true");
                mPausePlay.setVisible(true);
            } else {
                Log.i(TAG, "false");
                mPausePlay.setVisible(false);
            }
            invalidateOptionsMenu();
        } else {
            Log.i(TAG, "null");
        }
    }

    //For Calligraphy font library class
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new CalligraphyContextWrapper(newBase));
    }


}
