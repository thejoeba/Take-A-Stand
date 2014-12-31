package com.sean.takeastand.storage;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.Application;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.sean.takeastand.util.Constants;

import java.util.Calendar;

/**
 * Created by Sean on 2014-12-08.
 */
public class StoodLogsAdapter {

    private static final String TAG = "Stood Logs Adapter: ";
    private Context mContext;


    public StoodLogsAdapter(Context context)
    {
        mContext = context;
    }

    public long newStoodLog(int stoodMethod, Calendar timeStamp){
        Log.i(TAG, "newStoodLog");
        StoodSQLHelper stoodSQLHelper = new StoodSQLHelper(mContext);
        SQLiteDatabase localSQLiteDatabase = stoodSQLHelper.getWritableDatabase();
        ContentValues databaseInfo = new ContentValues();
        databaseInfo.put(StoodSQLHelper.STOOD_METHOD, stoodMethod);
        databaseInfo.put(StoodSQLHelper.STAND_TIMESTAMP, timeStamp.getTimeInMillis());
        databaseInfo.put(StoodSQLHelper.SESSION_ID, mContext.getSharedPreferences(Constants.USER_SHARED_PREFERENCES, Context.MODE_PRIVATE).getLong("CurrentSession", 0));
        long l = localSQLiteDatabase.insert(StoodSQLHelper.TABLE_MAIN, null, databaseInfo);
        localSQLiteDatabase.close();
        stoodSQLHelper.close();
        sendAnalyticsEvent(mContext, "Stood: " + stoodMethod);
        return l;
    }

    public void newSession(int sessionType){
        Log.i(TAG, "newSession");
        StoodSQLHelper stoodSQLHelper = new StoodSQLHelper(mContext);
        SQLiteDatabase localSQLiteDatabase = stoodSQLHelper.getWritableDatabase();
        ContentValues databaseInfo = new ContentValues();
        databaseInfo.put(StoodSQLHelper.SESSION_TYPE, sessionType);
        databaseInfo.put(StoodSQLHelper.SESSION_START, System.currentTimeMillis());
        databaseInfo.put(StoodSQLHelper.SESSION_SYNC_STATUS, 0);
        long sessionID = localSQLiteDatabase.insert(StoodSQLHelper.TABLE_SESSION, null, databaseInfo);
        localSQLiteDatabase.close();
        stoodSQLHelper.close();
        SharedPreferences.Editor editor = mContext.getSharedPreferences(Constants.USER_SHARED_PREFERENCES, Context.MODE_PRIVATE).edit();
        editor.putLong("CurrentSession", sessionID);
        editor.commit();
    }

    public int addFitSession(int sessionType, long sessionStart) {
        Log.i(TAG, "addFitSession");
        StoodSQLHelper stoodSQLHelper = new StoodSQLHelper(mContext);
        SQLiteDatabase localSQLiteDatabase = stoodSQLHelper.getWritableDatabase();
        ContentValues databaseInfo = new ContentValues();
        databaseInfo.put(StoodSQLHelper.SESSION_TYPE, sessionType);
        databaseInfo.put(StoodSQLHelper.SESSION_START, sessionStart);
        databaseInfo.put(StoodSQLHelper.SESSION_SYNC_STATUS, 1);
        int sessionNum = (int)localSQLiteDatabase.insert(StoodSQLHelper.TABLE_SESSION, null, databaseInfo);
        localSQLiteDatabase.close();
        stoodSQLHelper.close();
        return sessionNum;
    }

    public void addFitStand(int stoodMethod, long standTime, int session){
        Log.i(TAG, "addFitStand");
        StoodSQLHelper stoodSQLHelper = new StoodSQLHelper(mContext);
        SQLiteDatabase localSQLiteDatabase = stoodSQLHelper.getWritableDatabase();
        ContentValues databaseInfo = new ContentValues();
        databaseInfo.put(StoodSQLHelper.STOOD_METHOD, stoodMethod);
        databaseInfo.put(StoodSQLHelper.STAND_TIMESTAMP, standTime);
        databaseInfo.put(StoodSQLHelper.SESSION_ID, session);
        localSQLiteDatabase.insert(StoodSQLHelper.TABLE_MAIN, null, databaseInfo);
        localSQLiteDatabase.close();
        stoodSQLHelper.close();
    }

