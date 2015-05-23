package org.breizhcamp.bzhtime.services;

import org.breizhcamp.bzhtime.dto.Proposal;
import org.breizhcamp.bzhtime.events.CountdownMgtEvt;
import org.breizhcamp.bzhtime.events.CurrentSessionEvt;
import org.breizhcamp.bzhtime.events.FlushScheduleCacheEvt;
import org.breizhcamp.bzhtime.events.GetCurrentSessionEvt;
import org.breizhcamp.bzhtime.events.TimeEvent;
import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;

import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;

/**
 * Send message to UI when it needed to change the time value
 */
public class TimeService {
    private static final EventBus bus = EventBus.getDefault();
    private SchedulerService schedulerService;

    private boolean isRunning;

    private String room;
    private LocalDateTime endDate;
    private int lastRemaining;
    private Timer timer;

    public TimeService(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
        bus.register(this);
    }

    /**
     * Received when the activity starts or suspend
     * @param event Start/stop event, if start, room name must be defined
     */
    public void onEvent(CountdownMgtEvt event) {
        isRunning = event.isRunning();

        String oldRoom = room;
        room = event.getRoom();

        if (room == null || !room.equals(oldRoom)) {
            reloadCurrentSession();
        }
    }

    /**
     * Received when the force reload from schedule file is asked
     * @param event Event
     */
    public void onEvent(FlushScheduleCacheEvt event) {
        schedulerService.clearCache();
        reloadCurrentSession();
    }

    /**
     * Received when the current session are found, the countdown can be started
     * @param event Contains the current session
     */
    public void onEvent(CurrentSessionEvt event) {
        if (event.isError()) {
            return;
        }

        Proposal proposal = event.getProposal();
        if (proposal != null) {
            endDate = proposal.getEndDate();
        } else {
            endDate = null;
        }
        computeRemaining();
    }

    /**
     * Do the countdown and send the remaining time to the UI
     */
    protected void computeRemaining() {
        if (!isRunning) return;
        if (timer != null) timer.cancel();
        timer = null;
        int remaining = 0;

        //compute endDate if defined
        if (endDate != null) {
            remaining = Minutes.minutesBetween(LocalDateTime.now(), endDate).getMinutes();
        }
        if (remaining < 0) remaining = 0;

        if (lastRemaining != remaining) {
            //avoid sending to UI if not changed
            bus.post(new TimeEvent(remaining));
            lastRemaining = remaining;
        }

        //current session in progress, let's recompute end time in 5000 sec
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (endDate == null) {
                    reloadCurrentSession();
                } else {
                    computeRemaining();
                }
            }
        }, 5000);
    }

    private void reloadCurrentSession() {
        endDate = null;
        bus.post(new GetCurrentSessionEvt(room));
    }
}
