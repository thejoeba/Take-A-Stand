package com.sean.takeastand;

/**
 * Created by Sean on 2014-09-03.
 */
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class MainCurrentStatusFragment
        extends Fragment
{
    ImageView onOffImage;
    private static final String TAG = "MainCurrentStatusFragment";
    boolean repeatingAlarmOn;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle)
    {
        Log.i(TAG, "onCreateView");
        View view = layoutInflater.inflate(R.layout.main_current_status_fragment, viewGroup, false);
        onOffImage = (ImageView)view.findViewById(R.id.onOffImage);
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //setOnClickListener
        super.onViewCreated(view, savedInstanceState);
    }

    private void setOnClickListener(){
        //repeatingAlarmOn = checkIfOn
        /*if(repeatingAlarmOn){
            RepeatingAlarmController.cancelAlarm();
            switch image
        } else{
            RepeatingAlarmController.startAlarm();
            switch image
        }

         */
    }

    //private boolean checkIfOn()
    {
        /*
        Check to see if a repeatingAlarmController is set or not. Need to create
        a new public method in RepeatingAlarmController. Maybe using a static variable.
         */
    }
}