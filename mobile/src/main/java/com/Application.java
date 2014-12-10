package com;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Sean on 2014-12-09.
 */
public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CalligraphyConfig.initDefault("fonts/Roboto-Regular.ttf");
    }
}
