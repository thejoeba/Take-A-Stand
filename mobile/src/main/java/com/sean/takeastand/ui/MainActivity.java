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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.NumberPicker;

import com.heckbot.standdetector.StandDtectorTM;
import com.sean.takeastand.R;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;

import java.lang.reflect.Array;


public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private Menu mainMenu;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle paramBundle)
    {
        super.onCreate(paramBundle);
        //deleteDatabase("alarms_database");
        //Utils.setImageStatus(this, Constants.NO_ALARM_RUNNING);
        String[] sample_menu = {"About App", "Default Settings", "Science Behind App"};
        setContentView(R.layout.activity_main);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_closed) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle("Take A Stand");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle("Settings");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerList = (ListView)findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, sample_menu));
        //mDrawerList.setOnItemClickListener();

        //Navigation Drawer icon won't display without this
        getActionBar().setDisplayHomeAsUpEnabled(true);
        //Styling
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        mainMenu = menu;
        setVibrateText();
        setStandDetectorMenuText();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch(item.getItemId()){
            case R.id.schedules:
                Intent intent = new Intent(this, ScheduleListActivity.class);
                startActivity(intent);
                break;
            case R.id.default_frequency:
                showNumberPickerDialog(Utils.getDefaultFrequency(this), 1 , 100,
                        "Select Default Frequency", true);
                break;
            case R.id.default_alert_type:
                showAlertTypePicker();
                break;
            case R.id.default_delay_length:
                showNumberPickerDialog(Utils.getDefaultDelay(this), 1,
                        60,
                        "Select Default Delay Length", false);
                break;
            case R.id.science:
                Intent intentScience = new Intent(this, ScienceActivity.class);
                startActivity(intentScience);
                break;
            case R.id.vibrateOnSilent:
                vibrateOnSilent();
                break;
            case R.id.calibrate:
                calibrateStandDetector();
                break;
            case R.id.toggle_standdetector:
                toggleStandDetector();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleStandDetector() {
        // shared preferences declared on create
        // skip declaring boolean and just drop it into the editor
        SharedPreferences sharedPreferences =
                getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);
        boolean bStandDetector = !(sharedPreferences.getBoolean(Constants.STAND_DETECTOR, false));
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.STAND_DETECTOR, bStandDetector);
        editor.commit();
        setStandDetectorMenuText();
    }

    private void setStandDetectorMenuText(){
        MenuItem miStandDetector = mainMenu.findItem(R.id.toggle_standdetector);
        SharedPreferences sharedPreferences =
                getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);
        boolean bStandDetector = (sharedPreferences.getBoolean(Constants.STAND_DETECTOR, false));
        if(bStandDetector){
            miStandDetector.setTitle("StandDtector™: ON");
        } else {
            miStandDetector.setTitle("StandDtector™: OFF");
        }
    }

    private void calibrateStandDetector() {
        new AlertDialog.Builder(this)
                .setTitle("Calibration")
                .setMessage("To calibrate, the phone must be in your pocket and you must be sitting. The phone will Vibrate once, indicating you should stand. Once calibration is complete, the phone will vibrate again. Once you press OK, you will have 5 seconds to put the phone in your pocket and be sitting before you feel the first vibration.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent calibrationIntent = new Intent(MainActivity.this, StandDtectorTM.class);
                        calibrationIntent.putExtra("ACTION", "CALIBRATE");

                        Intent intent = new Intent(MainActivity.this, com.heckbot.standdetector.MyBroadcastReceiver.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
                        calibrationIntent.putExtra("pendingIntent", pendingIntent);

                        startService(calibrationIntent);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.schedules).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
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
                    setDefaultFrequency(MainActivity.this, numberPicker.getValue());
                } else {
                    setDefaultDelay(MainActivity.this, numberPicker.getValue());
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
                setDefaultAlertType(MainActivity.this, notificationTypes);
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

    private void vibrateOnSilent(){
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

    public static void setDefaultAlertType(Context context, int[] alertType){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.USER_ALERT_TYPE, Utils.convertIntArrayToString(alertType));
        editor.commit();
    }

    public static void setDefaultFrequency(Context context, int frequency){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.USER_FREQUENCY, frequency);
        editor.commit();
    }

    public static void setDefaultDelay(Context context, int delay){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.USER_DELAY, delay);
        editor.commit();
    }
}
