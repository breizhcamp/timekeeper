package org.breizhcamp.bzhtime.events;

/**
 * Start or stop the countdown
 */
public class CountdownMgtEvt {

    private boolean running;

    private String room;

    public CountdownMgtEvt(boolean running) {
        this.running = running;
    }

    public CountdownMgtEvt(boolean running, String room) {
        this.running = running;
        this.room = room;
    }

    public boolean isRunning() {
        return running;
    }

    public String getRoom() {
        return room;
    }
}
