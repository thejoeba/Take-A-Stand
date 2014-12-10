package com.sean.takeastand.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

import com.sean.takeastand.R;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class NotificationsActivity extends Activity {

    private CheckBox chbxLED;
    private CheckBox chbxVibrate;
    private CheckBox chbxSound;
    private TextView txtSilentMode;
    private TextView txtRepeat;
    private TextView txtNotificationFrequency;
    private Switch swSilent;
    private Switch swRepeat;
    private NumberPicker npNotificationTime;

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
        txtSilentMode = (TextView) findViewById(R.id.txtSilentMode);
        txtRepeat = (TextView) findViewById(R.id.txtRepeat);
        txtNotificationFrequency = (TextView) findViewById(R.id.txtNotificationFrequency);
        swSilent = (Switch) findViewById(R.id.toggleSilentMode);
        swSilent.setOnClickListener(silentModeListener);
        swSilent.setChecked(Utils.getVibrateOverride(this));
        swRepeat = (Switch) findViewById(R.id.toggleRepeat);
        swRepeat.setOnClickListener(repeatListener);
        swRepeat.setChecked(Utils.getRepeatAlerts(this));
        npNotificationTime = (NumberPicker) findViewById(R.id.alertNumberPicker);
        npNotificationTime.setMinValue(1);
        npNotificationTime.setMaxValue(60);
        npNotificationTime.setValue(Utils.getDefaultAlertDelay(this));
        npNotificationTime.setWrapSelectorWheel(false);
        npNotificationTime.setOnValueChangedListener(valueChangeListener);

        //Set up grayed views
        setGrayedAreas();
    }

    View.OnClickListener repeatListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(((Switch)view).isChecked()){
                setRepeatAlerts(NotificationsActivity.this, true);
            } else {
                setRepeatAlerts(NotificationsActivity.this, false);
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

    NumberPicker.OnValueChangeListener valueChangeListener = new NumberPicker.OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
            setDefaultAlertDelay(NotificationsActivity.this, newVal);
        }
    };

    View.OnClickListener checkBoxListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            setGrayedAreas();
            setDefaultAlertType(NotificationsActivity.this, new boolean[]{chbxLED.isChecked(),
                    chbxVibrate.isChecked(), chbxSound.isChecked()});
        }

    };

    private void setGrayedAreas(){
        if (Utils.getRepeatAlerts(NotificationsActivity.this)) {
            txtNotificationFrequency.setTextColor(getResources().getColor(android.R.color.primary_text_light));
            npNotificationTime.setEnabled(true);
            npNotificationTime.setAlpha((float)1);
            txtNotificationFrequency.setVisibility(View.VISIBLE);
            npNotificationTime.setVisibility(View.VISIBLE);
        } else {
            txtNotificationFrequency.setVisibility(View.GONE);
            npNotificationTime.setVisibility(View.GONE);
        }
        if (chbxVibrate.isChecked() || chbxSound.isChecked()) {
            txtRepeat.setTextColor(getResources().getColor(android.R.color.primary_text_light));
            txtNotificationFrequency.setTextColor(getResources().getColor(android.R.color.primary_text_light));
            swRepeat.setEnabled(true);
            swRepeat.setAlpha((float)1);
        } else {
            txtRepeat.setTextColor(getResources().getColor(R.color.LightGrey));
            txtNotificationFrequency.setTextColor(getResources().getColor(R.color.LightGrey));
            swRepeat.setEnabled(false);
            swRepeat.setAlpha((float)0.5);
            npNotificationTime.setEnabled(false);
            if(txtNotificationFrequency.getVisibility() != View.GONE){
                txtNotificationFrequency.setTextColor(getResources().getColor(R.color.LightGrey));
                npNotificationTime.setEnabled(false);
                npNotificationTime.setAlpha((float)0.5);
            }
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

    public void setDefaultAlertDelay(Context context, int delay) {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Closes Activity when user presses title
        finish();
        return super.onOptionsItemSelected(item);
    }

    //For Calligraphy font library class
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new CalligraphyContextWrapper(newBase));
    }

}
