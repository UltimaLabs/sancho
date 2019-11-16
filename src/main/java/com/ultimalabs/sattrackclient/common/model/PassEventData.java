package com.ultimalabs.sattrackclient.common.model;

import com.ultimalabs.sattrackclient.common.config.SatelliteData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Event data for a satellite pass
 */
@Slf4j
@Data
public class PassEventData implements Serializable, Comparable<PassEventData> {

    private long id;

    /**
     * Satellite data
     */
    private SatelliteData satelliteData;

    /**
     * Current date and time
     */
    private Date now;

    /**
     * Wait time for the rise event (rise - now), in seconds
     */
    private Double wait;

    /**
     * Satellite rise time
     */
    private Date rise;

    /**
     * Satellite set time
     */
    private Date set;

    /**
     * Pass duration, in seconds
     */
    private double duration;

    /**
     * Pass event entries
     */
    private List<PassEventDetailsEntry> eventDetails;

    /**
     * Comparable interface implementation
     * <p>
     * We declare that the pass is smaller (earlier) when this event's set time is before the other event's rise.
     * If they overlap, we declare that the smaller one is the one with longer duration (satellite visibility).
     * In this context smaller = higher priority.
     *
     * @param other object we're comparing this one with
     * @return comparation result
     */
    public int compareTo(PassEventData other) {

        Date thisRise = this.getRise();
        Date thisSet = this.getSet();
        Date otherRise = other.getRise();
        Date otherSet = other.getSet();

        if (thisRise.equals(otherRise) && thisSet.equals(otherSet)) {
            return 0;
        }

        // this one ends before or at the same time the other one begins
        if (thisSet.before(otherRise) || thisSet.equals(otherRise)) {
            return -1;
        }

        // the other one ends before or at the same time this one begins
        if (thisRise.after(otherSet) || thisRise.equals(otherSet)) {
            return 1;
        }

        // overlap, the other pass starts after this one
        if (thisSet.after(otherRise) && (thisSet.before(otherSet) || thisSet.equals(otherSet))) {
            // we have inversion (-1 * result) because this pass has higher priority if it's longer
            return -1 * Double.compare(this.getDuration(), other.getDuration());
        }

        // overlap, the other pass started before this one
        if (otherSet.after(thisRise) && (otherSet.before(thisSet) || otherSet.equals(thisSet))) {
            // we have inversion (-1 * result) because this pass has higher priority if it's longer
            return -1 * Double.compare(this.getDuration(), other.getDuration());
        }

        // overlap, the other pass occurs within this one, this pass is longer
        if (otherRise.after(thisRise) && otherSet.before(thisSet)) {
            return -1;
        }

        // overlap, this pass occurs within the other one, so this pass is shorter
        if (thisRise.after(otherRise) && thisSet.before(otherSet)) {
            return 1;
        }

        return -1;

    }


}

