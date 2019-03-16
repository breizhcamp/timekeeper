package org.breizhcamp.bzhtime.services;

import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.breizhcamp.bzhtime.RemainingTimeApp;
import org.breizhcamp.bzhtime.dto.Proposal;
import org.breizhcamp.bzhtime.events.CurrentSessionEvt;
import org.breizhcamp.bzhtime.events.GetCurrentSessionEvt;
import org.breizhcamp.bzhtime.events.MsgEvt;
import org.breizhcamp.bzhtime.util.IOUtils;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * Load schedule file from breizhcamp website and retrieve current session
 */
public class SchedulerService {
    private static final EventBus bus = EventBus.getDefault();
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private File cacheDir;
    private String scheduleUrl = RemainingTimeApp.DEFAULT_SCHEDULE_URL;

    private OkHttpClient httpClient = new OkHttpClient();

    //mapping between room names and venues in schedule file
    private Map<String, String> venues = new HashMap<>();

    public SchedulerService(File cacheDir) {
        this.cacheDir = cacheDir;
        bus.register(this);

        venues.put("Amphi A", "Amphi A");
        venues.put("Amphi B", "Amphi B");
        venues.put("Amphi C", "Amphi C");
        venues.put("Amphi D", "Amphi D");
    }

    /**
     * Retrieve the current session and send CurrentSessionEvt when loaded
     * @param event Event containing the name of the room
     */
    public void onEvent(GetCurrentSessionEvt event) {
        getCurrentSession(event.getRoom());
    }

    /**
     * Remove cached schedule file
     */
    public void clearCache() {
        File scheduleFile = new File(cacheDir, "schedule.json");
        if (scheduleFile.exists()) {
            scheduleFile.delete();
        }
    }

    private void getCurrentSession(String room) {
        File scheduleFile = new File(cacheDir, "schedule.json");
        if (!scheduleFile.exists()) {
            loadScheduleFile(room);
            //load will recall computeEndDate
            return;
        }

        List<Proposal> schedule = parseSchedule(scheduleFile);
        if (schedule == null) return;

        String venue = venues.get(room);
        LocalDateTime now = LocalDateTime.now();
        Proposal current = null;
        for (Proposal proposal : schedule) {
            if (!proposal.getVenue().equals(venue)) {
                continue;
            }

            //we're on the right room, let's check the start and end
            LocalDateTime start = LocalDateTime.parse(proposal.getEventStart(), dateTimeFormatter);
            LocalDateTime end = LocalDateTime.parse(proposal.getEventEnd(), dateTimeFormatter);

            if (start.isBefore(now) && end.isAfter(now)) {
                current = proposal;
                current.setEndDate(end);
                break;
            }
        }

        //current could be null
        bus.post(new CurrentSessionEvt(current));
    }


    private void loadScheduleFile(final String room) {
        Request request = new Request.Builder().url(scheduleUrl).build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Response response) throws IOException {
                File scheduleFile = new File(cacheDir, "schedule.json");
                try {
                    FileOutputStream out = new FileOutputStream(scheduleFile);
                    IOUtils.copy(response.body().byteStream(), out);
                    out.close();
                    bus.post(new MsgEvt("Fichier schedule.json chargé"));
                    getCurrentSession(room);

                } catch (IOException e) {
                    postError("Impossible de lire le fichier schedule [" + scheduleFile.getAbsolutePath() + "]", e);
                }
            }

            @Override
            public void onFailure(Request request, IOException e) {
                postError("Impossible de récupérer le fichier schedule depuis Internet", e);
            }
        });
    }

    private void postError(String msg, Exception e) {
        Log.e("org.breizhcamp.bzhtime", msg, e);
        bus.post(new MsgEvt(msg));
    }

    /**
     * Parse json file to get DTOs
     * @param scheduleFile file to parse
     * @return Schedule parsed or null if not found
     */
    private List<Proposal> parseSchedule(File scheduleFile) {
        try {
            Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
            Type collectionType = new TypeToken<ArrayList<Proposal>>(){}.getType();
            return gson.fromJson(new FileReader(scheduleFile), collectionType);
        } catch (FileNotFoundException e) {
            postError("Impossible de trouver le fichier schedule [" + scheduleFile.getAbsolutePath() + "]", e);
        }
        return null;
    }

    public void setScheduleUrl(String scheduleUrl) {
        this.scheduleUrl = scheduleUrl;
    }
}
