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
import java.lang.String;

/*
Note because SQL does not have a boolean datatype, for the variables of activated,
and the days of the week, 1 indicates activated/used, 0 =unactivated/unused
 */
public class AlarmsDatabaseAdapter
{

    private static final String TAG = "Database Adapter: ";
    private Context mContext;


    public AlarmsDatabaseAdapter(Context context)
    {
        mContext = context;
    }

    public long newAlarm(boolean activated, String alertType, String startTime, String endTime, int frequency,
                         String title, boolean sunday, boolean monday, boolean tuesday,
                         boolean wednesday, boolean thursday, boolean friday, boolean saturday)
    {
        Log.i(TAG, "new alarm: new database row");
        AlarmsSQLHelper alarmsSQLHelper = new AlarmsSQLHelper(mContext);
        SQLiteDatabase localSQLiteDatabase = alarmsSQLHelper.getWritableDatabase();
        ContentValues databaseInfo = new ContentValues();
        databaseInfo.put("activated", Utils.convertBooleanToInt(activated));
        databaseInfo.put("alert_type", alertType);
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
        alarmsSQLHelper.close();
        return l;
    }

    public int deleteAlarm(int rowID)
    {
        Log.i(TAG, "delete alarm: delete database row");
        AlarmsSQLHelper alarmsSQLHelper = new AlarmsSQLHelper(mContext);
        SQLiteDatabase alarmsDatabase = alarmsSQLHelper.getWritableDatabase();
        //Need to create a string array for the whereArgs, which determine row(s) to update
        String[] arrayOfString = new String[1];
        arrayOfString[0] = Integer.toString(rowID);
        int count = alarmsDatabase.delete(AlarmsSQLHelper.TABLE_MAIN,
                AlarmsSQLHelper.UID + " = ?", arrayOfString);
        alarmsDatabase.close();
        alarmsSQLHelper.close();
        return  count;
    }

    public int editAlarm(boolean activated, String alertType, String startTime, String endTime, int frequency,
                         String title, boolean sunday, boolean monday, boolean tuesday,
                         boolean wednesday, boolean thursday, boolean friday, boolean saturday, int rowID)
    {
        Log.i(TAG, "edit alarm: edit database row");
        AlarmsSQLHelper alarmsSQLHelper = new AlarmsSQLHelper(mContext);
        SQLiteDatabase alarmsDatabase = alarmsSQLHelper.getWritableDatabase();
        ContentValues databaseInfo = new ContentValues();
        databaseInfo.put(AlarmsSQLHelper.ACTIVATED, Utils.convertBooleanToInt(activated));
        databaseInfo.put(AlarmsSQLHelper.ALERT_TYPE, alertType);
        databaseInfo.put(AlarmsSQLHelper.START_TIME, startTime);
        databaseInfo.put(AlarmsSQLHelper.END_TIME, endTime);
        databaseInfo.put(AlarmsSQLHelper.FREQUENCY, frequency);
        databaseInfo.put(AlarmsSQLHelper.TITLE, title);
        databaseInfo.put(AlarmsSQLHelper.SUNDAY, Utils.convertBooleanToInt(sunday));
        databaseInfo.put(AlarmsSQLHelper.MONDAY, Utils.convertBooleanToInt(monday));
        databaseInfo.put(AlarmsSQLHelper.TUESDAY, Utils.convertBooleanToInt(tuesday));
        databaseInfo.put(AlarmsSQLHelper.WEDNESDAY, Utils.convertBooleanToInt(wednesday));
        databaseInfo.put(AlarmsSQLHelper.THURSDAY, Utils.convertBooleanToInt(thursday));
        databaseInfo.put(AlarmsSQLHelper.FRIDAY, Utils.convertBooleanToInt(friday));
        databaseInfo.put(AlarmsSQLHelper.SATURDAY, Utils.convertBooleanToInt(saturday));
        //Need to create a string array for the whereArgs, which determine row(s) to update
        String[] arrayOfString = new String[1];
        arrayOfString[0] = Integer.toString(rowID);
        int count = alarmsDatabase.update(AlarmsSQLHelper.TABLE_MAIN, databaseInfo,
                AlarmsSQLHelper.UID + "=? ", arrayOfString);
        alarmsDatabase.close();
        alarmsSQLHelper.close();
        return count;
    }

