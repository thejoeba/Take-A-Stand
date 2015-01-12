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

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.Application;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.PointTarget;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.sean.takeastand.R;
import com.sean.takeastand.alarmprocess.ScheduledRepeatingAlarm;
import com.sean.takeastand.alarmprocess.UnscheduledRepeatingAlarm;
import com.sean.takeastand.storage.FixedAlarmSchedule;
import com.sean.takeastand.storage.ScheduleDatabaseAdapter;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class MainActivity extends ActionBarActivity {
    //ToDo: Recenter layout
    private static final String TAG = "MainActivity";
    private static final String UI_PATH = "com.sean.takeastand.ui.";
    private final static Integer ACTIVITY_NUMBER = 0;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private ArrayList<String> mNavDrawerOptions;
    private ArrayAdapter mListAdapter;
    private MenuItem mPausePlay;
    private int[] pauseTimes = new int[]{5, 10, 15, 20, 25, 30, 45, 60, 75, 90, 105, 120, 135, 150,
            165, 180};
    private Handler mHandler;
    ImageView ivTutorialBlock;
    ShowcaseView showcaseView;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle paramBundle) {
        Log.i(TAG, "onCreate");
//        Log.d(TAG,"Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber());
        super.onCreate(paramBundle);

        sharedPreferences = getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);
        mNavDrawerOptions = new ArrayList<String>();
        //Find out how to initialize an arraylist; then update the arraylist when vibrate status changes
        //or standdtector status changes
        String activitiesArray[] = getResources().getStringArray(R.array.ActivityTitle);
        mNavDrawerOptions.add(activitiesArray[1]);
        mNavDrawerOptions.add(activitiesArray[2]);
        mNavDrawerOptions.add(activitiesArray[3]);
        mNavDrawerOptions.add(activitiesArray[4]);
        mNavDrawerOptions.add(activitiesArray[5]);
//        mNavDrawerOptions.add("Tutorial");
        this.setTitle(activitiesArray[ACTIVITY_NUMBER]);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                toolbar, R.string.app_name, R.string.options) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mListAdapter = new ArrayAdapter(this, R.layout.drawer_list_item, mNavDrawerOptions);
        mDrawerList.setAdapter(mListAdapter);
        mDrawerList.setOnItemClickListener(drawerClickListener);
        mHandler = new Handler();
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        ivTutorialBlock = (ImageView) findViewById(R.id.ivTutorialBlock);
        Tracker t = ((Application) this.getApplication()).getTracker(Application.TrackerName.APP_TRACKER);
        t.setScreenName("Main Activity");
        t.send(new HitBuilders.AppViewBuilder().build());
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    private AdapterView.OnItemClickListener drawerClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Intent intent = new Intent();
            String[] activities = getResources().getStringArray(R.array.ActivityClassName);
            if(position < activities.length - 1) {
                intent.setClassName(getPackageName(), UI_PATH + activities[position + 1]);
                startActivity(intent);
            }
