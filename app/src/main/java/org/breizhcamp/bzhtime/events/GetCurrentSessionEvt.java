package org.breizhcamp.bzhtime.events;

/**
 * Event sent to load the current session
 */
public class GetCurrentSessionEvt {

    private String room;

    public GetCurrentSessionEvt(String room) {
        this.room = room;
    }

    public String getRoom() {
        return room;
    }
}
