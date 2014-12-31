package com.sean.takeastand.storage;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.DataDeleteRequest;
import com.google.android.gms.fitness.request.DataTypeCreateRequest;
import com.google.android.gms.fitness.request.SessionInsertRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.DataTypeResult;
import com.google.android.gms.fitness.result.SessionReadResult;
import com.sean.takeastand.util.Constants;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Joey on 12/28/2014.
 */
public class GoogleFitService extends IntentService{

    SharedPreferences sharedPref;
    private GoogleApiClient mClient = null;
    private String mAction;
    private DataType standDataType;

    public GoogleFitService() {
        super("GoogleFitService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        sharedPref = getSharedPreferences(Constants.USER_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        mAction = intent.getAction();

        Log.i("onHandleIntent", "Action: " + mAction);
        buildFitnessClient();
    }

    private void buildFitnessClient() {
        // Create the Google API Client
        mClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {

                            @Override
                            public void onConnected(Bundle bundle) {
                                Log.i("buildFitnessClient", "Connected!!!");
                                // Now you can make calls to the Fitness APIs.
                                // Put application specific code here.
//                                readStandDataType();
                                createStandDataType();
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
            stopSelf();
        }
    }

    public void readStandDataType() {

        // 1. Invoke the Config API with:
        // - The Google API client object
        // - The custom data type name
        PendingResult<DataTypeResult> pendingResult =
                Fitness.ConfigApi.readDataType(mClient, "com.herbivoreapps.takeastand.stand");

        // 2. Check the result asynchronously
        // (The result may not be immediately available)
        pendingResult.setResultCallback(new ResultCallback<DataTypeResult>() {
            @Override
            public void onResult(DataTypeResult dataTypeResult) {
                Log.d("readStandDataType", "Status: " + dataTypeResult.getStatus());
                Log.d("readStandDataType", "Status Code: " + dataTypeResult.getStatus().getStatusCode());
                if (dataTypeResult.getStatus().isSuccess()) {
                    // Retrieve the custom data type
                    Log.d("readStandDataType", "Stand DataType found");
                    standDataType = dataTypeResult.getDataType();
                    // Use this custom data type with Google Fit
                    if(mAction.equals("InsertData")) {
                        insertUnsyncedData();
                    }
                    else if(mAction.equals("ReadData")) {
                        readData();
                    }
                    else if(mAction.equals("GetOldestSession")){
                        readOldestSession();
                    }
                    else if(mAction.equals("DeleteData")) {
                        deleteData();
                    }
                }
                else {
                    // Failed (not created)
                    // Status: Status{statusCode=unknown status code: 5003, resolution=null}
                    Log.d("readStandDataType", "Stand DataType not found, try creating");
                    createStandDataType();
                }
            }
        });
    }

    public void createStandDataType() {

        // 1. Build a request to create a new data type
        DataTypeCreateRequest request = new DataTypeCreateRequest.Builder()
                // The prefix of your data type name must match your app's package name
                .setName("com.herbivoreapps.takeastand.stand")
                        // Add some custom fields, both int and float
                .addField("STOOD_METHOD", Field.FORMAT_INT32)
                .build();

        // 2. Invoke the Config API with:
        // - The Google API client object
        // - The create data type request
        PendingResult<DataTypeResult> pendingResult =
                Fitness.ConfigApi.createCustomDataType(mClient, request);

        // 3. Check the result asynchronously
        // (The result may not be immediately available)
        pendingResult.setResultCallback(new ResultCallback<DataTypeResult>() {
            @Override
            public void onResult(DataTypeResult dataTypeResult) {
                Log.d("createStandDataType", "Status: " + dataTypeResult.getStatus());
                Log.d("createStandDataType", "Status Code: " + dataTypeResult.getStatus().getStatusCode());
                if (dataTypeResult.getStatus().isSuccess()) {
                    // Retrieve the created data type
                    Log.d("createStandDataType", "Stand DataType created");
                    standDataType = dataTypeResult.getDataType();
                    // Use this custom data type to insert data in your app
                    if(mAction.equals("InsertData")) {
                        insertUnsyncedData();
                    }
                    else if(mAction.equals("ReadData")) {
                        readData();
                    }
                    else if(mAction.equals("ImportFitSessions")){
                        insertUnsyncedData();
                    }
                    else if(mAction.equals("GetOldestSession")){
                        readOldestSession();
                    }
                    else if(mAction.equals("DeleteData")) {
                        deleteData();
                    }
                }
            }
        });
    }

    public void insertUnsyncedData() {
        Thread insertThread = new Thread() {
            @Override
            public void run() {
                long startTime;
                long endTime;
                StoodLogsAdapter stoodLogsAdapter = new StoodLogsAdapter(GoogleFitService.this);

                // Create a data source
                DataSource dataSource =
                        new DataSource.Builder()
                                .setAppPackageName(getPackageName())
                                .setDataType(standDataType)
                                .setName("Take A Stand - Session").setType(DataSource.TYPE_RAW).build();

                long[][] unsyncedSessions = stoodLogsAdapter.getUnsyncedSessions();
                Log.i("insertUnsyncedData", "unsyncedSessions: " + unsyncedSessions.length);

                if (unsyncedSessions.length > 0) {
                    for (int unsyncedRows = 0; unsyncedRows < unsyncedSessions.length; unsyncedRows++) {
                        int intSession = (int) unsyncedSessions[unsyncedRows][0];
                        startTime = unsyncedSessions[unsyncedRows][1];
                        int sessionType = (int) unsyncedSessions[unsyncedRows][2];

                        long[][] sessionArray = stoodLogsAdapter.getSessionStands(intSession);
                        Log.i("insertUnsyncedData", "Stands: " + sessionArray.length);

                        DataSet dataSet = DataSet.create(dataSource);

                        if (sessionArray.length > 0) {
                            for (int sessionRows = 0; sessionRows < sessionArray.length; sessionRows++) {
                                dataSet.add(dataSet.createDataPoint()
                                                .setTimestamp(sessionArray[sessionRows][1], TimeUnit.MILLISECONDS)
                                                .setIntValues((int) sessionArray[sessionRows][0])
                                );
                            }
                            endTime = sessionArray[sessionArray.length - 1][1];

                            String Session_Name;
                            if (sessionType == 1) {
                                Session_Name = "Unscheduled Stands";
                            } else if (sessionType == 2) {
                                Session_Name = "Scheduled Stands";
                            } else {
                                Session_Name = "Unknown Stands";
                            }

                            Log.i("insertUnsyncedData", "Session_Name: " + Session_Name);

                            // Create a session with metadata about the activity.
                            Session session = new Session.Builder()
                                    .setName(Session_Name)
                                    .setStartTime(startTime, TimeUnit.MILLISECONDS)
                                    .setEndTime(endTime, TimeUnit.MILLISECONDS)
    //                                .setDescription("Stands for today")
    //                                .setIdentifier("UniqueIdentifierHere")
    //                                .setActivity(FitnessActivities.RUNNING)
                                    .build();

                            Log.i("insertUnsyncedData", "Session built");

                            // Build a session insert request
                            SessionInsertRequest insertRequest = new SessionInsertRequest.Builder()
                                    .setSession(session)
                                    .addDataSet(dataSet)
                                    .build();

                            Log.i("insertUnsyncedData", "SessionInsertRequest built");

                            // Then, invoke the Sessions API to insert the session and await the result,
                            // which is possible here because of the AsyncTask. Always include a timeout when
                            // calling await() to avoid hanging that can occur from the service being shutdown
                            // because of low memory or other conditions.
                            Log.i("insertUnsyncedData", "Inserting the session in the History API");
                            com.google.android.gms.common.api.Status insertStatus =
                                    Fitness.SessionsApi.insertSession(mClient, insertRequest)
                                            .await(1, TimeUnit.MINUTES);

                            //Before querying the session, check to see if the insertion succeeded.
                            if (insertStatus.isSuccess()) {
                                // At this point, the session has been inserted and can be read.
                                Log.i("insertUnsyncedData", "Session " + intSession + " was added to Fit successful!");
                                stoodLogsAdapter.updateSyncedSession(intSession);
                            } else {
                                Log.i("insertUnsyncedData", "There was a problem inserting the session: " +
                                        insertStatus.getStatusMessage());
                            }
                        }
                        else {
                            stoodLogsAdapter.updateSyncedSession(intSession);
                        }
                    }
                }
                Log.i("insertUnsyncedData", "sync to fit complete, action: " + mAction);
                if(mAction.equals("ImportFitSessions")) {
                    Log.i("insertUnsyncedData", "Clearing Database, then importing from Fit");
                    stoodLogsAdapter.clearStands();
                    importFitSessions();
                }
                else{
                    disconnectClient();
                }
            }
        };

        insertThread.start();
    }

    public void readData() {
        Thread readThread = new Thread() {
            @Override
            public void run() {
                long startTime = 1419840000000l;
                long endTime = System.currentTimeMillis();
                // Build a session read request
                SessionReadRequest readRequest = new SessionReadRequest.Builder()
                        .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                        .read(standDataType)
                        .build();

                // Invoke the Sessions API to fetch the session with the query and wait for the result
                // of the read request.
                SessionReadResult sessionReadResult =
                        Fitness.SessionsApi.readSession(mClient, readRequest)
                                .await(1, TimeUnit.MINUTES);

                // Get a list of the sessions that match the criteria to check the result.
                Log.i("readSession", "Session read was successful. Number of returned sessions is: "
                        + sessionReadResult.getSessions().size());

                StoodLogsAdapter stoodLogsAdapter = new StoodLogsAdapter(GoogleFitService.this);

                for (Session session : sessionReadResult.getSessions()) {
                    int sessionType;
                    if (session.getName().equals("Unscheduled Stands")) {
                        sessionType = 1;
                    } else if (session.getName().equals("Scheduled Stands")) {
                        sessionType = 2;
                    } else {
                        sessionType = 3;
                    }
                    Log.d("GoogleFitReadSession",
                            "sessionType: " + sessionType +
                                    " sessionStart: " + session.getStartTime(TimeUnit.MILLISECONDS));
//                    int sessionNum = stoodLogsAdapter.addFitSession(sessionType, session.getStartTime(TimeUnit.MILLISECONDS));

                    List<DataSet> dataSets = sessionReadResult.getDataSet(session);
                    for (DataSet standDataSet : dataSets) {
                        for (DataPoint dp : standDataSet.getDataPoints()) {
                            long standTime = dp.getTimestamp(TimeUnit.MILLISECONDS);
                            int stoodMethod = 0;
                            for(Field field : dp.getDataType().getFields()) {
                                stoodMethod = dp.getValue(field).asInt();
                            }
                            Log.d("GoogleFitReadStand",
                                    "stoodMethod: " + stoodMethod +
                                            " standTime: " + standTime);
//                            stoodLogsAdapter.addFitStand(stoodMethod, standTime, sessionNum);
                        }
                    }
                }
                disconnectClient();
            }
        };

        readThread.start();
    }

    public void readOldestSession() {
        Thread readOldestThread = new Thread() {
            @Override
            public void run() {
                long startTime = 1419840000000l;
                long endTime = System.currentTimeMillis();
                // Build a session read request
                SessionReadRequest readRequest = new SessionReadRequest.Builder()
                        .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                        .read(standDataType)
//                        .setSessionName(SAMPLE_SESSION_NAME)
                        .build();

                // Invoke the Sessions API to fetch the session with the query and wait for the result
                // of the read request.
                SessionReadResult sessionReadResult =
                        Fitness.SessionsApi.readSession(mClient, readRequest)
                                .await(1, TimeUnit.MINUTES);

                // Get a list of the sessions that match the criteria to check the result.
                Log.i("readSession", "Session read was successful. Number of returned sessions is: "
                        + sessionReadResult.getSessions().size());

                long oldestSession = endTime;
                for (Session session : sessionReadResult.getSessions()) {
                    Long sessionTime = session.getStartTime(TimeUnit.MILLISECONDS);
                    if (sessionTime < oldestSession) {
                        oldestSession = sessionTime;
                    }
                }
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putLong(Constants.GOOGLE_FIT_OLDEST_SESSION, oldestSession);
                editor.commit();
                disconnectClient();
            }
        };
        readOldestThread.start();
    }

    public void importFitSessions() {
        Thread importFitSessionsThread = new Thread() {
            @Override
            public void run() {
                long startTime = 1419840000000l;
                long endTime = System.currentTimeMillis();
                // Build a session read request
                SessionReadRequest readRequest = new SessionReadRequest.Builder()
                        .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                        .read(standDataType)
                        .build();

                // Invoke the Sessions API to fetch the session with the query and wait for the result
                // of the read request.
                SessionReadResult sessionReadResult =
                        Fitness.SessionsApi.readSession(mClient, readRequest)
                                .await(1, TimeUnit.MINUTES);

                // Get a list of the sessions that match the criteria to check the result.
                Log.i("readSession", "Session read was successful. Number of returned sessions is: "
                        + sessionReadResult.getSessions().size());

                StoodLogsAdapter stoodLogsAdapter = new StoodLogsAdapter(GoogleFitService.this);

                for (Session session : sessionReadResult.getSessions()) {
                    int sessionType;
                    if (session.getName().equals("Unscheduled Stands")) {
                        sessionType = 1;
                    } else if (session.getName().equals("Scheduled Stands")) {
                        sessionType = 2;
                    } else {
                        sessionType = 3;
                    }
                    Log.d("GoogleFitReadSession",
                            "sessionType: " + sessionType +
                            " sessionStart: " + session.getStartTime(TimeUnit.MILLISECONDS));
                    int sessionNum = stoodLogsAdapter.addFitSession(sessionType, session.getStartTime(TimeUnit.MILLISECONDS));

                    List<DataSet> dataSets = sessionReadResult.getDataSet(session);
                    for (DataSet standDataSet : dataSets) {
                        for (DataPoint dp : standDataSet.getDataPoints()) {
                            long standTime = dp.getTimestamp(TimeUnit.MILLISECONDS);
                            int stoodMethod = 0;
                            for(Field field : dp.getDataType().getFields()) {
                                stoodMethod = dp.getValue(field).asInt();
                            }
                            Log.d("GoogleFitReadStand",
                                    "stoodMethod: " + stoodMethod +
                                    " standTime: " + standTime);
                            stoodLogsAdapter.addFitStand(stoodMethod, standTime, sessionNum);
                        }
                    }
                }
                disconnectClient();
            }
        };
        importFitSessionsThread.start();
    }

    public void deleteData() {
        Thread deleteThread = new Thread() {
            @Override
            public void run() {
                // 1419840000000 is 12/29/2014 in unix epoch time
                long startTime = 1419840000000l;
                long endTime = System.currentTimeMillis();

                //  Create a delete request object, providing a data type and a time interval
                DataDeleteRequest request = new DataDeleteRequest.Builder()
                        .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                        .deleteAllData()
                        .deleteAllSessions()
                        .build();


                // Invoke the History API with the Google API client object and delete request, and then
                // specify a callback that will check the result.
                Fitness.HistoryApi.deleteData(mClient, request)
                        .setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                if (status.isSuccess()) {
                                    Log.i("deleteData", "Successfully deleted data");
                                } else {
                                    // The deletion will fail if the requesting app tries to delete data
                                    // that it did not insert.
                                    Log.i("deleteData", "Failed to delete data");
                                }
                            }
                        });

                disconnectClient();
            }
        };

        deleteThread.start();
    }
}
