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
 * Reflects current alarm status and occasionally allows user ability to click to change status
 * Created by Sean on 2014-09-03.
 */

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.widget.ViewSwitcher;

import com.Application;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.sean.takeastand.R;
import com.sean.takeastand.alarmprocess.UnscheduledRepeatingAlarm;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;

public class MainImageButtonFragment extends Fragment {
    private ImageView ivStickFigure;
    private TextSwitcher txtTap;
    private TextView tapTextView;
    private static final String TAG = "MainImageButtonFragment";
    private Context mContext;
    private String mPraise;
    private boolean mJustReceivedUpdate;
    private Handler mHandler;
    private String mCurrentText;

    /*
    Instead of updateLayoutAnimated, updateLayoutStatic can have a boolean passed in, that checks
     */

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        mContext = getActivity();
        mHandler = new Handler();
//        registerReceivers();
        View view = layoutInflater.inflate(R.layout.fragment_main_image_button, viewGroup, false);
        ivStickFigure = (ImageView) view.findViewById(R.id.statusImage);
        ivStickFigure.setOnClickListener(imageListener);
        ivStickFigure.setOnTouchListener(imageButtonListener);
        txtTap = (TextSwitcher) view.findViewById(R.id.tap_to_set);
        setTextSwitchers();
//        updateLayoutStatic();
        //If stuck on a non-click listener view uncomment the below line:
//        Utils.setImageStatus(getActivity(), Constants.NO_ALARM_RUNNING);
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
        updateLayoutStatic();
        mJustReceivedUpdate = false;
    }

    @Override
    public void onStart() {
//        updateLayoutStatic();
        super.onStart();
    }

    private void updateLayoutAnimated() {
        int imageStatus = Utils.getImageStatus(getActivity());
        switch (imageStatus) {
            case Constants.NO_ALARM_RUNNING:
                ivStickFigure.setImageResource(R.drawable.alarm_image_inactive);
                ivStickFigure.setOnClickListener(imageListener);
                txtTap.setOnClickListener(imageListener);
                ivStickFigure.setOnTouchListener(imageButtonListener);
                if (!mCurrentText.equals(getResources().getString(R.string.tap_to_start))) {
                    txtTap.setText(getResources().getString(R.string.tap_to_start));
                }
                mCurrentText = getResources().getString(R.string.tap_to_start);
                updateMainActionBar();
                break;
            case Constants.NON_SCHEDULE_ALARM_RUNNING:
                ivStickFigure.setImageResource(R.drawable.alarm_unscheduled_running);
                ivStickFigure.setOnClickListener(imageListener);
                ivStickFigure.setOnTouchListener(imageButtonListener);
                txtTap.setOnClickListener(imageListener);
                txtTap.setText(getResources().getString(R.string.tap_to_stop));
                mCurrentText = getResources().getString(R.string.tap_to_stop);
                updateMainActionBar();
                break;
            case Constants.NON_SCHEDULE_TIME_TO_STAND:
                ivStickFigure.setImageResource(R.drawable.alarm_unscheduled_passed);
                ivStickFigure.setOnClickListener(imageListener);
                ivStickFigure.setOnTouchListener(imageButtonListener);
                txtTap.setOnClickListener(imageListener);
                if (!mCurrentText.equals(getResources().getString(R.string.stand_time))) {
                    txtTap.setText(getResources().getString(R.string.stand_time));
                }
                mCurrentText = getResources().getString(R.string.stand_time);
                break;
            case Constants.NON_SCHEDULE_STOOD_UP:
                ivStickFigure.setImageResource(R.drawable.alarm_unscheduled_stood);
                ivStickFigure.setOnClickListener(imageListener);
                ivStickFigure.setOnTouchListener(imageButtonListener);
                txtTap.setOnClickListener(imageListener);
                if (mPraise == null) {
                    Log.i(TAG, "mPraise is null");
                    txtTap.setText("");
                } else {
                    txtTap.setText(mPraise);
                    mCurrentText = mPraise;
                    mPraise = null;
                }
                break;
            case Constants.NON_SCHEDULE_PAUSED:
                ivStickFigure.setImageResource(R.drawable.alarm_unscheduled_paused);
                ivStickFigure.setOnClickListener(imageListener);
                ivStickFigure.setOnTouchListener(imageButtonListener);
                txtTap.setOnClickListener(imageListener);
                txtTap.setText(mContext.getString(R.string.paused));
                break;
            case Constants.SCHEDULE_RUNNING:
                ivStickFigure.setImageResource(R.drawable.alarm_schedule_running);
                ivStickFigure.setOnClickListener(null);
                ivStickFigure.setOnTouchListener(null);
                txtTap.setOnClickListener(null);
                if (getCurrentTitle().equals("")) {
                    txtTap.setText(mContext.getString(R.string.schedule_running));
                } else {
                    txtTap.setText(getCurrentTitle());
                }
                mCurrentText = mContext.getString(R.string.schedule_running);
                updateMainActionBar();
                break;
            case Constants.SCHEDULE_TIME_TO_STAND:
                ivStickFigure.setImageResource(R.drawable.alarm_schedule_passed);
                ivStickFigure.setOnClickListener(null);
                ivStickFigure.setOnTouchListener(null);
                txtTap.setOnClickListener(null);
                if (!mCurrentText.equals(getResources().getString(R.string.stand_time))) {
                    txtTap.setText(getResources().getString(R.string.stand_time));
                }
                mCurrentText = getResources().getString(R.string.stand_time);
                break;
            case Constants.SCHEDULE_STOOD_UP:
                ivStickFigure.setImageResource(R.drawable.alarm_schedule_stood);
                ivStickFigure.setOnClickListener(null);
                ivStickFigure.setOnTouchListener(null);
                txtTap.setOnClickListener(null);
                if (mPraise == null) {
                    txtTap.setText("");
                } else {
                    txtTap.setText(mPraise);
                    mCurrentText = mPraise;
                    mPraise = null;
                }
                break;
            case Constants.SCHEDULE_PAUSED:
                ivStickFigure.setImageResource(R.drawable.alarm_schedule_paused);
                ivStickFigure.setOnClickListener(null);
                ivStickFigure.setOnTouchListener(null);
                txtTap.setOnClickListener(null);
                txtTap.setText(mContext.getString(R.string.paused));
                break;
            default:
                ivStickFigure.setImageResource(R.drawable.alarm_image_inactive);
                ivStickFigure.setOnClickListener(imageListener);
                ivStickFigure.setOnTouchListener(imageButtonListener);
                txtTap.setOnClickListener(imageListener);
                txtTap.setText(getResources().getString(R.string.tap_to_start));
                mCurrentText = getResources().getString(R.string.tap_to_start);
                break;
        }
    }

    private void updateLayoutStatic() {
        int imageStatus = Utils.getImageStatus(getActivity());
        switch (imageStatus) {
            case Constants.NO_ALARM_RUNNING:
                ivStickFigure.setImageResource(R.drawable.alarm_image_inactive);
                ivStickFigure.setOnClickListener(imageListener);
                ivStickFigure.setOnTouchListener(imageButtonListener);
                txtTap.setOnClickListener(imageListener);
                txtTap.setCurrentText(getResources().getString(R.string.tap_to_start));
                mCurrentText = getResources().getString(R.string.tap_to_start);
                updateMainActionBar();
                break;
            case Constants.NON_SCHEDULE_ALARM_RUNNING:
                ivStickFigure.setImageResource(R.drawable.alarm_unscheduled_running);
                ivStickFigure.setOnClickListener(imageListener);
                ivStickFigure.setOnTouchListener(imageButtonListener);
                txtTap.setOnClickListener(imageListener);
                txtTap.setCurrentText(getResources().getString(R.string.tap_to_stop));
                mCurrentText = getResources().getString(R.string.tap_to_stop);
                updateMainActionBar();
                break;
            case Constants.NON_SCHEDULE_TIME_TO_STAND:
                ivStickFigure.setImageResource(R.drawable.alarm_unscheduled_passed);
                ivStickFigure.setOnClickListener(imageListener);
                ivStickFigure.setOnTouchListener(imageButtonListener);
                txtTap.setOnClickListener(imageListener);
                txtTap.setCurrentText(getResources().getString(R.string.stand_time));
                mCurrentText = getResources().getString(R.string.stand_time);
                break;
            case Constants.NON_SCHEDULE_STOOD_UP:
                ivStickFigure.setImageResource(R.drawable.alarm_unscheduled_stood);
                ivStickFigure.setOnClickListener(imageListener);
                ivStickFigure.setOnTouchListener(imageButtonListener);
                txtTap.setOnClickListener(imageListener);
                if (mPraise == null) {
                    txtTap.setCurrentText(mContext.getString(R.string.default_praise));
                } else {
                    txtTap.setCurrentText(mPraise);
                    mCurrentText = mPraise;
                    mPraise = null;
                }
                break;
            case Constants.NON_SCHEDULE_PAUSED:
                ivStickFigure.setImageResource(R.drawable.alarm_unscheduled_paused);
                ivStickFigure.setOnClickListener(imageListener);
                ivStickFigure.setOnTouchListener(imageButtonListener);
                txtTap.setOnClickListener(imageListener);
                txtTap.setCurrentText(mContext.getString(R.string.paused));
                break;
            case Constants.SCHEDULE_RUNNING:
                ivStickFigure.setImageResource(R.drawable.alarm_schedule_running);
                ivStickFigure.setOnClickListener(null);
                ivStickFigure.setOnTouchListener(null);
                txtTap.setOnClickListener(null);
                if (getCurrentTitle().equals("")) {
                    txtTap.setCurrentText(mContext.getString(R.string.schedule_running));
                } else {
                    txtTap.setCurrentText(getCurrentTitle());
                }
                mCurrentText = mContext.getString(R.string.schedule_running);
                updateMainActionBar();
                break;
            case Constants.SCHEDULE_TIME_TO_STAND:
                ivStickFigure.setImageResource(R.drawable.alarm_schedule_passed);
                ivStickFigure.setOnClickListener(null);
                ivStickFigure.setOnTouchListener(null);
                txtTap.setOnClickListener(null);
                txtTap.setCurrentText(getResources().getString(R.string.stand_time));
                mCurrentText = getResources().getString(R.string.stand_time);
                break;
            case Constants.SCHEDULE_STOOD_UP:
                ivStickFigure.setImageResource(R.drawable.alarm_schedule_stood);
                ivStickFigure.setOnClickListener(null);
                ivStickFigure.setOnTouchListener(null);
                txtTap.setOnClickListener(null);
                if (mPraise == null) {
                    txtTap.setCurrentText(mContext.getString(R.string.default_praise));
                } else {
                    txtTap.setCurrentText(mPraise);
                    mCurrentText = mPraise;
                    mPraise = null;
                }
                break;
            case Constants.SCHEDULE_PAUSED:
                ivStickFigure.setImageResource(R.drawable.alarm_schedule_paused);
                ivStickFigure.setOnClickListener(null);
                ivStickFigure.setOnTouchListener(null);
                txtTap.setOnClickListener(null);
                txtTap.setCurrentText(mContext.getString(R.string.paused));
                break;
            default:
                ivStickFigure.setImageResource(R.drawable.alarm_image_inactive);
                ivStickFigure.setOnClickListener(imageListener);
                ivStickFigure.setOnTouchListener(imageButtonListener);
                txtTap.setOnClickListener(imageListener);
                txtTap.setCurrentText(getResources().getString(R.string.tap_to_start));
                mCurrentText = getResources().getString(R.string.tap_to_start);
                break;
        }
    }

    private void registerReceivers() {
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(updateImageReceiver, new IntentFilter(Constants.INTENT_MAIN_IMAGE));
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(praiseTextReceiver, new IntentFilter(Constants.PRAISE_FOR_USER));
    }

    private void unregisterReceivers() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(updateImageReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(praiseTextReceiver);
    }

    private BroadcastReceiver updateImageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!mJustReceivedUpdate) {
                mJustReceivedUpdate = true;
                updateLayoutAnimated();
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
            mPraise = intent.getStringExtra("Praise");
        }
    };

    private OnClickListener imageListener = new OnClickListener() {

        @Override
        public void onClick(View view) {
            switchStatus();
        }
    };

    private void switchStatus() {
        UnscheduledRepeatingAlarm unscheduledRepeatingAlarm =
                new UnscheduledRepeatingAlarm(getActivity());
        int imageStatus = Utils.getImageStatus(getActivity());
        if (imageStatus == Constants.NO_ALARM_RUNNING) {
            Utils.setImageStatus(getActivity(), Constants.NON_SCHEDULE_ALARM_RUNNING);
            unscheduledRepeatingAlarm.setRepeatingAlarm();
            sendAnalyticsEvent("User began unscheduled alarm");
            Utils.startSession(mContext, Constants.NON_SCHEDULED_SESSION);

        } else if (imageStatus == Constants.NON_SCHEDULE_ALARM_RUNNING ||
                imageStatus == Constants.NON_SCHEDULE_TIME_TO_STAND ||
                imageStatus == Constants.NON_SCHEDULE_STOOD_UP ||
                imageStatus == Constants.NON_SCHEDULE_PAUSED) {
            Utils.setImageStatus(getActivity(), Constants.NO_ALARM_RUNNING);
            unscheduledRepeatingAlarm.cancelAlarm();
            sendAnalyticsEvent("User ended unscheduled alarm");
            Utils.endSession(mContext);
        }
    }

    private View.OnTouchListener imageButtonListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                ivStickFigure.setImageAlpha(220);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                ivStickFigure.setImageAlpha(255);
            }
            return false;
        }
    };


    private void setTextSwitchers() {
        txtTap.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                tapTextView = new TextView(getActivity());
                tapTextView.setTextSize(24);
                tapTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                tapTextView.setTextColor(getResources().getColor(android.R.color.secondary_text_light));
                tapTextView.setText(getResources().getString(R.string.tap_to_start));
                mCurrentText = getResources().getString(R.string.tap_to_start);
                return tapTextView;
            }
        });
        Animation in = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_left);

        txtTap.setInAnimation(in);
        txtTap.setOutAnimation(out);
    }

    private String getCurrentTitle() {
        SharedPreferences sharedPreferences =
                mContext.getSharedPreferences(Constants.EVENT_SHARED_PREFERENCES, 0);
        return sharedPreferences.getString(Constants.CURRENT_SCHEDULED_ALARM_TITLE, "");
    }

    private void updateMainActionBar() {
        Intent intent = new Intent(Constants.UPDATE_ACTION_BAR);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }

    private void sendAnalyticsEvent(String action) {
        Tracker t = ((Application) getActivity().getApplication()).getTracker(
                Application.TrackerName.APP_TRACKER);
        t.enableAdvertisingIdCollection(true);
        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder()
                .setCategory(Constants.UI_EVENT)
                .setAction(action)
                .build());
    }
}