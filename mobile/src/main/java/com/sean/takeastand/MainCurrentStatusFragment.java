package com.sean.takeastand;

/**
 * Created by Sean on 2014-09-03.
 */
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

public class MainCurrentStatusFragment
        extends Fragment
{
    ImageView onOffImage;
    private static final String TAG = "MainCurrentStatusFragment";
    boolean repeatingAlarmOn;
    private Context mContext;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle)
    {
        Log.i(TAG, "onCreateView");
        mContext = getActivity();
        View view = layoutInflater.inflate(R.layout.main_current_status_fragment, viewGroup, false);
        onOffImage = (ImageView)view.findViewById(R.id.onOffImage);
        onOffImage.setOnClickListener(imageListener);
        setImage();
        return view;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private OnClickListener imageListener = new OnClickListener(){

        @Override
        public void onClick(View view) {
           switchImage();
        }
    };

    private void setImage(){
        repeatingAlarmOn = checkIfOn();
        Log.i(TAG, "image is set to :" + Boolean.toString(repeatingAlarmOn));
        if(repeatingAlarmOn){
            onOffImage.setImageResource(R.drawable.alarm_image_active);
        } else{
            onOffImage.setImageResource(R.drawable.alarm_image_inactive);
        }
    }


    private void switchImage(){
        repeatingAlarmOn = checkIfOn();
        Log.i(TAG, "image is set to :" + Boolean.toString(!repeatingAlarmOn));
        if(repeatingAlarmOn){
            new RepeatingAlarmController(mContext).cancelAlarm();
            onOffImage.setImageResource(R.drawable.alarm_image_inactive);
        } else{
            new RepeatingAlarmController(mContext).setNewAlarm();
            onOffImage.setImageResource(R.drawable.alarm_image_active);
        }
    }

    private boolean checkIfOn()
    {
       return new RepeatingAlarmController(mContext).isAlarmSet();
    }
}