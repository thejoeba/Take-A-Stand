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

/*“This class is used for storing alarm schedule data, modifying them through “set” methods, and
returning the data via “get” methods.  In contrast to the FixedAlarmSchedule this one
is modifiable so is only used within the storage classes, which often need to change the schedule.
The Alarm process classes use the FixedAlarmSchedule which is immutable.   */

/**
 * Created by Sean on 2014-10-03.
 */
public class AlarmSchedule implements Parcelable {

    private int UID;
    private boolean activated;
    private boolean led;
    private boolean vibrate;
    private boolean sound;
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

    public AlarmSchedule(int UID, boolean activated, boolean led, boolean vibrate, boolean sound,
                         Calendar startTime, Calendar endTime, int frequency, String title,
                         boolean sunday, boolean monday, boolean tuesday,
                         boolean wednesday, boolean thursday, boolean friday, boolean saturday){
        this.UID = UID;
        this.activated = activated;
        this.led = led;
        this.vibrate = vibrate;
        this.sound = sound;
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
        led = arrayBooleans[8];
        vibrate = arrayBooleans[9];
        sound = arrayBooleans[10];
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
                                    thursday, friday, saturday, led, vibrate, sound};
        outParcel.writeBooleanArray(arrayBooleans);
        outParcel.writeInt(UID);
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

    public boolean[] getAlertType(){  return new boolean [] {led, vibrate, sound} ; }

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

    public void setActivated(boolean activated) {this.activated = activated; }

    public void setAlertType(boolean[] alertType) {
        led = alertType[0];
        vibrate = alertType[1];
        sound = alertType[2];
    }

    public void setStartTime(Calendar startTime) {this.startTime = startTime; }

    public void setEndTime(Calendar endTime) {this.endTime = endTime; }

    public void setFrequency(int frequency) {this.frequency = frequency;}

    public void setTitle(String title) {this.title = title;}

    public void setSunday(boolean sunday) {this.sunday = sunday; }

    public void setMonday(boolean monday) {this.monday = monday;}

    public void setTuesday(boolean tuesday) {this.tuesday = tuesday;}

    public void setWednesday(boolean wednesday) {this.wednesday = wednesday;}

    public void setThursday(boolean thursday) {this.thursday = thursday;}

    public void setFriday(boolean friday) {this.friday = friday;}

    public void setSaturday(boolean saturday) {this.saturday = saturday;}

}
