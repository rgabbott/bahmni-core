package org.bahmni.module.elisatomfeedclient.api.worker;

import org.apache.log4j.Logger;
import org.ict4h.atomfeed.client.domain.Event;
import org.ict4h.atomfeed.client.service.EventWorker;

public class IgnoreEventWorker implements EventWorker {

    private static Logger logger = Logger.getLogger(IgnoreEventWorker.class);
    private String message;

    public IgnoreEventWorker(String message) {
        this.message = message;
    }

    @Override
    public void process(Event event) {
         logger.warn(message);
    }

    @Override
    public void cleanUp(Event event) {

    }
}
