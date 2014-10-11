package com.sean.takeastand.ui;

/**
 * Created by Sean on 2014-09-03.
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sean.takeastand.R;
import com.sean.takeastand.alarmprocess.AlarmService;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;

public class ImageStatusFragment
        extends Fragment
{
    private ImageView statusImage;
    private static final String TAG = "ImageStatusFragment";
    private static final String CURRENT_IMAGE_STATE = "CurrentImageStatus";
    private static final String SHARED_PREFERENCES_NAME = "CurrentStatusFragmentSharedP";
    private Context mContext;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle)
    {
        Log.i(TAG, "onCreateView");
        mContext = getActivity();
        View view = layoutInflater.inflate(R.layout.fragment_main_image_status, viewGroup, false);
        statusImage = (ImageView)view.findViewById(R.id.statusImage);
        statusImage.setOnClickListener(imageListener);
        updateImage();
        registerReceivers();
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
        updateImage();
    }

    @Override
    public void onStart() {
        updateImage();
        super.onStart();
    }

    private void switchStatus(){
        int imageStatus = Utils.getCurrentImageStatus(getActivity());
        if(imageStatus == Constants.NO_ALARM_RUNNING){
            Utils.setCurrentMainActivityImage(getActivity(), Constants.NON_SCHEDULE_ALARM_RUNNING);
        } else if (imageStatus == Constants.NON_SCHEDULE_ALARM_RUNNING) {
           Utils.setCurrentMainActivityImage(getActivity(), Constants.NO_ALARM_RUNNING);
           endAlarmService();
        }
        updateImage();
    }

    private void endAlarmService(){
        Intent intent = new Intent("userSwitchedOffAlarm");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void updateImage(){
        int imageStatus = Utils.getCurrentImageStatus(getActivity());
        if(imageStatus == Constants.NO_ALARM_RUNNING) {
            statusImage.setImageResource(R.drawable.alarm_image_inactive);
            statusImage.setOnClickListener(imageListener);
        } else if(imageStatus == Constants.NON_SCHEDULE_ALARM_RUNNING){
            statusImage.setImageResource(R.drawable.alarm_image_active);
            statusImage.setOnClickListener(imageListener);
        } else if(imageStatus == Constants.SCHEDULE_RUNNING) {
            statusImage.setImageResource(R.drawable.alarm_schedule_running);
            statusImage.setOnClickListener(null);
        } else if( imageStatus == Constants.SCHEDULE_TIME_TO_STAND){
            statusImage.setImageResource(R.drawable.alarm_schedule_passed);
            statusImage.setOnClickListener(null);
        } else if( imageStatus == Constants.SCHEDULE_STOOD_UP) {
            statusImage.setImageResource(R.drawable.alarm_schedule_stood);
            statusImage.setOnClickListener(null);
        } else {
            statusImage.setImageResource(R.drawable.alarm_image_inactive);
            statusImage.setOnClickListener(imageListener);
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
            updateImage();
        }
    };

    private OnClickListener imageListener = new OnClickListener(){

        @Override
        public void onClick(View view) {
            switchStatus();
        }
    };

}