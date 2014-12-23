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

import android.app.ActionBar;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.Application;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.sean.takeastand.R;
import com.sean.takeastand.storage.AlarmSchedule;
import com.sean.takeastand.storage.ExpandableAdapter;
import com.sean.takeastand.storage.ScheduleDatabaseAdapter;
import com.sean.takeastand.storage.ScheduleListAdapter;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.widget.TimePickerFragment;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Sean on 2014-09-21.
 */
public class ScheduleListActivity extends ActionBarActivity {

    private static final String TAG = "SchedulesListActivity";
    private ImageView imgAddAlarm;
    private ScheduleListAdapter scheduleListAdapter;
    private ExpandableAdapter expandableAdapter;
    private  ArrayList<AlarmSchedule> mAlarmSchedules;
    private ListView mSchedulesList;
    private TimePickerFragment timePickerFragment;
    private String mNewAlarmStartTime;
    private boolean mJustReceivedTimePicker;
    private boolean mJustReceivedResponse;
    private Handler mHandler;
    private TextView txtNoAlarms;
    private RelativeLayout rlScheduleList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_list);
        setUpLayout();
        Tracker t = ((Application)this.getApplication()).getTracker(Application.TrackerName.APP_TRACKER);
        t.setScreenName("Schedules List Activity");
        t.send(new HitBuilders.AppViewBuilder().build());
    }

    @Override
    protected void onResume() {
        super.onResume(); registerReceivers();
        mJustReceivedTimePicker = true;
        mJustReceivedResponse = true;
        mHandler = new Handler();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceivers();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Closes Activity when user presses title
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }

    private void setUpLayout(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.schedule_list_toolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavUtils.navigateUpFromSameTask(ScheduleListActivity.this);
                }
            });
        }
        imgAddAlarm = (ImageView)this.findViewById(R.id.btn_add_alarm);
        imgAddAlarm.setOnClickListener(addAlarmOnClickListener);
        mSchedulesList = (ListView)findViewById(R.id.schedules_list);
        //registerForContextMenu(mSchedulesList);
        mAlarmSchedules = new ScheduleDatabaseAdapter(this).getAlarmSchedules();
        if(mAlarmSchedules.isEmpty()){
            txtNoAlarms = (TextView)findViewById(R.id.no_alarms);
            txtNoAlarms.setVisibility(View.VISIBLE);
            rlScheduleList = (RelativeLayout)findViewById(R.id.rl_schedule_list);
            rlScheduleList.setOnClickListener(addAlarmOnClickListener);
            findViewById(R.id.fl_schedule_list).setVisibility(View.GONE);
            imgAddAlarm.setVisibility(View.GONE);
        }
        scheduleListAdapter =
                new ScheduleListAdapter(this, android.R.id.list, mAlarmSchedules, getLayoutInflater());
        Log.i(TAG, "Number of Rows: " + Integer.toString(scheduleListAdapter.getCount()));
        expandableAdapter = new ExpandableAdapter(this, scheduleListAdapter, R.id.clickToExpand, R.id.bottomContainer);
        mSchedulesList.setAdapter(expandableAdapter);
    }

    private View.OnClickListener addAlarmOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.i(TAG, "OnClick");
            createNewAlarm();
        }
    };

    private void registerReceivers(){
        LocalBroadcastManager.getInstance(this).registerReceiver(titleChangeReceiver,
                new IntentFilter("TitleChange"));
        LocalBroadcastManager.getInstance(this).registerReceiver(timePickerResponseReceiver,
                new IntentFilter("TimePicker"));
        LocalBroadcastManager.getInstance(this).registerReceiver(showTimePickerReceiver,
                new IntentFilter("ShowTimePicker"));
    }

    private void unregisterReceivers(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(titleChangeReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(timePickerResponseReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(showTimePickerReceiver);
    };

    private void createNewAlarm(){
        showTimePickerDialog(true, true);
    }

    private void showTimePickerDialog(boolean startOrEndTime, boolean newAlarm)
    {
        Bundle args = new Bundle();
        args.putBoolean("StartOrEndButton", startOrEndTime);
        args.putBoolean("NewAlarm", newAlarm);
        DialogFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.setArguments(args);
        try{
            timePickerFragment.show(getFragmentManager(), "timePicker");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private BroadcastReceiver titleChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scheduleListAdapter.updateTitle(intent.getStringExtra("NewTitle"),
                    intent.getIntExtra("Position", -1));
        }
    };

    private BroadcastReceiver timePickerResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received timePicker intent");
            if(mJustReceivedResponse){
                if(intent.getBooleanExtra("NewAlarm", false)) {
                    if(intent.getBooleanExtra("StartTime", true)){
                        Log.i(TAG, "start time");
                        Bundle args = new Bundle();
                        args.putBoolean("StartOrEndButton", false);
                        args.putBoolean("NewAlarm", true);
                        mNewAlarmStartTime = intent.getStringExtra("TimeSelected");
                        //Once figure out how to restrict timepickerdialog
                        //args.putString("StartTime", mNewAlarmStartTime);
                        timePickerFragment = new TimePickerFragment();
                        timePickerFragment.setArguments(args);
                        try{
                            timePickerFragment.show(getFragmentManager(), "timePicker");
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    } else {
                        Log.i(TAG, "end time");
                        scheduleListAdapter.newSchedule(mNewAlarmStartTime, intent.getStringExtra("TimeSelected"));
                        //If there are no alarms, and a new one has been created, update layout
                        if(imgAddAlarm.getVisibility() == View.GONE){
                            txtNoAlarms.setVisibility(View.GONE);
                            rlScheduleList.setOnClickListener(null);
                            findViewById(R.id.fl_schedule_list).setVisibility(View.VISIBLE);
                            imgAddAlarm.setVisibility(View.VISIBLE);
                        }
                    }

                } else {
                    Log.i(TAG, "not new alarm, updating existing");
                    //Was called by the ScheduleListAdapter, pass data in
                    scheduleListAdapter.updateStartEndTime(intent.getStringExtra("TimeSelected"),
                            intent.getIntExtra("Position", -1));
                }
                mJustReceivedResponse = false;
                mHandler.postDelayed(updateStatusResponse, 290);
            }
        }
    };

    private BroadcastReceiver showTimePickerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(mJustReceivedTimePicker){
                Bundle args = new Bundle();
                args.putBoolean("StartOrEndButton", intent.getBooleanExtra("StartOrEndButton", true));
                Log.i(TAG, Boolean.toString(intent.getBooleanExtra("StartOrEndButton", true)));
                args.putBoolean("NewAlarm", intent.getBooleanExtra("NewAlarm", false));
                args.putInt("Position", intent.getIntExtra("Position", -1));
                if(intent.hasExtra(Constants.START_TIME_ARG)){
                    args.putString(Constants.START_TIME_ARG, (intent.getStringExtra(Constants.START_TIME_ARG)));
                }
                if(intent.hasExtra(Constants.END_TIME_ARG)){
                    args.putString(Constants.END_TIME_ARG, (intent.getStringExtra(Constants.END_TIME_ARG)));
                }
                timePickerFragment = new TimePickerFragment();
                timePickerFragment.setArguments(args);
                try{
                    timePickerFragment.show(getFragmentManager(), "timePicker");
                } catch (Exception e){
                    e.printStackTrace();
                }
                mJustReceivedTimePicker = false;
                mHandler.postDelayed(updateStatusPicker, 290);
            }

        }
    };

    private Runnable updateStatusResponse = new Runnable() {
        @Override
        public void run() {
            mJustReceivedResponse = true;

        }
    };

    private Runnable updateStatusPicker = new Runnable() {
        @Override
        public void run() {
            mJustReceivedTimePicker = true;
        }
    };

    //For Calligraphy font library class
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new CalligraphyContextWrapper(newBase));
    }


}