    public ArrayList<AlarmSchedule> getAlarmSchedules() {
        ArrayList<AlarmSchedule> alarmSchedules = new ArrayList<AlarmSchedule>();
        AlarmsSQLHelper alarmsSQLHelper = new AlarmsSQLHelper(mContext);
        Cursor cursor = alarmsSQLHelper.getWritableDatabase().query(AlarmsSQLHelper.TABLE_MAIN,
                null, null, null, null, null, null);
        cursor.moveToFirst();
        //Check to make sure there is a row; this prevents IndexOutOfBoundsException
        if(!(cursor.getCount()==0)){
            do {
                int UID = cursor.getInt(0);
                boolean activated = Utils.convertIntToBoolean(cursor.getInt(1));
                String alertType = cursor.getString(2);
                Calendar startTime = Utils.convertToCalendarTime(cursor.getString(3));
                Calendar endTime = Utils.convertToCalendarTime(cursor.getString(4));
                int frequency = cursor.getInt(5);
                String title = cursor.getString(6);
                boolean sunday = Utils.convertIntToBoolean(cursor.getInt(7));
                boolean monday = Utils.convertIntToBoolean(cursor.getInt(8));
                boolean tuesday = Utils.convertIntToBoolean(cursor.getInt(9));
                boolean wednesday = Utils.convertIntToBoolean(cursor.getInt(10));
                boolean thursday = Utils.convertIntToBoolean(cursor.getInt(11));
                boolean friday = Utils.convertIntToBoolean(cursor.getInt(12));
                boolean saturday = Utils.convertIntToBoolean(cursor.getInt(13));
                AlarmSchedule alarmSchedule = new AlarmSchedule(UID, activated, alertType, startTime, endTime,
                        frequency, title, sunday, monday, tuesday, wednesday, thursday, friday, saturday);
                alarmSchedules.add(alarmSchedule);
            } while (cursor.moveToNext());
        }
        alarmsSQLHelper.close();
        cursor.close();
        return alarmSchedules;
    }

    public boolean[] getAlreadyTakenAlarmDays(){
        //If 0 it is unactivated, if 1 it is activated
        boolean[] activatedDays = {false, false, false, false, false, false, false};
        AlarmsSQLHelper alarmsSQLHelper = new AlarmsSQLHelper(mContext);
        String[] columns = {AlarmsSQLHelper.SUNDAY, AlarmsSQLHelper.MONDAY,
                AlarmsSQLHelper.TUESDAY, AlarmsSQLHelper.WEDNESDAY, AlarmsSQLHelper.THURSDAY,
                AlarmsSQLHelper.FRIDAY, AlarmsSQLHelper.SATURDAY};
        Cursor cursor = alarmsSQLHelper.getWritableDatabase().query(AlarmsSQLHelper.TABLE_MAIN,
                columns, null, null, null, null, null);
        if(cursor != null && cursor.getCount() > 0){
            for(int column = 0; column <7; column++){
                cursor.moveToFirst();
                do{
                    if(cursor.getInt(column)==1){
                        activatedDays[column]=true;
                    }
                } while(cursor.moveToNext()&&activatedDays[column]);
            }
            cursor.close();
        }
        alarmsSQLHelper.close();
        return activatedDays;
    }

    public int getLastRowID(){
        AlarmsSQLHelper alarmsSQLHelper = new AlarmsSQLHelper(mContext);
        String selectQuery = "SELECT  * FROM " + AlarmsSQLHelper.TABLE_MAIN + " ORDER BY " +
                AlarmsSQLHelper.UID + " DESC LIMIT 1;";
        Cursor cursor = alarmsSQLHelper.getWritableDatabase().rawQuery(selectQuery, null);
        cursor.moveToFirst();
        int lastRowID = cursor.getInt(0);
        alarmsSQLHelper.close();
        cursor.close();
        return lastRowID;
    }

    public int getCount()
    {
        AlarmsSQLHelper alarmsSQLHelper = new AlarmsSQLHelper(mContext);
        int count = alarmsSQLHelper.getWritableDatabase().query("alarms_table", new String[] { "_id" }, null, null, null, null, null).getCount();
        alarmsSQLHelper.close();
        return count;
    }








    /*
    *
    *
    *   Nested Main Database Class
    *
    *
    *
     */
    public class AlarmsSQLHelper
            extends SQLiteOpenHelper
    {
        private static final String DATABASE_NAME = "alarms_database";
        private static final int DATABASE_VERSION = 1;
        private static final String TABLE_MAIN = "alarms_table";
        private static final String UID = "_id";
        private static final String ACTIVATED = "activated";
        private static final String ALERT_TYPE = "alert_type";
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


        private AlarmsSQLHelper(Context context)
        {
            super(context, AlarmsSQLHelper.DATABASE_NAME, null, DATABASE_VERSION);
        }


        public void onCreate(SQLiteDatabase sQLiteDatabase)
        {
            try
            {
                sQLiteDatabase.execSQL("CREATE TABLE " + AlarmsSQLHelper.TABLE_MAIN + " (" + AlarmsSQLHelper.UID
                        + " INTEGER PRIMARY KEY AUTOINCREMENT, " + AlarmsSQLHelper.ACTIVATED + " INTEGER, " +
                        AlarmsSQLHelper.ALERT_TYPE + " TEXT, " + AlarmsSQLHelper.START_TIME + " TEXT, " +
                        AlarmsSQLHelper.END_TIME + " TEXT, " + AlarmsSQLHelper.FREQUENCY + " INTEGER, " +
                        AlarmsSQLHelper.TITLE + " TEXT, " + AlarmsSQLHelper.SUNDAY + " INTEGER, " +
                        AlarmsSQLHelper.MONDAY + " INTEGER, " + AlarmsSQLHelper.TUESDAY + " INTEGER, " +
                        AlarmsSQLHelper.WEDNESDAY + " INTEGER, " + AlarmsSQLHelper.THURSDAY + " INTEGER, " +
                        AlarmsSQLHelper.FRIDAY + " INTEGER, " + AlarmsSQLHelper.SATURDAY + " INTEGER);");
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
                sQLiteDatabase.execSQL("DROP TABLE IF EXISTS " + AlarmsSQLHelper.TABLE_MAIN);
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

