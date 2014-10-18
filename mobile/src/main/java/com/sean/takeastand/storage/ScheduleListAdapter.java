

package com.sean.takeastand.storage;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ToggleButton;

import com.sean.takeastand.R;
import com.sean.takeastand.ui.ScheduleListActivity;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;
import com.sean.takeastand.widget.TimePickerFragment;

import java.util.ArrayList;
import java.util.Calendar;

/* This class is responsible for setting up the views and their characteristics that each row will
have in the ScheduleListActivity.  */
/*
 * Created by Sean on 2014-10-05.
 */
public class ScheduleListAdapter extends ArrayAdapter<AlarmSchedule> {


    private ArrayList<AlarmSchedule> mAlarmSchedules;
    private Context mContext;
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
    private boolean mStartEndButtonSelected;
    private int mPosition;

    public ScheduleListAdapter(Context context, int resource, ArrayList<AlarmSchedule> alarmSchedules) {
        super(context, resource, alarmSchedules);
        mAlarmSchedules = alarmSchedules;
        mContext = context;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        View rowView = view;
        if(rowView==null){
            LayoutInflater inflater =
                    (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.row_layout, null);

        }
        mPosition = position;
        initializeViewsAndButtons(rowView);
        setTextAndStatus(mAlarmSchedules, position);
        return rowView;
    }

    private void initializeViewsAndButtons(View view){
        btnActivated = (ToggleButton)view.findViewById(R.id.btnActivated);
        btnStartTime = (Button)view.findViewById(R.id.btnStartTime);
        btnStartTime.setOnClickListener(startTimeListener);
        btnEndTime = (Button)view.findViewById(R.id.btnEndTime);
        btnEndTime.setOnClickListener(endTimeListener);
        chBxSunday = (CheckBox)view.findViewById(R.id.chbxSun);
        chBxMonday = (CheckBox)view.findViewById(R.id.chbxMon);
        chBxTuesday = (CheckBox)view.findViewById(R.id.chbxTue);
        chBxWednesday = (CheckBox)view.findViewById(R.id.chbxWed);
        chBxThursday = (CheckBox)view.findViewById(R.id.chbxThu);
        chBxFriday = (CheckBox)view.findViewById(R.id.chbxFri);
        chBxSaturday = (CheckBox)view.findViewById(R.id.chbxSat);
        btnFrequency = (Button)view.findViewById(R.id.btnFrequency);
        btnFrequency.setOnClickListener(frequencyListener);
        scheduleName = (EditText)view.findViewById(R.id.editTitle);
    }

    private void setTextAndStatus(ArrayList<AlarmSchedule> alarmSchedules, int position){
        AlarmSchedule alarmSchedule = alarmSchedules.get(position);
        scheduleName.setHint(alarmSchedule.getTitle());
        btnActivated.setChecked(alarmSchedule.getActivated());
        //txtAlertType.setText(alarmSchedule.getAlertType());
        btnStartTime.setText(Utils.calendarToTimeString(alarmSchedule.getStartTime()));
        btnEndTime.setText(Utils.calendarToTimeString(alarmSchedule.getEndTime()));
        btnFrequency.setText(Integer.toString(alarmSchedule.getFrequency()));
        chBxSunday.setChecked(alarmSchedule.getSunday());
        chBxMonday.setChecked(alarmSchedule.getMonday());
        chBxTuesday.setChecked(alarmSchedule.getTuesday());
        chBxWednesday.setChecked(alarmSchedule.getWednesday());
        chBxThursday.setChecked(alarmSchedule.getThursday());
        chBxFriday.setChecked(alarmSchedule.getFriday());
        chBxSaturday.setChecked(alarmSchedule.getSaturday());
        setAvailableCheckboxes(alarmSchedule);
    }

