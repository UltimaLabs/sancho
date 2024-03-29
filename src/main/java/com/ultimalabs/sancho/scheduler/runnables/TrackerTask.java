package com.ultimalabs.sancho.scheduler.runnables;

import com.ultimalabs.sancho.rotctldclient.model.TrackingData;
import com.ultimalabs.sancho.rotctldclient.service.HamlibClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * Satellite tracking
 */
@Slf4j
@RequiredArgsConstructor
public class TrackerTask implements Runnable {

    private final HamlibClientService hamlibClientService;
    private final TrackingData trackingData;

    @Override
    public void run() {

        log.info("Started TrackerTask on thread {}", Thread.currentThread().getName());
        hamlibClientService.track(trackingData);

    }
}

