package org.breizhcamp.bzhtime.events;

/**
 * Start or stop the countdown
 */
public class CountdownMgtEvt {

    private boolean running;

    private String room;

    private int overrideMin;

    public CountdownMgtEvt(boolean running) {
        this.running = running;
    }

    public CountdownMgtEvt(boolean running, String room) {
        this.running = running;
        this.room = room;
    }

    public CountdownMgtEvt(boolean running, int overrideMin) {
        this.running = running;
        this.overrideMin = overrideMin;
    }

    public boolean isOverride() {
        return overrideMin > 0;
    }

    public boolean isRunning() {
        return running;
    }

    public String getRoom() {
        return room;
    }

    public int getOverrideMin() {
        return overrideMin;
    }
}
