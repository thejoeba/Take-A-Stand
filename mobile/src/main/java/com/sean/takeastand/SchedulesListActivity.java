package com.sean.takeastand;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Sean on 2014-09-21.
 */
public class SchedulesListActivity extends Activity{

    private static final int REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_list);
        if(new AlarmsDatabaseAdapter(this).getCount()==0) {
            startActivityForResult(new Intent(this, ScheduleCreatorActivity.class), REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE){
            if(resultCode ==  -1){
                finish();
            }
            if(resultCode == 0){
                finish();
            }
        }
    }


}
