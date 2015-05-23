package org.breizhcamp.bzhtime.events;

/**
 * Message send to UI to update number of minutes
 */
public class TimeEvent {

    private int nbMinutes;

    private String errorMsg;

    public TimeEvent(int nbMinutes) {
        this.nbMinutes = nbMinutes;
    }

    public TimeEvent(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public boolean isError() {
        return errorMsg != null;
    }

    public int getNbMinutes() {
        return nbMinutes;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
