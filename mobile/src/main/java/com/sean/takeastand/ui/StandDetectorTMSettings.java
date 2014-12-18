package com.sean.takeastand.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.heckbot.standdtector.StandDtectorBroadcastReceiver;
import com.heckbot.standdtector.StandDtectorTM;
import com.sean.takeastand.R;
import com.sean.takeastand.util.Constants;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class StandDetectorTMSettings extends Activity {
    private Switch toggleStepCounter;
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
        ActionBar actionBar = getActionBar();
        //Is possible actionBar will be null
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.standdtectortm_settings));
        }

        sharedPreferences = getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);

        toggleStepCounter = (Switch) findViewById(R.id.toggleStepCounter);
        toggleStepCounter.setOnClickListener(StepCounterListener);
        toggleStepCounter.setChecked(sharedPreferences.getBoolean(Constants.STEP_DETECTOR_ENABLED, false));
        toggleWearStepCounter = (Switch) findViewById(R.id.toggleWearStepCounter);
        toggleWearStepCounter.setOnClickListener(WearStepCounterListener);
        toggleWearStepCounter.setChecked(sharedPreferences.getBoolean(Constants.WEAR_STEP_DETECTOR_ENABLED, false));
        toggleStandDtectorTM = (Switch) findViewById(R.id.toggleStandDtectorTM);
        toggleStandDtectorTM.setOnClickListener(StandDtectorTMListener);
        toggleStandDtectorTM.setChecked(sharedPreferences.getBoolean(Constants.STANDDTECTORTM_ENABLED, false));
        btnCalibrate = (Button) findViewById(R.id.btnCalibrate);
        btnCalibrate.setOnClickListener(CalibrateListener);
        txtCalibratedValue = (TextView) findViewById(R.id.txtCalibratedValue);
        txtCalibratedValue.setText("Calibrated Value: " + getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).getFloat("CALIBRATEDVARIATION", 0));
    }

    View.OnClickListener StepCounterListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(Constants.STEP_DETECTOR_ENABLED, ((Switch)view).isChecked());
            editor.commit();
        }
    };

    View.OnClickListener WearStepCounterListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(Constants.WEAR_STEP_DETECTOR_ENABLED, ((Switch)view).isChecked());
            editor.commit();
        }
    };

    View.OnClickListener StandDtectorTMListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(Constants.STANDDTECTORTM_ENABLED, ((Switch)view).isChecked());
            editor.commit();
        }
    };

    View.OnClickListener CalibrateListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            setRequestedOrientation(getResources().getConfiguration().orientation);
                new AlertDialog.Builder(StandDetectorTMSettings.this)
                        .setTitle(getString(R.string.calibration))
                        .setMessage(getString(R.string.calibration_instructions))
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent calibrationIntent = new Intent(StandDetectorTMSettings.this, StandDtectorTM.class);
                                calibrationIntent.setAction("CALIBRATE");
                                Intent intent = new Intent(StandDetectorTMSettings.this, StandDtectorBroadcastReceiver.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                PendingIntent pendingIntent = PendingIntent.getBroadcast(StandDetectorTMSettings.this,
                                        0, intent, PendingIntent.FLAG_ONE_SHOT);
                                calibrationIntent.putExtra("pendingIntent", pendingIntent);
                                startService(calibrationIntent);
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

}
