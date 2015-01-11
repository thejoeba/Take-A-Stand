package com.sean.takeastand.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.sean.takeastand.R;


/**
 * Created by Sean on 2015-01-09.
 */
public class SettingsActivity extends ActionBarActivity {

    TextView txtReminderSettings;
    TextView txtFitSettings;
    TextView txtProSettings;
    private final static Integer ACTIVITY_NUMBER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.reminder_settings_toolbar);
        setSupportActionBar(toolbar);
        this.setTitle(getResources().getStringArray(R.array.ActivityTitle)[ACTIVITY_NUMBER]);
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
        String[] activityTitles = getResources().getStringArray(R.array.ActivityTitle);

        txtReminderSettings = (TextView)findViewById(R.id.reminderSettings);
        txtReminderSettings.setText(activityTitles[7]);
        txtReminderSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, ReminderSettingsActivity.class);
                startActivity(intent);
            }
        });

        txtFitSettings = (TextView)findViewById(R.id.googleFitSettings);
        txtFitSettings.setText(activityTitles[8]);
        txtFitSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, GoogleFitActivity.class);
                startActivity(intent);
            }
        });

        txtProSettings = (TextView)findViewById(R.id.proSettings);
        txtProSettings.setText(activityTitles[9]);
        txtProSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, ProSettings.class);
                startActivity(intent);
            }
        });
    }
}
