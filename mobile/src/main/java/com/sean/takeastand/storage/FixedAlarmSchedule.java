package com.sean.takeastand.storage;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

/* This class is used for storing alarm schedule data and returning the data via “get” methods.
Once an FixedAlarmSchedule object is created its data cannot be modified, thus preventing any corruption
of data.  This object is passed throughout the day between the ScheduledRepeatingAlarm, the
AlarmReceiver and the AlarmService, so ensuring its data is not changed is important.  Can be
 created from an AlarmSchedule or through a parameter list that initializes all of its class
 member fields. */

/**
 * Created by Sean on 2014-10-25.
 */
public class FixedAlarmSchedule implements Parcelable{

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

    public FixedAlarmSchedule(int UID, boolean activated, boolean led, boolean vibrate, boolean sound,
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

    public FixedAlarmSchedule(AlarmSchedule alarmSchedule){
        UID = alarmSchedule.getUID();
        activated = alarmSchedule.getActivated();
        led = alarmSchedule.getAlertType()[0];
        vibrate = alarmSchedule.getAlertType()[1];
        sound = alarmSchedule.getAlertType()[2];
        startTime = alarmSchedule.getStartTime();
        endTime = alarmSchedule.getEndTime();
        frequency = alarmSchedule.getFrequency();
        title = alarmSchedule.getTitle();
        sunday = alarmSchedule.getSunday();
        monday = alarmSchedule.getMonday();
        tuesday = alarmSchedule.getTuesday();
        wednesday = alarmSchedule.getWednesday();
        thursday = alarmSchedule.getThursday();
        friday = alarmSchedule.getFriday();
        saturday = alarmSchedule.getSaturday();
    }

    //This constructor is only called when an object of this class is being remade from a Parcel
    private FixedAlarmSchedule(Parcel inParcel){
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

    public static  final Creator<FixedAlarmSchedule> CREATOR =
            new Creator<FixedAlarmSchedule>(){

                @Override
                public FixedAlarmSchedule createFromParcel(Parcel inParcel) {
                    return new FixedAlarmSchedule(inParcel);
                }

                @Override
                public FixedAlarmSchedule[] newArray(int size) {
                    return new FixedAlarmSchedule[size];
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
}
