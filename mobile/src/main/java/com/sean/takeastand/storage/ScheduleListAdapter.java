

package com.sean.takeastand.storage;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
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

import com.Application;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.sean.takeastand.R;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;

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
    private boolean mUpdatingStartEndTime = false;
    private Handler mHandler;

    public ScheduleListAdapter(Context context, int resource, ArrayList<AlarmSchedule> alarmSchedules,
                               LayoutInflater layoutInflater) {
        super(context, resource, alarmSchedules);
        mAlarmSchedules = alarmSchedules;
        mContext = context;
        mLayoutInflater = layoutInflater;
        mHandler = new Handler();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        View rowView = view;
        if(rowView==null){
            LayoutInflater inflater =
                    (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.schedule_list_row, null);

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
            int schedulePosition = 1 + position;
            txtTitle.setText(mContext.getString(R.string.schedule) + schedulePosition);
        }
        btnActivated.setChecked(alarmSchedule.getActivated());
        boolean[] alertType = alarmSchedule.getAlertType();
        Log.i(TAG, alertType.toString());
        chBxLED.setChecked(alertType[0]);
        chBxVibrate.setChecked(alertType[1]);
        chBxSound.setChecked(alertType[2]);
        txtFrequencyValue.setText(Integer.toString(alarmSchedule.getFrequency()));
        txtStartTimeValue.setText(Utils.getFormattedCalendarTime(alarmSchedule.getStartTime(), mContext));
        txtEndTimeValue.setText(Utils.getFormattedCalendarTime(alarmSchedule.getEndTime(), mContext));
        chBxSunday.setChecked(alarmSchedule.getSunday());
        chBxMonday.setChecked(alarmSchedule.getMonday());
        chBxTuesday.setChecked(alarmSchedule.getTuesday());
        chBxWednesday.setChecked(alarmSchedule.getWednesday());
        chBxThursday.setChecked(alarmSchedule.getThursday());
        chBxFriday.setChecked(alarmSchedule.getFriday());
        chBxSaturday.setChecked(alarmSchedule.getSaturday());
        setAvailableCheckboxes(alarmSchedule, position);
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
            AlarmSchedule newAlarmSchedule = mAlarmSchedules.get(position);
            newAlarmSchedule.setActivated(isActivated);ScheduleEditor scheduleEditor = new ScheduleEditor(mContext);
            scheduleEditor.editActivated(newAlarmSchedule);
            mAlarmSchedules.remove(position);
            mAlarmSchedules.add(position, newAlarmSchedule);
            sendAnalyticsEvent("Schedule " + (1 + position) + " Activated: " + Boolean.toString(isActivated));
            Log.i(TAG, position + ".) Activated: " + Boolean.toString(isActivated));
        }
    };

    private View.OnClickListener deleteListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            int position = (Integer)view.getTag();
            AlarmSchedule deletedAlarmSchedule = mAlarmSchedules.get(position);
            ScheduleEditor scheduleEditor = new ScheduleEditor(mContext);
            scheduleEditor.deleteAlarm(deletedAlarmSchedule);
            if(position == 0 && mAlarmSchedules.size() == 1){
                Log.i(TAG, "Deleting the last alarmSchedule");
                mAlarmSchedules.clear();
                notifyDataSetChanged();
                sendAnalyticsEvent("Last Alarm Schedule deleted");
            } else {
                mAlarmSchedules.remove(position);
                notifyDataSetChanged();
                sendAnalyticsEvent("An alarm schedule was deleted");
            }
        }
    };

    private View.OnClickListener alertTypeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            CheckBox checkBox = (CheckBox) view;
            boolean isChecked = checkBox.isChecked();
            int position = (Integer) view.getTag();
            AlarmSchedule newAlarmSchedule = mAlarmSchedules.get(position);
            boolean[] alertType = newAlarmSchedule.getAlertType();
            ScheduleEditor scheduleEditor = new ScheduleEditor(mContext);
            switch (view.getId()) {
                case R.id.chbxLED:
                    alertType[0] = isChecked;
                    newAlarmSchedule.setAlertType(alertType);
                    scheduleEditor.editAlertType(newAlarmSchedule);
                    Log.i(TAG, position + ".) LED is checked: " + Boolean.toString(isChecked));
                    sendAnalyticsEvent("Schedule " + (1 + position) + " LED is checked: "
                            + Boolean.toString(isChecked));
                    break;
                case R.id.chbxVibrate:
                    alertType[1] = isChecked;
                    newAlarmSchedule.setAlertType(alertType);
                    scheduleEditor.editAlertType(newAlarmSchedule);
                    Log.i(TAG, position + ".) Vibrate is checked: " + Boolean.toString(isChecked));
                    sendAnalyticsEvent("Schedule " + (1 + position) + " Vibrate is checked: "
                            + Boolean.toString(isChecked));
                    break;
                case R.id.chbxSound:
                    alertType[2] = isChecked;
                    newAlarmSchedule.setAlertType(alertType);
                    scheduleEditor.editAlertType(newAlarmSchedule);
                    Log.i(TAG, position + ".) Sound is checked: " + Boolean.toString(isChecked));
                    sendAnalyticsEvent("Schedule " + (1 + position) + " Sound is checked: "
                            + Boolean.toString(isChecked));
                    break;
            }
        }
    };

    private View.OnClickListener startTimeListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            mStartEndButtonSelected = true;
            startActivityTimePicker(view);
        }
    };

    private View.OnClickListener endTimeListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            mStartEndButtonSelected = false;
            startActivityTimePicker(view);
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
            AlarmSchedule newAlarmSchedule = mAlarmSchedules.get(position);
            ScheduleEditor scheduleEditor = new ScheduleEditor(mContext);
            switch (view.getId()){
                case R.id.chbxSun:
                    Log.i(TAG, position + ".) Sunday is checked: " + Boolean.toString(isChecked));
                    sendAnalyticsEvent("Schedule " + (1 + position) + " Sunday is checked: "
                            + Boolean.toString(isChecked));
                    newAlarmSchedule.setSunday(isChecked);
                    scheduleEditor.editDays(Calendar.SUNDAY, isChecked, newAlarmSchedule);
                    //Change the other views so they can now check or not check this day
                    break;
                case R.id.chbxMon:
                    Log.i(TAG, position + ".) Monday is checked: " + Boolean.toString(isChecked));
                    sendAnalyticsEvent("Schedule " + (1 + position) + " Monday is checked: "
                            + Boolean.toString(isChecked));
                    newAlarmSchedule.setMonday(isChecked);
                    scheduleEditor.editDays(Calendar.MONDAY, isChecked, newAlarmSchedule);
                    break;
                case R.id.chbxTue:
                    Log.i(TAG, position + ".) Tuesday is checked: " + Boolean.toString(isChecked));
                    sendAnalyticsEvent("Schedule " + (1 + position) + " Tuesday is checked: "
                            + Boolean.toString(isChecked));
                    newAlarmSchedule.setTuesday(isChecked);
                    scheduleEditor.editDays(Calendar.TUESDAY, isChecked, newAlarmSchedule);
                    break;
                case R.id.chbxWed:
                    Log.i(TAG, position + ".) Wednesday is checked: " + Boolean.toString(isChecked));
                    sendAnalyticsEvent("Schedule " + (1 + position) + " Wednesday is checked: "
                            + Boolean.toString(isChecked));
                    newAlarmSchedule.setWednesday(isChecked);
                    scheduleEditor.editDays(Calendar.WEDNESDAY, isChecked, newAlarmSchedule);
                    break;
                case R.id.chbxThu:
                    Log.i(TAG, position + ".) Thursday is checked: " + Boolean.toString(isChecked));
                    sendAnalyticsEvent("Schedule " + (1 + position) + " Thursday is checked: "
                            + Boolean.toString(isChecked));
                    newAlarmSchedule.setThursday(isChecked);
                    scheduleEditor.editDays(Calendar.THURSDAY, isChecked, newAlarmSchedule);
                    break;
                case R.id.chbxFri:
                    Log.i(TAG, position + ".) Friday is checked: " + Boolean.toString(isChecked));
                    sendAnalyticsEvent("Schedule " + (1 + position) + " Friday is checked: "
                            + Boolean.toString(isChecked));
                    newAlarmSchedule.setFriday(isChecked);
                    scheduleEditor.editDays(Calendar.FRIDAY, isChecked, newAlarmSchedule);
                    break;
                case R.id.chbxSat:
                    Log.i(TAG, position + ".) Saturday is checked: " + Boolean.toString(isChecked));
                    sendAnalyticsEvent("Schedule " + (1 + position) + " Saturday is checked: "
                            + Boolean.toString(isChecked));
                    newAlarmSchedule.setSaturday(isChecked);
                    scheduleEditor.editDays(Calendar.SATURDAY, isChecked, newAlarmSchedule);
                    break;
            }
            mAlarmSchedules.remove(position);
            mAlarmSchedules.add(position, newAlarmSchedule);
            notifyDataSetChanged();
        }
    };

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
        if(currentTitle.startsWith(mContext.getString(R.string.schedule))){
            editText.setHint(currentTitle);
        } else {
            editText.setText(currentTitle);
            editText.setSelection(currentTitle.length());
        }
        builder.setMessage(mContext.getString(R.string.new_schedule_title));
        builder.setPositiveButton(mContext.getString(R.string.ok),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                EditText editText = (EditText)dialogView.findViewById(R.id.editText);
                Intent intent = new Intent("TitleChange");
                intent.putExtra("NewTitle", editText.getText().toString());
                intent.putExtra("Position", (Integer) selectedTitle.getTag());
                sendAnalyticsEvent("New Schedule Title: " + intent.getStringExtra("NewTitle"));
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
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
    private void startActivityTimePicker(View view)
    {
        RelativeLayout timeSelected = (RelativeLayout)view;
        if(timeSelected==null){
            Log.wtf(TAG, "txtTimeSelected is null");
        }
        Intent intent = new Intent("ShowTimePicker");
        if(mStartEndButtonSelected){
            txtTimeSelected = (TextView)timeSelected.findViewById(R.id.txtStartTimeValue);
            intent.putExtra(Constants.START_TIME_ARG, txtTimeSelected.getText().toString());
        } else {
            txtTimeSelected = (TextView)timeSelected.findViewById(R.id.txtEndTimeValue);
            intent.putExtra(Constants.END_TIME_ARG, txtTimeSelected.getText().toString());
        }
        intent.putExtra("StartOrEndButton", mStartEndButtonSelected);
        intent.putExtra("NewAlarm", false);
        intent.putExtra("Position", (Integer)view.getTag());
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
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
        numberPicker.setMinValue(2);
        numberPicker.setValue(Integer.valueOf(selectedFrequencyValue.getText().toString()));
        numberPicker.setWrapSelectorWheel(false);
        builder.setMessage(mContext.getString(R.string.select_frequency));
        builder.setPositiveButton(mContext.getString(R.string.ok),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //selectedFrequencyValue = (TextView)selectedFrequencyView.findViewById(R.id.txtFrequencyValue);
                //selectedFrequencyValue.setText(String.valueOf(numberPicker.getValue()));
                int position = (Integer)selectedFrequencyView.getTag();
                AlarmSchedule newFrequencySchedule = mAlarmSchedules.get(position);
                newFrequencySchedule.setFrequency(numberPicker.getValue());
                mAlarmSchedules.remove(position);
                mAlarmSchedules.add(position, newFrequencySchedule);
                notifyDataSetChanged();
                AlarmSchedule newAlarmSchedule = mAlarmSchedules.get(position);
                newAlarmSchedule.setFrequency(numberPicker.getValue());
                ScheduleEditor scheduleEditor = new ScheduleEditor(mContext);
                scheduleEditor.editFrequency(newAlarmSchedule);
                sendAnalyticsEvent("Schedule " + (1 + position) + " new frequency: "
                        + numberPicker.getValue());
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(mContext.getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.i(TAG, "Cancel");
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //This method is called by SchedulesListActivity after showTitleDialog is called here
    public void updateTitle(String newTitle, int position){
        ScheduleEditor scheduleEditor = new ScheduleEditor(mContext);
        scheduleEditor.editTitle(mAlarmSchedules.get(position).getUID(), newTitle);
        AlarmSchedule newAlarmSchedule = mAlarmSchedules.get(position);
        newAlarmSchedule.setTitle(newTitle);
        mAlarmSchedules.remove(position);
        mAlarmSchedules.add(position, newAlarmSchedule);
        Utils.setScheduleTitle(newTitle, mContext, newAlarmSchedule.getUID());
        notifyDataSetChanged();

    }

    //This method is called by SchedulesListActivity after showTimePickerDialog is called here
    public void updateStartEndTime(String newStartTime, int position) {
        if(!mUpdatingStartEndTime) {
            mUpdatingStartEndTime = true;
            mHandler.postDelayed(updatingStartEnd, 1000);
            ScheduleEditor scheduleEditor = new ScheduleEditor(mContext);
            AlarmSchedule newAlarmSchedule = mAlarmSchedules.get(position);
            if (mStartEndButtonSelected) {
                Calendar startTime = Utils.convertToCalendarTime(newStartTime, mContext);
                newAlarmSchedule.setStartTime(startTime);
                boolean todayActivated = Utils.isTodayActivated(newAlarmSchedule);
                scheduleEditor.editStartTime(todayActivated, newAlarmSchedule);
                mAlarmSchedules.remove(position);
                mAlarmSchedules.add(position, newAlarmSchedule);
                sendAnalyticsEvent("Schedule " + (1 + position) + " New Start Time: " + newStartTime);
            } else {
                Calendar endTime = Utils.convertToCalendarTime(newStartTime, mContext);
                newAlarmSchedule.setEndTime(endTime);
                boolean todayActivated = Utils.isTodayActivated(newAlarmSchedule);
                scheduleEditor.editEndTime(todayActivated, newAlarmSchedule);
                mAlarmSchedules.remove(position);
                mAlarmSchedules.add(position, newAlarmSchedule);
                sendAnalyticsEvent("Schedule " + (1 + position) + " New End Time: " + newStartTime);
            }
            notifyDataSetChanged();

        }
    }

    public void newSchedule(String startTime, String endTime){
            createNewSchedule(startTime, endTime);

    }

    private void setAvailableCheckboxes(AlarmSchedule alarmSchedule, int position){
        //If the day already is used by another alarm schedule, do not allow it to be checkable
        ScheduleDatabaseAdapter scheduleDatabaseAdapter = new ScheduleDatabaseAdapter(mContext);
        boolean[] activatedDays = scheduleDatabaseAdapter.getAlreadyTakenAlarmDays();
        resetCheckBoxes();
        if(activatedDays[0]){
            //If mAlarmSchedule == null, then this is a new schedule, so it couldn't be
            // //responsible for this day being activated already
            if(alarmSchedule==null){
                chBxSunday.setEnabled(false);
                Log.i(TAG, "AlarmSchedule null, New Schedule: " + position);
                //If Sunday isn't checked by this schedule don't allow this day to be checkable
            } else if (!alarmSchedule.getSunday()){
                chBxSunday.setEnabled(false);
                Log.i(TAG, "Sunday unavailable: " + position);
                //This final case means that this is the schedule that checked the day initially
            } else {
                chBxSunday.setChecked(true);
                chBxSunday.setEnabled(true);
            }
        }
        if(activatedDays[1]){
            if(alarmSchedule==null){
                chBxMonday.setEnabled(false);
                Log.i(TAG, "AlarmSchedule null, New Schedule: " + position);
            } else if (!alarmSchedule.getMonday()){
                chBxMonday.setEnabled(false);
                Log.i(TAG, "Monday unavailable: " + position);
            } else {
                chBxMonday.setChecked(true);
                chBxMonday.setEnabled(true);
            }
        }
        if(activatedDays[2]){
            if(alarmSchedule==null){
                chBxTuesday.setEnabled(false);
                Log.i(TAG, "AlarmSchedule null, New Schedule: " + position);
            } else if (!alarmSchedule.getTuesday()){
                chBxTuesday.setEnabled(false);
                Log.i(TAG, "Tuesday unavailable: " + position);
            } else {
                chBxTuesday.setChecked(true);
                chBxTuesday.setEnabled(true);
            }
        }
        if(activatedDays[3]){
            if(alarmSchedule==null){
                chBxWednesday.setEnabled(false);
                Log.i(TAG, "AlarmSchedule null, New Schedule: " + position);
            } else if (!alarmSchedule.getWednesday()){
                chBxWednesday.setEnabled(false);
                Log.i(TAG, "Wednesday unavailable: " + position);
            } else {
                chBxWednesday.setChecked(true);
                chBxWednesday.setEnabled(true);
            }
        }
        if(activatedDays[4]){
            if(alarmSchedule==null){
                chBxThursday.setEnabled(false);
                Log.i(TAG, "AlarmSchedule null, New Schedule: " + position);
            } else if (!alarmSchedule.getThursday()){
                chBxThursday.setEnabled(false);
                Log.i(TAG, "Thursday unavailable: " + position);
            } else {
                chBxThursday.setChecked(true);
                chBxThursday.setEnabled(true);
            }
        }
        if(activatedDays[5]){
            if(alarmSchedule==null){
                chBxFriday.setEnabled(false);
                Log.i(TAG, "AlarmSchedule null, New Schedule: " + position);
            } else if (!alarmSchedule.getFriday()){
                chBxFriday.setEnabled(false);
                Log.i(TAG, "Friday unavailable: " + position);
            } else {
                chBxFriday.setChecked(true);
                chBxFriday.setEnabled(true);
            }
        }
        if(activatedDays[6]){
            if(alarmSchedule==null){
                chBxSaturday.setEnabled(false);
                Log.i(TAG, "AlarmSchedule null, New Schedule: " + position);
            } else if (!alarmSchedule.getSaturday()){
                chBxSaturday.setEnabled(false);
                Log.i(TAG, "Saturday unavailable: " + position);
            } else {
                chBxSaturday.setChecked(true);
                chBxSaturday.setEnabled(true);
            }
        }
    }

    private void resetCheckBoxes(){
        chBxSunday.setEnabled(true);
        chBxMonday.setEnabled(true);
        chBxTuesday.setEnabled(true);
        chBxWednesday.setEnabled(true);
        chBxThursday.setEnabled(true);
        chBxFriday.setEnabled(true);
        chBxSaturday.setEnabled(true);
    }

    private void createNewSchedule(String startTime, String endTime){
        boolean[] availableDays = new ScheduleDatabaseAdapter(mContext).getAlreadyTakenAlarmDays();
        boolean[] newActivatedDays = {false, false, false, false, false, false, false};
        ScheduleEditor scheduleEditor = new ScheduleEditor(mContext);
        Calendar rightNow = Calendar.getInstance();
        if(!availableDays[ (rightNow.get(Calendar.DAY_OF_WEEK) - 1)]){
            newActivatedDays[ (rightNow.get(Calendar.DAY_OF_WEEK) - 1)] = true;
        }
        boolean[] alertTypes = Utils.getDefaultAlertType(mContext);
        scheduleEditor.newAlarm(true, alertTypes[0], alertTypes[1], alertTypes[2], startTime,
                endTime, Utils.getDefaultFrequency(mContext), "", newActivatedDays[0],
                newActivatedDays[1], newActivatedDays[2], newActivatedDays[3], newActivatedDays[4],
                newActivatedDays[5], newActivatedDays[6]);
        mAlarmSchedules.add((
                new ScheduleDatabaseAdapter(mContext).getAlarmSchedules().get(mAlarmSchedules.size())));
        notifyDataSetChanged();
        sendAnalyticsEvent("New Schedule Created");
    }

    Runnable updatingStartEnd = new Runnable() {
        @Override
        public void run() {
            mUpdatingStartEndTime = false;
            }

    };

    private void sendAnalyticsEvent(String action){
        Tracker t = ((Application)mContext.getApplicationContext()).getTracker(
                Application.TrackerName.APP_TRACKER);
        t.enableAdvertisingIdCollection(true);
        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder()
                .setCategory(Constants.SCHEUDULE_LIST_EVENT)
                .setAction(action)
                .build());
    }

}
