package com.sean.takeastand.widget;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TimePicker;

import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;

import java.util.Calendar;

/**
 * Created by Sean on 2014-09-03.
 */
public class TimePickerFragment
        extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener
{
    private static final String TAG = "TimePickerFragment";

    @Override
    public Dialog onCreateDialog(Bundle bundle)
    {
        boolean startOrEnd = getArguments().getBoolean("StartOrEndButton", true);
        if(startOrEnd){
            String startTime = getArguments().getString(Constants.START_TIME_ARG);
            Log.i(TAG, Integer.toString(Utils.readHourFromString(startTime)));
            return new TimePickerDialog(getActivity(), this, Utils.readHourFromString(startTime),
                    Utils.readMinutesFromString(startTime), DateFormat.is24HourFormat(getActivity()));
        } else {
            String endTime = getArguments().getString(Constants.END_TIME_ARG);
            Log.i(TAG, Integer.toString(Utils.readHourFromString(endTime)));
            return new TimePickerDialog(getActivity(), this, Utils.readHourFromString(endTime),
                    Utils.readMinutesFromString(endTime), DateFormat.is24HourFormat(getActivity()));
        }

    }

    private String correctMinuteFormat(String minute){
        if(minute.length()==1){
            minute = "0" + minute;
        }
        return minute;
    }

    public void onTimeSet(TimePicker timePicker, int hour, int minute)
    {
        ((EditButtonDialogListener)getActivity()).onTimeSelected(Integer.toString(hour) + ":"
                + correctMinuteFormat(Integer.toString(minute)));
    }

    public static abstract interface EditButtonDialogListener
    {
        public abstract void onTimeSelected(String paramString);
    }
}
