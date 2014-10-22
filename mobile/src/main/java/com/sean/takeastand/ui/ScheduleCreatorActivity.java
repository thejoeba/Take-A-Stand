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

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ToggleButton;

import com.sean.takeastand.R;
import com.sean.takeastand.storage.AlarmSchedule;
import com.sean.takeastand.storage.ScheduleDatabaseAdapter;
import com.sean.takeastand.storage.ScheduleEditor;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;
import com.sean.takeastand.widget.TimePickerFragment;

import java.util.Calendar;

/**
 * Created by Sean on 2014-09-03.
 */
public class ScheduleCreatorActivity extends FragmentActivity
    implements NumberPicker.OnValueChangeListener
{

    /*
    Edit so that if user chooses an end time after the start time then nothing changes and a toast
         is displayed saying end time can't be before start time.  If the user chooses a new start time,
         and that start time is after the current end time, move the end time back by the difference
         it was before. */


    private static final String TAG = "ScheduleCreatorActivity";
    private static final int NUMBER_HOURS_TO_ADD = 1;
    private ToggleButton btnActivated;
    private Button btnStartTime;
    private Button btnEndTime;
    private CheckBox chBxSunday;
    private CheckBox chBxMonday;
    private CheckBox chBxTuesday;
    private CheckBox chBxWednesday;
    private CheckBox chBxThursday;
    private CheckBox chBxFriday;
    private CheckBox chBxSaturday;
    private Button btnFrequency;
    private EditText scheduleName;
    private Button btnSave;
    private Button btnCancel;
    private boolean mStartEndButtonSelected;
    private AlarmSchedule mAlarmSchedule;
    private int mArrayPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_schedule);
        if(getIntent().hasExtra(Constants.SELECTED_ALARM_SCHEDULE)){
            mAlarmSchedule = getIntent().getParcelableExtra(Constants.SELECTED_ALARM_SCHEDULE);
        }
        mArrayPosition = getIntent().getIntExtra(Constants.EDITED_ALARM_POSITION, -1);
        initializeViewsAndButtons();
        setViewText();
        setViewStatus();
        removeKeyboardStart();
    }

    private void initializeViewsAndButtons()
    {
        btnActivated = (ToggleButton)findViewById(R.id.btnActivated);
        btnStartTime = (Button)findViewById(R.id.btnStartTime);
        btnStartTime.setOnClickListener(startTimeListener);
        btnEndTime = (Button)findViewById(R.id.btnEndTime);
        btnEndTime.setOnClickListener(endTimeListener);
        chBxSunday = (CheckBox)findViewById(R.id.chbxSun);
        chBxMonday = (CheckBox)findViewById(R.id.chbxMon);
        chBxTuesday = (CheckBox)findViewById(R.id.chbxTue);
        chBxWednesday = (CheckBox)findViewById(R.id.chbxWed);
        chBxThursday = (CheckBox)findViewById(R.id.chbxThu);
        chBxFriday = (CheckBox)findViewById(R.id.chbxFri);
        chBxSaturday = (CheckBox)findViewById(R.id.chbxSat);
        btnFrequency = (Button)findViewById(R.id.btnFrequency);
        btnFrequency.setOnClickListener(frequencyListener);
        scheduleName = (EditText)findViewById(R.id.editName);
        btnSave = (Button)findViewById(R.id.btnSave);
        btnSave.setOnClickListener(saveListener);
        btnCancel = (Button)findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(cancelListener);
    }

    private void setViewText(){
        if(mAlarmSchedule==null){
            Calendar calendar = Calendar.getInstance();
            String timeNow = Integer.toString(calendar.get(Calendar.HOUR_OF_DAY));
            timeNow += ":" + Utils.correctMinuteFormat(Integer.toString(calendar.get(Calendar.MINUTE)));
            btnStartTime.setText(timeNow);
            String endTime;
            if( ( calendar.get(Calendar.HOUR_OF_DAY) + NUMBER_HOURS_TO_ADD ) >23){
                endTime = Integer.toString(NUMBER_HOURS_TO_ADD - 1);
            } else {
                endTime= Integer.toString(calendar.get(Calendar.HOUR_OF_DAY) + NUMBER_HOURS_TO_ADD);
            }
            endTime += ":" + Utils.correctMinuteFormat(Integer.toString(calendar.get(Calendar.MINUTE)));
            btnEndTime.setText(endTime);
        } else {
            btnStartTime.setText(Utils.calendarToTimeString(mAlarmSchedule.getStartTime()));
            btnEndTime.setText(Utils.calendarToTimeString(mAlarmSchedule.getEndTime()));
            //Will also in future check for alertType
            btnFrequency.setText(Integer.toString(mAlarmSchedule.getFrequency()));
            scheduleName.setText(mAlarmSchedule.getTitle());
        }
    }

    private void setViewStatus(){
        if(mAlarmSchedule==null){
            btnActivated.setChecked(true);
        } else {
            btnActivated.setChecked(mAlarmSchedule.getActivated());
        }
        setAvailableCheckboxes();
    }

    private void removeKeyboardStart()
    {
        if (scheduleName != null) {
            scheduleName.clearFocus();
        }
        getWindow().setSoftInputMode(3);
    }

    View.OnClickListener startTimeListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            mStartEndButtonSelected = true;
            showTimePickerDialog(view);
        }
    };

    View.OnClickListener endTimeListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            mStartEndButtonSelected = false;
            showTimePickerDialog(view);
        }
    };

    private void showTimePickerDialog(View view)
    {
        Bundle args = new Bundle();
        args.putBoolean("StartOrEndButton", mStartEndButtonSelected);
        if(mStartEndButtonSelected){
            args.putString(Constants.START_TIME_ARG, btnStartTime.getText().toString());
        } else {
            args.putString(Constants.END_TIME_ARG, btnEndTime.getText().toString());
        }
        TimePickerFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.setArguments(args);
        timePickerFragment.show(getFragmentManager(), "timePicker");
    }

    View.OnClickListener frequencyListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            showNumberPickerDialog();
        }
    };



    public void showNumberPickerDialog()
    {
        final Dialog numberPickerDialog = new Dialog(this);
        numberPickerDialog.setTitle("NumberPicker");
        numberPickerDialog.setContentView(R.layout.dialog_number_picker);
        final NumberPicker numberPicker = (NumberPicker) numberPickerDialog.findViewById(R.id.numberPicker);
        numberPicker.setMaxValue(100);
        numberPicker.setMinValue(5);
        numberPicker.setValue(Integer.valueOf((btnFrequency.getText()).toString()));
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setOnValueChangedListener(this);
        /*btnSaveNp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                btnFrequency.setText(String.valueOf(numberPicker.getValue()));
                numberPickerDialog.dismiss();
            }
        });
        btnCancelNp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                numberPickerDialog.dismiss();
            }
        });
        numberPickerDialog.show();*/
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i2) {

    }




    public void setButton(String time)
    {
        if (mStartEndButtonSelected)
        {
            btnStartTime.setText(time);
        } else {
            btnEndTime.setText(time);
        }
    }

    View.OnClickListener saveListener = new View.OnClickListener()
    {
        public void onClick(View view)
        {
            boolean newAlarm;
            if(mAlarmSchedule==null){
                saveNewAlarm();
                newAlarm = true;
            } else {
                editAlarm();
                newAlarm = false;
            }
            Intent intent = new Intent();
            intent.putExtra(Constants.ACTIVITY_RESULT, "save");
            //You need to add the mAlarmSchedule so that it can be added to the arrayadapter
            //on ScheduleListActivity so that the new alarmSchedule is shown in the listview
            intent.putExtra(Constants.EDITED_ALARM_SCHEDULE, mAlarmSchedule);
            //This serves as a flag for the SchedulesListActivity to know whether to add a new
            //schedule to the array or to replace the existing
            intent.putExtra(Constants.NEW_ALARM_SCHEDULE, newAlarm);
            intent.putExtra(Constants.EDITED_ALARM_POSITION, mArrayPosition);
            setResult(0, intent);
            finish();
        }
    };

    View.OnClickListener cancelListener = new View.OnClickListener()
    {
        public void onClick(View view)
        {
            Intent intent = new Intent();
            intent.putExtra(Constants.ACTIVITY_RESULT, "cancel");
            setResult(-1, intent);
            finish();
        }
    };

    private void saveNewAlarm(){
        boolean activated = isAlarmActivated();
        String alarmType = "";
        String startTime = getStartTime();
        String endTime = getEndTime();
        int frequency = getFrequency();
        String title = getAlarmTitle();
        boolean[] checkedDays = getCheckedDays();
        ScheduleEditor scheduleEditor = new ScheduleEditor(this);
        scheduleEditor.newAlarm(activated, alarmType, startTime, endTime, frequency, title,
                        checkedDays[0], checkedDays[1], checkedDays[2], checkedDays[3],
                        checkedDays[4], checkedDays[5], checkedDays[6]);
        mAlarmSchedule = new AlarmSchedule(new ScheduleDatabaseAdapter(this).getLastRowID(),
                        activated, alarmType, Utils.convertToCalendarTime(startTime),
                        Utils.convertToCalendarTime(endTime), frequency, title,
                        checkedDays[0], checkedDays[1], checkedDays[2], checkedDays[3], checkedDays[4],
                        checkedDays[5], checkedDays[6]);
    }

    private void editAlarm(){
        boolean activated = isAlarmActivated();
        String alarmType = "";
        String startTime = getStartTime();
        String endTime = getEndTime();
        int frequency = getFrequency();
        String title = getAlarmTitle();
        boolean[] checkedDays = getCheckedDays();
        int UID = mAlarmSchedule.getUID();
        ScheduleEditor scheduleEditor = new ScheduleEditor(this);
        //scheduleEditor.editAlarm(activated, startTime, endTime, frequency, title,
                //alarmType, checkedDays[0], checkedDays[1], checkedDays[2], checkedDays[3],
                //checkedDays[4], checkedDays[5], checkedDays[6], UID);
        mAlarmSchedule = new AlarmSchedule(UID, activated, alarmType, Utils.convertToCalendarTime(startTime),
                        Utils.convertToCalendarTime(endTime), frequency, title,
                        checkedDays[0], checkedDays[1], checkedDays[2], checkedDays[3], checkedDays[4],
                        checkedDays[5], checkedDays[6]);
    }

    private boolean isAlarmActivated(){
        return btnActivated.isChecked();
    }

    private String getStartTime(){
        Log.i(TAG, btnStartTime.getText().toString());
        return btnStartTime.getText().toString();
    }

    private String getEndTime(){
        Log.i(TAG, btnEndTime.getText().toString());
        return btnEndTime.getText().toString();
    }

    private int getFrequency(){
        return Integer.valueOf(btnFrequency.getText().toString());
    }

    private String getAlarmTitle(){
        return scheduleName.getText().toString();
    }

    private void setAvailableCheckboxes(){
        //If the day already is used by another alarm schedule, do not allow it to be checkable
        ScheduleDatabaseAdapter scheduleDatabaseAdapter = new ScheduleDatabaseAdapter(this);
        boolean[] activatedDays = scheduleDatabaseAdapter.getAlreadyTakenAlarmDays();
        if(activatedDays[0]){
            //If mAlarmSchedule == null, then this is a new schedule, so it couldn't be
            // //responsible for this day being activated already
            if(mAlarmSchedule==null){
                chBxSunday.setEnabled(false);
                //If Sunday isn't checked by this schedule don't allow this day to be checkable
            } else if (!mAlarmSchedule.getSunday()){
                chBxSunday.setEnabled(false);
                //This final case means that this is the schedule that checked the day initially
            } else {
                chBxSunday.setChecked(true);
                chBxSunday.setEnabled(true);
            }
        }
        if(activatedDays[1]){
            if(mAlarmSchedule==null){
                chBxMonday.setEnabled(false);
            } else if (!mAlarmSchedule.getMonday()){
                chBxMonday.setEnabled(false);
            } else {
                chBxMonday.setChecked(true);
                chBxMonday.setEnabled(true);
            }
        }
        if(activatedDays[2]){
            if(mAlarmSchedule==null){
                chBxTuesday.setEnabled(false);
            } else if (!mAlarmSchedule.getTuesday()){
                chBxTuesday.setEnabled(false);
            } else {
                chBxTuesday.setChecked(true);
                chBxTuesday.setEnabled(true);
            }
        }
        if(activatedDays[3]){
            if(mAlarmSchedule==null){
                chBxWednesday.setEnabled(false);
            } else if (!mAlarmSchedule.getWednesday()){
                chBxWednesday.setEnabled(false);
            } else {
                chBxWednesday.setChecked(true);
                chBxWednesday.setEnabled(true);
            }
        }
        if(activatedDays[4]){
            if(mAlarmSchedule==null){
                Log.i(TAG, "1");
                chBxThursday.setEnabled(false);
            } else if (!mAlarmSchedule.getThursday()){
                Log.i(TAG, "2");
                chBxThursday.setEnabled(false);
            } else {
                Log.i(TAG, "3");
                chBxThursday.setChecked(true);
                chBxThursday.setEnabled(true);
            }
        }
        if(activatedDays[5]){
            if(mAlarmSchedule==null){
                Log.i(TAG, "1");
                chBxFriday.setEnabled(false);
            } else if (!mAlarmSchedule.getFriday()){
                Log.i(TAG, "2");
                chBxFriday.setEnabled(false);
            } else {
                Log.i(TAG, "3");
                chBxFriday.setChecked(true);
                chBxFriday.setEnabled(true);
            }
        }
        if(activatedDays[6]){
            if(mAlarmSchedule==null){
                chBxSaturday.setEnabled(false);
            } else if (!mAlarmSchedule.getSaturday()){
                chBxSaturday.setEnabled(false);
            } else {
                chBxSaturday.setChecked(true);
                chBxSaturday.setEnabled(true);
            }
        }
    }

    private boolean[] getCheckedDays(){
        boolean[] checkedDays = {false, false, false, false, false, false, false};
        if(chBxSunday.isChecked()){
            checkedDays[0]=true;
        }
        if(chBxMonday.isChecked()){
            checkedDays[1]=true;
        }
        if(chBxTuesday.isChecked()){
            checkedDays[2]=true;
        }
        if(chBxWednesday.isChecked()){
            checkedDays[3]=true;
        }
        if(chBxThursday.isChecked()){
            checkedDays[4]=true;
        }
        if(chBxFriday.isChecked()){
            checkedDays[5]=true;
        }
        if(chBxSaturday.isChecked()){
            checkedDays[6]=true;
        }
        return checkedDays;
    }

}
