package com.sean.takeastand.storage;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by Sean on 2014-10-03.
 */
public class AlarmSchedule implements Parcelable {

    private int UID;
    private boolean activated;
    private String alertType;
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
    private static final String TAG = "AlarmSchedule";

    //This constructor is the only way for the class variables to be initialized
    //This class was intentionally restricted, so it could not be modified after creation
    public AlarmSchedule(int UID, boolean activated, String alertType, Calendar startTime, Calendar endTime,
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
        alertType = inParcel.readString();
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
        outParcel.writeString(alertType);
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

    public String getAlertType(){
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
