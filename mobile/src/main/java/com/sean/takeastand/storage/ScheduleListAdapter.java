

package com.sean.takeastand.storage;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.sean.takeastand.R;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;
import com.sean.takeastand.widget.TimePickerFragment;

import java.util.ArrayList;
import java.util.Calendar;

/* This class is responsible for setting up the views and characteristics that each row will
have in the ScheduleListActivity.  */
/*
 * Created by Sean on 2014-10-05.
 */
public class ScheduleListAdapter extends ArrayAdapter<AlarmSchedule> {


    private ArrayList<AlarmSchedule> mAlarmSchedules;
    private Context mContext;
    private TextView txtTitle;
    private ToggleButton btnActivated;
    private ImageView deleteButton;
    private CheckBox chBxLED;
    private CheckBox chBxVibrate;
    private CheckBox chBxSound;
    private RelativeLayout frequencyLayout;
    private RelativeLayout startTimeLayout;
    private RelativeLayout endTimeLayout;
    private TextView txtFrequencyValue;
    private TextView txtStartTimeValue;
    private TextView txtEndTimeValue;
    private CheckBox chBxSunday;
    private CheckBox chBxMonday;
    private CheckBox chBxTuesday;
    private CheckBox chBxWednesday;
    private CheckBox chBxThursday;
    private CheckBox chBxFriday;
    private CheckBox chBxSaturday;
    private boolean mStartEndButtonSelected;
    private final String TAG = "ScheduleListAdapter";
    private LayoutInflater mLayoutInflater;

    public ScheduleListAdapter(Context context, int resource, ArrayList<AlarmSchedule> alarmSchedules,
                               LayoutInflater layoutInflater) {
        super(context, resource, alarmSchedules);
        mAlarmSchedules = alarmSchedules;
        mContext = context;
        mLayoutInflater = layoutInflater;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        View rowView = view;
        if(rowView==null){
            LayoutInflater inflater =
                    (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.row_layout, null);

        }
        initializeViewsAndButtons(rowView, position);
        setTextAndStatus(mAlarmSchedules, position);
        return rowView;
    }

    private void initializeViewsAndButtons(View view, int position){
        txtTitle = (TextView)view.findViewById(R.id.editTitle);
        btnActivated = (ToggleButton)view.findViewById(R.id.btnActivated);
        deleteButton = (ImageView)view.findViewById(R.id.deleteButton);
        chBxLED = (CheckBox)view.findViewById(R.id.chbxLED);
        chBxVibrate = (CheckBox)view.findViewById(R.id.chbxVibrate);
        chBxSound = (CheckBox)view.findViewById(R.id.chbxSound);
        frequencyLayout = (RelativeLayout)view.findViewById(R.id.frequency);
        startTimeLayout = (RelativeLayout)view.findViewById(R.id.startTime);
        endTimeLayout = (RelativeLayout)view.findViewById(R.id.endTime);
        txtFrequencyValue = (TextView)view.findViewById(R.id.txtFrequencyValue);
        txtStartTimeValue = (TextView)view.findViewById(R.id.txtStartTimeValue);
        txtEndTimeValue = (TextView)view.findViewById(R.id.txtEndTimeValue);
        chBxSunday = (CheckBox)view.findViewById(R.id.chbxSun);
        chBxMonday = (CheckBox)view.findViewById(R.id.chbxMon);
        chBxTuesday = (CheckBox)view.findViewById(R.id.chbxTue);
        chBxWednesday = (CheckBox)view.findViewById(R.id.chbxWed);
        chBxThursday = (CheckBox)view.findViewById(R.id.chbxThu);
        chBxFriday = (CheckBox)view.findViewById(R.id.chbxFri);
        chBxSaturday = (CheckBox)view.findViewById(R.id.chbxSat);
        setOnClickListeners();
        addPositionTags(position);
    }

