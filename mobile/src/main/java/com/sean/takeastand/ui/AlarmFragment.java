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
    private LinearLayout stoodLayout;
    private TextView nextAlert;
    private TextView stoodText;
    private boolean mJustReceivedUpdate;
    private Handler mHandler;
    private int mPreviousAlarmStatus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_alarm, container, false);
        nextAlertLayout = (LinearLayout)view.findViewById(R.id.next_alert_time_layout);
        nextAlertLayout.setVisibility(View.GONE);
        stoodLayout = (LinearLayout)view.findViewById(R.id.stood_layout);
        stoodLayout.setVisibility(View.GONE);
        nextAlert = (TextView)view.findViewById(R.id.next_alert_time);
        stoodText = (TextView)view.findViewById(R.id.stood);
        stoodText.setOnClickListener(stoodListener);
        stoodText.setOnTouchListener(textTouchListener);
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
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(updateTimeReceiver, new IntentFilter(Constants.UPDATE_NEXT_ALARM_TIME));
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

    private BroadcastReceiver updateTimeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateLayoutStatic();
        }
    };

    private Runnable updating = new Runnable() {
        @Override
        public void run() {
            mJustReceivedUpdate = false;
        }
    };

    private void updateLayoutAnimated(){
        int imageStatus = Utils.getImageStatus(getActivity());
        switch (imageStatus){
            case Constants.NO_ALARM_RUNNING:
                if(nextAlertLayout.getVisibility() == View.VISIBLE) {
                    mHandler.post(hideNextAlertAnimation);
                }
                stoodLayout.setVisibility(View.GONE);
                mPreviousAlarmStatus = Constants.NO_ALARM_RUNNING;
                break;
            case Constants.NON_SCHEDULE_ALARM_RUNNING:
                if(mPreviousAlarmStatus == Constants.NON_SCHEDULE_TIME_TO_STAND){
                    hideStood();
                } else if(!(Constants.NON_SCHEDULE_ALARM_RUNNING == mPreviousAlarmStatus)){
                    stoodLayout.setVisibility(View.GONE);
                    mHandler.post(showNextAlertAnimation);
                }
                mPreviousAlarmStatus = Constants.NON_SCHEDULE_ALARM_RUNNING;
                break;
            case Constants.NON_SCHEDULE_TIME_TO_STAND:
                if(Constants.NON_SCHEDULE_TIME_TO_STAND == mPreviousAlarmStatus){
                    stoodLayout.setVisibility(View.VISIBLE);
                    stoodText.clearAnimation();
                    stoodText.setVisibility(View.VISIBLE);
                } else {
                    mHandler.post(hideNextAlertAnimation);
                }
                mPreviousAlarmStatus = Constants.NON_SCHEDULE_TIME_TO_STAND;
                break;
            case Constants.NON_SCHEDULE_STOOD_UP:
                hideStood();
                mPreviousAlarmStatus = Constants.NON_SCHEDULE_STOOD_UP;
                break;
            case Constants.SCHEDULE_RUNNING:
                nextAlert.setText(Utils.getNextAlarmTimeString(getActivity()));
                if(mPreviousAlarmStatus == Constants.SCHEDULE_TIME_TO_STAND){
                    hideStood();
                } else if(!(Constants.SCHEDULE_RUNNING == mPreviousAlarmStatus)){
                    stoodLayout.setVisibility(View.GONE);
                    mHandler.post(showNextAlertAnimation);
                }
                mPreviousAlarmStatus = Constants.SCHEDULE_RUNNING;
                break;
            case Constants.SCHEDULE_TIME_TO_STAND:
                if(Constants.SCHEDULE_TIME_TO_STAND == mPreviousAlarmStatus){
                    stoodLayout.setVisibility(View.VISIBLE);
                    stoodText.clearAnimation();
                    stoodText.setVisibility(View.VISIBLE);
                } else {
                    mHandler.post(hideNextAlertAnimation);
                }
                mPreviousAlarmStatus = Constants.SCHEDULE_TIME_TO_STAND;
                break;
            case Constants.SCHEDULE_STOOD_UP:
                hideStood();
                mPreviousAlarmStatus = Constants.SCHEDULE_STOOD_UP;
                break;
            default:
                nextAlertLayout.setVisibility(View.GONE);
                stoodLayout.setVisibility(View.GONE);
                mPreviousAlarmStatus = Constants.NO_ALARM_RUNNING;
                break;
        }
    }

    private void updateLayoutStatic(){
        int imageStatus = Utils.getImageStatus(getActivity());
        switch (imageStatus){
            case Constants.NO_ALARM_RUNNING:
                nextAlertLayout.setVisibility(View.GONE);
                stoodLayout.setVisibility(View.GONE);
                mPreviousAlarmStatus = Constants.NO_ALARM_RUNNING;
                break;
            case Constants.NON_SCHEDULE_ALARM_RUNNING:
                nextAlertLayout.setVisibility(View.VISIBLE);
                nextAlert.setText(Utils.getNextAlarmTimeString(getActivity()));
                stoodLayout.setVisibility(View.GONE);
                mPreviousAlarmStatus = Constants.NON_SCHEDULE_ALARM_RUNNING;
                break;
            case Constants.NON_SCHEDULE_TIME_TO_STAND:
                stoodLayout.setVisibility(View.VISIBLE);
                stoodText.clearAnimation();
                stoodText.setVisibility(View.VISIBLE);
                nextAlertLayout.setVisibility(View.GONE);
                mPreviousAlarmStatus = Constants.NON_SCHEDULE_TIME_TO_STAND;
                break;
            case Constants.NON_SCHEDULE_STOOD_UP:
                stoodLayout.setVisibility(View.GONE);
                nextAlertLayout.setVisibility(View.GONE);
                mPreviousAlarmStatus = Constants.NON_SCHEDULE_STOOD_UP;
                break;
            case Constants.SCHEDULE_RUNNING:
                nextAlertLayout.setVisibility(View.VISIBLE);
                nextAlert.setText(Utils.getNextAlarmTimeString(getActivity()));
                stoodLayout.setVisibility(View.GONE);
                mPreviousAlarmStatus = Constants.SCHEDULE_RUNNING;
                break;
            case Constants.SCHEDULE_TIME_TO_STAND:
                stoodLayout.setVisibility(View.VISIBLE);
                stoodText.clearAnimation();
                stoodText.setVisibility(View.VISIBLE);
                nextAlertLayout.setVisibility(View.GONE);
                mPreviousAlarmStatus = Constants.SCHEDULE_TIME_TO_STAND;
                break;
            case Constants.SCHEDULE_STOOD_UP:
                stoodLayout.setVisibility(View.GONE);
                nextAlertLayout.setVisibility(View.GONE);
                mPreviousAlarmStatus = Constants.SCHEDULE_STOOD_UP;
                break;
            default:
                nextAlertLayout.setVisibility(View.GONE);
                stoodLayout.setVisibility(View.GONE);
                mPreviousAlarmStatus = Constants.NO_ALARM_RUNNING;
                break;
        }
    }

    private View.OnClickListener stoodListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent stoodUpIntent = new Intent(Constants.STOOD_RESULTS);
            stoodUpIntent.putExtra(Constants.STOOD_METHOD, Constants.TAPPED_ACTIVITY);
            getActivity().sendBroadcast(stoodUpIntent);
        }
    };

    private View.OnTouchListener textTouchListener = new View.OnTouchListener(){
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            TextView touchedText = (TextView)view;
            if(motionEvent.getAction()== MotionEvent.ACTION_DOWN){
                touchedText.setAlpha((float)0.7);
            }
            if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                touchedText.setAlpha((float)1);
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
                        nextAlertLayout.clearAnimation();
                        if(Utils.getImageStatus(getActivity()) == Constants.NON_SCHEDULE_TIME_TO_STAND ||
                                Utils.getImageStatus(getActivity()) == Constants.SCHEDULE_TIME_TO_STAND){
                            mHandler.postDelayed(showStoodAnimation, 100);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                nextAlertLayout.startAnimation(slideOut);

        }
    };

    Runnable showStoodAnimation = new Runnable() {
        @Override
        public void run() {
            stoodLayout.setVisibility(View.VISIBLE);
            stoodText.setVisibility(View.VISIBLE);
            Animation scaleIn = AnimationUtils.loadAnimation(getActivity(), R.anim.scale_in);
            stoodText.startAnimation(scaleIn);
        }
    };

    private void hideStood(){
            mHandler.post(hideStoodAnimation);
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
                    stoodLayout.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            stoodText.startAnimation(scaleOut);
        }
    };

}
