/*
 * Copyright (C) 2014 Sean Allen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sean.takeastand.widget;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
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
    private int mPosition = -1;
    private boolean mStartTime;
    private boolean mNewAlarm;

    @Override
    public Dialog onCreateDialog(Bundle bundle)
    {
        mStartTime = getArguments().getBoolean("StartOrEndButton", true);
        Log.i(TAG, Boolean.toString(mStartTime));
        mPosition = getArguments().getInt("Position");
        mNewAlarm = getArguments().getBoolean("NewAlarm");
        if(mStartTime && !mNewAlarm){
            String startTime = getArguments().getString(Constants.START_TIME_ARG);
            CustomTimePickerDialog timePickerDialog = new CustomTimePickerDialog(getActivity(), this, Utils.readHourFromString(startTime),
                    Utils.readMinutesFromString(startTime), DateFormat.is24HourFormat(getActivity()), "Select Start Time");
            timePickerDialog.setTitle("Select Start Time");
            Log.i(TAG, "Old Alarm");
            return timePickerDialog;
        } else if(!mStartTime && !mNewAlarm) {
            String endTime = getArguments().getString(Constants.END_TIME_ARG);
            CustomTimePickerDialog timePickerDialog = new CustomTimePickerDialog(getActivity(), this, Utils.readHourFromString(endTime),
                    Utils.readMinutesFromString(endTime), DateFormat.is24HourFormat(getActivity()), "Select End Time");
            timePickerDialog.setTitle("Select End Time");
            return timePickerDialog;
        } else if(mStartTime && mNewAlarm){
            Calendar rightNow = Calendar.getInstance();
            String startTime = Utils.calendarToTimeString(rightNow);
            CustomTimePickerDialog timePickerDialog = new CustomTimePickerDialog(getActivity(), this, Utils.readHourFromString(startTime),
                    Utils.readMinutesFromString(startTime), DateFormat.is24HourFormat(getActivity()), "Select Start Time");
            timePickerDialog.setTitle("Select Start Time");
            return timePickerDialog;
        } else if(!mStartTime && mNewAlarm){
            Calendar rightNow = Calendar.getInstance();
            rightNow.add(Calendar.HOUR_OF_DAY, 3);
            String endTime = Utils.calendarToTimeString(rightNow);
            CustomTimePickerDialog timePickerDialog = new CustomTimePickerDialog(getActivity(), this, Utils.readHourFromString(endTime),
                    Utils.readMinutesFromString(endTime), DateFormat.is24HourFormat(getActivity()), "Select End Time");
            timePickerDialog.setTitle("Select End Time");
            return timePickerDialog;
        } else {
            Log.i(TAG, "Error: not one of defaults" );
            return  null;
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
        Intent intent = new Intent("TimePicker");
        intent.putExtra("TimeSelected", Integer.toString(hour)+ ":" + correctMinuteFormat(Integer.toString(minute)));
        if(mPosition!=-1){
            intent.putExtra("Position", mPosition);
        }
        intent.putExtra("StartTime", mStartTime);
        intent.putExtra("NewAlarm", mNewAlarm);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }
}
