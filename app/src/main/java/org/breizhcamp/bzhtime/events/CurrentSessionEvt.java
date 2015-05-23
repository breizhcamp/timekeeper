package org.breizhcamp.bzhtime.events;

import org.breizhcamp.bzhtime.dto.Proposal;

/**
 * Event sent when the current session is loaded
 */
public class CurrentSessionEvt {

    /** Proposal could be null if no session is running for the current room */
    private Proposal proposal;

    private String errorMsg;

    public CurrentSessionEvt(Proposal proposal) {
        this.proposal = proposal;
    }

    public CurrentSessionEvt(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public boolean isError() {
        return errorMsg != null;
    }

    public Proposal getProposal() {
        return proposal;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
