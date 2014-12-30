package com.sean.takeastand.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.sean.takeastand.R;
import com.sean.takeastand.storage.StoodLogsAdapter;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.util.GoogleFitService;

/**
 * Created by Joey on 12/23/2014.
 */
public class GoogleFitActivity extends ActionBarActivity {
    //ToDo: add some branding and explain https://developers.google.com/fit/branding
    private Switch toggleGoogleFit;
    private Button btnDeauthorizeFit;
    private Button btnMarkDBSynced;
    private Button btnReadData;
    private Button btnDeleteData;

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

        setContentView(R.layout.activity_google_fit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.stand_count_toolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavUtils.navigateUpFromSameTask(GoogleFitActivity.this);
                }
            });
        }

        sharedPreferences = getSharedPreferences(Constants.USER_SHARED_PREFERENCES, 0);

        toggleGoogleFit = (Switch) findViewById(R.id.toggleGoogleFit);
        toggleGoogleFit.setChecked(sharedPreferences.getBoolean(Constants.GOOGLE_FIT_ENABLED, false));
        toggleGoogleFit.setOnClickListener(EnableFit);
        btnDeauthorizeFit = (Button) findViewById(R.id.btnDeauthorizeFit);
        btnDeauthorizeFit.setEnabled(sharedPreferences.getBoolean(Constants.GOOGLE_FIT_AUTHORIZED, false));
        btnDeauthorizeFit.setOnClickListener(DisableFit);
        btnMarkDBSynced = (Button) findViewById(R.id.btnMarkDBSynced);
        btnMarkDBSynced.setOnClickListener(MarkDBSynced);
        btnReadData = (Button) findViewById(R.id.btnReadData);
        btnReadData.setOnClickListener(ReadData);
        btnDeleteData = (Button) findViewById(R.id.btnDeleteData);
        btnDeleteData.setOnClickListener(DeleteData);

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OAUTH) {
            authInProgress = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mClient.isConnecting() && !mClient.isConnected()) {
                    mClient.connect();
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(AUTH_PENDING, authInProgress);
    }

    View.OnClickListener EnableFit = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (((Switch) view).isChecked()) {
                FitConnectionCallback = new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.i("buildFitnessClient", "Connected!!!");
                        // Now you can make calls to the Fitness APIs.
                        // Put application specific code here.
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(Constants.GOOGLE_FIT_ENABLED, true);
                        editor.putBoolean(Constants.GOOGLE_FIT_AUTHORIZED, true);
                        editor.commit();
                        disconnectClient();
                        btnDeauthorizeFit.setEnabled(true);
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
            } else {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(Constants.GOOGLE_FIT_ENABLED, false);
                editor.commit();
            }
        }
    };

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

    View.OnClickListener MarkDBSynced = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            StoodLogsAdapter stoodLogsAdapter = new StoodLogsAdapter(GoogleFitActivity.this);
            stoodLogsAdapter.updateSyncedSession(1);
        }
    };

    View.OnClickListener ReadData = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent insertRead = new Intent(GoogleFitActivity.this, GoogleFitService.class);
            insertRead.setAction("ReadData");
            startService(insertRead);
        }
    };

    View.OnClickListener DeleteData = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            new AlertDialog.Builder(GoogleFitActivity.this)
                    .setTitle("Delete Fit Data")
                    .setMessage("Deleting Fit Data cannot be undone. Are you sure?")
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent insertDelete = new Intent(GoogleFitActivity.this, GoogleFitService.class);
                            insertDelete.setAction("DeleteData");
                            startService(insertDelete);
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
        mClient = new GoogleApiClient.Builder(GoogleFitActivity.this)
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
                                            GoogleFitActivity.this, 0).show();
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
                                        result.startResolutionForResult(GoogleFitActivity.this,
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
}

