/*
 * Copyright (C) 2014 Sean Allen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sean.takeastand.storage;

/**
 * Created by Sean on 2014-09-03.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.sean.takeastand.util.Utils;

import java.util.ArrayList;
import java.util.Calendar;
/* All the alarm schedules are saved in a SQLite database.  This class provides methods for
 communicating with the SQLite database in order to retrieve, edit, and delete information.
 This class and its methods are primarily used by the ScheduleEditor.  The main exception is it’s
 getFixedAlarmSchedules which is called by the StartScheduleReceiver and the BootReceiver.*/

public class ScheduleDatabaseAdapter
{

    private static final String TAG = "Schedule Database Adapter: ";
    private Context mContext;


    public ScheduleDatabaseAdapter(Context context)
    {
        mContext = context;
    }

    public long newAlarm(boolean activated, boolean led, boolean vibrate, boolean sound,
                         String startTime, String endTime, int frequency,
                         String title, boolean sunday, boolean monday, boolean tuesday,
                         boolean wednesday, boolean thursday, boolean friday, boolean saturday)
    {
        Log.i(TAG, "new alarm: new database row");
        ScheduleSQLHelper scheduleSQLHelper = new ScheduleSQLHelper(mContext);
        SQLiteDatabase localSQLiteDatabase = scheduleSQLHelper.getWritableDatabase();
        ContentValues databaseInfo = new ContentValues();
        databaseInfo.put("activated", Utils.convertBooleanToInt(activated));
        databaseInfo.put("alert_led", Utils.convertBooleanToInt(led));
        databaseInfo.put("alert_vibrate", Utils.convertBooleanToInt(vibrate));
        databaseInfo.put("alert_sound", Utils.convertBooleanToInt(sound));
        databaseInfo.put("start_time", startTime);
        databaseInfo.put("end_time", endTime);
        databaseInfo.put("frequency", frequency);
        databaseInfo.put("title", title);
        databaseInfo.put("sunday", Utils.convertBooleanToInt(sunday));
        databaseInfo.put("monday", Utils.convertBooleanToInt(monday));
        databaseInfo.put("tuesday", Utils.convertBooleanToInt(tuesday));
        databaseInfo.put("wednesday", Utils.convertBooleanToInt(wednesday));
        databaseInfo.put("thursday", Utils.convertBooleanToInt(thursday));
        databaseInfo.put("friday", Utils.convertBooleanToInt(friday));
        databaseInfo.put("saturday", Utils.convertBooleanToInt(saturday));
        long l = localSQLiteDatabase.insert("alarms_table", null, databaseInfo);
        localSQLiteDatabase.close();
        scheduleSQLHelper.close();
        return l;
    }

    public int deleteAlarm(int rowID)
    {
        Log.i(TAG, "delete alarm: delete database row");
        ScheduleSQLHelper scheduleSQLHelper = new ScheduleSQLHelper(mContext);
        SQLiteDatabase alarmsDatabase = scheduleSQLHelper.getWritableDatabase();
        //Need to create a string array for the whereArgs, which determine row(s) to update
        String[] arrayOfString = new String[1];
        arrayOfString[0] = Integer.toString(rowID);
        int count = alarmsDatabase.delete(ScheduleSQLHelper.TABLE_MAIN,
                ScheduleSQLHelper.UID + " = ?", arrayOfString);
        alarmsDatabase.close();
        scheduleSQLHelper.close();
        return  count;
    }

