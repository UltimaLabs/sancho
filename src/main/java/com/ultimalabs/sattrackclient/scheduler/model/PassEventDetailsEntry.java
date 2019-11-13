package com.ultimalabs.sattrackclient.scheduler.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Satellite pass details
 */
@Data
class PassEventDetailsEntry implements Serializable {

    /**
     * Date and time for this data item
     */
    private String t;

    /**
     * Azimuth
     */
    private double az;

    /**
     * Elevation
     */
    private double el;

    /**
     * Distance, in meters
     */
    private double dst;

    /**
     * Doppler shift, in Hz
     */
    private double dop;

}