    public int getCount(){
        StoodSQLHelper stoodSQLHelper = new StoodSQLHelper(mContext);
        String[] columns = {StoodSQLHelper.UID };
        Cursor cursor = stoodSQLHelper.getReadableDatabase().query(StoodSQLHelper.TABLE_MAIN,
                columns, null, null, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        stoodSQLHelper.close();
        return count;
    }

    public long[][] getUnsyncedSessions(){
        StoodSQLHelper stoodSQLHelper = new StoodSQLHelper(mContext);
        String[] columns = {StoodSQLHelper.UID, StoodSQLHelper.SESSION_START, StoodSQLHelper.SESSION_TYPE};
        Cursor cursor = stoodSQLHelper.getReadableDatabase().query(
                StoodSQLHelper.TABLE_SESSION,
                columns,
                StoodSQLHelper.SESSION_SYNC_STATUS + "=?",
                new String[] { "0" },
                null,
                null,
                StoodSQLHelper.SESSION_START + " DESC",
                null
        );
        cursor.moveToFirst();
        long[][] unsyncedSessions = new long[cursor.getCount()][3];
        if(!(cursor.getCount()==0)) {
            Integer row = 0;
            while (!cursor.isAfterLast()) {
                unsyncedSessions[row][0] = (long) cursor.getInt(0);
                unsyncedSessions[row][1] = Long.parseLong(cursor.getString(1));
                unsyncedSessions[row][2] = (long) cursor.getInt(2);
                row++;
                cursor.moveToNext();
            }
        }
        else {
            unsyncedSessions = new long[0][0];
        }
        cursor.close();
        stoodSQLHelper.close();
        return unsyncedSessions;
    }

    public void updateSyncedSession(Integer session){
        Log.d("updateSyncedSession", "Marking Session " + session + " as synced.");
        StoodSQLHelper stoodSQLHelper = new StoodSQLHelper(mContext);
        ContentValues databaseInfo = new ContentValues();
        databaseInfo.put(StoodSQLHelper.SESSION_SYNC_STATUS, 1);
        stoodSQLHelper.getWritableDatabase().update(
                StoodSQLHelper.TABLE_SESSION,
                databaseInfo,
                StoodSQLHelper.UID + "=?",
                new String[] { session.toString() }
        );
    }

    public long[][] getSessionStands(Integer session){
        StoodSQLHelper stoodSQLHelper = new StoodSQLHelper(mContext);
        String[] columns = {StoodSQLHelper.STOOD_METHOD, StoodSQLHelper.STAND_TIMESTAMP};
        Cursor cursor = stoodSQLHelper.getReadableDatabase().query(
                StoodSQLHelper.TABLE_MAIN,
                columns,
                StoodSQLHelper.SESSION_ID + "=?",
                new String[] { session.toString() },
                null,
                null,
                StoodSQLHelper.STAND_TIMESTAMP,
                null
        );
        cursor.moveToFirst();

        long[][] sessionArray = new long[cursor.getCount()][cursor.getColumnCount()];

        int row = 0;

        if(!(cursor.getCount()==0)) {
            while (!cursor.isAfterLast()) {
                sessionArray[row][0] = (long) cursor.getInt(0);
                sessionArray[row][1] = Long.parseLong(cursor.getString(1));
                row++;
                cursor.moveToNext();
            }
        }
        else {
            sessionArray = new long[0][0];
        }
        cursor.close();
        stoodSQLHelper.close();
        return sessionArray;
    }

    //ToDo: I don't think this does anything
    public void getLastRow(){
        StoodSQLHelper stoodSQLHelper = new StoodSQLHelper(mContext);
        Cursor cursor = stoodSQLHelper.getReadableDatabase().query(StoodSQLHelper.TABLE_MAIN,
                null, null, null, null, null, null);
        cursor.moveToLast();
        //Check to make sure there is a row; this prevents IndexOutOfBoundsException
        if(!(cursor.getCount()==0)){
                int UID = cursor.getInt(0);
                int standMethod = cursor.getInt(1);
                String timeStamp = cursor.getString(2);
            Log.i(TAG, UID + " " + standMethod + " " + timeStamp);
        }
        cursor.close();
        stoodSQLHelper.close();
    }

    private void sendAnalyticsEvent(Context context, String action){
        Tracker t = ((Application)context.getApplicationContext()).getTracker(
                Application.TrackerName.APP_TRACKER);
        t.enableAdvertisingIdCollection(true);
        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder()
                .setCategory(Constants.ALARM_PROCESS_EVENT)
                .setAction(action)
                .build());
    }

