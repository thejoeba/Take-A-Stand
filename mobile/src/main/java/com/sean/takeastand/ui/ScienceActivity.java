package com.sean.takeastand.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.Application;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.sean.takeastand.R;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


/**  Displays links to science that supports the reason behind our app
 * Created by Sean on 2014-11-01.
 */

public class ScienceActivity extends ActionBarActivity {

    /*
    Need to figure out a way uo dynamically increase the parent layout's (scrollview's) height based
    on the new textviews added. GetHeight returns pixels.
     */

    //ToDo: Change to listview of cards
    private TextView txtMedical1Link;
    private TextView txtMedical2Link;
    //ToDo: Test Link 3
    private TextView txtMedical3Link;
    private TextView txtMedical4Link;
    private TextView txtNews1;
    private TextView txtNews2;
    private TextView txtNews3;
    private TextView txtNews4;
    private TextView txtNews5;
    private TextView txtNews6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_science);
        Toolbar toolbar = (Toolbar) findViewById(R.id.science_toolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavUtils.navigateUpFromSameTask(ScienceActivity.this);
                }
            });
        }
        setUpTextViews();
        Tracker t = ((Application)this.getApplication()).getTracker(Application.TrackerName.APP_TRACKER);
        t.setScreenName("Science Activity");
        t.send(new HitBuilders.AppViewBuilder().build());
    }

    private void setUpTextViews(){
        txtMedical1Link = (TextView)findViewById(R.id.medical1link);
        txtMedical2Link = (TextView)findViewById(R.id.medical2link);
        txtMedical3Link = (TextView)findViewById(R.id.medical3link);
        txtMedical4Link = (TextView)findViewById(R.id.medical4link);
        txtNews1 = (TextView)findViewById(R.id.news1);
        txtNews2 = (TextView)findViewById(R.id.news2);
        txtNews3 = (TextView)findViewById(R.id.news3);
        txtNews4 = (TextView)findViewById(R.id.news4);
        txtNews5 = (TextView)findViewById(R.id.news5);
        txtNews6 = (TextView)findViewById(R.id.news6);
        txtMedical1Link.setMovementMethod(LinkMovementMethod.getInstance());
        txtMedical2Link.setMovementMethod(LinkMovementMethod.getInstance());
        txtMedical3Link.setMovementMethod(LinkMovementMethod.getInstance());
        txtMedical4Link.setMovementMethod(LinkMovementMethod.getInstance());
        txtNews1.setMovementMethod(LinkMovementMethod.getInstance());
        txtNews2.setMovementMethod(LinkMovementMethod.getInstance());
        txtNews3.setMovementMethod(LinkMovementMethod.getInstance());
        txtNews4.setMovementMethod(LinkMovementMethod.getInstance());
        txtNews5.setMovementMethod(LinkMovementMethod.getInstance());
        txtNews6.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Closes Activity when user presses title
        finish();
        return super.onOptionsItemSelected(item);
    }

    //For Calligraphy font library class
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new CalligraphyContextWrapper(newBase));
    }

}
