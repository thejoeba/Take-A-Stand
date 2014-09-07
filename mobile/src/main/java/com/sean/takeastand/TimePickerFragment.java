package com.sean.takeastand;

import android.app.Dialog;
import android.app.DialogFragment
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

    @Override
    public Dialog onCreateDialog(Bundle bundle)
    {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        return new TimePickerDialog(getActivity(), this, hour, minute DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker timePicker, int hour, int minute)
    {
        ((EditButtonDialogListener)getActivity()).onTimeSelected(Integer.toString(hour) + ":" + Integer.toString(minute));
    }

    public static abstract interface EditButtonDialogListener
    {
        public abstract void onTimeSelected(String paramString);
    }
}
