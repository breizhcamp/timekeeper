package org.breizhcamp.bzhtime.events;

import org.breizhcamp.bzhtime.dto.Proposal;

/**
 * Event sent when the current session is loaded
 */
public class CurrentSessionEvt {

    /** Proposal could be null if no session is running for the current room */
    private final Proposal proposal;

    /** Next session, null if last session */
    private final Proposal next;

    public CurrentSessionEvt(Proposal proposal) {
        this.proposal = proposal;
        this.next = null;
    }

    public CurrentSessionEvt(Proposal proposal, Proposal next) {
        this.proposal = proposal;
        this.next = next;
    }

    public Proposal getProposal() {
        return proposal;
    }

    public Proposal getNext() {
        return next;
    }
}