    private void setTextAndStatus(ArrayList<AlarmSchedule> alarmSchedules, int position){
        AlarmSchedule alarmSchedule = alarmSchedules.get(position);
        txtTitle.setText(alarmSchedule.getTitle());
        if((alarmSchedule.getTitle()).equals("")){
            int schedulePosition = position + 1;
            txtTitle.setText("Schedule " + schedulePosition);
        }
        btnActivated.setChecked(alarmSchedule.getActivated());
        int[] alertType = alarmSchedule.getAlertType();
        Log.i(TAG, alertType.toString());
        chBxLED.setChecked(Utils.convertIntToBoolean(alertType[0]));
        chBxVibrate.setChecked(Utils.convertIntToBoolean((alertType[1])));
        chBxSound.setChecked(Utils.convertIntToBoolean(alertType[2]));
        txtFrequencyValue.setText(Integer.toString(alarmSchedule.getFrequency()));
        txtStartTimeValue.setText(Utils.calendarToTimeString(alarmSchedule.getStartTime()));
        txtEndTimeValue.setText(Utils.calendarToTimeString(alarmSchedule.getEndTime()));
        chBxSunday.setChecked(alarmSchedule.getSunday());
        chBxMonday.setChecked(alarmSchedule.getMonday());
        chBxTuesday.setChecked(alarmSchedule.getTuesday());
        chBxWednesday.setChecked(alarmSchedule.getWednesday());
        chBxThursday.setChecked(alarmSchedule.getThursday());
        chBxFriday.setChecked(alarmSchedule.getFriday());
        chBxSaturday.setChecked(alarmSchedule.getSaturday());
        setAvailableCheckboxes(alarmSchedule);
    }

    private void addPositionTags(int position){
        txtTitle.setTag(position);
        btnActivated.setTag(position);
        deleteButton.setTag(position);
        chBxLED.setTag(position);
        chBxVibrate.setTag(position);
        chBxSound.setTag(position);
        frequencyLayout.setTag(position);
        startTimeLayout.setTag(position);
        endTimeLayout.setTag(position);
        txtFrequencyValue.setTag(position);
        txtStartTimeValue.setTag(position);
        txtEndTimeValue.setTag(position); chBxSunday.setTag(position);
        chBxMonday.setTag(position);
        chBxTuesday.setTag(position);
        chBxWednesday.setTag(position);
        chBxThursday.setTag(position);
        chBxFriday.setTag(position);
        chBxSaturday.setTag(position);
    }

    private void setOnClickListeners(){
        txtTitle.setOnClickListener(titleListener);
        btnActivated.setOnClickListener(activatedListener);
        deleteButton.setOnClickListener(deleteListener);
        chBxLED.setOnClickListener(alertTypeListener);
        chBxVibrate.setOnClickListener(alertTypeListener);
        chBxSound.setOnClickListener(alertTypeListener);
        frequencyLayout.setOnClickListener(frequencyListener);
        startTimeLayout.setOnClickListener(startTimeListener);
        endTimeLayout.setOnClickListener(endTimeListener);
        chBxSunday.setOnClickListener(checkedDayListener);
        chBxMonday.setOnClickListener(checkedDayListener);
        chBxTuesday.setOnClickListener(checkedDayListener);
        chBxWednesday.setOnClickListener(checkedDayListener);
        chBxThursday.setOnClickListener(checkedDayListener);
        chBxFriday.setOnClickListener(checkedDayListener);
        chBxSaturday.setOnClickListener(checkedDayListener);
    }

