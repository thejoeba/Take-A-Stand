package com.sean.takeastand.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.sean.takeastand.R;

import org.w3c.dom.Text;

import java.util.ArrayList;


/**
 * Created by Sean on 2014-11-01.
 */
public class ScienceActivity extends Activity {

    LinearLayout medicalLinear;
    LinearLayout newsLinear;
    private static String TAG = "ScienceActivity";

    /*
    Need to figure out a way uo dynamically increase the parent layout's (scrollview's) height based
    on the new textviews added. GetHeight returns pixels.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_science, null);
        medicalLinear = (LinearLayout)view.findViewById(R.id.medical_studies);
        newsLinear = (LinearLayout)view.findViewById(R.id.news_articles);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Science Behind The App");
        int addedHeight = setUpTextViews();
        //setNewScrollViewHeight(view, addedHeight);
        setContentView(view);

    }

    private int setUpTextViews(){
        int newHeight = 0;
        String[] medicalStudies = getResources().getStringArray(R.array.science_text_medical);
        String[] newsArticles = getResources().getStringArray(R.array.science_text_news);
        int textViewsIndex = 0;
        for(int i = 0; i < medicalStudies.length; i ++){
            LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lParams.setMargins(0, 10, 0, 10);
            TextView textView=new TextView(this);
            textView.setLayoutParams(lParams);
            textView.setText(medicalStudies[i]);
            textView.setId(textViewsIndex);
            //In order for links to pop up browser, setMovementMethod
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            newHeight += textView.getMeasuredHeight();
            medicalLinear.addView(textView);
            textViewsIndex++;
        }
        for(int i = 0; i < newsArticles.length; i++){
            LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lParams.setMargins(0, 10, 0, 10);
            TextView textView=new TextView(this);
            textView.setLayoutParams(lParams);
            textView.setText(newsArticles[i]);
            textView.setId(textViewsIndex);
            //In order for links to pop up browser, setMovementMethod
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            newHeight += textView.getMeasuredHeight();
            newsLinear.addView(textView);
            textViewsIndex++;
        }
        return newHeight;
    }

    /*private void setNewScrollViewHeight(View view, int newHeight){
        ScrollView scrollView = (ScrollView)view.findViewById(R.id.science_scroll_view);
        int height = scrollView.getMeasuredHeight();
        Log.i(TAG, Integer.toString(height) + " " + Integer.toString(newHeight));
        height += newHeight;
        if(scrollView == null){
            Log.i(TAG, "ScrollView null");
        }
        ScrollView.LayoutParams params = new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT,
                height);
        scrollView.setLayoutParams(params);
    }*/



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}