    private View.OnClickListener startTimeListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            mStartEndButtonSelected = true;
            showTimePickerDialog(view);
        }
    };

    private View.OnClickListener endTimeListener = new View.OnClickListener(){

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
        Activity activity = (Activity)getContext();
        timePickerFragment.show(activity.getFragmentManager(), "timePicker");
    }

    private View.OnClickListener frequencyListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            showNumberPickerDialog();
        }
    };



    private void showNumberPickerDialog()
    {
        final Dialog numberPickerDialog = new Dialog(mContext);
        numberPickerDialog.setTitle("NumberPicker");
        numberPickerDialog.setContentView(R.layout.dialog_number_picker);
        Button btnCancelNp = (Button) numberPickerDialog.findViewById(R.id.btnCancelNp);
        Button btnSaveNp = (Button) numberPickerDialog.findViewById(R.id.btnSaveNp);
        final NumberPicker numberPicker = (NumberPicker) numberPickerDialog.findViewById(R.id.numberPicker);
        numberPicker.setMaxValue(100);
        numberPicker.setMinValue(5);
        numberPicker.setValue(Integer.valueOf((btnFrequency.getText()).toString()));
        numberPicker.setWrapSelectorWheel(false);
        //numberPicker.setOnValueChangedListener(ScheduleListActivity.class);
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

    /*@Override
    public void onTimeSelected(String time) {
        setButton(time);
    }*/

    private void setButton(String time)
    {
        ScheduleEditor scheduleEditor = new ScheduleEditor(mContext);
        AlarmSchedule previousAlarmSchedule = mAlarmSchedules.get(mPosition);
        if (mStartEndButtonSelected)
        {
            btnStartTime.setText(time);
            Calendar startTime = Utils.convertToCalendarTime(time);
            AlarmSchedule currentAlarmSchedule = new AlarmSchedule(previousAlarmSchedule.getUID(),
                    previousAlarmSchedule.getActivated(), previousAlarmSchedule.getAlertType(),
                    startTime, previousAlarmSchedule.getEndTime(), previousAlarmSchedule.getFrequency(),
                    previousAlarmSchedule.getTitle(), previousAlarmSchedule.getSunday(),
                    previousAlarmSchedule.getMonday(), previousAlarmSchedule.getTuesday(),
                    previousAlarmSchedule.getWednesday(), previousAlarmSchedule.getThursday(),
                    previousAlarmSchedule.getFriday(), previousAlarmSchedule.getSaturday());
            scheduleEditor.editStartTime(startTime, previousAlarmSchedule.getEndTime(),
                    Utils.isTodayActivated(previousAlarmSchedule.getSunday(), previousAlarmSchedule.getMonday(),
                            previousAlarmSchedule.getTuesday(), previousAlarmSchedule.getWednesday(),
                            previousAlarmSchedule.getThursday(), previousAlarmSchedule.getFriday(),
                            previousAlarmSchedule.getSaturday()),previousAlarmSchedule.getUID(),
                            currentAlarmSchedule);
        } else {
            btnEndTime.setText(time);
        }
    }

    private void setAvailableCheckboxes(AlarmSchedule alarmSchedule){
        //If the day already is used by another alarm schedule, do not allow it to be checkable
        ScheduleDatabaseAdapter scheduleDatabaseAdapter = new ScheduleDatabaseAdapter(mContext);
        boolean[] activatedDays = scheduleDatabaseAdapter.getAlreadyTakenAlarmDays();
        if(activatedDays[0]){
            //If mAlarmSchedule == null, then this is a new schedule, so it couldn't be
            // //responsible for this day being activated already
            if(alarmSchedule==null){
                chBxSunday.setEnabled(false);
                //If Sunday isn't checked by this schedule don't allow this day to be checkable
            } else if (!alarmSchedule.getSunday()){
                chBxSunday.setEnabled(false);
                //This final case means that this is the schedule that checked the day initially
            } else {
                chBxSunday.setChecked(true);
                chBxSunday.setEnabled(true);
            }
        }
        if(activatedDays[1]){
            if(alarmSchedule==null){
                chBxMonday.setEnabled(false);
            } else if (!alarmSchedule.getMonday()){
                chBxMonday.setEnabled(false);
            } else {
                chBxMonday.setChecked(true);
                chBxMonday.setEnabled(true);
            }
        }
        if(activatedDays[2]){
            if(alarmSchedule==null){
                chBxTuesday.setEnabled(false);
            } else if (!alarmSchedule.getTuesday()){
                chBxTuesday.setEnabled(false);
            } else {
                chBxTuesday.setChecked(true);
                chBxTuesday.setEnabled(true);
            }
        }
        if(activatedDays[3]){
            if(alarmSchedule==null){
                chBxWednesday.setEnabled(false);
            } else if (!alarmSchedule.getWednesday()){
                chBxWednesday.setEnabled(false);
            } else {
                chBxWednesday.setChecked(true);
                chBxWednesday.setEnabled(true);
            }
        }
        if(activatedDays[4]){
            if(alarmSchedule==null){
                chBxThursday.setEnabled(false);
            } else if (!alarmSchedule.getThursday()){
                chBxThursday.setEnabled(false);
            } else {
                chBxThursday.setChecked(true);
                chBxThursday.setEnabled(true);
            }
        }
        if(activatedDays[5]){
            if(alarmSchedule==null){
                chBxFriday.setEnabled(false);
            } else if (!alarmSchedule.getFriday()){
                chBxFriday.setEnabled(false);
            } else {
                chBxFriday.setChecked(true);
                chBxFriday.setEnabled(true);
            }
        }
        if(activatedDays[6]){
            if(alarmSchedule==null){
                chBxSaturday.setEnabled(false);
            } else if (!alarmSchedule.getSaturday()){
                chBxSaturday.setEnabled(false);
            } else {
                chBxSaturday.setChecked(true);
                chBxSaturday.setEnabled(true);
            }
        }
    }
}
