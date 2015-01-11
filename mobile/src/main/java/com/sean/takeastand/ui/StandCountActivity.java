package com.sean.takeastand.ui;

import android.app.AlertDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.Application;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.sean.takeastand.R;
import com.sean.takeastand.storage.StoodLogsAdapter;

/**
 * Created by Sean on 2014-12-15.
 */
public class StandCountActivity extends ActionBarActivity {

    //ToDo: Add more statistics
    private static final String TAG = "StandCountActivity";
    private TextView txtStandCount;
    private final static Integer ACTIVITY_NUMBER = 4;
//    private TextView tvLastStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standcount);
        this.setTitle(getResources().getStringArray(R.array.ActivityTitle)[ACTIVITY_NUMBER]);
        Toolbar toolbar = (Toolbar) findViewById(R.id.stand_count_toolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.help_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Resources resources = getResources();
        if (item.getItemId() ==  R.id.help) {
            new AlertDialog.Builder(this)
                    .setTitle(resources.getStringArray(R.array.ActivityTitle)[ACTIVITY_NUMBER])
                    .setMessage(resources.getStringArray(R.array.ActivityHelpText)[ACTIVITY_NUMBER])
                    .setPositiveButton(getString(R.string.ok), null)
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        }
        else {
            //Closes Activity when user presses title
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        txtStandCount.setText(Integer.toString(new StoodLogsAdapter(this).getCount()));
    }
}
