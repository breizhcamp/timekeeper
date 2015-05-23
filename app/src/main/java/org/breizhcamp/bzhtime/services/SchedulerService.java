package org.breizhcamp.bzhtime.services;

import android.util.Log;

import com.google.gson.Gson;

import org.breizhcamp.bzhtime.dto.Jour;
import org.breizhcamp.bzhtime.dto.Proposal;
import org.breizhcamp.bzhtime.dto.Schedule;
import org.breizhcamp.bzhtime.events.CurrentSessionEvt;
import org.breizhcamp.bzhtime.events.FlushScheduleCacheEvt;
import org.breizhcamp.bzhtime.events.GetCurrentSessionEvt;
import org.breizhcamp.bzhtime.repositories.ScheduleRepo;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Load schedule file from breizhcamp website and retrieve current session
 */
public class SchedulerService {
    private static final EventBus bus = EventBus.getDefault();
    private static final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy");
    private static final DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm");

    private File cacheDir;
    private ScheduleRepo scheduleRepo;


    public SchedulerService(File cacheDir, ScheduleRepo scheduleRepo) {
        this.cacheDir = cacheDir;
        this.scheduleRepo = scheduleRepo;
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
        scheduleRepo.get(new Callback<Response>() {
            @Override
            public void success(Response res, Response response) {
                File scheduleFile = new File(cacheDir, "schedule.json");
                try {
                    FileOutputStream out = new FileOutputStream(scheduleFile);
                    copy(res.getBody().in(), out);
                    out.close();
                    getCurrentSession(room);

                } catch (IOException e) {
                    postError("Impossible de lire le fichier schedule [" + scheduleFile.getAbsolutePath() + "]", e);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                postError("Impossible de récupérer le fichier schedule depuis Internet", error);
            }
        });
    }

    private void postError(String msg, Exception e) {
        Log.e("bzhtime", msg, e);
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

    public static int copy(InputStream input, OutputStream output) throws IOException {
        long count = copyLarge(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    public static long copyLarge(InputStream input, OutputStream output)
            throws IOException {
        byte[] buffer = new byte[4096];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
}
