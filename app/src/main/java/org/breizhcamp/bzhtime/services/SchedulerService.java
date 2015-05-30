package org.breizhcamp.bzhtime.services;

import android.util.Log;

import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.breizhcamp.bzhtime.RemainingTimeApp;
import org.breizhcamp.bzhtime.dto.Jour;
import org.breizhcamp.bzhtime.dto.Proposal;
import org.breizhcamp.bzhtime.dto.Schedule;
import org.breizhcamp.bzhtime.events.CurrentSessionEvt;
import org.breizhcamp.bzhtime.events.GetCurrentSessionEvt;
import org.breizhcamp.bzhtime.util.IOUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Load schedule file from breizhcamp website and retrieve current session
 */
public class SchedulerService {
    private static final EventBus bus = EventBus.getDefault();
    private static final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy");
    private static final DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm");

    private File cacheDir;
    private String scheduleUrl = RemainingTimeApp.DEFAULT_SCHEDULE_URL;

    private OkHttpClient httpClient = new OkHttpClient();

    public SchedulerService(File cacheDir) {
        this.cacheDir = cacheDir;
        bus.register(this);
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

        Schedule schedule = parseSchedule(scheduleFile);
        if (schedule == null) return;

        Proposal current = null;
        List<Jour> jours = schedule.getProgramme().getJours();
        for (Jour jour : jours) {
            LocalDate day = LocalDate.parse(jour.getDate(), dateFormatter);
            if (!day.isEqual(LocalDate.now())) {
                continue;
            }

            //we're on the good day, let's find the right proposal
            for (Proposal proposal : jour.getProposals()) {
                if (!proposal.getRoom().equals(room)) {
                    continue;
                }

                //we're on the right room, let's check the start and end
                LocalDateTime start = LocalDateTime.parse(proposal.getStart(), timeFormatter).withFields(day);
                LocalDateTime end = LocalDateTime.parse(proposal.getEnd(), timeFormatter).withFields(day);

                LocalDateTime now = LocalDateTime.now();
                if (start.isBefore(now) && end.isAfter(now)) {
                    current = proposal;
                    current.setEndDate(end);
                }
            }

            //on the right day, exit loop
            break;
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
        bus.post(new CurrentSessionEvt(msg));
    }

    /**
     * Parse json file to get DTOs
     * @param scheduleFile file to parse
     * @return Schedule parsed or null if not found
     */
    private Schedule parseSchedule(File scheduleFile) {
        try {
            Gson gson = new Gson();
            return gson.fromJson(new FileReader(scheduleFile), Schedule.class);
        } catch (FileNotFoundException e) {
            postError("Impossible de trouver le fichier schedule [" + scheduleFile.getAbsolutePath() + "]", e);
        }
        return null;
    }

    public void setScheduleUrl(String scheduleUrl) {
        this.scheduleUrl = scheduleUrl;
    }
}
