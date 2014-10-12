package com.sean.takeastand.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.sean.takeastand.alarmprocess.RepeatingAlarm;
import com.sean.takeastand.alarmprocess.ScheduledRepeatingAlarm;
import com.sean.takeastand.storage.AlarmSchedule;
import com.sean.takeastand.storage.AlarmScheduleListAdapter;
import com.sean.takeastand.storage.AlarmsDatabaseAdapter;
import com.sean.takeastand.R;
import com.sean.takeastand.storage.ScheduledAlarmEditor;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;

import java.util.ArrayList;

/**
 * Created by Sean on 2014-09-21.
 */
public class SchedulesListActivity extends ListActivity {

    private static final String TAG = "SchedulesListActivity";
    private static final int REQUEST_CODE = 1;
    private ImageView imgAddAlarm;
    private AlarmScheduleListAdapter alarmScheduleListAdapter;
    private  ArrayList<AlarmSchedule> alarmSchedules;
    private static final String EDIT_SCHEDULE = "edit";
    private View mContextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_list);

        //Deleting the database each time only during testing
        //deleteDatabase("alarms_database");
        launchEditIfNoAlarms();
        imgAddAlarm = (ImageView)this.findViewById(R.id.btn_add_alarm);
        imgAddAlarm.setOnClickListener(addAlarmOnClickListener);
        ListView listView = getListView();
        registerForContextMenu(listView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(alarmScheduleListAdapter == null){
            alarmScheduleListAdapter =
                    new AlarmScheduleListAdapter(this, android.R.id.list, alarmSchedules);
            setListAdapter(alarmScheduleListAdapter);
            alarmScheduleListAdapter.notifyDataSetChanged();
        } else {
            alarmScheduleListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        AlarmsDatabaseAdapter alarmsDatabaseAdapter = new AlarmsDatabaseAdapter(this);
        int numberOfAlarms = alarmsDatabaseAdapter.getCount();
        if(requestCode == REQUEST_CODE){
            if(resultCode ==  -1&& numberOfAlarms == 0){
                finish();
            } else {
                if (intent.getStringExtra(Constants.ACTIVITY_RESULT).equals("save")) {
                    Log.i(TAG, "Created/Edited Alarm has been saved");
                    AlarmSchedule editedAlarm =
                            intent.getParcelableExtra(Constants.EDITED_ALARM_SCHEDULE);
                    if (intent.getBooleanExtra(Constants.NEW_ALARM_SCHEDULE, true)) {
                        Log.i(TAG, "New Alarm added.");
                        alarmSchedules.add(editedAlarm);
                    } else {
                        int arrayPosition = intent.getIntExtra(Constants.EDITED_ALARM_POSITION, -1);
                        if(arrayPosition!=-1){
                            alarmSchedules.remove(arrayPosition);
                            alarmSchedules.add(arrayPosition, editedAlarm);
                            Log.i(TAG, "Alarm " + arrayPosition + " edited.");
                        }
                    }
                }
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
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_context_menu, menu);
        mContextView = view;
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

    private void launchEditIfNoAlarms(){
        AlarmsDatabaseAdapter alarmsDatabaseAdapter = new AlarmsDatabaseAdapter(this);
        alarmSchedules = alarmsDatabaseAdapter.getAlarmSchedules();
        if(alarmSchedules.size() == 0) {
            Log.i(TAG, "No alarm schedules");
           createNewAlarm();
        } else {
            Log.i(TAG, "AlarmSchedule ArrayList size: " + Integer.toString(alarmSchedules.size()));
            alarmScheduleListAdapter =
                    new AlarmScheduleListAdapter(this, android.R.id.list, alarmSchedules);
            setListAdapter(alarmScheduleListAdapter);
        }
    }

    private View.OnClickListener addAlarmOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            createNewAlarm();
        }
    };

    private void deleteSchedule(int position){
        ScheduledAlarmEditor scheduledAlarmEditor = new ScheduledAlarmEditor(this);
        scheduledAlarmEditor.deleteAlarm(alarmSchedules.get(position));
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
            alarmScheduleListAdapter.clear();
            setListAdapter(null);
        } else {
            alarmSchedules.remove(position);
            alarmScheduleListAdapter.notifyDataSetChanged();
        }
        //Service needs to cancel any running alarms and notifications it is currently managing
        Intent informServiceOfDeletion = new Intent(Constants.ALARM_SCHEDULE_DELETED);
        informServiceOfDeletion.putExtra("UID", deletedAlarmUID);
        LocalBroadcastManager.getInstance(this).sendBroadcast(informServiceOfDeletion);
    }

    private void createNewAlarm(){
        Intent intent = new Intent(SchedulesListActivity.this, ScheduleCreatorActivity.class);
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
}