    //This method may be deletable; no longer used in interactive listview
    public int editAlarm(boolean activated, boolean led, boolean vibrate, boolean sound,
                         String startTime, String endTime, int frequency,
                         String title, boolean sunday, boolean monday, boolean tuesday,
                         boolean wednesday, boolean thursday, boolean friday, boolean saturday, int rowID)
    {
        Log.i(TAG, "edit alarm: edit database row");
        ScheduleSQLHelper scheduleSQLHelper = new ScheduleSQLHelper(mContext);
        SQLiteDatabase alarmsDatabase = scheduleSQLHelper.getWritableDatabase();
        ContentValues databaseInfo = new ContentValues();
        databaseInfo.put(ScheduleSQLHelper.ACTIVATED, Utils.convertBooleanToInt(activated));
        databaseInfo.put(ScheduleSQLHelper.ALERT_LED, Utils.convertBooleanToInt(led));
        databaseInfo.put(ScheduleSQLHelper.ALERT_VIBRATE, Utils.convertBooleanToInt(vibrate));
        databaseInfo.put(ScheduleSQLHelper.ALERT_SOUND, Utils.convertBooleanToInt(sound));
        databaseInfo.put(ScheduleSQLHelper.START_TIME, startTime);
        databaseInfo.put(ScheduleSQLHelper.END_TIME, endTime);
        databaseInfo.put(ScheduleSQLHelper.FREQUENCY, frequency);
        databaseInfo.put(ScheduleSQLHelper.TITLE, title);
        databaseInfo.put(ScheduleSQLHelper.SUNDAY, Utils.convertBooleanToInt(sunday));
        databaseInfo.put(ScheduleSQLHelper.MONDAY, Utils.convertBooleanToInt(monday));
        databaseInfo.put(ScheduleSQLHelper.TUESDAY, Utils.convertBooleanToInt(tuesday));
        databaseInfo.put(ScheduleSQLHelper.WEDNESDAY, Utils.convertBooleanToInt(wednesday));
        databaseInfo.put(ScheduleSQLHelper.THURSDAY, Utils.convertBooleanToInt(thursday));
        databaseInfo.put(ScheduleSQLHelper.FRIDAY, Utils.convertBooleanToInt(friday));
        databaseInfo.put(ScheduleSQLHelper.SATURDAY, Utils.convertBooleanToInt(saturday));
        //Need to create a string array for the whereArgs, which determine row(s) to update
        String[] arrayOfString = new String[1];
        arrayOfString[0] = Integer.toString(rowID);
        int count = alarmsDatabase.update(ScheduleSQLHelper.TABLE_MAIN, databaseInfo,
                ScheduleSQLHelper.UID + "=? ", arrayOfString);
        alarmsDatabase.close();
        scheduleSQLHelper.close();
        return count;
    }

    public int updateActivated(int UID, boolean activated) {
        Log.i(TAG, "Updating Activated");
        ScheduleSQLHelper scheduleSQLHelper = new ScheduleSQLHelper(mContext);
        SQLiteDatabase scheduleDatabase = scheduleSQLHelper.getWritableDatabase();
        ContentValues updatedInfo = new ContentValues();
        updatedInfo.put(ScheduleSQLHelper.ACTIVATED, Utils.convertBooleanToInt(activated));
        //Need to create a string array for the whereArgs, which determine row(s) to update
        String[] arrayOfString = new String[1];
        arrayOfString[0] = Integer.toString(UID);
        int count = scheduleDatabase.update(ScheduleSQLHelper.TABLE_MAIN, updatedInfo,
                ScheduleSQLHelper.UID + "=? ", arrayOfString);
        scheduleDatabase.close();
        scheduleSQLHelper.close();
        return count;
    }

    public int updateAlertType(int UID, boolean led, boolean vibrate, boolean sound) {
        Log.i(TAG, "Updating AlertTypes");
        ScheduleSQLHelper scheduleSQLHelper = new ScheduleSQLHelper(mContext);
        SQLiteDatabase scheduleDatabase = scheduleSQLHelper.getWritableDatabase();
        ContentValues updatedInfo = new ContentValues();
        updatedInfo.put(ScheduleSQLHelper.ALERT_LED, Utils.convertBooleanToInt(led));
        updatedInfo.put(ScheduleSQLHelper.ALERT_VIBRATE, Utils.convertBooleanToInt(vibrate));
        updatedInfo.put(ScheduleSQLHelper.ALERT_SOUND, Utils.convertBooleanToInt(sound));
        String[] arrayOfString = new String[1];
        arrayOfString[0] = Integer.toString(UID);
        int count = scheduleDatabase.update(ScheduleSQLHelper.TABLE_MAIN, updatedInfo,
                ScheduleSQLHelper.UID + "=? ", arrayOfString);
        scheduleDatabase.close();
        scheduleSQLHelper.close();
        return count;
    }

    public int updateStartTime(int UID, String startTime) {
        Log.i(TAG, "Updating StartTime");
        ScheduleSQLHelper scheduleSQLHelper = new ScheduleSQLHelper(mContext);
        SQLiteDatabase scheduleDatabase = scheduleSQLHelper.getWritableDatabase();
        ContentValues updatedInfo = new ContentValues();
        updatedInfo.put(ScheduleSQLHelper.START_TIME, startTime);
        String[] arrayOfString = new String[1];
        arrayOfString[0] = Integer.toString(UID);
        int count = scheduleDatabase.update(ScheduleSQLHelper.TABLE_MAIN, updatedInfo,
                ScheduleSQLHelper.UID + "=? ", arrayOfString);
        scheduleDatabase.close();
        scheduleSQLHelper.close();
        return count;
    }