    private View.OnClickListener titleListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            showTitleDialog(view);
        }
    };

    private View.OnClickListener activatedListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            ToggleButton activated = (ToggleButton)view;
            boolean isActivated = activated.isChecked();
            int position = (Integer)view.getTag();
            AlarmSchedule previousAlarmSchedule = mAlarmSchedules.get(position);
            AlarmSchedule newAlarmSchedule = new AlarmSchedule(previousAlarmSchedule.getUID(),
                    isActivated, previousAlarmSchedule.getAlertType(),
                    previousAlarmSchedule.getStartTime(), previousAlarmSchedule.getEndTime(),
                    previousAlarmSchedule.getFrequency(), previousAlarmSchedule.getTitle(),
                    previousAlarmSchedule.getSunday(), previousAlarmSchedule.getMonday(),
                    previousAlarmSchedule.getTuesday(), previousAlarmSchedule.getWednesday(),
                    previousAlarmSchedule.getThursday(), previousAlarmSchedule.getFriday(),
                    previousAlarmSchedule.getSaturday());
            ScheduleEditor scheduleEditor = new ScheduleEditor(mContext);
            scheduleEditor.editActivated(newAlarmSchedule);
            Log.i(TAG, position + ".) Activated: " + Boolean.toString(isActivated));
        }
    };

    private View.OnClickListener deleteListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            showDeleteDialog(view);
        }
    };

    private View.OnClickListener alertTypeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            CheckBox checkBox = (CheckBox) view;
            boolean isChecked = checkBox.isChecked();
            int position = (Integer) view.getTag();
            AlarmSchedule previousAlarmSchedule = mAlarmSchedules.get(position);
            int[] alertType = previousAlarmSchedule.getAlertType();
            ScheduleEditor scheduleEditor = new ScheduleEditor(mContext);
            switch (view.getId()) {
                case R.id.chbxLED:
                    alertType[0] = Utils.convertBooleanToInt(isChecked);
                    AlarmSchedule newAlarmScheduleLED = new AlarmSchedule(previousAlarmSchedule.getUID(),
                            previousAlarmSchedule.getActivated(), alertType,
                            previousAlarmSchedule.getStartTime(), previousAlarmSchedule.getEndTime(),
                            previousAlarmSchedule.getFrequency(), previousAlarmSchedule.getTitle(),
                            previousAlarmSchedule.getSunday(), previousAlarmSchedule.getMonday(),
                            previousAlarmSchedule.getTuesday(), previousAlarmSchedule.getWednesday(),
                            previousAlarmSchedule.getThursday(), previousAlarmSchedule.getFriday(),
                            previousAlarmSchedule.getSaturday());
                    scheduleEditor.editAlertType(newAlarmScheduleLED);
                    Log.i(TAG, position + ".) LED is checked: " + Boolean.toString(isChecked));
                    break;
                case R.id.chbxVibrate:
                    alertType[1] = Utils.convertBooleanToInt(isChecked);
                    AlarmSchedule newAlarmScheduleVibrate = new AlarmSchedule(previousAlarmSchedule.getUID(),
                            previousAlarmSchedule.getActivated(), alertType,
                            previousAlarmSchedule.getStartTime(), previousAlarmSchedule.getEndTime(),
                            previousAlarmSchedule.getFrequency(), previousAlarmSchedule.getTitle(),
                            previousAlarmSchedule.getSunday(), previousAlarmSchedule.getMonday(),
                            previousAlarmSchedule.getTuesday(), previousAlarmSchedule.getWednesday(),
                            previousAlarmSchedule.getThursday(), previousAlarmSchedule.getFriday(),
                            previousAlarmSchedule.getSaturday());
                    scheduleEditor.editAlertType(newAlarmScheduleVibrate);
                    Log.i(TAG, position + ".) Vibrate is checked: " + Boolean.toString(isChecked));
                    break;
                case R.id.chbxSound:
                    alertType[2] = Utils.convertBooleanToInt(isChecked);
                    AlarmSchedule newAlarmScheduleSound = new AlarmSchedule(previousAlarmSchedule.getUID(),
                            previousAlarmSchedule.getActivated(), alertType,
                            previousAlarmSchedule.getStartTime(), previousAlarmSchedule.getEndTime(),
                            previousAlarmSchedule.getFrequency(), previousAlarmSchedule.getTitle(),
                            previousAlarmSchedule.getSunday(), previousAlarmSchedule.getMonday(),
                            previousAlarmSchedule.getTuesday(), previousAlarmSchedule.getWednesday(),
                            previousAlarmSchedule.getThursday(), previousAlarmSchedule.getFriday(),
                            previousAlarmSchedule.getSaturday());
                    scheduleEditor.editAlertType(newAlarmScheduleSound);
                    Log.i(TAG, position + ".) Sound is checked: " + Boolean.toString(isChecked));
                    break;
            }
        }
    };

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

    private View.OnClickListener frequencyListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            showNumberPickerDialog(view);
        }
    };

    private View.OnClickListener checkedDayListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            CheckBox checkBox = (CheckBox)view;
            boolean isChecked = checkBox.isChecked();
            int position = (Integer)view.getTag();
            AlarmSchedule previousAlarmSchedule = mAlarmSchedules.get(position);
            ScheduleEditor scheduleEditor = new ScheduleEditor(mContext);
            switch (view.getId()){
                case R.id.chbxSun:
                    Log.i(TAG, position + ".) Sunday is checked: " + Boolean.toString(isChecked));
                    scheduleEditor.editDays(Calendar.SUNDAY, isChecked,
                            newWeekDaySchedule(Calendar.SUNDAY, previousAlarmSchedule, isChecked));
                    break;
                case R.id.chbxMon:
                    Log.i(TAG, position + ".) Monday is checked: " + Boolean.toString(isChecked));
                    scheduleEditor.editDays(Calendar.MONDAY, isChecked,
                            newWeekDaySchedule(Calendar.MONDAY, previousAlarmSchedule, isChecked));
                    break;
                case R.id.chbxTue:
                    Log.i(TAG, position + ".) Tuesday is checked: " + Boolean.toString(isChecked));
                    scheduleEditor.editDays(Calendar.TUESDAY, isChecked,
                            newWeekDaySchedule(Calendar.TUESDAY, previousAlarmSchedule, isChecked));
                    break;
                case R.id.chbxWed:
                    Log.i(TAG, position + ".) Wednesday is checked: " + Boolean.toString(isChecked));
                    scheduleEditor.editDays(Calendar.WEDNESDAY, isChecked,
                            newWeekDaySchedule(Calendar.WEDNESDAY, previousAlarmSchedule, isChecked));
                    break;
                case R.id.chbxThu:
                    Log.i(TAG, position + ".) Thursday is checked: " + Boolean.toString(isChecked));
                    scheduleEditor.editDays(Calendar.THURSDAY, isChecked,
                            newWeekDaySchedule(Calendar.THURSDAY, previousAlarmSchedule, isChecked));
                    break;
                case R.id.chbxFri:
                    Log.i(TAG, position + ".) Friday is checked: " + Boolean.toString(isChecked));
                    scheduleEditor.editDays(Calendar.FRIDAY, isChecked,
                            newWeekDaySchedule(Calendar.FRIDAY, previousAlarmSchedule, isChecked));
                    break;
                case R.id.chbxSat:
                    Log.i(TAG, position + ".) Saturday is checked: " + Boolean.toString(isChecked));
                    scheduleEditor.editDays(Calendar.SATURDAY, isChecked,
                            newWeekDaySchedule(Calendar.SATURDAY, previousAlarmSchedule, isChecked));
                    break;
            }
        }
    };

    private void showDeleteDialog(View view){
        final ImageView deleteButton = (ImageView)view;
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("Are you sure that you want to delete " +
                txtTitle.getText().toString() + "?");
        builder.setTitle("Delete " + txtTitle.getText().toString());
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent("Delete");
                intent.putExtra("Row", (Integer)deleteButton.getTag());
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.i(TAG, "Cancel");
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private View dialogView;
    private TextView selectedTitle;

    private void showTitleDialog(View view){
        selectedTitle = (TextView)view;
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = mLayoutInflater;
        dialogView = inflater.inflate(R.layout.dialog_edit_text, null);
        builder.setView(dialogView);
        EditText editText = (EditText)dialogView.findViewById(R.id.editText);
        String currentTitle = selectedTitle.getText().toString();
        if(currentTitle.startsWith("Schedule ")){
            editText.setHint(currentTitle);
        } else {
            editText.setText(currentTitle);
            editText.setSelection(currentTitle.length());
        }
        builder.setMessage("New Title");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                EditText editText = (EditText)dialogView.findViewById(R.id.editText);
                Intent intent = new Intent("TitleChange");
                intent.putExtra("NewTitle", editText.getText().toString());
                intent.putExtra("Position", (Integer) selectedTitle.getTag());
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.i(TAG, "Cancel");
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //Only used for below method

    private TextView txtTimeSelected;
    private void showTimePickerDialog(View view)
    {
        RelativeLayout timeSelected = (RelativeLayout)view;
        Bundle args = new Bundle();
        args.putBoolean("StartOrEndButton", mStartEndButtonSelected);

        if(timeSelected==null){
            Log.i(TAG, "txtTimeSelected is null");
        }
        if(mStartEndButtonSelected){
            txtTimeSelected = (TextView)timeSelected.findViewById(R.id.txtStartTimeValue);
            args.putString(Constants.START_TIME_ARG, txtTimeSelected.getText().toString());
        } else {
            Log.i(TAG, "End time selected");
            txtTimeSelected = (TextView)timeSelected.findViewById(R.id.txtEndTimeValue);
            args.putString(Constants.END_TIME_ARG, txtTimeSelected.getText().toString());
        }
        args.putInt("Position", (Integer) view.getTag());
        TimePickerFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.setArguments(args);
        Activity activity = (Activity)getContext();
        timePickerFragment.show(activity.getFragmentManager(), "timePicker");

    }

    //Used only below

    private NumberPicker numberPicker;
    private RelativeLayout selectedFrequencyView;
    private TextView selectedFrequencyValue;

    private void showNumberPickerDialog(View view)
    {
        selectedFrequencyView = (RelativeLayout)view;
        selectedFrequencyValue = (TextView)selectedFrequencyView.findViewById(R.id.txtFrequencyValue);
        LayoutInflater inflater = mLayoutInflater;
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        View dialogView = inflater.inflate(R.layout.dialog_number_picker, null);
        builder.setView(dialogView);
        numberPicker = (NumberPicker)dialogView.findViewById(R.id.numberPicker);
        numberPicker.setMaxValue(100);
        numberPicker.setMinValue(5);
        numberPicker.setValue(Integer.valueOf(selectedFrequencyValue.getText().toString()));
        numberPicker.setWrapSelectorWheel(false);
        builder.setMessage("Select Frequency");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                selectedFrequencyValue = (TextView)selectedFrequencyView.findViewById(R.id.txtFrequencyValue);
                selectedFrequencyValue.setText(String.valueOf(numberPicker.getValue()));
                Intent intent = new Intent("NumberPicker");
                intent.putExtra("NewFrequency", numberPicker.getValue());
                intent.putExtra("Position", (Integer) selectedFrequencyView.getTag());
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.i(TAG, "Cancel");
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void removeAlarm(int position){
        Log.i(TAG, "Deleting alarm schedule at position " + position);
    }


    //This method is called by SchedulesListActivity after showTitleDialog is called here
    public void updateTitle(String newTitle, int position){
        selectedTitle.setText(newTitle);
        ScheduleEditor scheduleEditor = new ScheduleEditor(mContext);
        scheduleEditor.editTitle(mAlarmSchedules.get(position).getUID(), newTitle);
    }

    //This method is called by SchedulesListActivity after showTimePickerDialog is called here
    public void updateStartEndTime(String newStartTime, int position) {
        Log.i(TAG, newStartTime + " " + position);
        txtTimeSelected.setText(newStartTime);
        saveStartEndTime(newStartTime, position);
    }

    //This method is called by SchedulesListActivity after showNumberPickerDialog is called here
    public void updateFrequency(int frequency, int position){
        Log.i(TAG, "New frequency: " + frequency + " Position: " + position);
        AlarmSchedule previousAlarmSchedule = mAlarmSchedules.get(position);
        AlarmSchedule newAlarmSchedule = new AlarmSchedule(previousAlarmSchedule.getUID(),
                previousAlarmSchedule.getActivated(), previousAlarmSchedule.getAlertType(),
                previousAlarmSchedule.getStartTime(), previousAlarmSchedule.getEndTime(), frequency,
                previousAlarmSchedule.getTitle(), previousAlarmSchedule.getSunday(),
                previousAlarmSchedule.getMonday(), previousAlarmSchedule.getTuesday(),
                previousAlarmSchedule.getWednesday(), previousAlarmSchedule.getThursday(),
                previousAlarmSchedule.getFriday(), previousAlarmSchedule.getSaturday());
        ScheduleEditor scheduleEditor = new ScheduleEditor(mContext);
        scheduleEditor.editFrequency(newAlarmSchedule);
    }
    //Use the below method in onTimeSelected
    private void saveStartEndTime(String time, int position)
    {
        ScheduleEditor scheduleEditor = new ScheduleEditor(mContext);
        AlarmSchedule previousAlarmSchedule = mAlarmSchedules.get(position);
        if (mStartEndButtonSelected)
        {
            txtStartTimeValue.setText(time);
            Calendar startTime = Utils.convertToCalendarTime(time);
            AlarmSchedule newAlarmSchedule = new AlarmSchedule(previousAlarmSchedule.getUID(),
                    previousAlarmSchedule.getActivated(), previousAlarmSchedule.getAlertType(),
                    startTime, previousAlarmSchedule.getEndTime(), previousAlarmSchedule.getFrequency(),
                    previousAlarmSchedule.getTitle(), previousAlarmSchedule.getSunday(),
                    previousAlarmSchedule.getMonday(), previousAlarmSchedule.getTuesday(),
                    previousAlarmSchedule.getWednesday(), previousAlarmSchedule.getThursday(),
                    previousAlarmSchedule.getFriday(), previousAlarmSchedule.getSaturday());
            boolean todayActivated = Utils.isTodayActivated(newAlarmSchedule);
            scheduleEditor.editStartTime(todayActivated,newAlarmSchedule);
            Log.i(TAG, "start time");
        } else {
            txtEndTimeValue.setText(time);
            Calendar endTime = Utils.convertToCalendarTime(time);
            AlarmSchedule newAlarmSchedule = new AlarmSchedule(previousAlarmSchedule.getUID(),
                    previousAlarmSchedule.getActivated(), previousAlarmSchedule.getAlertType(),
                    previousAlarmSchedule.getStartTime(), endTime, previousAlarmSchedule.getFrequency(),
                    previousAlarmSchedule.getTitle(), previousAlarmSchedule.getSunday(),
                    previousAlarmSchedule.getMonday(), previousAlarmSchedule.getTuesday(),
                    previousAlarmSchedule.getWednesday(), previousAlarmSchedule.getThursday(),
                    previousAlarmSchedule.getFriday(), previousAlarmSchedule.getSaturday());
            boolean todayActivated = Utils.isTodayActivated(newAlarmSchedule);
            scheduleEditor.editEndTime(todayActivated, newAlarmSchedule);
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

    private AlarmSchedule newWeekDaySchedule(int day, AlarmSchedule previousAlarmSchedule, boolean
                                             isChecked){
        switch (day){
            case Calendar.SUNDAY:
                AlarmSchedule newAlarmScheduleSun = new AlarmSchedule(previousAlarmSchedule.getUID(),
                        previousAlarmSchedule.getActivated(), previousAlarmSchedule.getAlertType(),
                        previousAlarmSchedule.getStartTime(), previousAlarmSchedule.getEndTime(),
                        previousAlarmSchedule.getFrequency(), previousAlarmSchedule.getTitle(),
                        isChecked, previousAlarmSchedule.getMonday(),
                        previousAlarmSchedule.getTuesday(), previousAlarmSchedule.getWednesday(),
                        previousAlarmSchedule.getThursday(), previousAlarmSchedule.getFriday(),
                        previousAlarmSchedule.getSaturday());
                return newAlarmScheduleSun;
            case Calendar.MONDAY:
                AlarmSchedule newAlarmScheduleMon = new AlarmSchedule(previousAlarmSchedule.getUID(),
                        previousAlarmSchedule.getActivated(), previousAlarmSchedule.getAlertType(),
                        previousAlarmSchedule.getStartTime(), previousAlarmSchedule.getEndTime(),
                        previousAlarmSchedule.getFrequency(), previousAlarmSchedule.getTitle(),
                        previousAlarmSchedule.getSunday(), isChecked,
                        previousAlarmSchedule.getTuesday(), previousAlarmSchedule.getWednesday(),
                        previousAlarmSchedule.getThursday(), previousAlarmSchedule.getFriday(),
                        previousAlarmSchedule.getSaturday());
                return newAlarmScheduleMon;
            case Calendar.TUESDAY:
                AlarmSchedule newAlarmScheduleTue = new AlarmSchedule(previousAlarmSchedule.getUID(),
                        previousAlarmSchedule.getActivated(), previousAlarmSchedule.getAlertType(),
                        previousAlarmSchedule.getStartTime(), previousAlarmSchedule.getEndTime(),
                        previousAlarmSchedule.getFrequency(), previousAlarmSchedule.getTitle(),
                        previousAlarmSchedule.getSunday(), previousAlarmSchedule.getMonday(),
                        isChecked, previousAlarmSchedule.getWednesday(),
                        previousAlarmSchedule.getThursday(), previousAlarmSchedule.getFriday(),
                        previousAlarmSchedule.getSaturday());
                return newAlarmScheduleTue;
            case Calendar.WEDNESDAY:
                AlarmSchedule newAlarmScheduleWed = new AlarmSchedule(previousAlarmSchedule.getUID(),
                        previousAlarmSchedule.getActivated(), previousAlarmSchedule.getAlertType(),
                        previousAlarmSchedule.getStartTime(), previousAlarmSchedule.getEndTime(),
                        previousAlarmSchedule.getFrequency(), previousAlarmSchedule.getTitle(),
                        previousAlarmSchedule.getSunday(), previousAlarmSchedule.getMonday(),
                        previousAlarmSchedule.getTuesday(), isChecked,
                        previousAlarmSchedule.getThursday(), previousAlarmSchedule.getFriday(),
                        previousAlarmSchedule.getSaturday());
                return newAlarmScheduleWed;
            case Calendar.THURSDAY:
                AlarmSchedule newAlarmScheduleThurs = new AlarmSchedule(previousAlarmSchedule.getUID(),
                        previousAlarmSchedule.getActivated(), previousAlarmSchedule.getAlertType(),
                        previousAlarmSchedule.getStartTime(), previousAlarmSchedule.getEndTime(),
                        previousAlarmSchedule.getFrequency(), previousAlarmSchedule.getTitle(),
                        previousAlarmSchedule.getSunday(), previousAlarmSchedule.getMonday(),
                        previousAlarmSchedule.getTuesday(), previousAlarmSchedule.getWednesday(),
                        isChecked, previousAlarmSchedule.getFriday(),
                        previousAlarmSchedule.getSaturday());
                return newAlarmScheduleThurs;
            case Calendar.FRIDAY:
                AlarmSchedule newAlarmScheduleFri = new AlarmSchedule(previousAlarmSchedule.getUID(),
                        previousAlarmSchedule.getActivated(), previousAlarmSchedule.getAlertType(),
                        previousAlarmSchedule.getStartTime(), previousAlarmSchedule.getEndTime(),
                        previousAlarmSchedule.getFrequency(), previousAlarmSchedule.getTitle(),
                        previousAlarmSchedule.getSunday(), previousAlarmSchedule.getMonday(),
                        previousAlarmSchedule.getTuesday(), previousAlarmSchedule.getWednesday(),
                        previousAlarmSchedule.getThursday(), isChecked,
                        previousAlarmSchedule.getSaturday());
                return newAlarmScheduleFri;
            case Calendar.SATURDAY:
                AlarmSchedule newAlarmScheduleSat = new AlarmSchedule(previousAlarmSchedule.getUID(),
                        previousAlarmSchedule.getActivated(), previousAlarmSchedule.getAlertType(),
                        previousAlarmSchedule.getStartTime(), previousAlarmSchedule.getEndTime(),
                        previousAlarmSchedule.getFrequency(), previousAlarmSchedule.getTitle(),
                        previousAlarmSchedule.getSunday(), previousAlarmSchedule.getMonday(),
                        previousAlarmSchedule.getTuesday(), previousAlarmSchedule.getWednesday(),
                        previousAlarmSchedule.getThursday(), previousAlarmSchedule.getFriday(),
                        isChecked);
                return newAlarmScheduleSat;
            default:
                Log.i(TAG, "New day value is not 1 through 7");
                return null;
        }
    }
}
