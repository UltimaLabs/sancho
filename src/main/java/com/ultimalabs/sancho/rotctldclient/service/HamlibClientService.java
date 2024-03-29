package com.ultimalabs.sancho.rotctldclient.service;

import com.ultimalabs.sancho.common.config.SanchoConfig;
import com.ultimalabs.sancho.rotctldclient.model.AzimuthElevation;
import com.ultimalabs.sancho.rotctldclient.model.RadioParams;
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
 * Client service for rigctld/rotctld
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class HamlibClientService {

    private final SanchoConfig config;
    private Socket clientSocketRigctl;
    private PrintWriter outRigctl;
    private BufferedReader inRigctl;
    private Socket clientSocketRotctl;
    private PrintWriter outRotctl;
    private BufferedReader inRotctl;

    /**
     * Points the rotator in the specified direction
     *
     * @param position parking azimuth/elevation position
     * @return true if successful
     */
    public boolean parkRotator(AzimuthElevation position) {

        startConnection();
        String returnMessage = sendMessage(",\\set_pos " + position.getAzimuth() + " " + position.getElevation(), SendWhere.ROTCTL);
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

        int sleepDuration = (int) (config.getRotator().getStepSize() * 1000);

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

            String returnMessage = sendMessage(",\\set_pos " + newAzEl.getAzimuth() + " " + newAzEl.getElevation(), SendWhere.ROTCTL);

            if (isInvalidResponse(returnMessage)) {
                log.error("Rotctld failed executing setAzEl() command. Response: {}", returnMessage);
            }

            try {
                Thread.sleep(sleepDuration);
            } catch (InterruptedException e) {
                log.error("Interrupted exception: {}", e.getMessage());
                Thread.currentThread().interrupt();
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
        String returnMessage = sendMessage(",\\get_pos", SendWhere.ROTCTL);
        log.info("Got this back: {}", returnMessage);
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
     * Get the current radio parameters
     *
     * @return current radio parameters
     */
    public RadioParams getRadioParams() {

        startConnection();
        String returnMessage = sendMessage(",\\get_freq", SendWhere.RIGCTL);
        log.info("Got this back: {}", returnMessage);
        stopConnection();

        if (returnMessage == null) {
            return null;
        }

        String[] parts = returnMessage.split(",");

        if (isInvalidResponse(returnMessage)) {
            log.error("Rigctld failed executing getRadioFrequency() command. Response: {}", returnMessage);
            return null;
        }

        Double frequency = Double.parseDouble(parts[1].replace("Frequency: ", ""));

        return new RadioParams(frequency);

    }

    /**
     * Check whether rigctld/rotctld returned a successful response
     *
     * @param message string returned from rigctld/rotctld
     * @return whether response indicates a success
     */
    private boolean isInvalidResponse(String message) {
        if (message == null) {
            return true;
        }
        return !message.contains("RPRT 0");
    }

    /**
     * Open a connection to rigctld/rotctld
     */
    private void startConnection() {
        try {

            if (!config.getRotator().getRotctldHost().equals("")) {
                clientSocketRotctl = new Socket(config.getRotator().getRotctldHost(), config.getRotator().getRotctldPort());
            } else {
                clientSocketRotctl = null;
            }

            if (!config.getRadio().getRigctldHost().equals("")) {
                clientSocketRigctl = new Socket(config.getRadio().getRigctldHost(), config.getRadio().getRigctldPort());
            } else {
                clientSocketRigctl = null;
            }

        } catch (IOException e) {
            log.error("Error connecting to host: {}", e.getMessage());
            return;
        }
        try {

            if (clientSocketRotctl != null) {
                outRotctl = new PrintWriter(clientSocketRotctl.getOutputStream(), true);
            } else {
                outRotctl = null;
            }

            if (clientSocketRigctl != null) {
                outRigctl = new PrintWriter(clientSocketRigctl.getOutputStream(), true);
            } else {
                outRigctl = null;
            }

        } catch (IOException e) {
            log.error("Error getting output stream for host: {}", e.getMessage());
            return;
        }
        try {

            if (clientSocketRotctl != null) {
                inRotctl = new BufferedReader(new InputStreamReader(clientSocketRotctl.getInputStream()));
            } else {
                inRotctl = null;
            }

            if (clientSocketRigctl != null) {
                inRigctl = new BufferedReader(new InputStreamReader(clientSocketRigctl.getInputStream()));
            } else {
                inRigctl = null;
            }

        } catch (IOException e) {
            log.error("Error getting input stream for host: {}", e.getMessage());
        }
    }

    /**
     * Send a message over the socket
     *
     * @param msg   message to be sent
     * @param where where to - rigctl/rotclt
     * @return return message from rigctld/rotctld
     */
    private String sendMessage(String msg, SendWhere where) {

        PrintWriter out;
        BufferedReader in;

        if (where == SendWhere.RIGCTL) {
            out = outRigctl;
            in = inRigctl;
        } else if (where == SendWhere.ROTCTL) {
            out = outRotctl;
            in = inRotctl;
        } else {
            out = null;
            in = null;
        }

        if (out == null || in == null) {
            return null;
        }

        out.println(msg);
        String resp = null;
        try {
            resp = in.readLine();
        } catch (IOException e) {
            log.error("Error reading response from host: {}", e.getMessage());
        }
        return resp;
    }

    /**
     * Close the connection to both rigctld and rotcld
     */
    private void stopConnection() {

        closeParticularConnection(clientSocketRigctl, outRigctl, inRigctl);
        closeParticularConnection(clientSocketRotctl, outRotctl, inRotctl);

    }

    /**
     * Close the particular rigctld/rotcld connection
     *
     * @param clientSocket client socket
     * @param out          print writer
     * @param in           buffered reader
     */
    private void closeParticularConnection(Socket clientSocket, PrintWriter out, BufferedReader in) {

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

    private enum SendWhere {
        RIGCTL,
        ROTCTL
    }


}