    public int updateEndTime(int UID, String endTime) {
        Log.i(TAG, "Updating EndTime");
        ScheduleSQLHelper scheduleSQLHelper = new ScheduleSQLHelper(mContext);
        SQLiteDatabase scheduleDatabase = scheduleSQLHelper.getWritableDatabase();
        ContentValues updatedInfo = new ContentValues();
        updatedInfo.put(ScheduleSQLHelper.END_TIME, endTime);
        String[] arrayOfString = new String[1];
        arrayOfString[0] = Integer.toString(UID);
        int count = scheduleDatabase.update(ScheduleSQLHelper.TABLE_MAIN, updatedInfo,
                ScheduleSQLHelper.UID + "=? ", arrayOfString);
        scheduleDatabase.close();
        scheduleSQLHelper.close();
        return count;
    }

    public int updateFrequency(int UID, int frequency) {
        Log.i(TAG, "Updating Frequency");
        ScheduleSQLHelper scheduleSQLHelper = new ScheduleSQLHelper(mContext);
        SQLiteDatabase scheduleDatabase = scheduleSQLHelper.getWritableDatabase();
        ContentValues updatedInfo = new ContentValues();
        updatedInfo.put(ScheduleSQLHelper.FREQUENCY, frequency);
        String[] arrayOfString = new String[1];
        arrayOfString[0] = Integer.toString(UID);
        int count = scheduleDatabase.update(ScheduleSQLHelper.TABLE_MAIN, updatedInfo,
                ScheduleSQLHelper.UID + "=? ", arrayOfString);
        scheduleDatabase.close();
        scheduleSQLHelper.close();
        return count;
    }

    public int updateTitle(int UID, String title) {
        Log.i(TAG, "Updating Title");
        ScheduleSQLHelper scheduleSQLHelper = new ScheduleSQLHelper(mContext);
        SQLiteDatabase scheduleDatabase = scheduleSQLHelper.getWritableDatabase();
        ContentValues updatedInfo = new ContentValues();
        updatedInfo.put(ScheduleSQLHelper.TITLE, title);
        String[] arrayOfString = new String[1];
        arrayOfString[0] = Integer.toString(UID);
        int count = scheduleDatabase.update(ScheduleSQLHelper.TABLE_MAIN, updatedInfo,
                ScheduleSQLHelper.UID + "=? ", arrayOfString);
        scheduleDatabase.close();
        scheduleSQLHelper.close();
        return count;
    }


    public int updateSunday(int UID, boolean sunday) {
        Log.i(TAG, "Updating Sunday");
        ScheduleSQLHelper scheduleSQLHelper = new ScheduleSQLHelper(mContext);
        SQLiteDatabase scheduleDatabase = scheduleSQLHelper.getWritableDatabase();
        ContentValues updatedInfo = new ContentValues();
        updatedInfo.put(ScheduleSQLHelper.SUNDAY, Utils.convertBooleanToInt(sunday));
        String[] arrayOfString = new String[1];
        arrayOfString[0] = Integer.toString(UID);
        int count = scheduleDatabase.update(ScheduleSQLHelper.TABLE_MAIN, updatedInfo,
                ScheduleSQLHelper.UID + "=? ", arrayOfString);
        scheduleDatabase.close();
        scheduleSQLHelper.close();
        return count;
    }

    public int updateMonday(int UID, boolean monday) {
        Log.i(TAG, "Updating Monday");
        ScheduleSQLHelper scheduleSQLHelper = new ScheduleSQLHelper(mContext);
        SQLiteDatabase scheduleDatabase = scheduleSQLHelper.getWritableDatabase();
        ContentValues updatedInfo = new ContentValues();
        updatedInfo.put(ScheduleSQLHelper.MONDAY, Utils.convertBooleanToInt(monday));
        String[] arrayOfString = new String[1];
        arrayOfString[0] = Integer.toString(UID);
        int count = scheduleDatabase.update(ScheduleSQLHelper.TABLE_MAIN, updatedInfo,
                ScheduleSQLHelper.UID + "=? ", arrayOfString);
        scheduleDatabase.close();
        scheduleSQLHelper.close();
        return count;
    }

