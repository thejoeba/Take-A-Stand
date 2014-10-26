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
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sean.takeastand.R;
import com.sean.takeastand.alarmprocess.ScheduledRepeatingAlarm;
import com.sean.takeastand.storage.AlarmSchedule;
import com.sean.takeastand.storage.ExpandableAdapter;
import com.sean.takeastand.storage.FixedAlarmSchedule;
import com.sean.takeastand.storage.ScheduleDatabaseAdapter;
import com.sean.takeastand.storage.ScheduleEditor;
import com.sean.takeastand.storage.ScheduleListAdapter;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;
import com.sean.takeastand.widget.TimePickerFragment;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Sean on 2014-09-21.
 */
public class ScheduleListActivity extends ListActivity {

    private static final String TAG = "SchedulesListActivity";
    private static final int REQUEST_CODE = 1;
    private ImageView imgAddAlarm;
    private ScheduleListAdapter scheduleListAdapter;
    private ExpandableAdapter expandableAdapter;
    private  ArrayList<AlarmSchedule> alarmSchedules;
    private static final String EDIT_SCHEDULE = "edit";
    private String mNewAlarmStartTime;
    private String mNewAlarmEndTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_list);

        //Deleting the database each time only during testing
        //deleteDatabase("alarms_database");
        imgAddAlarm = (ImageView)this.findViewById(R.id.btn_add_alarm);
        imgAddAlarm.setOnClickListener(addAlarmOnClickListener);
        ListView listView = getListView();
        registerForContextMenu(listView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        alarmSchedules = new ScheduleDatabaseAdapter(this).getAlarmSchedules();
        scheduleListAdapter =
                new ScheduleListAdapter(this, android.R.id.list, alarmSchedules, getLayoutInflater());
        Log.i(TAG, Integer.toString(scheduleListAdapter.getCount()));
        expandableAdapter = new ExpandableAdapter(this, scheduleListAdapter, R.id.clickToExpand, R.id.bottomContainer);
        setListAdapter(expandableAdapter);
        registerReceivers();
    }

    private View.OnClickListener addAlarmOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            createNewAlarm();
        }
    };

    private void registerReceivers(){
        LocalBroadcastManager.getInstance(this).registerReceiver(deleteScheduleReceiver,
                new IntentFilter("Delete"));
        LocalBroadcastManager.getInstance(this).registerReceiver(titleChangeReceiver,
                new IntentFilter("TitleChange"));
        LocalBroadcastManager.getInstance(this).registerReceiver(timePickerReceiver,
                new IntentFilter("TimePicker"));
        LocalBroadcastManager.getInstance(this).registerReceiver(numberPickerReceiver,
                new IntentFilter("NumberPicker"));
    }

    private void deleteSchedule(int position){
        ScheduleEditor scheduleEditor = new ScheduleEditor(this);
        scheduleEditor.deleteAlarm(alarmSchedules.get(position));
        AlarmSchedule deletedAlarmSchedule = alarmSchedules.get(position);
        int deletedAlarmUID = deletedAlarmSchedule.getUID();
        int currentlyRunningAlarm = Utils.getRunningScheduledAlarm(this);
        if(deletedAlarmUID == currentlyRunningAlarm){
            FixedAlarmSchedule fixedAlarmSchedule = new FixedAlarmSchedule(deletedAlarmSchedule);
            new ScheduledRepeatingAlarm(this, fixedAlarmSchedule).cancelAlarm();
        }
        //If deleting the last alarm set listadapter to null
        if(position == 0&&alarmSchedules.size() == 1){
            Log.i(TAG, "Deleting the last alarmSchedule");
            alarmSchedules.clear();
            scheduleListAdapter.clear();
            setListAdapter(null);
        } else {
            alarmSchedules.remove(position);
            scheduleListAdapter.notifyDataSetChanged();
        }
        //Service needs to cancel any running alarms and notifications it is currently managing
        Intent informServiceOfDeletion = new Intent(Constants.ALARM_SCHEDULE_DELETED);
        informServiceOfDeletion.putExtra("UID", deletedAlarmUID);
        LocalBroadcastManager.getInstance(this).sendBroadcast(informServiceOfDeletion);
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

    private BroadcastReceiver deleteScheduleReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scheduleListAdapter.removeAlarm(intent.getIntExtra("Row", -1));
        }
    };

    private BroadcastReceiver titleChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scheduleListAdapter.updateTitle(intent.getStringExtra("NewTitle"),
                    intent.getIntExtra("Position", -1));
        }
    };

    private BroadcastReceiver timePickerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received timePicker intent");
            if(intent.getBooleanExtra("NewAlarm", false) &&
                    intent.getBooleanExtra("StartTime", true)){
                //Was called by ScheduleListActivity
                Log.i(TAG, "New Alarm Start Time:" + intent.getStringExtra("TimeSelected"));
                mNewAlarmStartTime = intent.getStringExtra("TimeSelected");
                showTimePickerDialog(false, true);
            } else if (intent.getBooleanExtra("NewAlarm", false) &&
                    !intent.getBooleanExtra("StartTime", true)){
                //Was called by ScheduleListActivity
                Log.i(TAG, "New Alarm End Time:" + intent.getStringExtra("TimeSelected"));
                mNewAlarmEndTime = intent.getStringExtra("TimeSelected");
                createNewSchedule();
            } else {
                //Was called by the ScheduleListAdapter, pass data in
                scheduleListAdapter.updateStartEndTime(intent.getStringExtra("TimeSelected"),
                        intent.getIntExtra("Position", -1));
            }

        }
    };

    private BroadcastReceiver numberPickerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scheduleListAdapter.updateFrequency(intent.getIntExtra("NewFrequency", -1),
                    intent.getIntExtra("Position", -1));
        }
    };

    private void createNewSchedule(){
        boolean[] availableDays = new ScheduleDatabaseAdapter(this).getAlreadyTakenAlarmDays();
        boolean[] newActivatedDays = {false, false, false, false, false, false, false};
        ScheduleEditor scheduleEditor = new ScheduleEditor(this);
        Calendar rightNow = Calendar.getInstance();
        if(!availableDays[ (rightNow.get(Calendar.DAY_OF_WEEK) - 1)]){
            newActivatedDays[ (rightNow.get(Calendar.DAY_OF_WEEK) - 1)] = true;
        }
        scheduleEditor.newAlarm(true, Utils.getDefaultAlertType(this), mNewAlarmStartTime,
                mNewAlarmEndTime, Utils.getDefaultFrequency(this), "", newActivatedDays[0],
                newActivatedDays[1], newActivatedDays[2], newActivatedDays[3], newActivatedDays[4],
                newActivatedDays[5], newActivatedDays[6]);
        alarmSchedules.add((
                new ScheduleDatabaseAdapter(this).getAlarmSchedules().get(alarmSchedules.size())));
        scheduleListAdapter.notifyDataSetChanged();
    }

}