    //ToDo: rename to more generic
    public class StoodSQLHelper
            extends SQLiteOpenHelper
    {
        //ToDo: Rename vars to logs
        private static final String DATABASE_NAME = "stood_logs_database";
        private static final int DATABASE_VERSION = 1;
        private static final String TABLE_MAIN = "stood_logs_table";
        private static final String UID = "_id";
        private static final String STOOD_METHOD = "stand_type";
        private static final String STAND_TIMESTAMP = "stand_timestamp";

        private static final String TABLE_SESSION = "session_table";
        private static final String SESSION_ID = "session_id";
        private static final String SESSION_TYPE = "session_type";
        private static final String SESSION_START = "session_start";
        private static final String SESSION_SYNC_STATUS = "session_synced";

        private StoodSQLHelper(Context context)
        {
            super(context, StoodSQLHelper.DATABASE_NAME, null, DATABASE_VERSION);
        }


        public void onCreate(SQLiteDatabase sQLiteDatabase)
        {
            try
            {
                sQLiteDatabase.execSQL("CREATE TABLE " + StoodSQLHelper.TABLE_SESSION + " (" + StoodSQLHelper.UID
                        + " INTEGER PRIMARY KEY AUTOINCREMENT, " + StoodSQLHelper.SESSION_TYPE + " INTEGER, " +
                        StoodSQLHelper.SESSION_START + " INTEGER, " + StoodSQLHelper.SESSION_SYNC_STATUS + " INTEGER);");
            }
            catch (Exception localException)
            {
                localException.printStackTrace();
                Log.i("DBAdapter InnerClass SQLHelper", "Problem creating session table");
                Log.e("DBAdapter InnerClass SQLHelper", "exception:" + localException.toString());
            }

            try
            {
                sQLiteDatabase.execSQL("CREATE TABLE " + StoodSQLHelper.TABLE_MAIN + " (" + StoodSQLHelper.UID
                        + " INTEGER PRIMARY KEY AUTOINCREMENT, " + StoodSQLHelper.STOOD_METHOD + " INTEGER, " +
                        StoodSQLHelper.STAND_TIMESTAMP + " INTEGER, " + StoodSQLHelper.SESSION_ID + " INTEGER);");
            }
            catch (Exception localException)
            {
                localException.printStackTrace();
                Log.i("DBAdapter InnerClass SQLHelper", "Problem creating stood log table");
                Log.e("DBAdapter InnerClass SQLHelper", "exception:" + localException.toString());
            }

        }

        public void onUpgrade(SQLiteDatabase sQLiteDatabase, int oldVersion, int newVersion)
        {
            try
            {
                sQLiteDatabase.execSQL("DROP TABLE IF EXISTS " + StoodSQLHelper.TABLE_MAIN);
                sQLiteDatabase.execSQL("DROP TABLE IF EXISTS " + StoodSQLHelper.TABLE_SESSION);
                onCreate(sQLiteDatabase);
            }
            catch (Exception localException)
            {
                Log.e("DBAdapter InnerClass SQLHelper", "Exception caught. Error onUpgrade");
                localException.printStackTrace();
            }
        }
    }
}
