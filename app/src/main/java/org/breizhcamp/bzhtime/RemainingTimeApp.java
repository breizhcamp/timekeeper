package org.breizhcamp.bzhtime;

import android.app.Application;

import net.danlew.android.joda.JodaTimeAndroid;

import org.breizhcamp.bzhtime.services.SchedulerService;
import org.breizhcamp.bzhtime.services.TimeService;

/**
 * "Main class"
 */
public class RemainingTimeApp extends Application {
    //TODO load this from json
    public static final String[] ROOMS = new String[] {
            "Amphi A", "Amphi B", "Amphi C", "Amphi D", "Labs", "Hall"
    };
    public static final String DEFAULT_ROOM = ROOMS[0];

    public static final String DEFAULT_SCHEDULE_URL = "http://www.breizhcamp.org/json/2016/schedule.json";

    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);

        //class are registered with constructor on bus which keep references
        SchedulerService schedulerService = new SchedulerService(getCacheDir());
        new TimeService(schedulerService);
    }

}
