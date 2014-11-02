package com.sean.takeastand.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;

import com.sean.takeastand.R;


/**
 * Created by Sean on 2014-11-01.
 */
public class ScienceActivity extends Activity {

    TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_science);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Science Behind The App");
        super.onCreate(savedInstanceState);
        text = (TextView)findViewById(R.id.scienceText);
        text.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}
