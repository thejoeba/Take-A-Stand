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

package com.sean.takeastand.ui;

/**
 * Created by Sean on 2014-09-03.
 */

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sean.takeastand.R;
import com.sean.takeastand.alarmprocess.UnscheduledRepeatingAlarm;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;

public class ImageStatusFragment
        extends Fragment
{
    private ImageView statusImage;
    private TextView txtTap;
    private static final String TAG = "ImageStatusFragment";
    private Context mContext;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle)
    {
        Log.i(TAG, "onCreateView");
        mContext = getActivity();
        View view = layoutInflater.inflate(R.layout.fragment_main_image_status, viewGroup, false);
        statusImage = (ImageView)view.findViewById(R.id.statusImage);
        statusImage.setOnClickListener(imageListener);
        txtTap = (TextView)view.findViewById(R.id.tap_to_set);
        updateLayout();
        registerReceivers();
        //If stuck on a non-click listener view uncomment the below line:
        //Utils.setCurrentMainActivityImage(getActivity(), Constants.NO_ALARM_RUNNING);
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceivers();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceivers();
        updateLayout();
    }

    @Override
    public void onStart() {
        updateLayout();
        super.onStart();
    }

    private void switchStatus(){
        UnscheduledRepeatingAlarm unscheduledRepeatingAlarm =
                new UnscheduledRepeatingAlarm(getActivity());
        int imageStatus = Utils.getCurrentImageStatus(getActivity());
        if(imageStatus == Constants.NO_ALARM_RUNNING){
            Utils.setCurrentMainActivityImage(getActivity(), Constants.NON_SCHEDULE_ALARM_RUNNING);
            unscheduledRepeatingAlarm.setRepeatingAlarm();
        } else if (imageStatus == Constants.NON_SCHEDULE_ALARM_RUNNING) {
           Utils.setCurrentMainActivityImage(getActivity(), Constants.NO_ALARM_RUNNING);
           endAlarmService();
           unscheduledRepeatingAlarm.cancelAlarm();
        }
        updateLayout();
    }

    private void endAlarmService(){
        Intent intent = new Intent("userSwitchedOffAlarm");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void updateLayout(){
        int imageStatus = Utils.getCurrentImageStatus(getActivity());
        switch (imageStatus){
            case Constants.NO_ALARM_RUNNING:
                statusImage.setImageResource(R.drawable.alarm_image_inactive);
                statusImage.setOnClickListener(imageListener);
                txtTap.setText(R.string.tap_to_start);
                break;
            case Constants.NON_SCHEDULE_ALARM_RUNNING:
                statusImage.setImageResource(R.drawable.alarm_unscheduled_running);
                statusImage.setOnClickListener(imageListener);
                txtTap.setText(R.string.tap_to_stop);
                break;
            case Constants.NON_SCHEDULE_TIME_TO_STAND:
                statusImage.setImageResource(R.drawable.alarm_unscheduled_passed);
                statusImage.setOnClickListener(imageListener);
                txtTap.setText(R.string.tap_to_stop);
                break;
            case Constants.NON_SCHEDULE_STOOD_UP:
                statusImage.setImageResource(R.drawable.alarm_unscheduled_stood);
                statusImage.setOnClickListener(imageListener);
                txtTap.setText(R.string.praise1);
                break;
            case Constants.SCHEDULE_RUNNING:
                statusImage.setImageResource(R.drawable.alarm_schedule_running);
                statusImage.setOnClickListener(null);
                txtTap.setText("");
                break;
            case Constants.SCHEDULE_TIME_TO_STAND:
                statusImage.setImageResource(R.drawable.alarm_schedule_passed);
                statusImage.setOnClickListener(null);
                txtTap.setText("");
                break;
            case Constants.SCHEDULE_STOOD_UP:
                statusImage.setImageResource(R.drawable.alarm_schedule_stood);
                statusImage.setOnClickListener(null);
                txtTap.setText(R.string.praise1);
                break;
            default:
                statusImage.setImageResource(R.drawable.alarm_image_inactive);
                statusImage.setOnClickListener(imageListener);
                txtTap.setText(R.string.tap_to_start);
                break;
        }
    }



    private void registerReceivers(){
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(updateImageReceiver, new IntentFilter(Constants.INTENT_MAIN_IMAGE));
    }

    private void unregisterReceivers(){
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(updateImageReceiver);
    }

    BroadcastReceiver updateImageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "UpdatingImage");
            updateLayout();
        }
    };

    private OnClickListener imageListener = new OnClickListener(){

        @Override
        public void onClick(View view) {
            switchStatus();
        }
    };

}