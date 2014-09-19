package com.sean.takeastand;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Sean on 2014-09-03.
 */
public class ScheduleCreatorActivity
        extends FragmentActivity
        implements TimePickerFragment.EditButtonDialogListener
{
    /*
    Like the Main Activity this will be recreated into fragments, this will be a list view, but
    dynamic
     */
    private static final String TAG = "ScheduleCreatorActivity";
    Button btnCancel;
    Button btnEndTime;
    Button btnSave;
    Button btnStartTime;
    View.OnClickListener cancelListener = new View.OnClickListener()
    {
        public void onClick(View paramAnonymousView)
        {
            Intent localIntent = new Intent();
            localIntent.putExtra("result", "cancel");
            ScheduleCreatorActivity.this.setResult(-1, localIntent);
            ScheduleCreatorActivity.this.finish();
        }
    };
    View.OnClickListener endTimeListener = new View.OnClickListener()
    {
        public void onClick(View paramAnonymousView)
        {
            ScheduleCreatorActivity.this.startEndButtonSelected = false;
            ScheduleCreatorActivity.this.showTimePickerDialog(paramAnonymousView);
        }
    };
    EditText name;
    View.OnClickListener saveListener = new View.OnClickListener()
    {
        public void onClick(View paramAnonymousView)
        {
            Intent localIntent = new Intent();
            localIntent.putExtra("result", "cancel");
            ScheduleCreatorActivity.this.setResult(-1, localIntent);
            ScheduleCreatorActivity.this.finish();
        }
    };
    boolean startEndButtonSelected;
    View.OnClickListener startTimeListener = new View.OnClickListener()
    {
        public void onClick(View paramAnonymousView)
        {
            ScheduleCreatorActivity.this.startEndButtonSelected = true;
            ScheduleCreatorActivity.this.showTimePickerDialog(paramAnonymousView);
        }
    };

    private void initializeViewsAndButtons()
    {
        this.name = ((EditText)findViewById(2131034202));
        this.btnStartTime = ((Button)findViewById(2131034179));
        this.btnStartTime.setOnClickListener(this.startTimeListener);
        this.btnEndTime = ((Button)findViewById(2131034181));
        this.btnEndTime.setOnClickListener(this.endTimeListener);
        this.btnSave = ((Button)findViewById(2131034203));
        this.btnSave.setOnClickListener(this.saveListener);
        this.btnCancel = ((Button)findViewById(2131034205));
        this.btnCancel.setOnClickListener(this.cancelListener);
    }

    private void removeKeyboardStart()
    {
        if (this.name != null) {
            this.name.clearFocus();
        }
        getWindow().setSoftInputMode(3);
    }

    protected void onCreate(Bundle paramBundle)
    {
        setContentView(2130903065);
        super.onCreate(paramBundle);
        initializeViewsAndButtons();
        removeKeyboardStart();
    }

    public void onTimeSelected(String paramString)
    {
        Log.i(TAG, "onTimeSelected");
        setButton(paramString);
    }

    public void setButton(String paramString)
    {
        if (this.startEndButtonSelected)
        {
            this.btnStartTime.setText(paramString);
            return;
        }
        this.btnEndTime.setText(paramString);
    }

    public void showTimePickerDialog(View paramView)
    {
        new TimePickerFragment().show(getFragmentManager(), "timePicker");
    }
}
