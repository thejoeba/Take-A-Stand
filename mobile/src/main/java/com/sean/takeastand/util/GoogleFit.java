package com.sean.takeastand.util;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.ConfigApi;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.SessionInsertRequest;

import java.util.concurrent.TimeUnit;

/**
 * Created by Joey on 12/23/2014.
 */
public class GoogleFit extends Activity {
    private static final int REQUEST_OAUTH = 1;

    /**
     *  Track whether an authorization activity is stacking over the current activity, i.e. when
     *  a known auth error is being resolved, such as showing the account chooser or presenting a
     *  consent dialog. This avoids common duplications as might happen on screen rotations, etc.
     */
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;

    private GoogleApiClient mClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Put application specific code here.

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }

        buildFitnessClient();
    }

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
        mClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.API)
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ))
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {

                            @Override
                            public void onConnected(Bundle bundle) {
                                Log.i("buildFitnessClient", "Connected!!!");
                                // Now you can make calls to the Fitness APIs.
                                // Put application specific code here.
                                InsertSession();
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
                        }
                )
                .addOnConnectionFailedListener(
                        new GoogleApiClient.OnConnectionFailedListener() {
                            // Called whenever the API client fails to connect.
                            @Override
                            public void onConnectionFailed(ConnectionResult result) {
                                Log.i("onConnectionFailed", "Connection failed. Cause: " + result.toString());
                                if (!result.hasResolution()) {
                                    // Show the localized error dialog
                                    GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(),
                                            GoogleFit.this, 0).show();
                                    return;
                                }
                                // The failure has a resolution. Resolve it.
                                // Called typically when the app is not yet authorized, and an
                                // authorization dialog is displayed to the user.
                                if (!authInProgress) {
                                    try {
                                        Log.i("onConnectionFailed", "Attempting to resolve failed connection");
                                        authInProgress = true;
                                        result.startResolutionForResult(GoogleFit.this,
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect to the Fitness API
        Log.i("onStart", "Connecting...");
        mClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mClient.isConnected()) {
            mClient.disconnect();
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

    private void InsertSession(){
//        // Create a session with metadata about the activity.
//        Session session = new Session.Builder()
//                .setName(SAMPLE_SESSION_NAME)
//                .setDescription("Long run around Shoreline Park")
//                .setIdentifier("UniqueIdentifierHere")
//                .setActivity(FitnessActivities.RUNNING)
//                .setStartTime(startTime, TimeUnit.MILLISECONDS)
//                .setEndTime(endTime, TimeUnit.MILLISECONDS)
//                .build();
//
//        // Build a session insert request
//        SessionInsertRequest insertRequest = new SessionInsertRequest.Builder()
//                .setSession(session)
//                .addDataSet(runningDataSet)
//                .build();
//
//        // Then, invoke the Sessions API to insert the session and await the result,
//        // which is possible here because of the AsyncTask. Always include a timeout when
//        // calling await() to avoid hanging that can occur from the service being shutdown
//        // because of low memory or other conditions.
//        Log.i("InsertSession", "Inserting the session in the History API");
//        com.google.android.gms.common.api.Status insertStatus =
//                Fitness.SessionsApi.insertSession(mClient, insertRequest)
//                        .await(1, TimeUnit.MINUTES);
//
//        // Before querying the session, check to see if the insertion succeeded.
//        if (!insertStatus.isSuccess()) {
//            Log.i("InsertSession", "There was a problem inserting the session: " +
//                    insertStatus.getStatusMessage());
//        }
//
//        // At this point, the session has been inserted and can be read.
//        Log.i("InsertSession", "Session insert was successful!");

    }

    //ToDo: Create a disconnect from fit button so users can 'log out'
    private void DisconnectFit() {
//        // 1. Invoke the Config API with the Google API client object
//        PendingResult<Status> pendingResult = ConfigApi.disableFit(mClient);
//
//        // 2. Check the result (see other examples)
    }

}
