package com.ultimalabs.sattrackclient.common.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Satellite pass details
 */
@Data
public class PassEventDetailsEntry implements Serializable {

    /**
     * Date and time for this data item
     */
    private Date t;

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
