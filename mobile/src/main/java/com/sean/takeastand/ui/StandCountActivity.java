package com.sean.takeastand.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.Application;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.sean.takeastand.R;
import com.sean.takeastand.storage.StoodLogsAdapter;

import org.w3c.dom.Text;

/**
 * Created by Sean on 2014-12-15.
 */
public class StandCountActivity extends Activity {

    private static final String TAG = "StandCountActivity";
    private TextView txtStandCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standcount);
        txtStandCount = (TextView)findViewById(R.id.number_of_stands);
        ActionBar actionBar = getActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.stand_count));
        }
        Tracker t = ((Application)this.getApplication()).getTracker(Application.TrackerName.APP_TRACKER);
        t.setScreenName("Stand Counter");
        t.send(new HitBuilders.AppViewBuilder().build());
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
