package org.breizhcamp.bzhtime.events;

import org.breizhcamp.bzhtime.dto.Proposal;

/**
 * Event sent when the current session is loaded
 */
public class CurrentSessionEvt {

    /** Proposal could be null if no session is running for the current room */
    private Proposal proposal;

    public CurrentSessionEvt(Proposal proposal) {
        this.proposal = proposal;
    }

    public Proposal getProposal() {
        return proposal;
    }
}
