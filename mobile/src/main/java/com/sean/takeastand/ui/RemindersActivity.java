package com.sean.takeastand.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.sean.takeastand.R;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class RemindersActivity extends Activity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        setUpLayout();
    }

    private void setUpLayout() {
        ActionBar actionBar = getActionBar();
        //Is possible actionBar will be null
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.notification_settings));
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
            if(((Switch)view).isChecked()){
                setRepeatAlerts(RemindersActivity.this, true);
            } else {
                setRepeatAlerts(RemindersActivity.this, false);
            }
            setGrayedAreas();
        }
    };

    View.OnClickListener silentModeListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            silentMode(((Switch)view).isChecked());
        }
    };

    View.OnClickListener reminderFrequencyListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            LayoutInflater inflater = getLayoutInflater();
            AlertDialog.Builder builder = new AlertDialog.Builder(RemindersActivity.this);
            View dialogView = inflater.inflate(R.layout.dialog_number_picker, null);
            builder.setView(dialogView);
            TextView title = new TextView(RemindersActivity.this);
            title.setPadding(50, 50, 50, 50);
            title.setTextSize(22);
            title.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
            title.setText(getResources().getString(R.string.reminder_frequency));
            builder.setCustomTitle(title);
            final NumberPicker numberPicker = (NumberPicker)dialogView.findViewById(R.id.numberPicker);
            numberPicker.setMaxValue(120);
            numberPicker.setMinValue(2);
            numberPicker.setValue(Utils.getDefaultFrequency(RemindersActivity.this));
            numberPicker.setWrapSelectorWheel(false);
            builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    int frequency = numberPicker.getValue();
                    setDefaultFrequency(frequency);
                    txtReminderFrequency.setText(Integer.toString(frequency) +
                            getString(R.string.minutes));
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
            setGrayedAreas();
            setDefaultAlertType(RemindersActivity.this, new boolean[]{chbxLED.isChecked(),
                    chbxVibrate.isChecked(), chbxSound.isChecked()});
        }

    };

    View.OnClickListener notificationFrequencyListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            LayoutInflater inflater = getLayoutInflater();
            AlertDialog.Builder builder = new AlertDialog.Builder(RemindersActivity.this);
            View dialogView = inflater.inflate(R.layout.dialog_number_picker, null);
            builder.setView(dialogView);
            TextView title = new TextView(RemindersActivity.this);
            title.setPadding(50, 50, 50, 50);
            title.setTextSize(22);
            title.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
            title.setText(getResources().getString(R.string.notification_reminder_frequency));
            builder.setCustomTitle(title);
            final NumberPicker numberPicker = (NumberPicker)dialogView.findViewById(R.id.numberPicker);
            numberPicker.setMaxValue(60);
            numberPicker.setMinValue(1);
            numberPicker.setValue(Utils.getNotificationReminderFrequency(RemindersActivity.this));
            numberPicker.setWrapSelectorWheel(false);
            builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    int frequency = numberPicker.getValue();
                    setNotificationReminderFrequency(RemindersActivity.this, frequency);
                    txtNotificationFrequency.setText(getString(R.string.every) + " " + setMinutes(frequency));
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

    private void setGrayedAreas(){
        if (Utils.getRepeatAlerts(RemindersActivity.this)) {
            //txtNotificationFrequencyTitle.setTextColor(getResources().getColor(android.R.color.primary_text_light));
            txtNotificationFrequency.setTextColor(getResources().getColor(android.R.color.primary_text_light));
            rlNotificationFrequency.setVisibility(View.VISIBLE);
        } else {
            rlNotificationFrequency.setVisibility(View.GONE);
        }
        if (chbxVibrate.isChecked() || chbxSound.isChecked()) {
            txtRepeat.setTextColor(getResources().getColor(android.R.color.primary_text_light));
            //txtNotificationFrequencyTitle.setTextColor(getResources().getColor(android.R.color.primary_text_light));
            txtNotificationFrequency.setTextColor(getResources().getColor(android.R.color.primary_text_light));
            swRepeat.setEnabled(true);
            swRepeat.setAlpha((float)1);
        } else {
            txtRepeat.setTextColor(getResources().getColor(R.color.LightGrey));
            //txtNotificationFrequencyTitle.setTextColor(getResources().getColor(R.color.LightGrey));
            swRepeat.setEnabled(false);
            swRepeat.setAlpha((float)0.5);
            txtNotificationFrequency.setTextColor(getResources().getColor(R.color.LightGrey));
        }
        if (chbxVibrate.isChecked()){
            txtSilentMode.setTextColor(getResources().getColor(android.R.color.primary_text_light));
            swSilent.setEnabled(true);
            swSilent.setAlpha((float)1);
        } else {
            txtSilentMode.setTextColor(getResources().getColor(R.color.LightGrey));
            swSilent.setEnabled(false);
            swSilent.setAlpha((float)0.5);
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

    private void setDefaultFrequency(int frequency){
        SharedPreferences sharedPreferences =
                getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.USER_FREQUENCY, frequency);
        editor.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Closes Activity when user presses title
        finish();
        return super.onOptionsItemSelected(item);
    }

    private String setMinutes(int minutes){
        if(minutes > 1 ){
            return Integer.toString(minutes) + getString(R.string.minutes);
        } else {
            return getString(R.string.minute);
        }
    }

    //For Calligraphy font library class
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new CalligraphyContextWrapper(newBase));
    }

}
