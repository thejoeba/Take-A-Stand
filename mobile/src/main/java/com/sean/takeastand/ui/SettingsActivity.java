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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import com.Application;
import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.heckbot.standdtector.StandDtectorTM;
import com.sean.takeastand.R;
import com.sean.takeastand.storage.GoogleFitService;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


/**
 * Created by Sean on 2015-01-09.
 */
public class SettingsActivity extends ActionBarActivity {

    private final static Integer ACTIVITY_NUMBER = 1;
    //Reminder Settings variables and views
    private CheckBox chbxLED;
    private CheckBox chbxVibrate;
    private CheckBox chbxSound;
    private CheckBox chbxToast;
    private TextView txtSilentMode;
    private TextView txtRepeat;
    private TextView txtReminderFrequency;
    private TextView txtNotificationFrequency;
    private TextView txtNotificationFrequencyTitle;
    private CheckBox chbxSilent;
    private CheckBox chbxRepeat;
    private boolean mNotificationAlertChanged;
    private ImageView ivReminderHelp;
    private ImageView ivFitHelp;
    private ImageView ivProHelp;

    //Google Fit Settings variables and views
    //ToDo: add some branding https://developers.google.com/fit/branding
    private Switch toggleGoogleFit;
    private Button btnDeauthorizeFit;
    private Button btnDeleteData;

    //Pro Settings variables and views
    private Switch toggleDeviceStepCounter;
    private Switch toggleWearStepCounter;
    private Switch toggleStandDtectorTM;
    private Button btnCalibrate;
    private TextView txtCalibratedValue;
    private Button btnPurchase;
    private TextView tvProStatus;
    private TextView tvStepDetectionTitle;
    private TextView tvStepDetectionDescription;
    private TextView tvStandDtectorTitle;
    private TextView tvStandDtectorDescription;
    private TextView tvWearTitle;
    private TextView tvWearDescription;

    String skuPro = "take_a_stand_pro";

    /**
     *  Track whether an authorization activity is stacking over the current activity, i.e. when
     *  a known auth error is being resolved, such as showing the account chooser or presenting a
     *  consent dialog. This avoids common duplications as might happen on screen rotations, etc.
     */
    private static final int REQUEST_OAUTH = 1;
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;

