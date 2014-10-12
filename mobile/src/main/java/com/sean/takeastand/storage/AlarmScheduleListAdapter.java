

package com.sean.takeastand.storage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sean.takeastand.R;
import com.sean.takeastand.util.Utils;

import java.util.ArrayList;

/**
 * Created by Sean on 2014-10-05.
 */
public class AlarmScheduleListAdapter extends ArrayAdapter<AlarmSchedule> {

    private ArrayList<AlarmSchedule> mAlarmSchedules;
    private Context mContext;
    private TextView txtTitle;
    private TextView txtActivated;
    private TextView txtAlertType;
    private TextView txtStartTime;
    private TextView txtEndTime;
    private TextView txtFrequency;
    private TextView txtSunday;
    private TextView txtMonday;
    private TextView txtTuesday;
    private TextView txtWednesday;
    private TextView txtThursday;
    private TextView txtFriday;
    private TextView txtSaturday;

    public AlarmScheduleListAdapter(Context context, int resource, ArrayList<AlarmSchedule> alarmSchedules) {
        super(context, resource, alarmSchedules);
        mAlarmSchedules = alarmSchedules;
        mContext = context;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        View rowView = view;
        if(rowView==null){
            LayoutInflater inflater =
                    (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.row_layout, null);

        }
        initializeTextViews(rowView);
        setText(mAlarmSchedules, position);
        return rowView;
    }

    private void initializeTextViews(View view){
        txtTitle = (TextView)view.findViewById(R.id.rowTitle);
        txtActivated = (TextView)view.findViewById(R.id.rowActivated);
        txtAlertType = (TextView)view.findViewById(R.id.rowAlertType);
        txtStartTime = (TextView)view.findViewById(R.id.rowStartTime);
        txtEndTime = (TextView)view.findViewById(R.id.rowEndTime);
        txtFrequency = (TextView)view.findViewById(R.id.rowFrequency);
        txtSunday = (TextView)view.findViewById(R.id.rowSunday);
        txtMonday = (TextView)view.findViewById(R.id.rowMonday);
        txtTuesday= (TextView)view.findViewById(R.id.rowTuesday);
        txtWednesday = (TextView)view.findViewById(R.id.rowWednesday);
        txtThursday = (TextView)view.findViewById(R.id.rowThursday);
        txtFriday = (TextView)view.findViewById(R.id.rowFriday);
        txtSaturday = (TextView)view.findViewById(R.id.rowSaturday);
    }

    private void setText(ArrayList<AlarmSchedule> alarmSchedules, int position){
        AlarmSchedule alarmSchedule = alarmSchedules.get(position);
        txtTitle.setText(alarmSchedule.getTitle());
        txtActivated.setText("Activated: " + Boolean.toString(alarmSchedule.getActivated()));
        txtAlertType.setText(alarmSchedule.getAlertType());
        txtStartTime.setText(Utils.calendarToTimeString(alarmSchedule.getStartTime()));
        txtEndTime.setText(Utils.calendarToTimeString(alarmSchedule.getEndTime()));
        txtFrequency.setText(Integer.toString(alarmSchedule.getFrequency()));
        txtSunday.setText("Sunday: " + Boolean.toString(alarmSchedule.getSunday()));
        txtMonday.setText("Monday: " + Boolean.toString(alarmSchedule.getMonday()));
        txtTuesday.setText("Tuesday: " + Boolean.toString(alarmSchedule.getTuesday()));
        txtWednesday.setText("Wednesday: " + Boolean.toString(alarmSchedule.getWednesday()));
        txtThursday.setText("Thursday: " + Boolean.toString(alarmSchedule.getThursday()));
        txtFriday.setText("Friday: " + Boolean.toString(alarmSchedule.getFriday()));
        txtSaturday.setText("Saturday: " + Boolean.toString(alarmSchedule.getSaturday()));
    }
}