    public int updateTuesday(int UID, boolean tuesday) {
        Log.i(TAG, "Updating Tuesday");
        ScheduleSQLHelper scheduleSQLHelper = new ScheduleSQLHelper(mContext);
        SQLiteDatabase scheduleDatabase = scheduleSQLHelper.getWritableDatabase();
        ContentValues updatedInfo = new ContentValues();
        updatedInfo.put(ScheduleSQLHelper.TUESDAY, Utils.convertBooleanToInt(tuesday));
        String[] arrayOfString = new String[1];
        arrayOfString[0] = Integer.toString(UID);
        int count = scheduleDatabase.update(ScheduleSQLHelper.TABLE_MAIN, updatedInfo,
                ScheduleSQLHelper.UID + "=? ", arrayOfString);
        scheduleDatabase.close();
        scheduleSQLHelper.close();
        return count;
    }

    public int updateWednesday(int UID, boolean wednesday) {
        Log.i(TAG, "Updating Wednesday");
        ScheduleSQLHelper scheduleSQLHelper = new ScheduleSQLHelper(mContext);
        SQLiteDatabase scheduleDatabase = scheduleSQLHelper.getWritableDatabase();
        ContentValues updatedInfo = new ContentValues();
        updatedInfo.put(ScheduleSQLHelper.WEDNESDAY, Utils.convertBooleanToInt(wednesday));
        String[] arrayOfString = new String[1];
        arrayOfString[0] = Integer.toString(UID);
        int count = scheduleDatabase.update(ScheduleSQLHelper.TABLE_MAIN, updatedInfo,
                ScheduleSQLHelper.UID + "=? ", arrayOfString);
        scheduleDatabase.close();
        scheduleSQLHelper.close();
        return count;
    }

    public int updateThursday(int UID, boolean thursday) {
        Log.i(TAG, "Updating Thursday");
        ScheduleSQLHelper scheduleSQLHelper = new ScheduleSQLHelper(mContext);
        SQLiteDatabase scheduleDatabase = scheduleSQLHelper.getWritableDatabase();
        ContentValues updatedInfo = new ContentValues();
        updatedInfo.put(ScheduleSQLHelper.THURSDAY, Utils.convertBooleanToInt(thursday));
        String[] arrayOfString = new String[1];
        arrayOfString[0] = Integer.toString(UID);
        int count = scheduleDatabase.update(ScheduleSQLHelper.TABLE_MAIN, updatedInfo,
                ScheduleSQLHelper.UID + "=? ", arrayOfString);
        scheduleDatabase.close();
        scheduleSQLHelper.close();
        return count;
    }

    public int updateFriday(int UID, boolean friday) {
        Log.i(TAG, "Updating Friday");
        ScheduleSQLHelper scheduleSQLHelper = new ScheduleSQLHelper(mContext);
        SQLiteDatabase scheduleDatabase = scheduleSQLHelper.getWritableDatabase();
        ContentValues updatedInfo = new ContentValues();
        updatedInfo.put(ScheduleSQLHelper.FRIDAY, Utils.convertBooleanToInt(friday));
        String[] arrayOfString = new String[1];
        arrayOfString[0] = Integer.toString(UID);
        int count = scheduleDatabase.update(ScheduleSQLHelper.TABLE_MAIN, updatedInfo,
                ScheduleSQLHelper.UID + "=? ", arrayOfString);
        scheduleDatabase.close();
        scheduleSQLHelper.close();
        return count;
    }

    public int updateSaturday(int UID, boolean saturday) {
        Log.i(TAG, "Updating Saturday");
        ScheduleSQLHelper scheduleSQLHelper = new ScheduleSQLHelper(mContext);
        SQLiteDatabase scheduleDatabase = scheduleSQLHelper.getWritableDatabase();
        ContentValues updatedInfo = new ContentValues();
        updatedInfo.put(ScheduleSQLHelper.SATURDAY, Utils.convertBooleanToInt(saturday));
        String[] arrayOfString = new String[1];
        arrayOfString[0] = Integer.toString(UID);
        int count = scheduleDatabase.update(ScheduleSQLHelper.TABLE_MAIN, updatedInfo,
                ScheduleSQLHelper.UID + "=? ", arrayOfString);
        scheduleDatabase.close();
        scheduleSQLHelper.close();
        return count;
    }

