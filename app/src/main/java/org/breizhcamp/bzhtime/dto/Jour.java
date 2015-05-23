package org.breizhcamp.bzhtime.dto;

import java.util.List;

public class Jour {

    private String titre;

    /** dd/mm/yyyy */
    private String date;

    private List<Proposal> proposals;

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<Proposal> getProposals() {
        return proposals;
    }

    public void setProposals(List<Proposal> proposals) {
        this.proposals = proposals;
    }
}
