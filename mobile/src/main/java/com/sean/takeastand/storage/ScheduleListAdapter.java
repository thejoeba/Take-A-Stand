

package com.sean.takeastand.storage;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
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
    private EditText scheduleName;
    private ToggleButton btnActivated;
    private CheckBox chBxLED;
    private CheckBox chBxVibrate;
    private CheckBox chBxSound;
    //Eventually change the below three buttons to stylized textViews with onclicklisteners
    //Black/Grey, largish-medium text, with a background of transparent blue that fades away
    //which indicates that it is clickable
    private Button btnFrequency;
    private Button btnStartTime;
    private Button btnEndTime;
    private CheckBox chBxSunday;
    private CheckBox chBxMonday;
    private CheckBox chBxTuesday;
    private CheckBox chBxWednesday;
    private CheckBox chBxThursday;
    private CheckBox chBxFriday;
    private CheckBox chBxSaturday;
    private boolean mStartEndButtonSelected;
    private int mPosition;
    private final String TAG = "ScheduleListAdapter";
    private Button btnTimeSelected;

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
        initializeViewsAndButtons(rowView, position);
        setTextAndStatus(mAlarmSchedules, position);
        return rowView;
    }

    private void initializeViewsAndButtons(View view, int position){
        scheduleName = (EditText)view.findViewById(R.id.editTitle);
        btnActivated = (ToggleButton)view.findViewById(R.id.btnActivated);
        chBxLED = (CheckBox)view.findViewById(R.id.chbxLED);
        chBxVibrate = (CheckBox)view.findViewById(R.id.chbxVibrate);
        chBxSound = (CheckBox)view.findViewById(R.id.chbxSound);
        btnFrequency = (Button)view.findViewById(R.id.btnFrequency);
        btnStartTime = (Button)view.findViewById(R.id.btnStartTime);
        btnEndTime = (Button)view.findViewById(R.id.btnEndTime);
        chBxSunday = (CheckBox)view.findViewById(R.id.chbxSun);
        chBxMonday = (CheckBox)view.findViewById(R.id.chbxMon);
        chBxTuesday = (CheckBox)view.findViewById(R.id.chbxTue);
        chBxWednesday = (CheckBox)view.findViewById(R.id.chbxWed);
        chBxThursday = (CheckBox)view.findViewById(R.id.chbxThu);
        chBxFriday = (CheckBox)view.findViewById(R.id.chbxFri);
        chBxSaturday = (CheckBox)view.findViewById(R.id.chbxSat);
        setOnClickListeners();
        addPositionTags(position);
        removeEditTextFocus(view);
    }

    private void setTextAndStatus(ArrayList<AlarmSchedule> alarmSchedules, int position){
        AlarmSchedule alarmSchedule = alarmSchedules.get(position);
        scheduleName.setHint(alarmSchedule.getTitle());
        btnActivated.setChecked(alarmSchedule.getActivated());
        //txtAlertType.setText(alarmSchedule.getAlertType());
        //defaults are true-true-false
        chBxLED.setChecked(true);
        chBxVibrate.setChecked(true);
        chBxSound.setChecked(false);
        btnFrequency.setText(Integer.toString(alarmSchedule.getFrequency()));
        btnStartTime.setText(Utils.calendarToTimeString(alarmSchedule.getStartTime()));
        btnEndTime.setText(Utils.calendarToTimeString(alarmSchedule.getEndTime()));
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
        scheduleName.setTag(position);
        btnActivated.setTag(position);
        chBxLED.setTag(position);
        chBxVibrate.setTag(position);
        chBxSound.setTag(position);
        btnFrequency.setTag(position);
        btnStartTime.setTag(position);
        btnEndTime.setTag(position); chBxSunday.setTag(position);
        chBxMonday.setTag(position);
        chBxTuesday.setTag(position);
        chBxWednesday.setTag(position);
        chBxThursday.setTag(position);
        chBxFriday.setTag(position);
        chBxSaturday.setTag(position);
    }

    private void setOnClickListeners(){
        scheduleName.addTextChangedListener(titleTextWatcher);
        btnActivated.setOnClickListener(activatedListener);
        chBxLED.setOnClickListener(alertTypeListener);
        chBxVibrate.setOnClickListener(alertTypeListener);
        chBxSound.setOnClickListener(alertTypeListener);
        btnFrequency.setOnClickListener(frequencyListener);
        btnStartTime.setOnClickListener(startTimeListener);
        btnEndTime.setOnClickListener(endTimeListener);
        chBxSunday.setOnClickListener(checkedDayListener);
        chBxMonday.setOnClickListener(checkedDayListener);
        chBxTuesday.setOnClickListener(checkedDayListener);
        chBxWednesday.setOnClickListener(checkedDayListener);
        chBxThursday.setOnClickListener(checkedDayListener);
        chBxFriday.setOnClickListener(checkedDayListener);
        chBxSaturday.setOnClickListener(checkedDayListener);
    }

    private TextWatcher titleTextWatcher = new TextWatcher(){
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            Log.i(TAG, charSequence.toString());
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private View.OnClickListener activatedListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            ToggleButton activated = (ToggleButton)view;
            boolean isActivated = activated.isChecked();
            int position = (Integer)view.getTag();
            Log.i(TAG, position + ".) Activated: " + Boolean.toString(isActivated));
        }
    };

    private View.OnClickListener alertTypeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            CheckBox checkBox = (CheckBox) view;
            boolean isChecked = checkBox.isChecked();
            int position = (Integer) view.getTag();
            switch (view.getId()) {
                case R.id.chbxLED:
                    Log.i(TAG, position + ".) LED is checked: " + Boolean.toString(isChecked));
                    break;
                case R.id.chbxVibrate:
                    Log.i(TAG, position + ".) Vibrate is checked: " + Boolean.toString(isChecked));
                    break;
                case R.id.chbxSound:
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
            //Have to set before because only final variable can be used in
            showNumberPickerDialog(view);
        }
    };

    private View.OnClickListener checkedDayListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            CheckBox checkBox = (CheckBox)view;
            boolean isChecked = checkBox.isChecked();
            int position = (Integer)view.getTag();
            switch (view.getId()){
                case R.id.chbxSun:
                    Log.i(TAG, position + ".) Sunday is checked: " + Boolean.toString(isChecked));
                    break;
                case R.id.chbxMon:
                    Log.i(TAG, position + ".) Monday is checked: " + Boolean.toString(isChecked));
                    break;
                case R.id.chbxTue:
                    Log.i(TAG, position + ".) Tuesday is checked: " + Boolean.toString(isChecked));
                    break;
                case R.id.chbxWed:
                    Log.i(TAG, position + ".) Wednesday is checked: " + Boolean.toString(isChecked));
                    break;
                case R.id.chbxThu:
                    Log.i(TAG, position + ".) Thursday is checked: " + Boolean.toString(isChecked));
                    break;
                case R.id.chbxFri:
                    Log.i(TAG, position + ".) Friday is checked: " + Boolean.toString(isChecked));
                    break;
                case R.id.chbxSat:
                    Log.i(TAG, position + ".) Saturday is checked: " + Boolean.toString(isChecked));
                    break;
            }
        }
    };

    private void showTimePickerDialog(View view)
    {
        Bundle args = new Bundle();
        args.putBoolean("StartOrEndButton", mStartEndButtonSelected);
        btnTimeSelected = (Button)view;
        if(btnTimeSelected==null){
            Log.i(TAG, "btnTimeSelected is null");
        }
        if(mStartEndButtonSelected){
            args.putString(Constants.START_TIME_ARG, btnTimeSelected.getText().toString());
                    //Utils.calendarToTimeString(mAlarmSchedules.get(mPosition).getStartTime()));
        } else {
            args.putString(Constants.END_TIME_ARG,
                    btnTimeSelected.getText().toString());
        }
        args.putInt("Position", (Integer) view.getTag());
        TimePickerFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.setArguments(args);
        Activity activity = (Activity)getContext();
        timePickerFragment.show(activity.getFragmentManager(), "timePicker");

    }

    private void showNumberPickerDialog(View view)
    {
        final Button btnFrequencySelected = (Button)view;
        final Dialog numberPickerDialog = new Dialog(mContext);
        numberPickerDialog.setTitle(mContext.getString(R.string.select_frequency));
        numberPickerDialog.setContentView(R.layout.dialog_number_picker);
        Button btnCancelNp = (Button) numberPickerDialog.findViewById(R.id.btnCancelNp);
        Button btnSaveNp = (Button) numberPickerDialog.findViewById(R.id.btnSaveNp);
        final NumberPicker numberPicker = (NumberPicker) numberPickerDialog.findViewById(R.id.numberPicker);
        numberPicker.setMaxValue(100);
        numberPicker.setMinValue(5);
        numberPicker.setValue(Integer.valueOf(btnFrequencySelected.getText().toString()));
        numberPicker.setWrapSelectorWheel(false);
        //numberPicker.setOnValueChangedListener(ScheduleListActivity.class);
        btnSaveNp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                btnFrequencySelected.setText(String.valueOf(numberPicker.getValue()));
                Intent intent = new Intent("NumberPicker");
                intent.putExtra("NewFrequency", numberPicker.getValue());
                intent.putExtra("Position", (Integer)btnFrequencySelected.getTag());
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
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

    //This method is called by SchedulesListActivity after showTimePickerDialog is called here
    public void updateStartEndTime(String paramString, int position) {
        Log.i(TAG, paramString + " " + position);
        btnTimeSelected.setText(paramString);
        //saveStartEndTime is called here
    }

    //This method is called by SchedulesListActivity after showNumberPickerDialog is called here
    public void updateFrequency(int frequency, int position){
        Log.i(TAG, "New frequency: " + frequency + " Position: " + position);
    }
    //Use the below method in onTimeSelected
    private void saveStartEndTime(String time)
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

    //At the moment this doesn't seem to be working
    private void removeEditTextFocus(View view){
        //This sets it so that when you touch the middle of the screen the focus is taken
        //away from the EditText, otherwise EditText is constantly flashing and keyboard won't go
        //away
        FrameLayout touchInterceptor = (FrameLayout)view.findViewById(R.id.touchInterceptor);
        touchInterceptor.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (scheduleName.isFocused()) {
                        Rect outRect = new Rect();
                        scheduleName.getGlobalVisibleRect(outRect);
                        if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                            scheduleName.clearFocus();
                            InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        }
                    }
                }
                return false;
            }
        });
    }
}