    public ArrayList<AlarmSchedule> getAlarmSchedules() {
        ArrayList<AlarmSchedule> alarmSchedules = new ArrayList<AlarmSchedule>();
        ScheduleSQLHelper scheduleSQLHelper = new ScheduleSQLHelper(mContext);
        Cursor cursor = scheduleSQLHelper.getWritableDatabase().query(ScheduleSQLHelper.TABLE_MAIN,
                null, null, null, null, null, null);
        cursor.moveToFirst();
        //Check to make sure there is a row; this prevents IndexOutOfBoundsException
        if(!(cursor.getCount()==0)){
            do {
                int UID = cursor.getInt(0);
                Log.i(TAG, "Row UID " + Integer.toString(UID));
                boolean activated = Utils.convertIntToBoolean(cursor.getInt(1));
                boolean led = Utils.convertIntToBoolean(cursor.getInt(2));
                boolean vibrate = Utils.convertIntToBoolean(cursor.getInt(3));
                boolean sound = Utils.convertIntToBoolean(cursor.getInt(4));
                Calendar startTime = Utils.convertToCalendarTime(cursor.getString(5), mContext);
                Calendar endTime = Utils.convertToCalendarTime(cursor.getString(6), mContext);
                int frequency = cursor.getInt(7);
                String title = cursor.getString(8);
                boolean sunday = Utils.convertIntToBoolean(cursor.getInt(9));
                boolean monday = Utils.convertIntToBoolean(cursor.getInt(10));
                boolean tuesday = Utils.convertIntToBoolean(cursor.getInt(11));
                boolean wednesday = Utils.convertIntToBoolean(cursor.getInt(12));
                boolean thursday = Utils.convertIntToBoolean(cursor.getInt(13));
                boolean friday = Utils.convertIntToBoolean(cursor.getInt(14));
                boolean saturday = Utils.convertIntToBoolean(cursor.getInt(15));
                AlarmSchedule alarmSchedule = new AlarmSchedule(UID, activated, led, vibrate, sound,
                        startTime, endTime, frequency, title, sunday, monday, tuesday, wednesday,
                        thursday, friday, saturday);
                alarmSchedules.add(alarmSchedule);
            } while (cursor.moveToNext());
        }
        scheduleSQLHelper.close();
        cursor.close();
        return alarmSchedules;
    }

    public ArrayList<FixedAlarmSchedule> getFixedAlarmSchedules(){
        ArrayList<FixedAlarmSchedule> fixedAlarmSchedules = new ArrayList<FixedAlarmSchedule>();
        ScheduleSQLHelper scheduleSQLHelper = new ScheduleSQLHelper(mContext);
        Cursor cursor = scheduleSQLHelper.getWritableDatabase().query(ScheduleSQLHelper.TABLE_MAIN,
                null, null, null, null, null, null);
        cursor.moveToFirst();
        //Check to make sure there is a row; this prevents IndexOutOfBoundsException
        if(!(cursor.getCount()==0)){
            do {
                int UID = cursor.getInt(0);
                Log.i(TAG, "Row UID " + Integer.toString(UID));
                boolean activated = Utils.convertIntToBoolean(cursor.getInt(1));
                boolean led = Utils.convertIntToBoolean(cursor.getInt(2));
                boolean vibrate = Utils.convertIntToBoolean(cursor.getInt(3));
                boolean sound = Utils.convertIntToBoolean(cursor.getInt(4));
                Calendar startTime = Utils.convertToCalendarTime(cursor.getString(5), mContext);
                Calendar endTime = Utils.convertToCalendarTime(cursor.getString(6), mContext);
                int frequency = cursor.getInt(7);
                String title = cursor.getString(8);
                boolean sunday = Utils.convertIntToBoolean(cursor.getInt(9));
                boolean monday = Utils.convertIntToBoolean(cursor.getInt(10));
                boolean tuesday = Utils.convertIntToBoolean(cursor.getInt(11));
                boolean wednesday = Utils.convertIntToBoolean(cursor.getInt(12));
                boolean thursday = Utils.convertIntToBoolean(cursor.getInt(13));
                boolean friday = Utils.convertIntToBoolean(cursor.getInt(14));
                boolean saturday = Utils.convertIntToBoolean(cursor.getInt(15));
                AlarmSchedule alarmSchedule = new AlarmSchedule(UID, activated, led, vibrate, sound,
                        startTime, endTime, frequency, title, sunday, monday, tuesday, wednesday,
                        thursday, friday, saturday);
                fixedAlarmSchedules.add(new FixedAlarmSchedule(alarmSchedule));
            } while (cursor.moveToNext());
        }
        scheduleSQLHelper.close();
        cursor.close();
        return fixedAlarmSchedules;
    }

