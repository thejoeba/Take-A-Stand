package com.sean.takeastand.ui;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextThemeWrapper;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.sean.takeastand.storage.AlarmSchedule;
import com.sean.takeastand.storage.AlarmScheduleListAdapter;
import com.sean.takeastand.storage.AlarmsDatabaseAdapter;
import com.sean.takeastand.R;
import com.sean.takeastand.storage.ScheduledAlarmEditor;

import java.util.ArrayList;

/**
 * Created by Sean on 2014-09-21.
 */
public class SchedulesListActivity extends ListActivity {

    private static final String TAG = "SchedulesListActivity";
    private static final int REQUEST_CODE = 1;
    private AlarmScheduleListAdapter alarmScheduleListAdapter;
    private  ArrayList<AlarmSchedule> alarmSchedules;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_list);
        //Deleting the database each time only during testing
        //deleteDatabase("alarms_database");
        AlarmsDatabaseAdapter alarmsDatabaseAdapter = new AlarmsDatabaseAdapter(this);
        alarmSchedules = alarmsDatabaseAdapter.getAlarmSchedules();
        if(alarmSchedules.size()==0) {
            Log.i(TAG, "No alarm schedules");
            startActivityForResult(new Intent(this, ScheduleCreatorActivity.class), REQUEST_CODE);
        } else {
            Log.i(TAG, "AlarmSchedule ArrayList size: " + Integer.toString(alarmSchedules.size()));
            alarmScheduleListAdapter =
                    new AlarmScheduleListAdapter(this, android.R.id.list, alarmSchedules);
            setListAdapter(alarmScheduleListAdapter);
        }

        ListView listView = getListView();
        registerForContextMenu(listView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE){
            if(resultCode ==  -1){
                finish();
            } else {
                //Needs to be reinitialized to prevent null pointer exception (is not initialized
                //if launches the activity);
                alarmScheduleListAdapter =
                        new AlarmScheduleListAdapter(this, android.R.id.list, alarmSchedules);
                setListAdapter(alarmScheduleListAdapter);
                alarmScheduleListAdapter.notifyDataSetChanged();
            }
        }
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_context_menu, menu);
    }

    //Respond to the user selecting an item within the context menu
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            //DELETE
            case R.id.delete:
                ScheduledAlarmEditor scheduledAlarmEditor = new ScheduledAlarmEditor(this);
                scheduledAlarmEditor.deleteAlarm(alarmSchedules.get(info.position));
                alarmSchedules.remove(info.position);
                alarmScheduleListAdapter.notifyDataSetChanged();
                return true;
            default:
                return false;
        }
    }


}
