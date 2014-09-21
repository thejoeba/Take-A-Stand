package com.sean.takeastand;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ToggleButton;

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


    private static final String TAG = "ScheduleCreatorActivity";
    private static final int NUMBER_HOURS_TO_ADD = 4;
    ToggleButton btnActivated;
    Button btnStartTime;
    Button btnEndTime;
    CheckBox chBxSunday;
    CheckBox chBxMonday;
    CheckBox chBxTuesday;
    CheckBox chBxWednesday;
    CheckBox chBxThursday;
    CheckBox chBxFriday;
    CheckBox chBxSaturday;
    Button btnFrequency;
    EditText scheduleName;
    Button btnSave;
    Button btnCancel;

    boolean mStartEndButtonSelected;

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
        public void onClick(View paramAnonymousView)
        {
            Intent localIntent = new Intent();
            localIntent.putExtra("result", "cancel");
            setResult(-1, localIntent);
            finish();
        }
    };

    View.OnClickListener cancelListener = new View.OnClickListener()
    {
        public void onClick(View paramAnonymousView)
        {
            Intent localIntent = new Intent();
            localIntent.putExtra("result", "cancel");
            setResult(-1, localIntent);
            finish();
        }
    };

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i2) {

    }
}