    public boolean[] getAlreadyTakenAlarmDays(){
        //If 0 it is unactivated, if 1 it is activated
        boolean[] activatedDays = {false, false, false, false, false, false, false};
        ScheduleSQLHelper scheduleSQLHelper = new ScheduleSQLHelper(mContext);
        String[] columns = {ScheduleSQLHelper.SUNDAY, ScheduleSQLHelper.MONDAY,
                ScheduleSQLHelper.TUESDAY, ScheduleSQLHelper.WEDNESDAY, ScheduleSQLHelper.THURSDAY,
                ScheduleSQLHelper.FRIDAY, ScheduleSQLHelper.SATURDAY};
        Cursor cursor = scheduleSQLHelper.getWritableDatabase().query(ScheduleSQLHelper.TABLE_MAIN,
                columns, null, null, null, null, null);
        if(cursor != null && cursor.getCount() > 0){
            cursor.moveToFirst();
            do{
                //Purpose is to go through each row's 7 columns and mark which days are
                //true, which means they are already taken
                for(int index = 0; index <7; index++){
                    if(cursor.getInt(index)==1){
                        activatedDays[index]=true;
                    }
                }
            } while(cursor.moveToNext());
            cursor.close();
        }
        scheduleSQLHelper.close();
        return activatedDays;
    }

    public int getLastRowID(){
        ScheduleSQLHelper scheduleSQLHelper = new ScheduleSQLHelper(mContext);
        String selectQuery = "SELECT  * FROM " + ScheduleSQLHelper.TABLE_MAIN + " ORDER BY " +
                ScheduleSQLHelper.UID + " DESC LIMIT 1;";
        Cursor cursor = scheduleSQLHelper.getWritableDatabase().rawQuery(selectQuery, null);
        cursor.moveToFirst();
        int lastRowID = cursor.getInt(0);
        scheduleSQLHelper.close();
        cursor.close();
        return lastRowID;
    }

    public int getCount()
    {
        ScheduleSQLHelper scheduleSQLHelper = new ScheduleSQLHelper(mContext);
        int count = scheduleSQLHelper.getWritableDatabase().query("alarms_table", new String[]
                { "_id" }, null, null, null, null, null).getCount();
        scheduleSQLHelper.close();
        return count;
    }

    /*public AlarmSchedule getSpecificAlarmSchedule(int UID){
        AlarmSchedule alarmSchedule;
        ScheduleSQLHelper scheduleSQLHelper = new ScheduleSQLHelper(mContext);
        //Need to create a string array for the whereArgs, which determine row(s) to update
        String[] arrayOfString = new String[1];
        arrayOfString[0] = Integer.toString(UID);
        Cursor cursor = scheduleSQLHelper.getWritableDatabase().query(ScheduleSQLHelper.TABLE_MAIN,
                null, null, null, null, null, null);
        //Check to make sure there is a row; this prevents IndexOutOfBoundsException
        if(!(cursor.getCount()==0)){
                Log.i(TAG, "Row UID " + Integer.toString(UID));
                boolean activated = Utils.convertIntToBoolean(cursor.getInt(1));
                boolean led = Utils.convertIntToBoolean(cursor.getInt(2));
                boolean vibrate = Utils.convertIntToBoolean(cursor.getInt(3));
                boolean sound = Utils.convertIntToBoolean(cursor.getInt(4));
                Calendar startTime = Utils.convertToCalendarTime(cursor.getString(5), mContext);
                Calendar endTime = Utils.convertToCalendarTime(cursor.getString(6), mContext);
                int frequency = cursor.getInt(7);
                String title = cursor.getString(8);
                boolean sunday = Utils.convertIntToBoolean(cursor.getInt(9));
                boolean monday = Utils.convertIntToBoolean(cursor.getInt(10));
                boolean tuesday = Utils.convertIntToBoolean(cursor.getInt(11));
                boolean wednesday = Utils.convertIntToBoolean(cursor.getInt(12));
                boolean thursday = Utils.convertIntToBoolean(cursor.getInt(13));
                boolean friday = Utils.convertIntToBoolean(cursor.getInt(14));
                boolean saturday = Utils.convertIntToBoolean(cursor.getInt(15));
                alarmSchedule = new AlarmSchedule(UID, activated, led, vibrate, sound,
                        startTime, endTime, frequency, title, sunday, monday, tuesday, wednesday,
                        thursday, friday, saturday);
            Log.i(TAG, "Activated" + Boolean.toString(alarmSchedule.getActivated()));
            Log.i(TAG, "Sunday" + Boolean.toString(alarmSchedule.getSunday()));
            Log.i(TAG, "Monday" + Boolean.toString(alarmSchedule.getMonday()));
            Log.i(TAG, "Title" + alarmSchedule.getTitle());
            scheduleSQLHelper.close();
            cursor.close();
            return alarmSchedule;
        }
        scheduleSQLHelper.close();
        cursor.close();
        return null;
    }*/

