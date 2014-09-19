package com.sean.takeastand;

/**
 * Created by Sean on 2014-09-03.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.lang.String;

public class AlarmsDatabaseAdapter
{
    private final String TAG = "Database Adapter: ";
    Context mContext;
    public AlarmsDatabaseAdapter(Context context)
    {
        mContext = context;
    }

    public long newAlarm(int activated, String startTime, String endTime, int frequency,
                         String title, String alertType, int sunday, int monday, int tuesday,
                         int wednesday, int thursday, int friday, int saturday)
    {
        Log.i(TAG, "new alarm: new database row");
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
        long l = localSQLiteDatabase.insert("alarms_table", null, localContentValues);
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
        int count = alarmsDatabase.delete("alarms_table", "_id = ?", arrayOfString);
        alarmsDatabase.close();
        alarmsSQLHelper.close();
        return  count;
    }

    public int editAlarm(int activated, String startTime, String endTime, int frequency,
                         String title, String alertType, int sunday, int monday, int tuesday,
                         int wednesday, int thursday, int friday, int saturday, int rowID)
    {
        Log.i(TAG, "edit alarm: edit database row");
        AlarmsSQLHelper alarmsSQLHelper = new AlarmsSQLHelper(mContext);
        SQLiteDatabase alarmsDatabase = alarmsSQLHelper.getWritableDatabase();
        ContentValues localContentValues = new ContentValues();
        localContentValues.put(AlarmsSQLHelper.ACTIVATED, activated);
        localContentValues.put(AlarmsSQLHelper.START_TIME, startTime);
        localContentValues.put(AlarmsSQLHelper.END_TIME, endTime);
        localContentValues.put(AlarmsSQLHelper.FREQUENCY, frequency);
        localContentValues.put(AlarmsSQLHelper.TITLE, title);
        localContentValues.put(AlarmsSQLHelper.ALERT_TYPE, alertType);
        localContentValues.put(AlarmsSQLHelper.SUNDAY, sunday);
        localContentValues.put(AlarmsSQLHelper.MONDAY, monday);
        localContentValues.put(AlarmsSQLHelper.TUESDAY, tuesday);
        localContentValues.put(AlarmsSQLHelper.WEDNESDAY, wednesday);
        localContentValues.put(AlarmsSQLHelper.THURSDAY, thursday);
        localContentValues.put(AlarmsSQLHelper.FRIDAY, friday);
        localContentValues.put(AlarmsSQLHelper.SATURDAY, saturday);
        //Need to create a string array for the whereArgs, which determine row(s) to update
        String[] arrayOfString = new String[1];
        arrayOfString[0] = Integer.toString(rowID);
        int count = alarmsDatabase.update("alarms_table", localContentValues, "_id =? ", arrayOfString);
        alarmsDatabase.close();
        alarmsSQLHelper.close();
        return count;
    }

    /*
    Searches the database for the activated alarm date.
    It's parameter should be today's date.  Tomorrow if today's
    alarm time has already passed.
     */
    public String getNextActivatedDay(Date nextDay) {
        AlarmsSQLHelper alarmsSQLHelper = new AlarmsSQLHelper(mContext);
        Cursor cursor = getNextActivatedDayCursor(alarmsSQLHelper);
        int count = cursor.getCount();
        String alarmDetails = "";
        int currentColumn = firstColumn(nextDay);
        if (count != 0) {
            while (alarmDetails.equals("")) {
                cursor.moveToFirst();
                do{
                    int status = cursor.getInt(currentColumn);
                    if (status == 1) {
                        alarmDetails = cursor.getColumnName(cursor.getPosition());
                        //Add the start time
                        alarmDetails += "|" + cursor.getString(8);
                        //Add the end time
                        alarmDetails += "|" + cursor.getString(9);
                    }
                } while (cursor.moveToNext());
                if(currentColumn==7){
                    currentColumn=1;
                } else {
                    currentColumn++;
                    if(currentColumn==firstColumn(nextDay)){
                        break;
                    }
                }
            }
        }
        cursor.close();
        alarmsSQLHelper.close();
        return alarmDetails;
    }

    /*
    Supports getNextActivatedDay
     */
    private Cursor getNextActivatedDayCursor(AlarmsSQLHelper alarmsSQLHelper){
        ArrayList<String> UIDs= getUIDofActivated();
        String[] columns = {AlarmsSQLHelper.UID, AlarmsSQLHelper.SUNDAY, AlarmsSQLHelper.MONDAY,
                AlarmsSQLHelper.TUESDAY, AlarmsSQLHelper.WEDNESDAY, AlarmsSQLHelper.THURSDAY,
                AlarmsSQLHelper.FRIDAY, AlarmsSQLHelper.SATURDAY, AlarmsSQLHelper.START_TIME,
                AlarmsSQLHelper.END_TIME};
        String[] uidArgs = new String[UIDs.size()];
        uidArgs = UIDs.toArray(uidArgs);
        Cursor cursor = alarmsSQLHelper.getWritableDatabase().query(AlarmsSQLHelper.TABLE_MAIN,
                columns, "_id =? ", uidArgs, null, null, null);
        return cursor;
    }

    /*
    Supports getNextActivatedDay
     */
    private int firstColumn(Date nextDay){
        Calendar c = Calendar.getInstance();
        c.setTime(nextDay);
        String weekDay;
        //This formats it into a weekday name
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.US);
        weekDay = dayFormat.format(c.getTime());
        //Return the column number to start with
        if(weekDay.equals("Sunday")){
            return 1;
        } else if(weekDay.equals("Monday")){
            return 2;
        } else if(weekDay.equals("Tuesday")){
            return 3;
        } else if(weekDay.equals("Wednesday")){
            return 4;
        } else if(weekDay.equals("Thursday")){
            return 5;
        } else if(weekDay.equals("Friday")){
            return 6;
        } else  if(weekDay.equals("Saturday")){
            return 7;
        } else{
            return 1;
        }
    }

    /*
    Supports getNextActivatedDay
     */
    private ArrayList<String> getUIDofActivated()
    {
        AlarmsSQLHelper alarmsSQLHelper = new AlarmsSQLHelper(mContext);
        ArrayList<String> UIDs = new ArrayList<String>();
        Cursor localCursor = alarmsSQLHelper.getWritableDatabase().query("alarms_table", new String[] { "_id", "activated" }, null, null, null, null, null);
        int size = localCursor.getCount();
        localCursor.moveToFirst();
        for (int j = 0; j < size ; j++)
        {
            int uid = localCursor.getInt(0);
            if (localCursor.getInt(1) == 1) {
                UIDs.add(Integer.toString(uid));
            }
            localCursor.moveToNext();
        }
        alarmsSQLHelper.close();
        localCursor.close();
        return UIDs;
    }

    public int[] getActivatedDays(){
        int[] activatedDays = {0,0,0,0,0,0,0};
        AlarmsSQLHelper alarmsSQLHelper = new AlarmsSQLHelper(mContext);
        String[] columns = {AlarmsSQLHelper.SUNDAY, AlarmsSQLHelper.MONDAY,
                AlarmsSQLHelper.TUESDAY, AlarmsSQLHelper.WEDNESDAY, AlarmsSQLHelper.THURSDAY,
                AlarmsSQLHelper.FRIDAY, AlarmsSQLHelper.SATURDAY};
        Cursor cursor = alarmsSQLHelper.getWritableDatabase().query(AlarmsSQLHelper.TABLE_MAIN,
                columns, null, null, null, null, null);
        for(int column = 0; column <7; column++){
            cursor.moveToFirst();
            do{
                if(cursor.getInt(column)==1){
                    activatedDays[column]=1;
                }
            } while(cursor.moveToNext()&&activatedDays[column]==0);
        }
        cursor.close();
        alarmsSQLHelper.close();
        return activatedDays;
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
    *Nested Class
    *
    *
    *
     */
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
        private AlarmsSQLHelper mInstance = null;


        private AlarmsSQLHelper(Context context)
        {
            super(context, AlarmsSQLHelper.DATABASE_NAME, null, DATABASE_VERSION);
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
                sQLiteDatabase.execSQL("CREATE TABLE " + AlarmsSQLHelper.TABLE_MAIN + " (" + AlarmsSQLHelper.UID
                        + " INTEGER PRIMARY KEY AUTOINCREMENT, " + AlarmsSQLHelper.ACTIVATED + " INTEGER, " +
                        AlarmsSQLHelper.START_TIME + " TEXT, " + AlarmsSQLHelper.END_TIME + " TEXT, " +
                        AlarmsSQLHelper.FREQUENCY + " INTEGER, " + AlarmsSQLHelper.TITLE + " TEXT, " +
                        AlarmsSQLHelper.ALERT_TYPE + " TEXT, " + AlarmsSQLHelper.SUNDAY + " INTEGER, " +
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

