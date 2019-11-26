package com.ultimalabs.sancho.rotctldclient.service;

import com.ultimalabs.sancho.common.config.SanchoConfig;
import com.ultimalabs.sancho.rotctldclient.model.AzimuthElevation;
import com.ultimalabs.sancho.rotctldclient.model.TrackingData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Instant;

/**
 * Client service for rotctld
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class RotctldClientService {

    private final SanchoConfig config;

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    /**
     * Points the rotator in the specified direction
     *
     * @param position parking azimuth/elevation position
     * @return true if successful
     */
    public boolean parkRotator(AzimuthElevation position) {

        startConnection();
        String returnMessage = sendMessage(",\\set_pos " + position.getAzimuth() + " " + position.getElevation());
        stopConnection();

        if (isInvalidResponse(returnMessage)) {
            log.error("Parking failed; rotctld failed executing setAzEl() command. Response: {}", returnMessage);
            return false;
        }

        log.info("Parked the rotator at {}, {}.", position.getAzimuth(), position.getElevation());
        return true;

    }

    /**
     * Tracks the satellite
     * <p>
     * Sends a sequence of "set position" commands to the rotctld, with a
     * short pause (sleepDuration) between them.
     *
     * @param trackingData tracking data
     */
    public void track(TrackingData trackingData) {

        int sleepDuration = (int) config.getRotator().getStepSize() * 1000;

        startConnection();

        log.info("Started tracking {}.", trackingData.getSatName());

        AzimuthElevation oldAzEl = trackingData.getRiseAzimuthElevation();

        while ((Instant.now().toEpochMilli() / 1000) < trackingData.getTrackingEnd()) {

            AzimuthElevation newAzEl = trackingData.getCurrentAzimuthElevation();

            if (newAzEl == null || newAzEl.equals(oldAzEl)) {
                if (newAzEl == null) {
                    log.info("Az/el not found for current timestamp.");
                }
                continue;
            }

            String returnMessage = sendMessage(",\\set_pos " + newAzEl.getAzimuth() + " " + newAzEl.getElevation());

            if (isInvalidResponse(returnMessage)) {
                log.error("Rotctld failed executing setAzEl() command. Response: {}", returnMessage);
            }

            try {
                Thread.sleep(sleepDuration);
            } catch (InterruptedException e) {
                log.error("Interrupted exception: {}", e.getMessage());
            }

            oldAzEl = newAzEl;

        }

        log.info("Stopped tracking {}.", trackingData.getSatName());

        stopConnection();

    }

    /**
     * Get rotator azimuth & elevation
     *
     * @return azimuth & elevation
     */
    public AzimuthElevation getAzEl() {

        startConnection();
        String returnMessage = sendMessage(",\\get_pos");
        stopConnection();

        if (isInvalidResponse(returnMessage)) {
            log.error("Rotctld failed executing getAzEl() command. Response: {}", returnMessage);
            return null;
        }

        String[] parts = returnMessage.split(",");

        return new AzimuthElevation(Double.parseDouble(parts[1].replace("Azimuth: ", "")),
                Double.parseDouble(parts[2].replace("Elevation: ", "")));

    }

    /**
     * Check whether rotctld returned a successful response
     *
     * @param message string returned from rotctld
     * @return whether response indicates a success
     */
    private boolean isInvalidResponse(String message) {
        if (message == null) {
            return true;
        }
        return !message.contains("RPRT 0");
    }

    /**
     * Open a connection to rotctld
     */
    private void startConnection() {
        try {
            clientSocket = new Socket(config.getRotator().getRotctldHost(), config.getRotator().getRotctldPort());
        } catch (IOException e) {
            log.error("Error connecting to rotctld host: {}", e.getMessage());
            return;
        }
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            log.error("Error getting output stream for rotctld host: {}", e.getMessage());
            return;
        }
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            log.error("Error getting input stream for rotctld host: {}", e.getMessage());
        }
    }

    /**
     * Send a message over the socket
     *
     * @param msg message to be sent
     * @return return message from rotctld
     */
    private String sendMessage(String msg) {

        if (out == null || in == null) {
            return null;
        }

        out.println(msg);
        String resp = null;
        try {
            resp = in.readLine();
        } catch (IOException e) {
            log.error("Error reading response from rotctld host: {}", e.getMessage());
        }
        return resp;
    }

    /**
     * Close the connection
     */
    private void stopConnection() {

        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                log.error("Error closing socket reader: {}", e.getMessage());
            }
        }

        if (out != null) {
            out.close();
        }

        if (clientSocket != null) {
            try {
                clientSocket.close();
            } catch (IOException e) {
                log.error("Error closing socket: {}", e.getMessage());
            }
        }
    }

}
