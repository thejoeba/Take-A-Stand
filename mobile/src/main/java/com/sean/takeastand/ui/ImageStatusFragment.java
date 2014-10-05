package com.sean.takeastand.ui;

/**
 * Created by Sean on 2014-09-03.
 */
import android.content.Context;
import android.content.Intent;
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
import com.sean.takeastand.alarmprocess.RepeatingAlarmController;

public class ImageStatusFragment
        extends Fragment
{
    private ImageView onOffImage;
    private static final String TAG = "ImageStatusFragment";
    private static final String CURRENT_IMAGE_STATE = "CurrentImageStatus";
    private static final String SHARED_PREFERENCES_NAME = "CurrentStatusFragmentSharedP";
    private boolean mRepeatingAlarmOn;
    private Context mContext;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle)
    {
        Log.i(TAG, "onCreateView");
        mContext = getActivity();
        View view = layoutInflater.inflate(R.layout.fragment_main_image_status, viewGroup, false);
        //In future before setting image, will check to see that alarmSchedule is not running
        onOffImage = (ImageView)view.findViewById(R.id.onOffImage);
        onOffImage.setOnClickListener(imageListener);
        setInitialImage();
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor saveSetup = sharedPreferences.edit();
        saveSetup.putBoolean(CURRENT_IMAGE_STATE, mRepeatingAlarmOn);
        saveSetup.commit();
    }

    private OnClickListener imageListener = new OnClickListener(){

        @Override
        public void onClick(View view) {
           switchImage();
        }
    };

    private void setInitialImage(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        boolean hasOnOffState = sharedPreferences.contains(CURRENT_IMAGE_STATE);
        if(hasOnOffState){
            mRepeatingAlarmOn = sharedPreferences.getBoolean(CURRENT_IMAGE_STATE, false);
            setImage(mRepeatingAlarmOn);
        } else {
            setImage();
        }
    }

    private void setImage(){
        mRepeatingAlarmOn = checkIfOn();
        Log.i(TAG, "image is set to :" + Boolean.toString(mRepeatingAlarmOn));
        if(mRepeatingAlarmOn){
            onOffImage.setImageResource(R.drawable.alarm_image_active);
        } else{
            onOffImage.setImageResource(R.drawable.alarm_image_inactive);
        }
    }

    private void setImage(boolean onOffState){
        if(onOffState){
            onOffImage.setImageResource(R.drawable.alarm_image_active);
        } else{
            onOffImage.setImageResource(R.drawable.alarm_image_inactive);
        }
    }

    private void switchImage(){
        mRepeatingAlarmOn = checkIfOn();
        Log.i(TAG, "image is set to :" + Boolean.toString(!mRepeatingAlarmOn));
        if(mRepeatingAlarmOn){
            new RepeatingAlarmController(mContext).cancelAlarm();
            onOffImage.setImageResource(R.drawable.alarm_image_inactive);
            endAlarmService();
            mRepeatingAlarmOn = false;
        } else{
            new RepeatingAlarmController(mContext).setNonScheduleRepeatingAlarm();
            onOffImage.setImageResource(R.drawable.alarm_image_active);
            mRepeatingAlarmOn = true;
        }
    }

    private void endAlarmService(){
        Intent intent = new Intent("userSwitchedOffAlarm");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private boolean checkIfOn()
    {
       return new RepeatingAlarmController(mContext).isAlarmSet();
    }



}