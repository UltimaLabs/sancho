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
public class SatellitePass implements Serializable, Comparable<SatellitePass> {

    private long id;

    /**
     * TLE data used for pass prediction
     */
    private String tle;

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
     * Satellite rise data point
     */
    private PassEventDataPoint risePoint;

    /**
     * Satellite pass midpoint
     */
    private PassEventDataPoint midPoint;

    /**
     * Satellite set data point
     */
    private PassEventDataPoint setPoint;

    /**
     * Pass duration, in seconds
     */
    private double duration;

    /**
     * Pass event entries
     */
    private List<PassEventDataPoint> eventDetails;

    /**
     * Comparable interface implementation
     * <p>
     * We declare that the pass is smaller (earlier) when this event's set time is before the other event's rise.
     * If they overlap, we declare that the smaller one is the one with higher maximum elevation.
     * In this context smaller = higher priority.
     *
     * @param other object we're comparing this one with
     * @return comparison result
     */
    public int compareTo(SatellitePass other) {

        if (compareEqual(other) != null) {
            return 0;
        }

        Integer thisHigherPriority = compareThisHigherPriority(other);

        if ( thisHigherPriority != null) {
            return thisHigherPriority;
        }

        Integer otherHigherPriority = compareOtherHigherPriority(other);

        if (otherHigherPriority != null) {
            return 1;
        }

        return -1;

    }

    /**
     * Check whether this and the other pass are equal
     *
     * @param other object we're comparing this one with
     * @return comparison result
     */
    private Integer compareEqual(SatellitePass other) {

        Date thisRise = this.getRisePoint().getT();
        Date thisSet = this.getSetPoint().getT();
        Date otherRise = other.getRisePoint().getT();
        Date otherSet = other.getSetPoint().getT();

        if (this == other) {
            return 0;
        }

        if (thisRise.equals(otherRise) && thisSet.equals(otherSet)) {
            return 0;
        }

        return null;

    }

    /**
     * Check whether this pass is smaller (has higher priority)
     *
     * @param other object we're comparing this one with
     * @return comparison result
     */
    private Integer compareThisHigherPriority(SatellitePass other) {

        Date thisRise = this.getRisePoint().getT();
        Date thisSet = this.getSetPoint().getT();
        Date otherRise = other.getRisePoint().getT();
        Date otherSet = other.getSetPoint().getT();

        // this one ends before or at the same time the other one begins
        if (thisSet.before(otherRise) || thisSet.equals(otherRise)) {
            return -1;
        }

        // overlap, the other pass starts after this one
        if (thisSet.after(otherRise) && (thisSet.before(otherSet) || thisSet.equals(otherSet))) {
            // we have inversion (-1 * result) because this pass has higher priority
            // if it has higher maximum elevation
            return -1 * Double.compare(this.getMidPoint().getEl(), other.getMidPoint().getEl());
        }

        // overlap, the other pass started before this one
        if (otherSet.after(thisRise) && (otherSet.before(thisSet) || otherSet.equals(thisSet))) {
            // we have inversion (-1 * result) because this pass has higher priority
            // if it has higher maximum elevation
            return -1 * Double.compare(this.getMidPoint().getEl(), other.getMidPoint().getEl());
        }

        // overlap, the other pass occurs within this one, this pass is longer
        if (otherRise.after(thisRise) && otherSet.before(thisSet)) {
            return -1;
        }

        return null;

    }

    /**
     * Check whether the other pass is smaller (has higher priority)
     *
     * @param other object we're comparing this one with
     * @return comparison result
     */
    private Integer compareOtherHigherPriority(SatellitePass other) {

        Date thisRise = this.getRisePoint().getT();
        Date thisSet = this.getSetPoint().getT();
        Date otherRise = other.getRisePoint().getT();
        Date otherSet = other.getSetPoint().getT();

        // the other one ends before or at the same time this one begins
        if (thisRise.after(otherSet) || thisRise.equals(otherSet)) {
            return 1;
        }

        // overlap, this pass occurs within the other one, so this pass is shorter
        if (thisRise.after(otherRise) && thisSet.before(otherSet)) {
            return 1;
        }

        return null;

    }

}

