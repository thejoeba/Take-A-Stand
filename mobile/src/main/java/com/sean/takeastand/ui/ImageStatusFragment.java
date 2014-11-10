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
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.sean.takeastand.R;
import com.sean.takeastand.alarmprocess.UnscheduledRepeatingAlarm;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;

public class ImageStatusFragment
        extends Fragment
{
    private ImageView statusImage;
    private TextSwitcher txtTap;
    private TextView tapTextView;
    private static final String TAG = "ImageStatusFragment";
    private Context mContext;
    private String praise;
    private boolean mJustReceivedUpdate;
    private Handler mHandler;
    private String currentText;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle)
    {
        mContext = getActivity();
        mHandler = new Handler();
        registerReceivers();
        View view = layoutInflater.inflate(R.layout.fragment_main_image_status, viewGroup, false);
        statusImage = (ImageView)view.findViewById(R.id.statusImage);
        statusImage.setOnClickListener(imageListener);
        statusImage.setOnTouchListener(imageButtonListener);
        txtTap = (TextSwitcher)view.findViewById(R.id.tap_to_set);
        setTextSwitchers();
        updateLayout();
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
        mJustReceivedUpdate = false;
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
                statusImage.setOnTouchListener(imageButtonListener);
                if(!currentText.equals(getResources().getString(R.string.tap_to_start))){
                    txtTap.setText(getResources().getString(R.string.tap_to_start));
                }
                currentText = getResources().getString(R.string.tap_to_start);
                break;
            case Constants.NON_SCHEDULE_ALARM_RUNNING:
                statusImage.setImageResource(R.drawable.alarm_unscheduled_running);
                statusImage.setOnClickListener(imageListener);
                statusImage.setOnTouchListener(imageButtonListener);
                if(!currentText.equals(getResources().getString(R.string.tap_to_stop))){
                    txtTap.setText(getResources().getString(R.string.tap_to_stop));
                }
                currentText = getResources().getString(R.string.tap_to_stop);
                break;
            case Constants.NON_SCHEDULE_TIME_TO_STAND:
                statusImage.setImageResource(R.drawable.alarm_unscheduled_passed);
                statusImage.setOnClickListener(null);
                statusImage.setOnTouchListener(null);
                if(!currentText.equals(getResources().getString(R.string.stand_time))){
                    txtTap.setText(getResources().getString(R.string.stand_time));
                }
                currentText = getResources().getString(R.string.stand_time);
                break;
            case Constants.NON_SCHEDULE_STOOD_UP:
                statusImage.setImageResource(R.drawable.alarm_unscheduled_stood);
                statusImage.setOnClickListener(null);
                statusImage.setOnTouchListener(null);
                if(praise == null){
                    Log.i(TAG, "praise is null");
                    txtTap.setText("");
                } else {
                    txtTap.setText(praise);
                    currentText = praise;
                    praise = null;
                }
                break;
            case Constants.SCHEDULE_RUNNING:
                statusImage.setImageResource(R.drawable.alarm_schedule_running);
                statusImage.setOnClickListener(null);
                statusImage.setOnTouchListener(null);
                if(!currentText.equals("Schedule Running")){
                    txtTap.setText("Schedule Running");
                }
                currentText = "Schedule Running";
                break;
            case Constants.SCHEDULE_TIME_TO_STAND:
                statusImage.setImageResource(R.drawable.alarm_schedule_passed);
                statusImage.setOnClickListener(null);
                statusImage.setOnTouchListener(null);
                if(!currentText.equals(getResources().getString(R.string.stand_time))){
                    txtTap.setText(getResources().getString(R.string.stand_time));
                }
                currentText = getResources().getString(R.string.stand_time);
                break;
            case Constants.SCHEDULE_STOOD_UP:
                statusImage.setImageResource(R.drawable.alarm_schedule_stood);
                statusImage.setOnClickListener(null);
                statusImage.setOnTouchListener(null);
                if(praise == null){
                    txtTap.setText("");
                } else {
                    txtTap.setText(praise);
                    currentText = praise;
                    praise = null;
                }
                break;
            default:
                statusImage.setImageResource(R.drawable.alarm_image_inactive);
                statusImage.setOnClickListener(imageListener);
                statusImage.setOnTouchListener(imageButtonListener);
                txtTap.setText(getResources().getString(R.string.tap_to_start));
                currentText = getResources().getString(R.string.tap_to_start);
                break;
        }
    }



    private void registerReceivers(){
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(updateImageReceiver, new IntentFilter(Constants.INTENT_MAIN_IMAGE));
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(praiseTextReceiver, new IntentFilter("PraiseForUser"));
    }

    private void unregisterReceivers(){
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(updateImageReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(praiseTextReceiver);
    }

    private BroadcastReceiver updateImageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(!mJustReceivedUpdate){
                mJustReceivedUpdate = true;
                updateLayout();
                mHandler.postDelayed(updating, 290);
            }

        }
    };

    private Runnable updating = new Runnable() {
        @Override
        public void run() {
            mJustReceivedUpdate = false;
        }
    };

    private BroadcastReceiver praiseTextReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            praise = intent.getStringExtra("Praise");
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
        } else if (imageStatus == Constants.NON_SCHEDULE_ALARM_RUNNING) {
            Utils.setCurrentMainActivityImage(getActivity(), Constants.NO_ALARM_RUNNING);
            unscheduledRepeatingAlarm.cancelAlarm();
        }
    }

    private View.OnTouchListener imageButtonListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                statusImage.setImageAlpha(220);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                statusImage.setImageAlpha(255);
            }
            return false;
        }
    };



    private void setTextSwitchers(){
        txtTap.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                tapTextView = new TextView(getActivity());
                tapTextView.setTextSize(25);
                tapTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                tapTextView.setTextColor(getResources().getColor(android.R.color.primary_text_light));
                tapTextView.setText(getResources().getString(R.string.tap_to_start));
                currentText = getResources().getString(R.string.tap_to_start);
                return tapTextView;
            }
        });
        Animation in = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_left);

        txtTap.setInAnimation(in);
        txtTap.setOutAnimation(out);
    }

}