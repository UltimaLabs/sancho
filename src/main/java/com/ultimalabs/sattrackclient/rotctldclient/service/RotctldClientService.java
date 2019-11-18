package com.ultimalabs.sattrackclient.rotctldclient.service;

import com.ultimalabs.sattrackclient.common.config.SatTrackClientConfig;
import com.ultimalabs.sattrackclient.rotctldclient.model.AzimuthElevation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Client service for clientd
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class RotctldClientService {

    private final SatTrackClientConfig config;

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    /**
     * Parks the rotator
     *
     * @param position parking azimuth/elevation
     * @return true if successful
     */
    public boolean parkRotator(AzimuthElevation position) {

        List<AzimuthElevation> positions = new ArrayList<>();
        positions.add(position);
        int sleepDuration = config.getRotatorConfig().getWaitAfterParkingCommand() * 1000;

        return setAzEl(positions, sleepDuration);

    }

    /**
     *
     *
     * @param azimuthElevation list of azimuth/elevation positions
     * @param sleepDuration sleep (in ms) between positioning commands
     * @return true if the operation was successful
     */
    public boolean setAzEl(List<AzimuthElevation> azimuthElevation, int sleepDuration) {

        startConnection();

        for (AzimuthElevation azEl : azimuthElevation) {

            String returnMessage = sendMessage(",\\set_pos " + azEl.getAzimuth() + " " + azEl.getElevation());

            if (isInvalidResponse(returnMessage)) {
                log.error("Rotctld failed executing setAzEl() command. Response: {}", returnMessage);
                return false;
            }

            try {
                Thread.sleep(sleepDuration);
            } catch (InterruptedException e) {
                log.error("Interrupted exception: {}", e.getMessage());
            }
        }

        stopConnection();

        return true;

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

        if (returnMessage == null) {
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
            return false;
        }
        return message.contains("RPRT 0");
    }

    /**
     * Open a connection to collectd
     */
    private void startConnection() {
        try {
            clientSocket = new Socket(config.getRotatorConfig().getRotctldHost(), config.getRotatorConfig().getRotctldPort());
        } catch (IOException e) {
            log.error("Error connecting to collectd host: {}", e.getMessage());
            return;
        }
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            log.error("Error getting output stream for collectd host: {}", e.getMessage());
            return;
        }
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            log.error("Error getting input stream for collectd host: {}", e.getMessage());
        }
    }

    /**
     * Send a message over the socket
     *
     * @param msg message to be sent
     * @return return message from collectd
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
            log.error("Error reading response from collectd host: {}", e.getMessage());
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
