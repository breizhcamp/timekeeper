package org.breizhcamp.bzhtime.events;

/**
 * Event sent when services want to display messages to user
 */
public class MsgEvt {

    private String msg;

    public MsgEvt(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
