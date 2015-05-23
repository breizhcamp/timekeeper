package org.breizhcamp.bzhtime.repositories;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.GET;

/**
 * Retrieve schedule file
 */
public interface ScheduleRepo {

    @GET("/schedule.json")
    void get(Callback<Response> cb);

}
