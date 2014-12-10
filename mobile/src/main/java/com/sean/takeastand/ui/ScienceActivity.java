package com.sean.takeastand.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;

import com.sean.takeastand.R;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


/**  Displays links to science that supports the reason behind our app
 * Created by Sean on 2014-11-01.
 */

public class ScienceActivity extends Activity {

    /*
    Need to figure out a way uo dynamically increase the parent layout's (scrollview's) height based
    on the new textviews added. GetHeight returns pixels.
     */

    private TextView txtMedical1;
    private TextView txtMedical2;
    private TextView txtMedical3;
    private TextView txtMedical4;
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
        ActionBar actionBar = getActionBar();
        //Is possible actionBar will be null
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Science Behind The App");
        }
        setUpTextViews();

    }

    private void setUpTextViews(){
        txtMedical1 = (TextView)findViewById(R.id.medical1);
        txtMedical2 = (TextView)findViewById(R.id.medical2);
        txtMedical3 = (TextView)findViewById(R.id.medical3);
        txtMedical4 = (TextView)findViewById(R.id.medical4);
        txtNews1 = (TextView)findViewById(R.id.news1);
        txtNews2 = (TextView)findViewById(R.id.news2);
        txtNews3 = (TextView)findViewById(R.id.news3);
        txtNews4 = (TextView)findViewById(R.id.news4);
        txtNews5 = (TextView)findViewById(R.id.news5);
        txtNews6 = (TextView)findViewById(R.id.news6);
        txtMedical1.setMovementMethod(LinkMovementMethod.getInstance());
        txtMedical2.setMovementMethod(LinkMovementMethod.getInstance());
        txtMedical3.setMovementMethod(LinkMovementMethod.getInstance());
        txtMedical4.setMovementMethod(LinkMovementMethod.getInstance());
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
