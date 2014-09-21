package com.sean.takeastand;

import android.content.Context;

/**
 * Created by Sean on 2014-09-03.
 */
public class AlarmScheduleEditor {
    private static final String TAG = "AlarmScheduleEditor";
    AlarmsDatabaseAdapter alarmsDatabaseAdapter;
    Context mContext;

    public AlarmScheduleEditor(Context context)
    {
        mContext = context;
    }
    /*
    This method will be used by the activity class in order to know which checkboxes to make
    checkable
     */
    public int[] checkIfDaysFree(){
        return alarmsDatabaseAdapter.getActivatedDays();
    }


    public void editAlarm(int activated, String startTime, String endTime, int frequency,
                             String title, String alarmType, int sunday, int monday,
                             int tuesday, int wednesday, int thursday, int friday, int saturday,
                             int rowID)
    {
        alarmsDatabaseAdapter.editAlarm(activated, startTime, endTime, frequency, title,
                alarmType, sunday, monday, tuesday, wednesday, thursday, friday, saturday, rowID);
        if (activated == 1)
        {
            NextScheduleAlarmSetter nextScheduleAlarmSetter = new NextScheduleAlarmSetter(mContext);
            nextScheduleAlarmSetter.setNextAlarm();
        }
    }

    public void newAlarm(int activated, String startTime, String endTime, int frequency,
                            String title, String alarmType, int sunday, int monday, int tuesday,
                            int wednesday, int thursday, int friday, int saturday)
    {
       alarmsDatabaseAdapter.newAlarm(activated, startTime, endTime, frequency, title,
                alarmType, sunday, monday, tuesday, wednesday, thursday, friday, saturday);
        if (activated == 1)
        {
            NextScheduleAlarmSetter nextScheduleAlarmSetter = new NextScheduleAlarmSetter(mContext);
            nextScheduleAlarmSetter.setNextAlarm();
        }
    }

    public void deleteAlarm(int activated, int rowID)
    {
        alarmsDatabaseAdapter.deleteAlarm(rowID);
        NextScheduleAlarmSetter nextScheduleAlarmSetter = new NextScheduleAlarmSetter(mContext);
        nextScheduleAlarmSetter.setNextAlarm();
    }

}
