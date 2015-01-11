package com.sean.takeastand.ui;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.android.vending.billing.IInAppBillingService;
import com.heckbot.standdtector.StandDtectorTM;
import com.sean.takeastand.R;
import com.sean.takeastand.util.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ProSettings extends ActionBarActivity {
    //ToDo: Add Analytics to StandDtectorTMSettings
    //ToDo: Explain Settings
    private Switch toggleDeviceStepCounter;
    private Switch toggleWearStepCounter;
    private Switch toggleStandDtectorTM;
    private Button btnCalibrate;
    private TextView txtCalibratedValue;
    private Button btnPurchase;
    private TextView tvProStatus;
    private final static Integer ACTIVITY_NUMBER = 9;

    SharedPreferences sharedPreferences;

    String skuPro = "take_a_stand_pro";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pro_settings);
        this.setTitle(getResources().getStringArray(R.array.ActivityTitle)[ACTIVITY_NUMBER]);
        Toolbar toolbar = (Toolbar) findViewById(R.id.standdtectortm_settings_toolbar);
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

        sharedPreferences = getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);

        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001) {
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    Log.d("onActivityResult", "You have bought the " + sku + ". Excellent choice!");
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putLong(Constants.PRO_VERIFIED, System.currentTimeMillis());
                    editor.commit();
                    setUpLayout();
                }
                catch (JSONException e) {
                    Log.d("onActivityResult", "Failed to parse purchase data.");
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean checkPro() {
        try {
            String android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            if (sharedPreferences.getString(Constants.PRO_ANDROID_ID, "").equals(android_id)) {
                if (sharedPreferences.getLong(Constants.PRO_VERIFIED, 0) > System.currentTimeMillis() - 604800000l) {
                    return true;
                }
            } else {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Constants.PRO_ANDROID_ID, android_id);
                editor.commit();
            }
        }
        catch (Exception e) {
            Log.e("checkPro", "Failed to verify pro against sharedPreferences. Checking Purchase.");
        }

        try {
            Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);

            int response = ownedItems.getInt("RESPONSE_CODE");
            if (response == 0) {
                ArrayList<String> ownedSkus =
                        ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                ArrayList<String>  purchaseDataList =
                        ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
//                ArrayList<String>  signatureList =
//                        ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE");
//                String continuationToken =
//                        ownedItems.getString("INAPP_CONTINUATION_TOKEN");

                for (int i = 0; i < purchaseDataList.size(); ++i) {
//                    String purchaseData = purchaseDataList.get(i);
//                    String signature = signatureList.get(i);
                    String sku = ownedSkus.get(i);

                    // do something with this purchase information
                    // e.g. display the updated list of products owned by user

                    if (sku.equals(skuPro)) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putLong(Constants.PRO_VERIFIED, System.currentTimeMillis());
                        editor.commit();
                        return true;
                    }

                }

                // if continuationToken != null, call getPurchases again
                // and pass in the token to retrieve more items
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void setUpLayout() {
        //ToDo: check trial expiration when starting session
        boolean enablePro = checkPro();
//        boolean enablePro = false;
        boolean trial = false;
        long installTime;

        toggleDeviceStepCounter = (Switch) findViewById(R.id.toggleDeviceStepCounter);
        toggleWearStepCounter = (Switch) findViewById(R.id.toggleWearStepCounter);
        toggleStandDtectorTM = (Switch) findViewById(R.id.toggleStandDtectorTM);
        btnCalibrate = (Button) findViewById(R.id.btnCalibrate);
        txtCalibratedValue = (TextView) findViewById(R.id.txtCalibratedValue);
        btnPurchase = (Button) findViewById(R.id.btnPurchase);
        btnPurchase.setOnClickListener(UpgradePurchase);
        tvProStatus = (TextView) findViewById(R.id.tvProStatus);

        Log.d("setUpLayout", "enablePro: " + enablePro);
        if (enablePro) {
            btnPurchase.setVisibility(View.GONE);
            tvProStatus.setText("Pro Purchased");
        } else {
            long freeTrialTime = 604800000l;
            try {
                installTime = this
                        .getPackageManager()
                        .getPackageInfo(getPackageName(), 0)
                        .firstInstallTime;
                int installDate = Math.round(installTime / 86400000f);
                int daysSinceInstall = Math.round(System.currentTimeMillis() / 86400000f) - installDate;
                if (daysSinceInstall <= 7) {
                    if (sharedPreferences.getBoolean(Constants.GOOGLE_FIT_AUTHORIZED, false)) {
                        int daysSinceFirstFit = Math.round(sharedPreferences.getLong(Constants.GOOGLE_FIT_OLDEST_SESSION, System.currentTimeMillis()) / 86400000f) - installDate;
                        if (daysSinceFirstFit <= 7) {
                            trial = true;
                            tvProStatus.setText(getString(R.string.trial) + (7 - daysSinceInstall) + getString(R.string.days_remaining));
                        }
                    }
                    else {
                        trial = true;
                        tvProStatus.setText(getString(R.string.trial) + (7 - daysSinceInstall) + getString(R.string.days_remaining));
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (enablePro || trial) {
            toggleDeviceStepCounter.setOnClickListener(StepCounterListener);
            toggleDeviceStepCounter.setChecked(sharedPreferences.getBoolean(Constants.DEVICE_STEP_DETECTOR_ENABLED, false));
            toggleWearStepCounter.setOnClickListener(WearStepCounterListener);
            toggleWearStepCounter.setChecked(sharedPreferences.getBoolean(Constants.WEAR_STEP_DETECTOR_ENABLED, false));
            toggleStandDtectorTM.setOnClickListener(StandDtectorTMListener);
            toggleStandDtectorTM.setChecked(sharedPreferences.getBoolean(Constants.STANDDTECTORTM_ENABLED, false));
            btnCalibrate.setOnClickListener(CalibrateListener);
            txtCalibratedValue.setText(getString(R.string.calibrated_value) + sharedPreferences.getFloat("CALIBRATEDVARIATION", 0));
            FeatureCheck();
        }
        else {
            toggleDeviceStepCounter.setChecked(false);
            toggleDeviceStepCounter.setOnClickListener(UpgradePurchase);

            toggleWearStepCounter.setChecked(false);
            toggleWearStepCounter.setOnClickListener(UpgradePurchase);

            toggleStandDtectorTM.setChecked(false);
            toggleStandDtectorTM.setOnClickListener(UpgradePurchase);

            btnCalibrate.setOnClickListener(UpgradePurchase);

            tvProStatus.setText(getString(R.string.trial_expired));

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(Constants.DEVICE_STEP_DETECTOR_ENABLED, false);
            editor.putBoolean(Constants.WEAR_STEP_DETECTOR_ENABLED, false);
            editor.putBoolean(Constants.STANDDTECTORTM_ENABLED, false);
            editor.commit();
        }
    }

    private void FeatureCheck() {

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)) {
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
            Log.e("FeatureCheck", "Unable to determine if wear installed: " + e.toString());
        }

        if (!wear_installed) {
            toggleWearStepCounter.setChecked(false);
            toggleWearStepCounter.setEnabled(false);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(Constants.WEAR_STEP_DETECTOR_ENABLED, false);
            editor.commit();
        }

    }

    View.OnClickListener UpgradePurchase = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            toggleDeviceStepCounter.setChecked(false);
            toggleWearStepCounter.setChecked(false);
            toggleStandDtectorTM.setChecked(false);

            Bundle buyIntentBundle = null;
            try {
                buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
                        skuPro, "inapp", "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

            try {
                startIntentSenderForResult(pendingIntent.getIntentSender(),
                        1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0),
                        Integer.valueOf(0));
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }

        }
    };

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
            //ToDo: calibrate first
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(Constants.STANDDTECTORTM_ENABLED, ((Switch) view).isChecked());
            editor.commit();
        }
    };

    View.OnClickListener CalibrateListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            setRequestedOrientation(getResources().getConfiguration().orientation);
            new AlertDialog.Builder(ProSettings.this)
                    .setTitle(getString(R.string.calibration))
                    .setMessage(getString(R.string.calibration_instructions))
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent calibrationIntent = new Intent(ProSettings.this, StandDtectorTM.class);
                            calibrationIntent.setAction(com.heckbot.standdtector.Constants.CALIBRATE);
                            Intent intent = new Intent("CalibrationFinished");
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                                    0, intent, PendingIntent.FLAG_ONE_SHOT);
                            calibrationIntent.putExtra("pendingIntent", pendingIntent);
                            startService(calibrationIntent);
                            getApplicationContext().registerReceiver(calibrationFinishedReceiver, new IntentFilter("CalibrationFinished"));
                            btnCalibrate.setEnabled(false);
                            btnCalibrate.setText(getString(R.string.calibrating));
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        }
    };

    //For Calligraphy font library class
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new CalligraphyContextWrapper(newBase));
    }

    private BroadcastReceiver calibrationFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("StandDtectorTMSettings", "Calibration Finished");
            LocalBroadcastManager.getInstance(ProSettings.this).unregisterReceiver(calibrationFinishedReceiver);
            if (intent.getExtras().getString("Results").equals("Success")) {
                txtCalibratedValue.setText(getString(R.string.new_calibrated) + sharedPreferences.getFloat("CALIBRATEDVARIATION", 0));
            } else {
                LocalBroadcastManager.getInstance(ProSettings.this).unregisterReceiver(calibrationFinishedReceiver);
                txtCalibratedValue.setText(getString(R.string.calibration_failed));
            }
            btnCalibrate.setEnabled(true);
            btnCalibrate.setText(R.string.standdtectortm_calbirate);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    };

    IInAppBillingService mService;

    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);

            setUpLayout();
        }
    };

}