    /*
    *
    *
    *   Nested Main Database Class
    *
    *
    *
     */
    /*
    Note because SQL does not have a boolean datatype, for the variables of activated,
    and the days of the week, 1 indicates activated/used, 0 =unactivated/unused
    */
    public class ScheduleSQLHelper
            extends SQLiteOpenHelper
    {
        private static final String DATABASE_NAME = "schedules_database";
        private static final int DATABASE_VERSION = 1;
        private static final String TABLE_MAIN = "alarms_table";
        private static final String UID = "_id";
        private static final String ACTIVATED = "activated";
        private static final String ALERT_LED = "alert_led";
        private static final String ALERT_VIBRATE = "alert_vibrate";
        private static final String ALERT_SOUND = "alert_sound";
        private static final String START_TIME = "start_time";
        private static final String END_TIME = "end_time";
        private static final String FREQUENCY = "frequency";
        private static final String TITLE = "title";
        private static final String SUNDAY = "sunday";
        private static final String MONDAY = "monday";
        private static final String TUESDAY = "tuesday";
        private static final String WEDNESDAY = "wednesday";
        private static final String THURSDAY = "thursday";
        private static final String FRIDAY = "friday";
        private static final String SATURDAY = "saturday";


        private ScheduleSQLHelper(Context context)
        {
            super(context, ScheduleSQLHelper.DATABASE_NAME, null, DATABASE_VERSION);
        }


        public void onCreate(SQLiteDatabase sQLiteDatabase)
        {
            try
            {
                sQLiteDatabase.execSQL("CREATE TABLE " + ScheduleSQLHelper.TABLE_MAIN + " (" + ScheduleSQLHelper.UID
                        + " INTEGER PRIMARY KEY AUTOINCREMENT, " + ScheduleSQLHelper.ACTIVATED + " INTEGER, " +
                        ScheduleSQLHelper.ALERT_LED + " INTEGER, " + ScheduleSQLHelper.ALERT_VIBRATE + " INTEGER, " +
                        ScheduleSQLHelper.ALERT_SOUND + " INTEGER, " + ScheduleSQLHelper.START_TIME + " TEXT, " +
                        ScheduleSQLHelper.END_TIME + " TEXT, " + ScheduleSQLHelper.FREQUENCY + " INTEGER, " +
                        ScheduleSQLHelper.TITLE + " TEXT, " + ScheduleSQLHelper.SUNDAY + " INTEGER, " +
                        ScheduleSQLHelper.MONDAY + " INTEGER, " + ScheduleSQLHelper.TUESDAY + " INTEGER, " +
                        ScheduleSQLHelper.WEDNESDAY + " INTEGER, " + ScheduleSQLHelper.THURSDAY + " INTEGER, " +
                        ScheduleSQLHelper.FRIDAY + " INTEGER, " + ScheduleSQLHelper.SATURDAY + " INTEGER);");
            }
            catch (Exception localException)
            {
                localException.printStackTrace();
                Log.i("DBAdapter InnerClass SQLHelper", "Problem creating tables");
            }
        }

        public void onUpgrade(SQLiteDatabase sQLiteDatabase, int oldVersion, int newVersion)
        {
            try
            {
                sQLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ScheduleSQLHelper.TABLE_MAIN);
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

