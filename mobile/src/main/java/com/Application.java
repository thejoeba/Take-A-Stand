package com;

import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.sean.takeastand.R;

import java.util.HashMap;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Sean on 2014-12-09.
 */
public class Application extends android.app.Application {

    private static final String TAG = "Application";

    @Override
    public void onCreate() {
        super.onCreate();
        CalligraphyConfig.initDefault("fonts/Roboto-Regular.ttf");
    }

    public static int GENERAL_TRACKER = 0;

    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            //This is an if-else nested in an if-else
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(R.xml.app_tracker)
                    : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker)
                    : analytics.newTracker(R.xml.ecommerce_tracker);
            mTrackers.put(trackerId, t);
            Log.i(TAG, "Worked " + Boolean.toString(trackerId == TrackerName.APP_TRACKER));
        } else {
            Log.i(TAG, "Analytics Error" );
        }
        return mTrackers.get(trackerId);
    }

}
