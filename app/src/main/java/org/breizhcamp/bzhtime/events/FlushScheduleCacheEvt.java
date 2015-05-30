package org.breizhcamp.bzhtime.events;

/**
 * Remove cache schedule file, reload from network at next execution
 */
public class FlushScheduleCacheEvt {

    /** new url to retrieve schedule file from */
    private String newScheduleUrl;

    public FlushScheduleCacheEvt(String newScheduleUrl) {
        this.newScheduleUrl = newScheduleUrl;
    }

    public String getNewScheduleUrl() {
        return newScheduleUrl;
    }
}
