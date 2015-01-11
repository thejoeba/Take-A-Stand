package com.sean.takeastand.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.Application;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.sean.takeastand.R;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ReminderSettingsActivity extends ActionBarActivity {

    private CheckBox chbxLED;
    private CheckBox chbxVibrate;
    private CheckBox chbxSound;
    private TextView txtSilentMode;
    private TextView txtRepeat;
    private TextView txtReminderFrequency;
    private TextView txtNotificationFrequency;
    private RelativeLayout rlReminderFrequency;
    private RelativeLayout rlNotificationFrequency;
    private Switch swSilent;
    private Switch swRepeat;
    private boolean mNotificationAlertChanged;
    private final static Integer ACTIVITY_NUMBER = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_settings);
        this.setTitle(getResources().getStringArray(R.array.ActivityTitle)[ACTIVITY_NUMBER]);
        setUpLayout();
        Tracker t = ((Application) this.getApplication()).getTracker(Application.TrackerName.APP_TRACKER);
        t.setScreenName("Reminder Settings");
        t.send(new HitBuilders.AppViewBuilder().build());
        mNotificationAlertChanged = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.help_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Resources resources = getResources();
        if (item.getItemId() ==  R.id.help) {
                new AlertDialog.Builder(this)
                        .setTitle(resources.getStringArray(R.array.ActivityTitle)[ACTIVITY_NUMBER])
                        .setMessage(resources.getStringArray(R.array.ActivityHelpText)[ACTIVITY_NUMBER])
                        .setPositiveButton(getString(R.string.ok), null)
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show();
        }
        else {
            //Closes Activity when user presses title
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpLayout() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.reminder_settings_toolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
        boolean[] currentNotification = Utils.getDefaultAlertType(this);
        chbxLED = (CheckBox) findViewById(R.id.chbxLED);
        chbxLED.setChecked(currentNotification[0]);
        chbxLED.setOnClickListener(checkBoxListener);
        chbxVibrate = (CheckBox) findViewById(R.id.chbxVibrate);
        chbxVibrate.setChecked(currentNotification[1]);
        chbxVibrate.setOnClickListener(checkBoxListener);
        chbxSound = (CheckBox) findViewById(R.id.chbxSound);
        chbxSound.setChecked(currentNotification[2]);
        chbxSound.setOnClickListener(checkBoxListener);
        txtSilentMode = (TextView) findViewById(R.id.tvStepCounter);
        txtRepeat = (TextView) findViewById(R.id.txtRepeat);
        //txtNotificationFrequencyTitle = (TextView) findViewById(R.id.txtNotificationFrequencyTitle);
        txtNotificationFrequency = (TextView) findViewById(R.id.txtNotificationFrequencyTitle);
        int notifFrequency = Utils.getNotificationReminderFrequency(this);
        txtNotificationFrequency.setText(getString(R.string.every) + " " + setMinutes(notifFrequency));
        txtReminderFrequency = (TextView) findViewById(R.id.txtReminderFrequency);
        txtReminderFrequency.setText(setMinutes(Utils.getDefaultFrequency(this)));
        rlReminderFrequency = (RelativeLayout) findViewById(R.id.reminderFrequency);
        rlReminderFrequency.setOnClickListener(reminderFrequencyListener);
        rlNotificationFrequency = (RelativeLayout) findViewById(R.id.notificationFrequency);
        rlNotificationFrequency.setOnClickListener(notificationFrequencyListener);
        swSilent = (Switch) findViewById(R.id.toggleSilentMode);
        swSilent.setOnClickListener(silentModeListener);
        swSilent.setChecked(Utils.getVibrateOverride(this));
        swRepeat = (Switch) findViewById(R.id.toggleRepeat);
        swRepeat.setOnClickListener(repeatListener);
        swRepeat.setChecked(Utils.getRepeatAlerts(this));
        //Set up grayed views
        setGrayedAreas();
    }

    View.OnClickListener repeatListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (((Switch) view).isChecked()) {
                setRepeatAlerts(ReminderSettingsActivity.this, true);
                sendAnalyticsEvent("Notification Reminders Repeat: On");
            } else {
                setRepeatAlerts(ReminderSettingsActivity.this, false);
                sendAnalyticsEvent("Notification Reminders Repeat: Off");
            }
            setGrayedAreas();
        }
    };

    View.OnClickListener silentModeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean silentModeOn = ((Switch) view).isChecked();
            silentMode(silentModeOn);
            if (silentModeOn) {
                sendAnalyticsEvent("Override silent: On");
            } else {
                sendAnalyticsEvent("Override silent: Off");
            }
        }
    };

    View.OnClickListener reminderFrequencyListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            LayoutInflater inflater = getLayoutInflater();
            AlertDialog.Builder builder = new AlertDialog.Builder(ReminderSettingsActivity.this);
            View dialogView = inflater.inflate(R.layout.dialog_number_picker, null);
            builder.setView(dialogView);
            TextView title = new TextView(ReminderSettingsActivity.this);
            title.setPadding(50, 50, 50, 50);
            title.setTextSize(22);
            title.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
            title.setText(getResources().getString(R.string.reminder_frequency));
            builder.setCustomTitle(title);
            final NumberPicker numberPicker = (NumberPicker) dialogView.findViewById(R.id.numberPicker);
            numberPicker.setMaxValue(120);
            numberPicker.setMinValue(2);
            numberPicker.setValue(Utils.getDefaultFrequency(ReminderSettingsActivity.this));
            numberPicker.setWrapSelectorWheel(false);
            builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    int frequency = numberPicker.getValue();
                    setDefaultFrequency(frequency);
                    txtReminderFrequency.setText(Integer.toString(frequency) +
                            getString(R.string.minutes));
                    sendAnalyticsEvent("Stand reminder frequency " + Integer.toString(frequency));
                    dialogInterface.dismiss();
                }
            });
            builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    };

    View.OnClickListener checkBoxListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mNotificationAlertChanged = true;
            setGrayedAreas();
            setDefaultAlertType(ReminderSettingsActivity.this, new boolean[]{chbxLED.isChecked(),
                    chbxVibrate.isChecked(), chbxSound.isChecked()});
        }

    };

    View.OnClickListener notificationFrequencyListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            LayoutInflater inflater = getLayoutInflater();
            AlertDialog.Builder builder = new AlertDialog.Builder(ReminderSettingsActivity.this);
            View dialogView = inflater.inflate(R.layout.dialog_number_picker, null);
            builder.setView(dialogView);
            TextView title = new TextView(ReminderSettingsActivity.this);
            title.setPadding(50, 50, 50, 50);
            title.setTextSize(22);
            title.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
            title.setText(getResources().getString(R.string.notification_reminder_frequency));
            builder.setCustomTitle(title);
            final NumberPicker numberPicker = (NumberPicker) dialogView.findViewById(R.id.numberPicker);
            numberPicker.setMaxValue(60);
            numberPicker.setMinValue(1);
            numberPicker.setValue(Utils.getNotificationReminderFrequency(ReminderSettingsActivity.this));
            numberPicker.setWrapSelectorWheel(false);
            builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    int frequency = numberPicker.getValue();
                    setNotificationReminderFrequency(ReminderSettingsActivity.this, frequency);
                    txtNotificationFrequency.setText(getString(R.string.every) + " " + setMinutes(frequency));
                    sendAnalyticsEvent("Notification Reminder Frequency " + Integer.toString(frequency));
                    dialogInterface.dismiss();
                }
            });
            builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    };

    private void setGrayedAreas() {
        if (Utils.getRepeatAlerts(ReminderSettingsActivity.this)) {
            //txtNotificationFrequencyTitle.setTextColor(getResources().getColor(android.R.color.primary_text_light));
            txtNotificationFrequency.setTextColor(getResources().getColor(android.R.color.primary_text_light));
            rlNotificationFrequency.setVisibility(View.VISIBLE);
        } else {
            rlNotificationFrequency.setVisibility(View.GONE);
        }
        if (chbxVibrate.isChecked()) {
            txtSilentMode.setTextColor(getResources().getColor(android.R.color.primary_text_light));
            swSilent.setEnabled(true);
            swSilent.setAlpha((float) 1);
        } else {
            txtSilentMode.setTextColor(getResources().getColor(R.color.LightGrey));
            swSilent.setEnabled(false);
            swSilent.setAlpha((float) 0.5);
        }
    }

    private void silentMode(boolean notificationWhenSilent) {
        SharedPreferences sharedPreferences =
                getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.VIBRATE_SILENT, notificationWhenSilent);
        editor.commit();
    }

    public void setDefaultAlertType(Context context, boolean[] alertType) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.USER_ALERT_LED, alertType[0]);
        editor.putBoolean(Constants.USER_ALERT_VIBRATE, alertType[1]);
        editor.putBoolean(Constants.USER_ALERT_SOUND, alertType[2]);
        editor.commit();
    }

    public void setNotificationReminderFrequency(Context context, int delay) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.USER_DELAY, delay);
        editor.commit();
    }

    public void setRepeatAlerts(Context context, boolean repeat) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.USER_ALERT_FREQUENCY, repeat);
        editor.commit();
    }

    private void setDefaultFrequency(int frequency) {
        SharedPreferences sharedPreferences =
                getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.USER_FREQUENCY, frequency);
        editor.commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mNotificationAlertChanged) {
            boolean[] alertTypes = Utils.getDefaultAlertType(this);
            String newAlert = "Alert Type: ";
            newAlert += Boolean.toString(alertTypes[0]) + "-" + Boolean.toString(alertTypes[1]) + "-"
                    + Boolean.toString(alertTypes[2]);
            sendAnalyticsEvent(newAlert);
        }
    }

    private String setMinutes(int minutes) {
        if (minutes == 1) {
            return Integer.toString(minutes) + getString(R.string.minute);
        } else {
            return Integer.toString(minutes) + getString(R.string.minutes);
        }
    }

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
}
