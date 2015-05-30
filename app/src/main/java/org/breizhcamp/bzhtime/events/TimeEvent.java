package org.breizhcamp.bzhtime.events;

import org.joda.time.Duration;
import org.joda.time.Period;

/**
 * Message send to UI to update remaining time
 */
public class TimeEvent {

    private Period remaining;

    private String errorMsg;

    public TimeEvent(Period remaining) {
        this.remaining = remaining;
    }

    public TimeEvent(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public boolean isError() {
        return errorMsg != null;
    }

    public Period getRemaining() {
        return remaining;
    }
}
