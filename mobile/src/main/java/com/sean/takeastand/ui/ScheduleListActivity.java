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
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sean.takeastand.R;
import com.sean.takeastand.storage.AlarmSchedule;
import com.sean.takeastand.storage.ExpandableAdapter;
import com.sean.takeastand.storage.ScheduleDatabaseAdapter;
import com.sean.takeastand.storage.ScheduleListAdapter;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.widget.TimePickerFragment;

import java.util.ArrayList;

/**
 * Created by Sean on 2014-09-21.
 */
public class ScheduleListActivity extends ListActivity {

    private static final String TAG = "SchedulesListActivity";
    private ImageView imgAddAlarm;
    private ScheduleListAdapter scheduleListAdapter;
    private ExpandableAdapter expandableAdapter;
    private  ArrayList<AlarmSchedule> mAlarmSchedules;
    private String mNewAlarmStartTime;
    private boolean mJustReceivedTimePicker;
    private boolean mJustReceivedResponse;
    private Handler mHandler;
    private TimePickerFragment timePickerFragment;
    private TextView txtNoAlarms;
    private RelativeLayout rlScheduleList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_list);

        //Deleting the database each time only during testing
        //deleteDatabase("alarms_database");

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpLayout();
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
        setListAdapter(expandableAdapter);
        registerReceivers();
        mJustReceivedTimePicker = true;
        mJustReceivedResponse = true;
        mHandler = new Handler();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }

    private void setUpLayout(){
        ActionBar actionBar = getActionBar();
        //Is possible actionBar will be null
        if(actionBar !=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.schedules));
        }
        imgAddAlarm = (ImageView)this.findViewById(R.id.btn_add_alarm);
        imgAddAlarm.setOnClickListener(addAlarmOnClickListener);
        ListView listView = getListView();
        registerForContextMenu(listView);
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

    private void createNewAlarm(){
        showTimePickerDialog(true, true);
    }

    private void showTimePickerDialog(boolean startOrEndTime, boolean newAlarm)
    {
        Bundle args = new Bundle();
        args.putBoolean("StartOrEndButton", startOrEndTime);
        args.putBoolean("NewAlarm", newAlarm);
        final TimePickerFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.setArguments(args);
        timePickerFragment.show(getFragmentManager(), "timePicker");
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
                        final TimePickerFragment timePickerFragment = new TimePickerFragment();
                        timePickerFragment.setArguments(args);
                        timePickerFragment.show(getFragmentManager(), "timePicker");
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
                timePickerFragment.show(getFragmentManager(), "timePicker");
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

}