//            else {
//                switch (position) {
//                    case 7:
//                        RunTutorial();
//                        break;
//                }
//            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        Resources resources = getResources();
        switch (item.getItemId()) {
            case R.id.main_help:
                new AlertDialog.Builder(this)
                        .setTitle(resources.getStringArray(R.array.ActivityTitle)[ACTIVITY_NUMBER])
                        .setMessage(resources.getStringArray(R.array.ActivityHelpText)[ACTIVITY_NUMBER])
                        .setPositiveButton(getString(R.string.ok), null)
                        .show();
                break;
            case R.id.pauseplay:
                togglePausePlay();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        Intent intent = new Intent(Constants.MAIN_ACTIVITY_VISIBILITY_STATUS);
        intent.putExtra("Visible", false);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
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
        Intent intent = new Intent(Constants.MAIN_ACTIVITY_VISIBILITY_STATUS);
        intent.putExtra("Visible", true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        invalidateOptionsMenu();
        if(mDrawerLayout.isDrawerOpen(mDrawerList)){
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDrawerLayout.closeDrawers();
                }
            }, 400);
        }

        if (sharedPreferences.getBoolean("RunTutorial", true)) {
            sharedPreferences.edit().putBoolean("RunTutorial", false).commit();
            RunTutorial();
        }
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        mPausePlay = menu.findItem(R.id.pauseplay);
        updatePausePlayIcon();
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        mPausePlay = menu.findItem(R.id.pauseplay);
        if (drawerOpen) {
            mPausePlay.setVisible(false);
        } else {
            updatePausePlayIcon();
        }
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

    private void registerReceivers() {
        LocalBroadcastManager.getInstance(this).registerReceiver(visibilityReceiver,
                new IntentFilter("Visible"));
        LocalBroadcastManager.getInstance(this).registerReceiver(updateActionBarReceiver,
                new IntentFilter(Constants.UPDATE_ACTION_BAR));
    }

    private void unregisterReceivers() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(visibilityReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateActionBarReceiver);
    }

    private BroadcastReceiver visibilityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent newIntent = new Intent(Constants.MAIN_ACTIVITY_VISIBILITY_STATUS);
            newIntent.putExtra("Visible", true);
            LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(newIntent);
        }
    };

    private BroadcastReceiver updateActionBarReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received intent");
            invalidateOptionsMenu();
        }
    };

    private void togglePausePlay() {
        int status = Utils.getImageStatus(this);
        if (status == Constants.NON_SCHEDULE_ALARM_RUNNING || status == Constants.NON_SCHEDULE_STOOD_UP ||
                status == Constants.NON_SCHEDULE_TIME_TO_STAND) {
            mPausePlay.setIcon(getResources().getDrawable(R.drawable.ic_action_play));
            showPauseSettingsDialog();
        } else if (status == Constants.SCHEDULE_RUNNING || status == Constants.SCHEDULE_STOOD_UP ||
                status == Constants.SCHEDULE_TIME_TO_STAND) {
            mPausePlay.setIcon(getResources().getDrawable(R.drawable.ic_action_play));
            showPauseSettingsDialog();
        } else if (status == Constants.NON_SCHEDULE_PAUSED) {
            unPauseUnscheduled();
            mPausePlay.setIcon(getResources().getDrawable(R.drawable.ic_action_pause));
            sendAnalyticsEvent("Unpaused");
        } else if (status == Constants.SCHEDULE_PAUSED) {
            unPauseScheduled();
            mPausePlay.setIcon(getResources().getDrawable(R.drawable.ic_action_pause));
            sendAnalyticsEvent("Unpaused");
        }
    }

    private void showPauseSettingsDialog() {
        LayoutInflater inflater = getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = inflater.inflate(R.layout.dialog_pause, null);
        TextView title = new TextView(this);
        title.setPadding(50, 50, 50, 50);
        title.setTextSize(22);
        title.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
        title.setText(getResources().getString(R.string.select_pause_type));
        builder.setCustomTitle(title);
        String[] valueSet = new String[pauseTimes.length];
        for (int i = 0; i < pauseTimes.length; i++) {
            valueSet[i] = Integer.toString(pauseTimes[i]);
        }
        builder.setView(dialogView);
        final NumberPicker npPause = (NumberPicker) dialogView.findViewById(R.id.pauseNumberPicker);
        npPause.setDisplayedValues(valueSet);
        npPause.setMinValue(0);
        npPause.setMaxValue(valueSet.length - 1);
        npPause.setWrapSelectorWheel(false);
        int initialValue = Utils.getDefaultPauseAmount(this);
        for (int i = 0; i < pauseTimes.length; i++) {
            if (initialValue == pauseTimes[i]) {
                initialValue = i;
            }
        }
        npPause.setValue(initialValue);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //getValue returns the index number, so need to do some math to correct correct
                //actual value
                setDefaultPauseAmount(pauseTimes[npPause.getValue()]);
                int currentStatus = Utils.getImageStatus(MainActivity.this);
                if (currentStatus == Constants.NON_SCHEDULE_ALARM_RUNNING ||
                        currentStatus == Constants.NON_SCHEDULE_STOOD_UP ||
                        currentStatus == Constants.NON_SCHEDULE_TIME_TO_STAND) {
                    pauseUnscheduled();
                } else {
                    pauseSchedule();
                    sendAnalyticsEvent("Paused");
                }
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mPausePlay.setIcon(getResources().getDrawable(R.drawable.ic_action_pause));
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void updatePausePlayIcon() {
        if (mPausePlay != null) {
            int currentImageStatus = Utils.getImageStatus(this);
            if (currentImageStatus != Constants.NO_ALARM_RUNNING &&
                    currentImageStatus != Constants.NON_SCHEDULE_PAUSED &&
                    currentImageStatus != Constants.SCHEDULE_PAUSED) {
                mPausePlay.setVisible(true);
                mPausePlay.setIcon(getResources().getDrawable(R.drawable.ic_action_pause));
                if (sharedPreferences.getBoolean("PausePlayTutorial", true)) {
                    sharedPreferences.edit().putBoolean("PausePlayTutorial", false).commit();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tutorialPausePlay();
                        }
                    }, 1000);
                }
            } else if (currentImageStatus == Constants.SCHEDULE_PAUSED ||
                    currentImageStatus == Constants.NON_SCHEDULE_PAUSED) {
                mPausePlay.setVisible(true);
                mPausePlay.setIcon(getResources().getDrawable(R.drawable.ic_action_play));
            } else {
                mPausePlay.setVisible(false);
            }
        } else {
            Log.i(TAG, "null");
        }
    }

    private void pauseUnscheduled() {
        new UnscheduledRepeatingAlarm(this).pause();
    }

    private void pauseSchedule() {
        int currentRunningAlarmUID = Utils.getRunningScheduledAlarm(this);
        ScheduleDatabaseAdapter scheduleDatabaseAdapter = new ScheduleDatabaseAdapter(this);
        FixedAlarmSchedule currentAlarmSchedule =
                new FixedAlarmSchedule(
                        scheduleDatabaseAdapter.getSpecificAlarmSchedule(currentRunningAlarmUID));
        new ScheduledRepeatingAlarm(this, currentAlarmSchedule).pause();
    }

    private void unPauseUnscheduled() {
        new UnscheduledRepeatingAlarm(this).unpause();
    }

    private void unPauseScheduled() {
        int currentRunningAlarmUID = Utils.getRunningScheduledAlarm(this);
        ScheduleDatabaseAdapter scheduleDatabaseAdapter = new ScheduleDatabaseAdapter(this);
        FixedAlarmSchedule currentAlarmSchedule =
                new FixedAlarmSchedule(
                        scheduleDatabaseAdapter.getSpecificAlarmSchedule(currentRunningAlarmUID));
        new ScheduledRepeatingAlarm(this, currentAlarmSchedule).unpause();
    }

    private void setDefaultPauseAmount(int pauseAmount) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.PAUSE_TIME, pauseAmount);
        editor.commit();
    }

    //ToDo:ReEnable this, but troubleshoot it, it doesn't work?
    //For Calligraphy font library class
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new CalligraphyContextWrapper(newBase));
    }

    private void sendAnalyticsEvent(String action) {
        Tracker t = ((Application) this.getApplication()).getTracker(
                Application.TrackerName.APP_TRACKER);
        t.enableAdvertisingIdCollection(true);
        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder()
                .setCategory(Constants.UI_EVENT)
                .setAction(action)
                .build());
    }

    private void RunTutorial() {
        tutorialStart();
        tutorialManualStart();
    }

    private void tutorialWelcome() {
        showcaseView = new ShowcaseView.Builder(this)
                .setTarget(new ViewTarget(R.id.alarm_fragment, this))
                .setStyle(R.style.Tutorial)
                .setContentTitle("Take A Stand")
                .setContentText(getResources().getTextArray(R.array.Tutorial)[0])
                .hideOnTouchOutside()
                .setShowcaseEventListener(new OnShowcaseEventListener() {
                    @Override
                    public void onShowcaseViewHide(ShowcaseView showcaseView) {
                        tutorialManualStart();
                    }

                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {

                    }

                    @Override
                    public void onShowcaseViewShow(ShowcaseView showcaseView) {

                    }
                })
                .build();
        showcaseView.hideButton();
    }

    private void tutorialManualStart() {
        showcaseView = new ShowcaseView.Builder(this)
                .setTarget(new ViewTarget(R.id.spaceTutorialStartReminder, this))
                .setStyle(R.style.Tutorial)
                .setContentTitle("Start Stand Reminder")
                .setContentText(getResources().getTextArray(R.array.Tutorial)[1])
                .hideOnTouchOutside()
                .setShowcaseEventListener(new OnShowcaseEventListener() {
                    @Override
                    public void onShowcaseViewHide(ShowcaseView showcaseView) {
                        tutorialMenuFragment();
                    }

                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {

                    }

                    @Override
                    public void onShowcaseViewShow(ShowcaseView showcaseView) {

                    }
                })
                .build();
        showcaseView.hideButton();
    }

    private void tutorialMenuFragment() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        }, 800);
        int actionBarHeight = getSupportActionBar().getHeight();
        showcaseView = new ShowcaseView.Builder(MainActivity.this)
                .setTarget(new PointTarget(actionBarHeight / 2, actionBarHeight / 2))
                .setStyle(R.style.Tutorial)
                .setContentTitle("Other options")
                .setContentText(getResources().getTextArray(R.array.Tutorial)[2])
                .hideOnTouchOutside()
                .setShowcaseEventListener(new OnShowcaseEventListener() {
                    @Override
                    public void onShowcaseViewHide(ShowcaseView showcaseView) {
                        mDrawerLayout.closeDrawers();
                        tutorialHelpButton();
                    }

                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {

                    }

                    @Override
                    public void onShowcaseViewShow(ShowcaseView showcaseView) {

                    }
                })
                .build();
        showcaseView.hideButton();
    }

    private void tutorialHelpButton() {
        showcaseView = new ShowcaseView.Builder(MainActivity.this)
                .setTarget(new ViewTarget(R.id.main_help, this))
                .setStyle(R.style.Tutorial)
                .setContentTitle("More Info")
                .setContentText(getResources().getTextArray(R.array.Tutorial)[3])
                .hideOnTouchOutside()
                .setShowcaseEventListener(new OnShowcaseEventListener() {
                    @Override
                    public void onShowcaseViewHide(ShowcaseView showcaseView) {
                        sharedPreferences.edit().putBoolean("PausePlayTutorial", true).commit();
                        tutorialFinish();
                        if (!sharedPreferences.getBoolean(Constants.GOOGLE_FIT_ENABLED, false)) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle(getString(R.string.prompt_fit_title))
                                    .setMessage(getString(R.string.prompt_fit_text))
                                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                                            intent.setAction(Constants.CONNECT_FIT);
                                            startActivity(intent);
                                        }
                                    })
                                    .setNegativeButton(getString(R.string.no), null)
                                    .show();
                        }
                    }

                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                    }

                    @Override
                    public void onShowcaseViewShow(ShowcaseView showcaseView) {
                    }
                })
                .build();
        showcaseView.hideButton();
    }

    private void tutorialStart() {
        mDrawerLayout.closeDrawers();
        //ToDo: Lock orientation
//        setRequestedOrientation(getResources().getConfiguration().orientation);
        ivTutorialBlock.setVisibility(View.VISIBLE);

        ivTutorialBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showcaseView.hide();
            }
        });

    }

    private void tutorialFinish() {
        ivTutorialBlock.setVisibility(View.GONE);
    }

    private void tutorialPausePlay() {
        tutorialStart();
        showcaseView = new ShowcaseView.Builder(this)
                .setTarget(new ViewTarget(R.id.pauseplay, this))
                .setStyle(R.style.Tutorial)
                .setContentTitle("Pause Reminder")
                .setContentText(getResources().getTextArray(R.array.Tutorial)[4])
                .hideOnTouchOutside()
                .setShowcaseEventListener(new OnShowcaseEventListener() {
                    @Override
                    public void onShowcaseViewHide(ShowcaseView showcaseView) {
                        tutorialFinish();
                    }

                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {

                    }

                    @Override
                    public void onShowcaseViewShow(ShowcaseView showcaseView) {

                    }
                })
                .build();
        showcaseView.hideButton();
    }
}
