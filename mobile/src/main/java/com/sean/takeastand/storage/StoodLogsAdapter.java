package com.sean.takeastand.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.sean.takeastand.util.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

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
        StoodSQLHelper scheduleSQLHelper = new StoodSQLHelper(mContext);
        SQLiteDatabase localSQLiteDatabase = scheduleSQLHelper.getWritableDatabase();
        ContentValues databaseInfo = new ContentValues();
        databaseInfo.put(StoodSQLHelper.STOOD_METHOD, stoodMethod);
        databaseInfo.put(StoodSQLHelper.TIME_STAMP, Long.toString(timeStamp.getTimeInMillis()));
        long l = localSQLiteDatabase.insert(StoodSQLHelper.TABLE_MAIN, null, databaseInfo);
        localSQLiteDatabase.close();
        scheduleSQLHelper.close();
        return l;
    }

    public int getCount(){
        StoodSQLHelper stoodSQLHelper = new StoodSQLHelper(mContext);
        String[] columns = {StoodSQLHelper.UID };
        Cursor cursor = stoodSQLHelper.getWritableDatabase().query(StoodSQLHelper.TABLE_MAIN,
                columns, null, null, null, null, null);
        return cursor.getCount();
    }


    public void getLastRow(){
        StoodSQLHelper stoodSQLHelper = new StoodSQLHelper(mContext);
        Cursor cursor = stoodSQLHelper.getWritableDatabase().query(StoodSQLHelper.TABLE_MAIN,
                null, null, null, null, null, null);
        cursor.moveToLast();
        //Check to make sure there is a row; this prevents IndexOutOfBoundsException
        if(!(cursor.getCount()==0)){
                int UID = cursor.getInt(0);
                int standMethod = cursor.getInt(1);
                String timeStamp = cursor.getString(2);
            Log.i(TAG, UID + " " + standMethod + " " + timeStamp);
        }
        stoodSQLHelper.close();
        cursor.close();
    }


    public class StoodSQLHelper
            extends SQLiteOpenHelper
    {
        private static final String DATABASE_NAME = "stood_logs_database";
        private static final int DATABASE_VERSION = 1;
        private static final String TABLE_MAIN = "stood_logs_table";
        private static final String UID = "_id";
        private static final String STOOD_METHOD = "how_we_know_user_stood";
        private static final String TIME_STAMP = "CURRENT_TIMESTAMP";

        private StoodSQLHelper(Context context)
        {
            super(context, StoodSQLHelper.DATABASE_NAME, null, DATABASE_VERSION);
        }


        public void onCreate(SQLiteDatabase sQLiteDatabase)
        {
            try
            {
                sQLiteDatabase.execSQL("CREATE TABLE " + StoodSQLHelper.TABLE_MAIN + " (" + StoodSQLHelper.UID
                        + " INTEGER PRIMARY KEY AUTOINCREMENT, " + StoodSQLHelper.STOOD_METHOD + " INTEGER, " +
                        StoodSQLHelper.TIME_STAMP + " TEXT);");
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
                sQLiteDatabase.execSQL("DROP TABLE IF EXISTS " + StoodSQLHelper.TABLE_MAIN);
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
