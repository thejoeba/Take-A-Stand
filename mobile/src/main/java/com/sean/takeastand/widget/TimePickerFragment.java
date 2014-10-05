package com.sean.takeastand.widget;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by Sean on 2014-09-03.
 */
public class TimePickerFragment
        extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener
{

    private static final int NUMBER_HOURS_TO_ADD = 4;

    @Override
    public Dialog onCreateDialog(Bundle bundle)
    {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        boolean startOrEnd = getArguments().getBoolean("StartOrEndButton", true);
        if(startOrEnd){
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        } else {
            return new TimePickerDialog(getActivity(), this, hour + NUMBER_HOURS_TO_ADD, minute,
                    DateFormat.is24HourFormat(getActivity()));
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