    private GoogleApiClient mClient = null;
    private SharedPreferences sharedPreferences;
    private GoogleApiClient.ConnectionCallbacks FitConnectionCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        this.setTitle(getResources().getStringArray(R.array.ActivityTitle)[ACTIVITY_NUMBER]);
        setUpReminderSettingsLayout();
        Tracker t = ((Application) this.getApplication()).getTracker(Application.TrackerName.APP_TRACKER);
        t.setScreenName("Settings Menu");
        t.send(new HitBuilders.AppViewBuilder().build());
        mNotificationAlertChanged = false;
        sharedPreferences = getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);

        setupGoogleFitSettingsLayout();

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING, false);

        }
        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

        setupHelpButtons();

        if(Constants.CONNECT_FIT.equals(getIntent().getAction())) {
            ConnectFit();
            final ScrollView mScrollView = (ScrollView) findViewById(R.id.scrollviewSettings);
            mScrollView.postDelayed(new Runnable() {
                public void run() {
                    int[] fitLocation = new int[2];;
                    ivFitHelp.getLocationInWindow(fitLocation);
                    mScrollView.smoothScrollTo(0, fitLocation[1] - 150);
                }
            },1000);
            toggleGoogleFit.setChecked(true);
        }
    }

    private void setupHelpButtons(){
        ivReminderHelp = (ImageView) findViewById(R.id.ivReminderHelp);
        ivFitHelp = (ImageView) findViewById(R.id.ivFitHelp);
        ivProHelp = (ImageView) findViewById(R.id.ivProHelp);

        ivReminderHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowHelp(7);
            }
        });
        ivFitHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowHelp(8);
            }
        });
        ivProHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowHelp(9);
            }
        });
    }

    private void ShowHelp(int helpNum) {
        new AlertDialog.Builder(this)
                .setTitle(getResources().getStringArray(R.array.ActivityTitle)[helpNum])
                .setMessage(getResources().getStringArray(R.array.ActivityHelpText)[helpNum])
                .setPositiveButton(getString(R.string.ok), null)
                .show();
    }

    private void setupGoogleFitSettingsLayout() {
        toggleGoogleFit = (Switch) findViewById(R.id.fit_sync_switch);
        toggleGoogleFit.setChecked(sharedPreferences.getBoolean(Constants.GOOGLE_FIT_ENABLED, false));
        toggleGoogleFit.setOnClickListener(EnableFit);
        btnDeauthorizeFit = (Button) findViewById(R.id.fit_disconnect_button);
        btnDeauthorizeFit.setEnabled(sharedPreferences.getBoolean(Constants.GOOGLE_FIT_AUTHORIZED, false));
        btnDeauthorizeFit.setOnClickListener(DisableFit);
        btnDeleteData = (Button) findViewById(R.id.fit_delete_button);
        btnDeleteData.setOnClickListener(DeleteData);
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
            ShowHelp(ACTIVITY_NUMBER);
        }
        else {
            //Closes Activity when user presses title
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Fit Settings
        if (requestCode == REQUEST_OAUTH) {
            authInProgress = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mClient.isConnecting() && !mClient.isConnected()) {
                    mClient.connect();
                }
            }
        }

        //Pro Settings
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
                    setUpProLayout();
                }
                catch (JSONException e) {
                    Log.d("onActivityResult", "Failed to parse purchase data.");
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(AUTH_PENDING, authInProgress);
    }

    /************************
     * REMINDER SETTINGS CODE
     ************************/

    private void setUpReminderSettingsLayout() {
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
        chbxToast = (CheckBox) findViewById(R.id.chbxToast);
        chbxToast.setOnClickListener(checkBoxToastListener);
        chbxToast.setChecked(Utils.getToastEnabled(this));
        txtSilentMode = (TextView) findViewById(R.id.txtSilentMode);
        txtRepeat = (TextView) findViewById(R.id.txtRepeat);
        //txtNotificationFrequencyTitle = (TextView) findViewById(R.id.txtNotificationFrequencyTitle);
        txtNotificationFrequency = (TextView) findViewById(R.id.txtNotificationFrequencyTitle);
        int notifFrequency = Utils.getNotificationReminderFrequency(this);
        txtNotificationFrequency.setText(getString(R.string.every) + " " + setMinutes(notifFrequency));
        txtReminderFrequency = (TextView) findViewById(R.id.txtReminderFrequency);
        txtReminderFrequency.setText(setMinutes(Utils.getDefaultFrequency(this)));
        txtReminderFrequency.setOnClickListener(reminderFrequencyListener);
        txtNotificationFrequencyTitle = (TextView) findViewById(R.id.txtNotificationFrequencyTitle);
        txtNotificationFrequencyTitle.setOnClickListener(notificationFrequencyListener);
        chbxSilent = (CheckBox) findViewById(R.id.toggleSilentMode);
        chbxSilent.setOnClickListener(silentModeListener);
        chbxSilent.setChecked(Utils.getVibrateOverride(this));
        chbxRepeat = (CheckBox)findViewById(R.id.toggleRepeat);
        chbxRepeat.setOnClickListener(repeatListener);
        chbxRepeat.setChecked(Utils.getRepeatAlerts(this));
        //Set up grayed views
        setGrayedAreas();
    }

    View.OnClickListener repeatListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (((CheckBox) view).isChecked()) {
                setRepeatAlerts(SettingsActivity.this, true);
                sendAnalyticsEvent("Notification Reminders Repeat: On");
            } else {
                setRepeatAlerts(SettingsActivity.this, false);
                sendAnalyticsEvent("Notification Reminders Repeat: Off");
            }
            setGrayedAreas();
        }
    };

    View.OnClickListener silentModeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean silentModeOn = ((CheckBox) view).isChecked();
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
            AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
            View dialogView = inflater.inflate(R.layout.dialog_number_picker, null);
            builder.setView(dialogView);
            TextView title = new TextView(SettingsActivity.this);
            title.setPadding(50, 50, 50, 50);
            title.setTextSize(22);
            title.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
            title.setText(getResources().getString(R.string.reminder_frequency));
            builder.setCustomTitle(title);
            final NumberPicker numberPicker = (NumberPicker) dialogView.findViewById(R.id.numberPicker);
            numberPicker.setMaxValue(120);
            numberPicker.setMinValue(2);
            numberPicker.setValue(Utils.getDefaultFrequency(SettingsActivity.this));
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
            setDefaultAlertType(SettingsActivity.this, new boolean[]{chbxLED.isChecked(),
                    chbxVibrate.isChecked(), chbxSound.isChecked()});
        }

    };

    View.OnClickListener checkBoxToastListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setToastOnOff(((CheckBox)v).isChecked());
        }
    };

    View.OnClickListener notificationFrequencyListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            LayoutInflater inflater = getLayoutInflater();
            AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
            View dialogView = inflater.inflate(R.layout.dialog_number_picker, null);
            builder.setView(dialogView);
            TextView title = new TextView(SettingsActivity.this);
            title.setPadding(50, 50, 50, 50);
            title.setTextSize(22);
            title.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
            title.setText(getResources().getString(R.string.notification_reminder_frequency));
            builder.setCustomTitle(title);
            final NumberPicker numberPicker = (NumberPicker) dialogView.findViewById(R.id.numberPicker);
            numberPicker.setMaxValue(60);
            numberPicker.setMinValue(1);
            numberPicker.setValue(Utils.getNotificationReminderFrequency(SettingsActivity.this));
            numberPicker.setWrapSelectorWheel(false);
            builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    int frequency = numberPicker.getValue();
                    setNotificationReminderFrequency(SettingsActivity.this, frequency);
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
        if (Utils.getRepeatAlerts(SettingsActivity.this)) {
            //txtNotificationFrequencyTitle.setTextColor(getResources().getColor(android.R.color.primary_text_light));
            txtNotificationFrequency.setTextColor(getResources().getColor(android.R.color.primary_text_light));
            txtNotificationFrequencyTitle.setVisibility(View.VISIBLE);
        } else {
            txtNotificationFrequencyTitle.setVisibility(View.GONE);
        }
        if (chbxVibrate.isChecked()) {
            txtSilentMode.setTextColor(getResources().getColor(android.R.color.primary_text_light));
            chbxSilent.setEnabled(true);
            chbxSilent.setAlpha((float) 1);
        } else {
            txtSilentMode.setTextColor(getResources().getColor(R.color.LightGrey));
            chbxSilent.setEnabled(false);
            chbxSilent.setAlpha((float) 0.5);
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

    public void setToastOnOff(boolean enabled){
        SharedPreferences sharedPreferences =
                getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.TOAST_ENABLED, enabled);
        editor.commit();
    }

    /******************
     * GOOGLE FIT CODE
     *****************/

    View.OnClickListener EnableFit = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (((Switch) view).isChecked()) {
                ConnectFit();
            } else {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(Constants.GOOGLE_FIT_ENABLED, false);
                editor.commit();
                sendAnalyticsEvent("Google Fit Disabled");
            }
        }
    };

    private void ConnectFit() {
        FitConnectionCallback = new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                Log.i("buildFitnessClient", "Connected!!!");
                // Now you can make calls to the Fitness APIs.
                // Put application specific code here.
                if(!sharedPreferences.getBoolean(Constants.GOOGLE_FIT_AUTHORIZED, false)) {
                    Intent intentImport = new Intent(SettingsActivity.this, GoogleFitService.class);
                    intentImport.setAction("ImportFitSessions");
                    startService(intentImport);
                }
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(Constants.GOOGLE_FIT_ENABLED, true);
                editor.putBoolean(Constants.GOOGLE_FIT_AUTHORIZED, true);
                editor.commit();
                disconnectClient();
                btnDeauthorizeFit.setEnabled(true);
                sendAnalyticsEvent("Google Fit Enabled");
            }
            @Override
            public void onConnectionSuspended(int i) {
                // If your connection to the sensor gets lost at some point,
                // you'll be able to determine the reason and react to it here.
                if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                    Log.i("onConnectionSuspended", "Connection lost.  Cause: Network Lost.");
                } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                    Log.i("onConnectionSuspended", "Connection lost.  Reason: Service Disconnected");
                }
            }
        };
        buildFitnessClient();
    }

    View.OnClickListener DisableFit = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            FitConnectionCallback = new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {
                    Log.i("buildFitnessClient", "Connected!!!");
                    // Now you can make calls to the Fitness APIs.
                    // Put application specific code here.
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(Constants.GOOGLE_FIT_ENABLED, false);
                    editor.commit();
                    sendAnalyticsEvent("Google Fit Deauthorized");
                    deauthorizeFit();
                }
                @Override
                public void onConnectionSuspended(int i) {
                    // If your connection to the sensor gets lost at some point,
                    // you'll be able to determine the reason and react to it here.
                    if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                        Log.i("onConnectionSuspended", "Connection lost.  Cause: Network Lost.");
                    } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                        Log.i("onConnectionSuspended", "Connection lost.  Reason: Service Disconnected");
                    }
                }
            };
            buildFitnessClient();
        }
    };

    View.OnClickListener DeleteData = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            new AlertDialog.Builder(SettingsActivity.this)
                    .setTitle(getString(R.string.delete_fit_data))
                    .setMessage(getString(R.string.delete_fit_data_message))
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent insertDelete = new Intent(SettingsActivity.this, GoogleFitService.class);
                            insertDelete.setAction("DeleteData");
                            startService(insertDelete);
                            sendAnalyticsEvent("User Deleted All Google Fit Data");
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        }
    };


    /**
     *  Build a {@link GoogleApiClient} that will authenticate the user and allow the application
     *  to connect to Fitness APIs. The scopes included should match the scopes your app needs
     *  (see documentation for details). Authentication will occasionally fail intentionally,
     *  and in those cases, there will be a known resolution, which the OnConnectionFailedListener()
     *  can address. Examples of this include the user never having signed in before, or having
     *  multiple accounts on the device and needing to specify which account to use, etc.
     */
    private void buildFitnessClient() {
        // Create the Google API Client
        mClient = new GoogleApiClient.Builder(SettingsActivity.this)
                .addApi(Fitness.API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(FitConnectionCallback)
                .addOnConnectionFailedListener(
                        new GoogleApiClient.OnConnectionFailedListener() {
                            // Called whenever the API client fails to connect.
                            @Override
                            public void onConnectionFailed(ConnectionResult result) {
                                Log.i("onConnectionFailed", "Connection failed. Cause: " + result.toString());
                                if (!result.hasResolution()) {
                                    // Show the localized error dialog
                                    GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(),
                                            SettingsActivity.this, 0).show();
                                    toggleGoogleFit.setChecked(false);
                                    return;
                                }
                                // The failure has a resolution. Resolve it.
                                // Called typically when the app is not yet authorized, and an
                                // authorization dialog is displayed to the user.
                                if (!authInProgress) {
                                    try {
                                        Log.i("onConnectionFailed", "Attempting to resolve failed connection");
                                        authInProgress = true;
                                        result.startResolutionForResult(SettingsActivity.this,
                                                REQUEST_OAUTH);
                                    } catch (IntentSender.SendIntentException e) {
                                        Log.e("onConnectionFailed",
                                                "Exception while starting resolution activity", e);
                                    }
                                }
                            }
                        }
                )
                .build();
        connectClient();
    }

    public void connectClient() {
        // Connect to the Fitness API
        Log.i("connectClient", "Connecting...");
        mClient.connect();
    }

    public void disconnectClient() {
        if (mClient.isConnected()) {
            Log.i("disconnectClient", "Disconnecting...");
            mClient.disconnect();
        }
    }

    public void deauthorizeFit() {
        Log.d("deauthorizeFit", "Deauthorizing Fit");
//        // 1. Invoke the Config API with the Google API client object
        PendingResult<Status> pendingResult = Fitness.ConfigApi.disableFit(mClient);

//        // 2. Check the result
        pendingResult.setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Log.d("deauthorizeFit", "Status: " + status);
                        Log.d("deauthorizeFit", "Status Code: " + status.getStatusCode());
                        if (status.isSuccess()) {
                            Log.d("deauthorizeFit", "Fit Deauthorized");
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(Constants.GOOGLE_FIT_ENABLED, false);
                            editor.putBoolean(Constants.GOOGLE_FIT_AUTHORIZED, false);
                            editor.commit();
                            btnDeauthorizeFit.setEnabled(false);
                            toggleGoogleFit.setChecked(false);
                        }
                    }
                }
        );
    }

    /********************
     * PRO SETTINGS CODE
     *******************/

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

    private void setUpProLayout() {
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
        tvStepDetectionTitle = (TextView)findViewById(R.id.tvStepCounter);
        tvStepDetectionDescription = (TextView)findViewById(R.id.tvStepDetectionDescription);
        tvStandDtectorTitle = (TextView)findViewById(R.id.tvStandDtector);
        tvStandDtectorDescription = (TextView)findViewById(R.id.tvStandDtectorDescription);
        tvWearTitle = (TextView)findViewById(R.id.tvWearStepCounter);
        tvWearDescription = (TextView)findViewById(R.id.tvWearStepDescription);

        Log.d("setUpProLayout", "enablePro: " + enablePro);
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
            toggleDeviceStepCounter.setAlpha((float) 1);
            toggleWearStepCounter.setOnClickListener(WearStepCounterListener);
            toggleWearStepCounter.setChecked(sharedPreferences.getBoolean(Constants.WEAR_STEP_DETECTOR_ENABLED, false));
            toggleWearStepCounter.setAlpha((float) 1);
            toggleStandDtectorTM.setOnClickListener(StandDtectorTMListener);
            toggleStandDtectorTM.setChecked(sharedPreferences.getBoolean(Constants.STANDDTECTORTM_ENABLED, false));
            toggleStandDtectorTM.setAlpha((float) 1);
            btnCalibrate.setOnClickListener(CalibrateListener);
            txtCalibratedValue.setText(getString(R.string.calibrated_value) + sharedPreferences.getFloat("CALIBRATEDVARIATION", 0));
            FeatureCheck();

            //Make sure textviews aren't grayed out
            tvStepDetectionTitle.setTextColor(getResources().getColor(android.R.color.primary_text_light));
            tvStepDetectionDescription.setTextColor(getResources().getColor(android.R.color.secondary_text_light));
            tvStandDtectorTitle.setTextColor(getResources().getColor(android.R.color.primary_text_light));
            tvStandDtectorDescription.setTextColor(getResources().getColor(android.R.color.secondary_text_light));
            tvWearTitle.setTextColor(getResources().getColor(android.R.color.primary_text_light));
            tvWearDescription.setTextColor(getResources().getColor(android.R.color.secondary_text_light));
        }
        else {
            toggleDeviceStepCounter.setChecked(false);
            toggleDeviceStepCounter.setEnabled(false);
            toggleDeviceStepCounter.setOnClickListener(UpgradePurchase);
            toggleDeviceStepCounter.setAlpha((float)0.5);

            toggleWearStepCounter.setChecked(false);
            toggleWearStepCounter.setEnabled(false);
            toggleWearStepCounter.setOnClickListener(UpgradePurchase);
            toggleWearStepCounter.setAlpha((float)0.5);

            toggleStandDtectorTM.setChecked(false);
            toggleStandDtectorTM.setEnabled(false);
            toggleStandDtectorTM.setOnClickListener(UpgradePurchase);
            toggleStandDtectorTM.setAlpha((float)0.5);

            btnCalibrate.setOnClickListener(UpgradePurchase);
            btnCalibrate.setEnabled(false);

            //Gray out all pro textviews
            tvStepDetectionTitle.setTextColor(getResources().getColor(R.color.LightGrey));
            tvStepDetectionDescription.setTextColor(getResources().getColor(R.color.LightGrey));
            tvStandDtectorTitle.setTextColor(getResources().getColor(R.color.LightGrey));
            tvStandDtectorDescription.setTextColor(getResources().getColor(R.color.LightGrey));
            tvWearTitle.setTextColor(getResources().getColor(R.color.LightGrey));
            tvWearDescription.setTextColor(getResources().getColor(R.color.LightGrey));

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
            new AlertDialog.Builder(SettingsActivity.this)
                    .setTitle(getString(R.string.calibration))
                    .setMessage(getString(R.string.calibration_instructions))
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent calibrationIntent = new Intent(SettingsActivity.this, StandDtectorTM.class);
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


    private BroadcastReceiver calibrationFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("StandDtectorTMSettings", "Calibration Finished");
            LocalBroadcastManager.getInstance(SettingsActivity.this).unregisterReceiver(calibrationFinishedReceiver);
            if (intent.getExtras().getString("Results").equals("Success")) {
                txtCalibratedValue.setText(getString(R.string.new_calibrated) + sharedPreferences.getFloat("CALIBRATEDVARIATION", 0));
            } else {
                LocalBroadcastManager.getInstance(SettingsActivity.this).unregisterReceiver(calibrationFinishedReceiver);
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

            setUpProLayout();
        }
    };

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
