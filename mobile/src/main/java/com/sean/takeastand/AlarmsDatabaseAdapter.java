package com.sean.takeastand;

/**
 * Created by Sean on 2014-09-03.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.Time;
import android.util.Log;
import java.util.ArrayList;

public class AlarmsDatabaseAdapter
{
    private final String TAG = "Database Adapter: ";
    Context mContext;
    public AlarmsDatabaseAdapter(Context context)
    {
        mContext = context;
    }

    public int deleteAlarm(int rowID)
    {
        Log.i(TAG, "delete alarm: delete database row");
        AlarmsSQLHelper alarmsSQLHelper = new AlarmsSQLHelper(mContext);
        SQLiteDatabase localSQLiteDatabase = alarmsSQLHelper.getWritableDatabase();
        //Need to create a string array for the whereArgs, which determine row(s) to update
        String[] arrayOfString = new String[1];
        arrayOfString[0] = Integer.toString(rowID);
        alarmsSQLHelper.close();
        return localSQLiteDatabase.delete("alarms_table", "_id = ?", arrayOfString);
    }

    public int editAlarm(int activated, String startTime, String endTime, int frequency,
                         String title, String alertType, int sunday, int monday, int tuesday,
                         int wednesday, int thursday, int friday, int saturday, int rowID)
    {
        Log.i(TAG, "edit alarm: edit database row");
        AlarmsSQLHelper alarmsSQLHelper = new AlarmsSQLHelper(mContext);
        SQLiteDatabase localSQLiteDatabase = alarmsSQLHelper.getWritableDatabase();
        ContentValues localContentValues = new ContentValues();
        localContentValues.put("activated", activated);
        localContentValues.put("start_time", startTime);
        localContentValues.put("end_time", endTime);
        localContentValues.put("frequency", frequency);
        localContentValues.put("title", title);
        localContentValues.put("alert_type", alertType);
        localContentValues.put("sunday", sunday);
        localContentValues.put("monday", monday);
        localContentValues.put("tuesday", tuesday);
        localContentValues.put("wednesday", wednesday);
        localContentValues.put("thursday", thursday);
        localContentValues.put("friday", friday);
        localContentValues.put("saturday", saturday);
        //Need to create a string array for the whereArgs, which determine row(s) to update
        String[] arrayOfString = new String[1];
        arrayOfString[0] = Integer.toString(rowID);
        return localSQLiteDatabase.update("alarms_table", localContentValues, "_id =? ", arrayOfString);
    }

    public String getNextActivatedDay(Time nextDay) {
        ArrayList<String> UIDs= getUIDofActivated();
        AlarmsSQLHelper alarmsSQLHelper = new AlarmsSQLHelper(mContext);
        ArrayList<String> dayAndID = new ArrayList();
        String[] columns = {AlarmsSQLHelper.UID, AlarmsSQLHelper.SUNDAY, AlarmsSQLHelper.MONDAY,
                AlarmsSQLHelper.TUESDAY, AlarmsSQLHelper.WEDNESDAY, AlarmsSQLHelper.THURSDAY,
                AlarmsSQLHelper.FRIDAY, AlarmsSQLHelper.SATURDAY, AlarmsSQLHelper.START_TIME,
                AlarmsSQLHelper.END_TIME};
        String[] uidArgs = new String[UIDs.size()];
        uidArgs = UIDs.toArray(uidArgs);
        Cursor cursor = alarmsSQLHelper.getWritableDatabase().query(AlarmsSQLHelper.TABLE_MAIN,
                columns, "_id =? ", uidsArgs, null, null, null);
        int count = cursor.getCount();
        String alarmDetails = "";
        int currentColumn = firstColumn(nextDay);
        if (count != 0) {
            while (alarmDetails.equals("")) {
                while (cursor.moveToNext()) {
                    int status = cursor.getInt(currentColumn);
                        if (status == 1) {
                            alarmDetails = cursor.getColumnName(cursor.getPosition());
                            //Add the start time
                            alarmDetails += "|" + cursor.getString(8);
                            //Add the end time
                            alarmDetails += "|" + cursor.getString(9);
                        }
                }
                if(currentColumn==7){
                    currentColumn=1;
                } else {
                    currentColumn++;
                    if(currentColumn==firstColumn(nextDay)){
                        break;
                    }
                }
                cursor.moveToFirst();
            }
        }
        mAlarmsSQLHelper.close();
        cursor.close();
        return alarmDetails;
    }


    private int firstColumn(Time nextDay){
        switch(nextDay.weekDay) {
            //Return the column number to start with
            case 0:
                return 1;
            case 1:
                return 2;
            case 2:
                return 3;
            case 3:
                return 4;
            case 4:
                return 5;
            case 5:
                return 6;
            case 6:
                return 7;
            default:
                return 1;
        }
    }

    public int getCount()
    {
        AlarmsSQLHelper alarmsSQLHelper = new AlarmsSQLHelper(mContext);
        Count = alarmsSQLHelper.getWritableDatabase().query("alarms_table", new String[] { "_id" }, null, null, null, null, null).getCount();
        alarmsSQLHelper.close();
        return count;
    }

    public ArrayList<String> getUIDofActivated()
    {
        AlarmsSQLHelper alarmsSQLHelper = new AlarmsSQLHelper(mContext);
        ArrayList UIDs = new ArrayList();
        Cursor localCursor = alarmsSQLHelper.getWritableDatabase().query("alarms_table", new String[] { "_id", "activated" }, null, null, null, null, null);
        int i = localCursor.getCount();
        localCursor.moveToFirst();
        for (int j = 0; j < i ; j++)
        {
            int k = localCursor.getInt(0);
            if (localCursor.getInt(1) == 1) {
                localArrayList.add(Integer.toString(k));
            }
            localCursor.moveToNext();
        }
        alarmsSQLHelper.close();
        localCursor.close();
        return UIDs;
    }

    public long newAlarm(int activated, String startTime, String endTime, int frequency,
                         String title, String alertType, int sunday, int monday, int tuesday,
                         int wednesday, int thursday, int friday, int saturday)
    {
        Log.i(TAG, "new alarm: new database row");
        AlarmsSQLHelper alarmsSQLHelper = new AlarmsSQLHelper(mContext);
        SQLiteDatabase localSQLiteDatabase = aAlarmsSQLHelper.getWritableDatabase();
        ContentValues localContentValues = new ContentValues();
        localContentValues.put("activated", Integer.valueOf(activated));
        localContentValues.put("start_time", startTime);
        localContentValues.put("end_time", endTime);
        localContentValues.put("frequency", frequency);
        localContentValues.put("title", title);
        localContentValues.put("alert_type", alertType);
        localContentValues.put("sunday", sunday);
        localContentValues.put("monday", monday);
        localContentValues.put("tuesday", tuesday);
        localContentValues.put("wednesday", wednesday);
        localContentValues.put("thursday", thursday);
        localContentValues.put("friday", friday);
        localContentValues.put("saturday", saturday);
        long l = localSQLiteDatabase.insert("alarms_table", null, localContentValues);
        localSQLiteDatabase.close();
        alarmsSQLHelper.close();
        return l;
    }

    public class AlarmsSQLHelper
            extends SQLiteOpenHelper
    {
        private static final String ACTIVATED = "activated";
        private static final String ALERT_TYPE = "alert_type";
        private static final String DATABASE_NAME = "alarms_database";
        private static final int DATABASE_VERSION = 1;
        private static final String END_TIME = "end_time";
        private static final String FREQUENCY = "frequency";
        private static final String FRIDAY = "friday";
        private static final String MONDAY = "monday";
        private static final String SATURDAY = "saturday";
        private static final String START_TIME = "start_time";
        private static final String SUNDAY = "sunday";
        private static final String TABLE_MAIN = "alarms_table";
        private static final String THURSDAY = "thursday";
        private static final String TITLE = "title";
        private static final String TUESDAY = "tuesday";
        private static final String UID = "_id";
        private static final String WEDNESDAY = "wednesday";
        private final String TAG = "DBAdapter InnerClass SQLHelper";
        private AlarmsSQLHelper mInstance = null;


        private AlarmsSQLHelper(Context context)
        {
            super(context, "alarms_database", null, DATABASE_VERSION);
        }

        public AlarmsSQLHelper getInstance(Context context)
        {
            if (this.mInstance == null) {
                this.mInstance = new AlarmsSQLHelper(context.getApplicationContext());
            }
            return this.mInstance;
        }

        public void onCreate(SQLiteDatabase sQLiteDatabase)
        {
            try
            {
                sQLiteDatabase.execSQL("CREATE TABLE alarms_table (_id INTEGER PRIMARY KEY AUTOINCREMENT, activated INTEGER, " +
                        "start_time TEXT, end_time TEXT, frequency INTEGER, " +
                        "title TEXT, alert_type TEXT, sunday INTEGER,monday INTEGER," +
                        "tuesday INTEGER,wednesday INTEGER,thursday INTEGER,friday INTEGER,saturday INTEGER);");
                return;
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
                sQLiteDatabase.execSQL("DROP TABLE IF EXISTS alarms_table");
                onCreate(sQLiteDatabase);
                return;
            }
            catch (Exception localException)
            {
                Log.e("DBAdapter InnerClass SQLHelper", "Exception caught. Error onUpgrade");
                localException.printStackTrace();
            }
        }
    }
}

