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

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sean.takeastand.R;
import com.sean.takeastand.alarmprocess.AlarmService;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;

import java.util.Arrays;


public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private Menu mainMenu;

    @Override
    protected void onCreate(Bundle paramBundle)
    {
        super.onCreate(paramBundle);
        //deleteDatabase("alarms_database");
        //Utils.setCurrentMainActivityImage(this, Constants.NO_ALARM_RUNNING);
        setUpLayout();
        if(isNewUser()){
            setUserDefaults();
            setNotNewUser();
            Log.i(TAG, "New User");
        } else {
            Log.i(TAG, "Not New User");
        }
    }

    private void setUpLayout()
    {
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        mainMenu = menu;
        setVibrateText();
        return true;
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId()){
            case R.id.schedules:
                Intent intent = new Intent(this, ScheduleListActivity.class);
                startActivity(intent);
                break;
            case R.id.default_frequency:
                showNumberPickerDialog(Utils.getDefaultFrequency(this), 2 , 100,
                        "Select Default Frequency", true);
                break;
            case R.id.default_alert_type:
                showAlertTypePicker();
                break;
            case R.id.default_delay_length:
                showNumberPickerDialog(Utils.getDefaultDelay(this), 1,
                        (Utils.getDefaultFrequency(this) - 1),
                        "Select Default Delay Length", false);
                break;
            case R.id.science:
                Intent intentScience = new Intent(this, ScienceActivity.class);
                startActivity(intentScience);
                break;
            case R.id.vibrateOnSilent:
                vibrateOnSilent(item);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        Intent intent = new Intent("VisibilityStatus");
        intent.putExtra("Visible", false);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceivers();
    }

    @Override
    protected void onResume() {
        registerReceivers();
        Intent intent = new Intent("VisibilityStatus");
        intent.putExtra("Visible", true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        super.onResume();
    }

    private void registerReceivers(){
        LocalBroadcastManager.getInstance(this).registerReceiver(visibilityReceiver,
                new IntentFilter("Visible"));
    }

    private void unregisterReceivers(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(visibilityReceiver);
    }

    private BroadcastReceiver visibilityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent newIntent = new Intent("VisibilityStatus");
            newIntent.putExtra("Visible", true);
            LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(newIntent);
        }
    };

    //Must be class-level to access within onClick
    NumberPicker numberPicker;

    private void showNumberPickerDialog(int startingValue, int min, int max, String title,
                                        final boolean frequency)
    {
        LayoutInflater inflater = getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = inflater.inflate(R.layout.dialog_number_picker, null);
        builder.setView(dialogView);
        numberPicker = (NumberPicker)dialogView.findViewById(R.id.numberPicker);
        numberPicker.setMaxValue(max);
        numberPicker.setMinValue(min);
        numberPicker.setValue(startingValue);
        numberPicker.setWrapSelectorWheel(false);
        builder.setMessage(title);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(frequency){
                    Utils.setDefaultFrequency(MainActivity.this, numberPicker.getValue());
                } else {
                    Utils.setDefaultDelay(MainActivity.this, numberPicker.getValue());
                }
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.i(TAG, "Cancel");
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    View dialogView;
    private void showAlertTypePicker(){
        LayoutInflater inflater = getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        dialogView = inflater.inflate(R.layout.dialog_alert_type, null);
        int[] currentNotification = Utils.getDefaultAlertType(this);
        CheckBox LED = (CheckBox)dialogView.findViewById(R.id.chbxLED);
        LED.setChecked(Utils.convertIntToBoolean(currentNotification[0]));
        CheckBox vibrate = (CheckBox)dialogView.findViewById(R.id.chbxVibrate);
        vibrate.setChecked(Utils.convertIntToBoolean(currentNotification[1]));
        CheckBox sound = (CheckBox)dialogView.findViewById(R.id.chbxSound);
        sound.setChecked(Utils.convertIntToBoolean(currentNotification[2]));
        builder.setView(dialogView);
        builder.setMessage("Set Default Notification Types");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int[] notificationTypes = new int[3];
                CheckBox LED = (CheckBox)dialogView.findViewById(R.id.chbxLED);
                CheckBox Vibrate = (CheckBox)dialogView.findViewById(R.id.chbxVibrate);
                CheckBox Sound = (CheckBox)dialogView.findViewById(R.id.chbxSound);
                if(LED.isChecked()){
                    notificationTypes[0] = 1;
                } else {
                    notificationTypes[0] = 0;
                }
                if(Vibrate.isChecked()){
                    notificationTypes[1] = 1;
                } else {
                    notificationTypes[1] = 0;
                }
                if(Sound.isChecked()){
                    notificationTypes[2] = 1;
                } else {
                    notificationTypes[2] = 0;
                }
                Utils.setDefaultAlertType(MainActivity.this, notificationTypes);
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.i(TAG, "Cancel");
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private boolean isNewUser(){
        //This is used to identify a new user in order to set defaults
        SharedPreferences sharedPreferences =
                getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);
        return sharedPreferences.getBoolean(Constants.NEW_USER, true);
    }

    private void setUserDefaults(){
        SharedPreferences sharedPreferences =
                getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        int[] alertType = new int[] {1, 1, 0};
        editor.putString(Constants.USER_ALERT_TYPE, Utils.convertIntArrayToString(alertType));
        editor.putInt(Constants.USER_FREQUENCY, 20);
        editor.putInt(Constants.USER_DELAY, 5);
        editor.putBoolean(Constants.VIBRATE_SILENT, false);
        editor.commit();
    }

    private void setNotNewUser(){
        SharedPreferences sharedPreferences =
                getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.NEW_USER, false);
        editor.commit();
    }

    private void vibrateOnSilent(MenuItem item){
        SharedPreferences sharedPreferences =
                getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);
        boolean vibrate = !(sharedPreferences.getBoolean(Constants.VIBRATE_SILENT, true));
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.VIBRATE_SILENT, vibrate);
        editor.commit();
        setVibrateText();
    }

    private void setVibrateText(){
        MenuItem vibrateSilent = mainMenu.findItem(R.id.vibrateOnSilent);
        boolean vibrate = Utils.getVibrateOverride(this);
        if(vibrate){
            vibrateSilent.setTitle("Vibrate when Silent: ON");
        } else {
            vibrateSilent.setTitle("Vibrate when Silent: OFF");
        }
    }
}
