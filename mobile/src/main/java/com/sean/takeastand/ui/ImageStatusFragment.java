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
import android.app.PendingIntent;
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
import android.widget.Toast;

import com.sean.takeastand.R;
import com.sean.takeastand.alarmprocess.AlarmService;
import com.sean.takeastand.alarmprocess.UnscheduledRepeatingAlarm;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;

import org.w3c.dom.Text;

import java.util.Random;

public class ImageStatusFragment
        extends Fragment
{
    private ImageView statusImage;
    private TextView txtTap;
    private TextView txtStood;
    private TextView txtDelay;
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
        txtStood = (TextView)view.findViewById(R.id.txtStood);
        txtDelay = (TextView)view.findViewById(R.id.txtDelay);
        txtStood.setOnClickListener(stoodListener);
        txtDelay.setOnClickListener(delayListener);
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

    private void updateLayout(){
        int imageStatus = Utils.getCurrentImageStatus(getActivity());
        switch (imageStatus){
            case Constants.NO_ALARM_RUNNING:
                statusImage.setImageResource(R.drawable.alarm_image_inactive);
                statusImage.setOnClickListener(imageListener);
                txtTap.setText(R.string.tap_to_start);
                txtStood.setVisibility(View.GONE);
                txtDelay.setVisibility(View.GONE);
                break;
            case Constants.NON_SCHEDULE_ALARM_RUNNING:
                statusImage.setImageResource(R.drawable.alarm_unscheduled_running);
                statusImage.setOnClickListener(imageListener);
                txtTap.setText(R.string.tap_to_stop);
                txtStood.setVisibility(View.GONE);
                txtDelay.setVisibility(View.GONE);
                break;
            case Constants.NON_SCHEDULE_TIME_TO_STAND:
                statusImage.setImageResource(R.drawable.alarm_unscheduled_passed);
                statusImage.setOnClickListener(null);
                txtTap.setText("");
                txtStood.setVisibility(View.VISIBLE);
                txtDelay.setVisibility(View.VISIBLE);
                break;
            case Constants.NON_SCHEDULE_STOOD_UP:
                statusImage.setImageResource(R.drawable.alarm_unscheduled_stood);
                statusImage.setOnClickListener(null);
                String praiseNon = praiseForUser();
                txtTap.setText(praiseNon);
                txtStood.setVisibility(View.GONE);
                txtDelay.setVisibility(View.GONE);
                Toast.makeText(mContext, praiseNon, Toast.LENGTH_LONG).show();
                break;
            case Constants.SCHEDULE_RUNNING:
                statusImage.setImageResource(R.drawable.alarm_schedule_running);
                statusImage.setOnClickListener(null);
                txtTap.setText("");
                txtStood.setVisibility(View.GONE);
                txtDelay.setVisibility(View.GONE);
                break;
            case Constants.SCHEDULE_TIME_TO_STAND:
                statusImage.setImageResource(R.drawable.alarm_schedule_passed);
                statusImage.setOnClickListener(null);
                txtTap.setText("");
                txtStood.setVisibility(View.VISIBLE);
                txtDelay.setVisibility(View.VISIBLE);
                break;
            case Constants.SCHEDULE_STOOD_UP:
                statusImage.setImageResource(R.drawable.alarm_schedule_stood);
                statusImage.setOnClickListener(null);
                String praiseSchedule = praiseForUser();
                txtTap.setText(praiseSchedule);
                txtStood.setVisibility(View.GONE);
                txtDelay.setVisibility(View.GONE);
                Toast.makeText(mContext, praiseSchedule, Toast.LENGTH_LONG).show();
                break;
            default:
                statusImage.setImageResource(R.drawable.alarm_image_inactive);
                statusImage.setOnClickListener(imageListener);
                txtTap.setText(R.string.tap_to_start);
                txtStood.setVisibility(View.GONE);
                txtDelay.setVisibility(View.GONE);
                break;
        }
    }

    private String praiseForUser(){
        String[] praise = getResources().getStringArray(R.array.praise);
        Random random = new Random(System.currentTimeMillis());
        int randomNumber = random.nextInt(praise.length);
        return praise[randomNumber];
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

    private void switchStatus(){
        UnscheduledRepeatingAlarm unscheduledRepeatingAlarm =
                new UnscheduledRepeatingAlarm(getActivity());
        int imageStatus = Utils.getCurrentImageStatus(getActivity());
        if(imageStatus == Constants.NO_ALARM_RUNNING){
            Utils.setCurrentMainActivityImage(getActivity(), Constants.NON_SCHEDULE_ALARM_RUNNING);
            unscheduledRepeatingAlarm.setRepeatingAlarm();
            Toast.makeText(mContext, "Stand Reminders On", Toast.LENGTH_LONG).show();
            Log.i(TAG, "OnClick Alarm Started");
        } else if (imageStatus == Constants.NON_SCHEDULE_ALARM_RUNNING) {
            Utils.setCurrentMainActivityImage(getActivity(), Constants.NO_ALARM_RUNNING);
            unscheduledRepeatingAlarm.cancelAlarm();
            Toast.makeText(mContext, "Stand Reminders Cancelled", Toast.LENGTH_LONG).show();
            Log.i(TAG, "OnClick Alarm Ended");
        }
        updateLayout();
    }

    private OnClickListener stoodListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.i(TAG, "onClick Stood");
            Intent stoodUpIntent = new Intent("StoodUp");
            getActivity().sendBroadcast(stoodUpIntent);
        }
    };

    private OnClickListener delayListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.i(TAG, "onClick Delay");
            Intent delayAlarmIntent = new Intent("DelayAlarm");
            getActivity().sendBroadcast(delayAlarmIntent);
        }
    };
}