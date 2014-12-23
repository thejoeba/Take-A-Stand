package com.sean.takeastand.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.heckbot.standdtector.StandDtectorTM;
import com.sean.takeastand.R;
import com.sean.takeastand.util.Constants;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class StandDtectorTMSettings extends ActionBarActivity {
    private Switch toggleDeviceStepCounter;
    private Switch toggleWearStepCounter;
    private Switch toggleStandDtectorTM;
    private Button btnCalibrate;
    private TextView txtCalibratedValue;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standdtectortm_settings);
        setUpLayout();
    }

    private void setUpLayout() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.standdtectortm_settings_toolbar);
        setSupportActionBar(toolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavUtils.navigateUpFromSameTask(StandDtectorTMSettings.this);
                }
            });
        }

        sharedPreferences = getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);

        toggleDeviceStepCounter = (Switch) findViewById(R.id.toggleDeviceStepCounter);
        toggleDeviceStepCounter.setOnClickListener(StepCounterListener);
        toggleDeviceStepCounter.setChecked(sharedPreferences.getBoolean(Constants.DEVICE_STEP_DETECTOR_ENABLED, false));
        toggleWearStepCounter = (Switch) findViewById(R.id.toggleWearStepCounter);
        toggleWearStepCounter.setOnClickListener(WearStepCounterListener);
        toggleWearStepCounter.setChecked(sharedPreferences.getBoolean(Constants.WEAR_STEP_DETECTOR_ENABLED, false));
        toggleStandDtectorTM = (Switch) findViewById(R.id.toggleStandDtectorTM);
        toggleStandDtectorTM.setOnClickListener(StandDtectorTMListener);
        toggleStandDtectorTM.setChecked(sharedPreferences.getBoolean(Constants.STANDDTECTORTM_ENABLED, false));
        btnCalibrate = (Button) findViewById(R.id.btnCalibrate);
        btnCalibrate.setOnClickListener(CalibrateListener);
        txtCalibratedValue = (TextView) findViewById(R.id.txtCalibratedValue);
        //ToDo: look into getPackageName for all vars
        txtCalibratedValue.setText("Calibrated Value: " + getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).getFloat("CALIBRATEDVARIATION", 0));
        FeatureCheck();
    }

    private void FeatureCheck() {
        if (!getPackageManager().hasSystemFeature(getPackageManager().FEATURE_SENSOR_STEP_COUNTER)) {
            toggleDeviceStepCounter.setChecked(false);
            toggleDeviceStepCounter.setEnabled(false);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(Constants.DEVICE_STEP_DETECTOR_ENABLED, false);
            editor.commit();
        }

        boolean wear_installed = false;
        try {
            getPackageManager().getPackageInfo("com.google.android.wearable.app", PackageManager.GET_ACTIVITIES);
            wear_installed = true;
        } catch (Exception e) {
        }

        if (!wear_installed) {
            toggleWearStepCounter.setChecked(false);
            toggleWearStepCounter.setEnabled(false);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(Constants.WEAR_STEP_DETECTOR_ENABLED, false);
            editor.commit();
        }

    }

    View.OnClickListener StepCounterListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(Constants.DEVICE_STEP_DETECTOR_ENABLED, ((Switch) view).isChecked());
            editor.commit();
        }
    };

    View.OnClickListener WearStepCounterListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(Constants.WEAR_STEP_DETECTOR_ENABLED, ((Switch) view).isChecked());
            editor.commit();
        }
    };

    View.OnClickListener StandDtectorTMListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(Constants.STANDDTECTORTM_ENABLED, ((Switch) view).isChecked());
            editor.commit();
        }
    };

    View.OnClickListener CalibrateListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            setRequestedOrientation(getResources().getConfiguration().orientation);
            new AlertDialog.Builder(StandDtectorTMSettings.this)
                    .setTitle(getString(R.string.calibration))
                    .setMessage(getString(R.string.calibration_instructions))
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent calibrationIntent = new Intent(StandDtectorTMSettings.this, StandDtectorTM.class);
                            calibrationIntent.setAction("StandDtectorTMCalibrate");
                            Intent intent = new Intent("CalibrationFinished");
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                                    0, intent, PendingIntent.FLAG_ONE_SHOT);
                            calibrationIntent.putExtra("pendingIntent", pendingIntent);
                            startService(calibrationIntent);
                            getApplicationContext().registerReceiver(calibrationFinishedReceiver, new IntentFilter("CalibrationFinished"));
                            btnCalibrate.setEnabled(false);
                            btnCalibrate.setText("Calibrating");
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        }
    };

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

    private BroadcastReceiver calibrationFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("StandDtectorTMSettings", "Calibration Finished");
            LocalBroadcastManager.getInstance(StandDtectorTMSettings.this).unregisterReceiver(calibrationFinishedReceiver);
            if (intent.getExtras().getString("Results").equals("Success")) {
                txtCalibratedValue.setText("New Calibrated Value: " + getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).getFloat("CALIBRATEDVARIATION", 0));
            } else {
                LocalBroadcastManager.getInstance(StandDtectorTMSettings.this).unregisterReceiver(calibrationFinishedReceiver);
                txtCalibratedValue.setText("Calibration Failed");
            }
            btnCalibrate.setEnabled(true);
            btnCalibrate.setText("Calibrate");
        }
    };

}
