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

import com.sean.takeastand.storage.ScheduledAlarmEditor;
import com.sean.takeastand.storage.AlarmsDatabaseAdapter;
import com.sean.takeastand.R;
import com.sean.takeastand.widget.TimePickerFragment;

import java.util.Calendar;

/**
 * Created by Sean on 2014-09-03.
 */
public class ScheduleCreatorActivity
        extends FragmentActivity
        implements TimePickerFragment.EditButtonDialogListener,
        NumberPicker.OnValueChangeListener
{

    /*
    Edit so that if user chooses an end time after the start time then nothing changes and a toast
         is displayed saying end time can't be before start time.  If the user chooses a new start time,
         and that start time is after the current end time, move the end time back by the difference
         it was before. */


    private static final String TAG = "ScheduleCreatorActivity";
    private static final int NUMBER_HOURS_TO_ADD = 4;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_schedule);
        initializeViewsAndButtons();
        removeKeyboardStart();
    }

    private void initializeViewsAndButtons()
    {
        btnActivated = (ToggleButton)findViewById(R.id.btnActivated);
        btnActivated.setChecked(true);
        btnStartTime = (Button)findViewById(R.id.btnStartTime);
        btnStartTime.setOnClickListener(startTimeListener);
        Calendar calendar = Calendar.getInstance();
        String timeNow = Integer.toString(calendar.get(Calendar.HOUR_OF_DAY));
        timeNow += ":" + correctMinuteFormat(Integer.toString(calendar.get(Calendar.MINUTE)));
        btnStartTime.setText(timeNow);
        btnEndTime = (Button)findViewById(R.id.btnEndTime);
        btnEndTime.setOnClickListener(endTimeListener);
        String endTime= Integer.toString(calendar.get(Calendar.HOUR_OF_DAY) + NUMBER_HOURS_TO_ADD);
        endTime += ":" + correctMinuteFormat(Integer.toString(calendar.get(Calendar.MINUTE)));
        btnEndTime.setText(endTime);
        chBxSunday = (CheckBox)findViewById(R.id.chbxSun);
        chBxMonday = (CheckBox)findViewById(R.id.chbxMon);
        chBxTuesday = (CheckBox)findViewById(R.id.chbxTue);
        chBxWednesday = (CheckBox)findViewById(R.id.chbxWed);
        chBxThursday = (CheckBox)findViewById(R.id.chbxThu);
        chBxFriday = (CheckBox)findViewById(R.id.chbxFri);
        chBxSaturday = (CheckBox)findViewById(R.id.chbxSat);
        setAvailableCheckboxes();
        btnFrequency = (Button)findViewById(R.id.btnFrequency);
        btnFrequency.setOnClickListener(frequencyListener);
        scheduleName = (EditText)findViewById(R.id.editName);
        btnSave = (Button)findViewById(R.id.btnSave);
        btnSave.setOnClickListener(saveListener);
        btnCancel = (Button)findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(cancelListener);
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

    private String correctMinuteFormat(String minute){
        if(minute.length()==1){
            minute = "0" + minute;
        }
        return minute;
    }
    public void showNumberPickerDialog()
    {
        final Dialog numberPickerDialog = new Dialog(this);
        numberPickerDialog.setTitle("NumberPicker");
        numberPickerDialog.setContentView(R.layout.dialog_number_picker);
        Button btnCancelNp = (Button) numberPickerDialog.findViewById(R.id.btnCancelNp);
        Button btnSaveNp = (Button) numberPickerDialog.findViewById(R.id.btnSaveNp);
        final NumberPicker numberPicker = (NumberPicker) numberPickerDialog.findViewById(R.id.numberPicker);
        numberPicker.setMaxValue(100);
        numberPicker.setMinValue(5);
        numberPicker.setValue(Integer.valueOf((btnFrequency.getText()).toString()));
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setOnValueChangedListener(this);
        btnSaveNp.setOnClickListener(new View.OnClickListener()
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
        numberPickerDialog.show();
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i2) {

    }

    private void setAvailableCheckboxes(){
        //If the day already has an alarm schedule do not allow it to be checkable
        AlarmsDatabaseAdapter alarmsDatabaseAdapter = new AlarmsDatabaseAdapter(this);
        boolean[] activatedDays = alarmsDatabaseAdapter.getAlreadyTakenAlarmDays();
        if(activatedDays[0]){
            chBxSunday.setEnabled(false);
        }
        if(activatedDays[1]){
            chBxMonday.setEnabled(false);
        }
        if(activatedDays[2]){
            chBxTuesday.setEnabled(false);
        }
        if(activatedDays[3]){
            chBxWednesday.setEnabled(false);
        }
        if(activatedDays[4]){
            chBxThursday.setEnabled(false);
        }
        if(activatedDays[5]){
            chBxFriday.setEnabled(false);
        }
        if(activatedDays[6]){
            chBxSaturday.setEnabled(false);
        }
    }

    @Override
    public void onTimeSelected(String time) {
        setButton(time);
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
            saveNewAlarm();
            Intent localIntent = new Intent();
            localIntent.putExtra("result", "save");
            setResult(0, localIntent);
            finish();
        }
    };

    View.OnClickListener cancelListener = new View.OnClickListener()
    {
        public void onClick(View view)
        {
            Intent localIntent = new Intent();
            localIntent.putExtra("result", "cancel");
            setResult(-1, localIntent);
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
        ScheduledAlarmEditor scheduledAlarmEditor = new ScheduledAlarmEditor(this);
        scheduledAlarmEditor.newAlarm(activated, alarmType, startTime, endTime, frequency, title,
                        checkedDays[0], checkedDays[1], checkedDays[2], checkedDays[3],
                        checkedDays[4], checkedDays[5], checkedDays[6]);
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
