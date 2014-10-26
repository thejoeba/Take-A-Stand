package com.sean.takeastand.widget;

import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.Button;
import android.widget.TimePicker;

/**
 * Created by Sean on 2014-10-25.
 */
public class CustomTimePickerDialog extends TimePickerDialog {

    private String title;
    public CustomTimePickerDialog(Context context, TimePickerDialog.OnTimeSetListener callBack, int hourOfDay, int minute, boolean is24HourView, String dialogTitle){
        super(context, callBack, hourOfDay, minute, is24HourView);
        title = dialogTitle;
    }

    //Had to override and setTitle otherwise title constantly changed to match selected time
    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        super.onTimeChanged(view, hourOfDay, minute);
        setTitle(title);
    }


}
