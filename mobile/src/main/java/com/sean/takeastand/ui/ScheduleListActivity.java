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
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;

import com.sean.takeastand.R;
import com.sean.takeastand.alarmprocess.ScheduledRepeatingAlarm;
import com.sean.takeastand.storage.AlarmSchedule;
import com.sean.takeastand.storage.ExpandableAdapter;
import com.sean.takeastand.storage.ScheduleDatabaseAdapter;
import com.sean.takeastand.storage.ScheduleEditor;
import com.sean.takeastand.storage.ScheduleListAdapter;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;
import com.sean.takeastand.widget.TimePickerFragment;

import java.util.ArrayList;

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

        //removeKeyboardStart();
    }

    private void removeKeyboardStart()
    {
        getWindow().setSoftInputMode(3);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(scheduleListAdapter == null){
            scheduleListAdapter =
                    new ScheduleListAdapter(this, android.R.id.list, alarmSchedules, getLayoutInflater());
            //setListAdapter(scheduleListAdapter);
            //scheduleListAdapter.notifyDataSetChanged();
        } else {
            //scheduleListAdapter.notifyDataSetChanged();
        }
        alarmSchedules = new ScheduleDatabaseAdapter(this).getAlarmSchedules();
        scheduleListAdapter =
                new ScheduleListAdapter(this, android.R.id.list, alarmSchedules, getLayoutInflater());
        Log.i(TAG, Integer.toString(scheduleListAdapter.getCount()));
        expandableAdapter = new ExpandableAdapter(this, scheduleListAdapter, R.id.clickToExpand, R.id.bottomContainer);
        setListAdapter(expandableAdapter);
        registerReceivers();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_context_menu, menu);
    }

    //Respond to the user selecting an item within the context menu
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            //DELETE
            case R.id.delete:
                deleteSchedule(info.position);
                return true;
            case R.id.edit:
                editAlarm(info.position);
                return true;
            default:
                return false;
        }
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
            new ScheduledRepeatingAlarm(this, deletedAlarmSchedule).cancelAlarm();
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
        Intent intent = new Intent(ScheduleListActivity.this, ScheduleCreatorActivity.class);
        intent.putExtra(EDIT_SCHEDULE, false);
        startActivityForResult(intent, REQUEST_CODE);
    }

    private void editAlarm(int position){
        AlarmSchedule selectedAlarm = alarmSchedules.get(position);
        Intent intent = new Intent(this, ScheduleCreatorActivity.class);
        intent.putExtra(EDIT_SCHEDULE, true);
        intent.putExtra(Constants.SELECTED_ALARM_SCHEDULE, selectedAlarm);
        intent.putExtra(Constants.EDITED_ALARM_POSITION, position);
        startActivityForResult(intent, REQUEST_CODE);
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
            scheduleListAdapter.updateStartEndTime(intent.getStringExtra("TimeSelected"),
                    intent.getIntExtra("Position", -1));
        }
    };

    private BroadcastReceiver numberPickerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scheduleListAdapter.updateFrequency(intent.getIntExtra("NewFrequency", -1),
                    intent.getIntExtra("Position", -1));
        }
    };

}
