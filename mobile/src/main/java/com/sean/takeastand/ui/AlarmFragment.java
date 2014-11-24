package com.sean.takeastand.ui;


import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sean.takeastand.R;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;

/**
 * Small view that pops up depending on current alarm status
 * Created by Sean on 2014-11-08.
 */
public class AlarmFragment extends Fragment{

    private static final String TAG = "AlarmFragment";
    private LinearLayout nextAlertLayout;
    private LinearLayout stoodDelayLayout;
    private TextView nextAlert;
    private TextView stoodText;
    private TextView delayText;
    private boolean mJustReceivedUpdate;
    private Handler mHandler;
    private int currentAlarmStatus;
    private int previousAlarmStatus;
    private boolean stoodDelay;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_alarm, container, false);
        nextAlertLayout = (LinearLayout)view.findViewById(R.id.next_alert_time_layout);
        nextAlertLayout.setVisibility(View.GONE);
        stoodDelayLayout = (LinearLayout)view.findViewById(R.id.stood_or_delay_layout);
        stoodDelayLayout.setVisibility(View.GONE);
        nextAlert = (TextView)view.findViewById(R.id.next_alert_time);
        stoodText = (TextView)view.findViewById(R.id.stood);
        stoodText.setOnClickListener(stoodListener);
        stoodText.setOnTouchListener(textTouchListener);
        delayText = (TextView)view.findViewById(R.id.delay);
        delayText.setOnClickListener(delayListener);
        delayText.setOnTouchListener(textTouchListener);
        registerReceivers();
        mHandler = new Handler();
        return view;
    }

    @Override
    public void onPause() {
        unregisterReceivers();
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        updateLayoutStatic();
        registerReceivers();
        super.onResume();
    }

    private void registerReceivers(){
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(updateReceiver, new IntentFilter(Constants.INTENT_MAIN_IMAGE));
    }

    private void unregisterReceivers(){
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(updateReceiver);
    }

    private BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(!mJustReceivedUpdate){
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

    /*
    Whenever previousStatus == currentStatus, it means user has closed the app, and came back
    Don't show the animation again.
     */
    private void updateLayoutAnimated(){
        int imageStatus = Utils.getImageStatus(getActivity());
        switch (imageStatus){
            case Constants.NO_ALARM_RUNNING:
                currentAlarmStatus = Constants.NO_ALARM_RUNNING;
                if(nextAlertLayout.getVisibility() == View.VISIBLE) {
                    mHandler.post(hideNextAlertAnimation);
                }
                stoodDelayLayout.setVisibility(View.GONE);
                previousAlarmStatus = Constants.NO_ALARM_RUNNING;
                break;
            case Constants.NON_SCHEDULE_ALARM_RUNNING:
                currentAlarmStatus = Constants.NON_SCHEDULE_ALARM_RUNNING;
                if(previousAlarmStatus == Constants.NON_SCHEDULE_TIME_TO_STAND && !stoodDelay){
                    hideStoodDelay();
                } else if(!(currentAlarmStatus == previousAlarmStatus)){
                    stoodDelayLayout.setVisibility(View.GONE);
                    mHandler.post(showNextAlertAnimation);
                }
                previousAlarmStatus = Constants.NON_SCHEDULE_ALARM_RUNNING;
                break;
            case Constants.NON_SCHEDULE_TIME_TO_STAND:
                currentAlarmStatus = Constants.NON_SCHEDULE_TIME_TO_STAND;
                if(currentAlarmStatus == previousAlarmStatus){
                    stoodDelayLayout.setVisibility(View.VISIBLE);
                } else {
                    mHandler.post(hideNextAlertAnimation);
                }
                previousAlarmStatus = Constants.NON_SCHEDULE_TIME_TO_STAND;
                break;
            case Constants.NON_SCHEDULE_STOOD_UP:
                currentAlarmStatus = Constants.NON_SCHEDULE_STOOD_UP;
                hideStoodDelay();
                previousAlarmStatus = Constants.NON_SCHEDULE_STOOD_UP;
                break;
            case Constants.SCHEDULE_RUNNING:
                currentAlarmStatus = Constants.SCHEDULE_RUNNING;
                nextAlert.setText(Utils.getNextAlarmTimeString(getActivity()));
                if(previousAlarmStatus == Constants.SCHEDULE_TIME_TO_STAND && !stoodDelay){
                    hideStoodDelay();
                } else if(!(currentAlarmStatus == previousAlarmStatus)){
                    stoodDelayLayout.setVisibility(View.GONE);
                    mHandler.post(showNextAlertAnimation);
                }
                previousAlarmStatus = Constants.SCHEDULE_RUNNING;
                break;
            case Constants.SCHEDULE_TIME_TO_STAND:
                currentAlarmStatus = Constants.SCHEDULE_TIME_TO_STAND;
                if(currentAlarmStatus == previousAlarmStatus){
                    stoodDelayLayout.setVisibility(View.VISIBLE);
                } else {
                    mHandler.post(hideNextAlertAnimation);
                }
                previousAlarmStatus = Constants.SCHEDULE_TIME_TO_STAND;
                break;
            case Constants.SCHEDULE_STOOD_UP:
                currentAlarmStatus = Constants.SCHEDULE_STOOD_UP;
                hideStoodDelay();
                previousAlarmStatus = Constants.SCHEDULE_STOOD_UP;
                break;
            default:
                currentAlarmStatus = Constants.NO_ALARM_RUNNING;
                nextAlertLayout.setVisibility(View.GONE);
                stoodDelayLayout.setVisibility(View.GONE);
                previousAlarmStatus = Constants.NO_ALARM_RUNNING;
                break;
        }
    }

    private void updateLayoutStatic(){
        int imageStatus = Utils.getImageStatus(getActivity());
        switch (imageStatus){
            case Constants.NO_ALARM_RUNNING:
                currentAlarmStatus = Constants.NO_ALARM_RUNNING;
                nextAlertLayout.setVisibility(View.GONE);
                stoodDelayLayout.setVisibility(View.GONE);
                previousAlarmStatus = Constants.NO_ALARM_RUNNING;
                break;
            case Constants.NON_SCHEDULE_ALARM_RUNNING:
                currentAlarmStatus = Constants.NON_SCHEDULE_ALARM_RUNNING;
                nextAlertLayout.setVisibility(View.VISIBLE);
                nextAlert.setText(Utils.getNextAlarmTimeString(getActivity()));
                stoodDelayLayout.setVisibility(View.GONE);
                previousAlarmStatus = Constants.NON_SCHEDULE_ALARM_RUNNING;
                break;
            case Constants.NON_SCHEDULE_TIME_TO_STAND:
                currentAlarmStatus = Constants.NON_SCHEDULE_TIME_TO_STAND;
                stoodDelayLayout.setVisibility(View.VISIBLE);
                stoodText.setVisibility(View.VISIBLE);
                delayText.setVisibility(View.VISIBLE);
                nextAlertLayout.setVisibility(View.GONE);
                previousAlarmStatus = Constants.NON_SCHEDULE_TIME_TO_STAND;
                break;
            case Constants.NON_SCHEDULE_STOOD_UP:
                currentAlarmStatus = Constants.NON_SCHEDULE_STOOD_UP;
                stoodDelayLayout.setVisibility(View.GONE);
                nextAlertLayout.setVisibility(View.GONE);
                previousAlarmStatus = Constants.NON_SCHEDULE_STOOD_UP;
                break;
            case Constants.SCHEDULE_RUNNING:
                currentAlarmStatus = Constants.SCHEDULE_RUNNING;
                nextAlertLayout.setVisibility(View.VISIBLE);
                nextAlert.setText(Utils.getNextAlarmTimeString(getActivity()));
                stoodDelayLayout.setVisibility(View.GONE);
                previousAlarmStatus = Constants.SCHEDULE_RUNNING;
                break;
            case Constants.SCHEDULE_TIME_TO_STAND:
                currentAlarmStatus = Constants.SCHEDULE_TIME_TO_STAND;
                stoodDelayLayout.setVisibility(View.VISIBLE);
                stoodText.setVisibility(View.VISIBLE);
                delayText.setVisibility(View.VISIBLE);
                nextAlertLayout.setVisibility(View.GONE);
                previousAlarmStatus = Constants.SCHEDULE_TIME_TO_STAND;
                break;
            case Constants.SCHEDULE_STOOD_UP:
                currentAlarmStatus = Constants.SCHEDULE_STOOD_UP;
                stoodDelayLayout.setVisibility(View.GONE);
                nextAlertLayout.setVisibility(View.GONE);
                previousAlarmStatus = Constants.SCHEDULE_STOOD_UP;
                break;
            default:
                currentAlarmStatus = Constants.NO_ALARM_RUNNING;
                nextAlertLayout.setVisibility(View.GONE);
                stoodDelayLayout.setVisibility(View.GONE);
                previousAlarmStatus = Constants.NO_ALARM_RUNNING;
                break;
        }
    }

    private View.OnClickListener stoodListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent stoodUpIntent = new Intent("StoodUp");
            getActivity().sendBroadcast(stoodUpIntent);
            stoodDelay = true;
        }
    };

    private View.OnClickListener delayListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent delayAlarmIntent = new Intent("DelayAlarm");
            getActivity().sendBroadcast(delayAlarmIntent);
            stoodDelay = false;
        }
    };

    private View.OnTouchListener textTouchListener = new View.OnTouchListener(){
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            TextView touchedText = (TextView)view;
            if(motionEvent.getAction()== MotionEvent.ACTION_DOWN){
                touchedText.setTextColor(getResources().getColor(android.R.color.secondary_text_light));
            }
            if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                touchedText.setTextColor(getResources().getColor(android.R.color.primary_text_light));
            }
            return false;
        }
    };

    Runnable showNextAlertAnimation = new Runnable() {
        @Override
        public void run() {
            nextAlertLayout.setVisibility(View.VISIBLE);
            nextAlert.setText(Utils.getNextAlarmTimeString(getActivity()));
            Animation slideIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_up);
            nextAlertLayout.startAnimation(slideIn);
        }
    };

    Runnable hideNextAlertAnimation = new Runnable() {
        @Override
        public void run() {

                nextAlert.setText(Utils.getNextAlarmTimeString(getActivity()));
                Animation slideOut = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_down);
                slideOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        nextAlertLayout.setVisibility(View.GONE);
                        if(currentAlarmStatus == Constants.NON_SCHEDULE_TIME_TO_STAND ||
                                currentAlarmStatus == Constants.SCHEDULE_TIME_TO_STAND){
                            mHandler.postDelayed(showStoodDelayAnimation, 100);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                nextAlertLayout.startAnimation(slideOut);

        }
    };

    Runnable showStoodDelayAnimation = new Runnable() {
        @Override
        public void run() {
            stoodDelayLayout.setVisibility(View.VISIBLE);
            stoodText.setVisibility(View.VISIBLE);
            delayText.setVisibility(View.VISIBLE);
            Animation scaleIn = AnimationUtils.loadAnimation(getActivity(), R.anim.scale_in);
            stoodText.startAnimation(scaleIn);
            delayText.startAnimation(scaleIn);
        }
    };

    private void hideStoodDelay(){
        if(stoodDelay){
            mHandler.post(hideStoodAnimation);
            mHandler.postDelayed(hideDelayAnimation, 200);
        } else {
            mHandler.post(hideDelayAnimation);
            mHandler.postDelayed(hideStoodAnimation, 200);
        }
    }

    Runnable hideStoodAnimation = new Runnable() {
        @Override
        public void run() {
            Animation scaleOut = AnimationUtils.loadAnimation(getActivity(), R.anim.scale_out);
            scaleOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    stoodText.setVisibility(View.INVISIBLE);
                    if(stoodDelay){
                        stoodDelayLayout.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            stoodText.startAnimation(scaleOut);
        }
    };

    Runnable hideDelayAnimation = new Runnable() {
        @Override
        public void run() {
            Animation scaleOut = AnimationUtils.loadAnimation(getActivity(), R.anim.scale_out);
            scaleOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    delayText.setVisibility(View.INVISIBLE);
                    if(!stoodDelay){
                        stoodDelayLayout.setVisibility(View.GONE);
                        mHandler.post(showNextAlertAnimation);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            delayText.startAnimation(scaleOut);
        }
    };
}
