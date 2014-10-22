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

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Button;
import android.widget.TimePicker;

import com.sean.takeastand.storage.ScheduleListAdapter;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;

import java.util.ArrayList;

/**
 * Created by Sean on 2014-09-03.
 */
public class TimePickerFragment
        extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener
{
    private static final String TAG = "TimePickerFragment";
    private int mPosition;
    private boolean mStartTime;

    @Override
    public Dialog onCreateDialog(Bundle bundle)
    {
        mStartTime = getArguments().getBoolean("StartOrEndButton", true);
        mPosition = getArguments().getInt("Position");
        if(mStartTime){
            String startTime = getArguments().getString(Constants.START_TIME_ARG);
            return new TimePickerDialog(getActivity(), this, Utils.readHourFromString(startTime),
                    Utils.readMinutesFromString(startTime), DateFormat.is24HourFormat(getActivity()));
        } else {
            String endTime = getArguments().getString(Constants.END_TIME_ARG);
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
        Intent intent = new Intent("TimePicker");
        intent.putExtra("TimeSelected", Integer.toString(hour)+ ":" + correctMinuteFormat(Integer.toString(minute)));
        intent.putExtra("Position", mPosition);
        intent.putExtra("StartTime", mStartTime);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }
}
