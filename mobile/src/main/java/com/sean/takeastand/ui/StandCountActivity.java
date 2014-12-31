package com.sean.takeastand.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.Application;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.sean.takeastand.util.Constants;
import com.sean.takeastand.R;
import com.sean.takeastand.storage.StoodLogsAdapter;

/**
 * Created by Sean on 2014-12-15.
 */
public class StandCountActivity extends ActionBarActivity {

    //ToDo: Add more statistics
    private static final String TAG = "StandCountActivity";
    private TextView txtStandCount;
//    private TextView tvLastStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standcount);
        Toolbar toolbar = (Toolbar) findViewById(R.id.stand_count_toolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavUtils.navigateUpFromSameTask(StandCountActivity.this);
                }
            });
        }
        txtStandCount = (TextView) findViewById(R.id.number_of_stands);
        Tracker t = ((Application) this.getApplication()).getTracker(Application.TrackerName.APP_TRACKER);
        t.setScreenName("Stand Counter");
        t.send(new HitBuilders.AppViewBuilder().build());

//        tvLastStep = (TextView) findViewById(R.id.tvLastStep);
//        tvLastStep.setText("Last Step: " + getSharedPreferences(com.heckbot.standdtector.Constants.STANDDTECTORTM_SHARED_PREFERENCES, Context.MODE_PRIVATE).getLong(com.heckbot.standdtector.Constants.DEVICE_LAST_STEP, 0));
    }

    @Override
    protected void onResume() {
        super.onResume();
        txtStandCount.setText(Integer.toString(new StoodLogsAdapter(this).getCount()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Closes Activity when user presses title
        finish();
        return super.onOptionsItemSelected(item);
    }
}
