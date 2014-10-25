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

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

/* This class is used for storing alarm schedule data and returning the data via “get” methods.
Once an alarm schedule object is created its data cannot be modified, thus preventing any corruption
of data.  This object is passed throughout the day between the ScheduledRepeatingAlarm, the
AlarmReceiver and the AlarmService, so ensuring its data is not changed is important.  The
AlarmSchedule is initially created by the AlarmsDatabaseAdapter in its getAlarmSchedules method.
This method is typically called by StartScheduleReceiver and then passed into the
ScheduledRepeatingAlarm, in which it circulates between the previously mentioned classes
until the schedule reaches its end time. */

/**
 * Created by Sean on 2014-10-03.
 */
public class AlarmSchedule implements Parcelable {


    //ToDo: storage is getting to complicated and too many lines of code, add setter methods

    private int UID;
    private boolean activated;
    private int[] alertType = {0, 0, 0};
    private Calendar startTime;
    private Calendar endTime;
    private int frequency;
    private String title;
    private boolean sunday;
    private boolean monday;
    private boolean tuesday;
    private boolean wednesday;
    private boolean thursday;
    private boolean friday;
    private boolean saturday;

    //This constructor is the only way for the class variables to be initialized
    //This class was intentionally restricted, so it could not be modified after creation
    public AlarmSchedule(int UID, boolean activated, int[] alertType, Calendar startTime, Calendar endTime,
                         int frequency, String title, boolean sunday, boolean monday, boolean tuesday,
                         boolean wednesday, boolean thursday, boolean friday, boolean saturday){
        this.UID = UID;
        this.activated = activated;
        this.alertType = alertType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.frequency = frequency;
        this.title = title;
        this.sunday = sunday;
        this.monday = monday;
        this.tuesday = tuesday;
        this.wednesday = wednesday;
        this.thursday = thursday;
        this.friday = friday;
        this.saturday = saturday;
    }

    //This constructor is only called when an object of this class is being remade from a Parcel
    private AlarmSchedule(Parcel inParcel){
        boolean[] arrayBooleans = new boolean[8];
        inParcel.readBooleanArray(arrayBooleans);
        activated = arrayBooleans[0];
        sunday = arrayBooleans[1];
        monday = arrayBooleans[2];
        tuesday = arrayBooleans[3];
        wednesday = arrayBooleans[4];
        thursday = arrayBooleans[5];
        friday = arrayBooleans[6];
        saturday = arrayBooleans[7];
        UID = inParcel.readInt();
        alertType[0] = inParcel.readInt();
        alertType[1] = inParcel.readInt();
        alertType[2] = inParcel.readInt();
        title = inParcel.readString();
        startTime = Calendar.getInstance();
        startTime.setTimeInMillis(Long.decode(inParcel.readString()));
        endTime = Calendar.getInstance();
        endTime.setTimeInMillis(Long.decode(inParcel.readString()));
        frequency = inParcel.readInt();

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel outParcel, int flags) {
        boolean[] arrayBooleans = {activated, sunday, monday, tuesday, wednesday,
                                    thursday, friday, saturday};
        outParcel.writeBooleanArray(arrayBooleans);
        outParcel.writeInt(UID);
        outParcel.writeInt(alertType[0]);
        outParcel.writeInt(alertType[1]);
        outParcel.writeInt(alertType[2]);
        outParcel.writeString(title);
        outParcel.writeString(Long.toString(startTime.getTimeInMillis()));
        outParcel.writeString(Long.toString(endTime.getTimeInMillis()));
        outParcel.writeInt(frequency);

    }

    public static  final Creator<AlarmSchedule> CREATOR =
            new Creator<AlarmSchedule>(){

                @Override
                public AlarmSchedule createFromParcel(Parcel inParcel) {
                    return new AlarmSchedule(inParcel);
                }

                @Override
                public AlarmSchedule[] newArray(int size) {
                    return new AlarmSchedule[size];
                }
    };

    public int getUID() { return UID; }

    public boolean getActivated(){
        return activated;
    }

    public int[] getAlertType(){
        return alertType;
    }

    public Calendar getStartTime(){
        return startTime;
    }

    public Calendar getEndTime(){
        return endTime;
    }

    public int getFrequency(){
        return frequency;
    }

    public String getTitle() { return title; }

    public boolean getSunday(){
        return sunday;
    }

    public boolean getMonday(){
        return monday;
    }

    public boolean getTuesday(){
        return tuesday;
    }

    public boolean getWednesday(){
        return wednesday;
    }

    public boolean getThursday(){
        return thursday;
    }

    public boolean getFriday(){
        return friday;
    }

    public boolean getSaturday(){
        return saturday;
    }

}
