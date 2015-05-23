package org.breizhcamp.bzhtime;

import android.app.Application;

import net.danlew.android.joda.JodaTimeAndroid;

import org.breizhcamp.bzhtime.repositories.ScheduleRepo;
import org.breizhcamp.bzhtime.services.SchedulerService;
import org.breizhcamp.bzhtime.services.TimeService;

import retrofit.RestAdapter;

/**
 * "Main class"
 */
public class RemainingTimeApp extends Application {
    //TODO load this from json
    public static final String[] ROOMS = new String[] {
            "Ouessant", "Bréhat", "Molène", "Belle-Ile-en-Mer", "Groix", "Arz"
    };
    public static final String DEFAULT_ROOM = ROOMS[0];

    //private static final String API_URL = "http://www.breizhcamp.org/json";
    private static final String API_URL = "http://192.168.0.1/breizhcamp-vote/server";

    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);

        //class are registered with constructor on bus which keep references
        SchedulerService schedulerService = new SchedulerService(getCacheDir(), scheduleRepo());
        new TimeService(schedulerService);
    }

    private ScheduleRepo scheduleRepo() {
        return new RestAdapter.Builder()
                .setEndpoint(API_URL)
                .build()
                .create(ScheduleRepo.class);
    }


}
