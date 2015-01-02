package com;

import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
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

//    public synchronized Tracker getTracker(TrackerName trackerId) {
//        if (!mTrackers.containsKey(trackerId)) {
//
//            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
//            //This is an if-else nested in an if-else
//            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(R.xml.app_tracker)
//                    : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker)
//                    : analytics.newTracker(R.xml.ecommerce_tracker);
//            mTrackers.put(trackerId, t);
//            Log.i(TAG, "Worked " + Boolean.toString(trackerId == TrackerName.APP_TRACKER));
//        }
//        return mTrackers.get(trackerId);
//    }
//ToDo: fixed via http://stackoverflow.com/questions/27533679/google-analytics-blocks-android-app
    public synchronized Tracker getTracker(TrackerName trackerId) {
        Log.d(TAG, "getTracker()");
        if (!mTrackers.containsKey(trackerId)) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);

            // Global GA Settings
            // <!-- Google Analytics SDK V4 BUG20141213 Using a GA global xml freezes the app! Do config by coding. -->
            analytics.setDryRun(false);

            analytics.getLogger().setLogLevel(Logger.LogLevel.INFO);
            //analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);

            // Create a new tracker
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(R.xml.analytics_global_config) : null;
            if (t != null) {
                t.enableAdvertisingIdCollection(true);
            }
            mTrackers.put(trackerId, t);
        }
        return mTrackers.get(trackerId);
    }

}
