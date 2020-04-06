package com.sanda.truckdoc.client.updater.receivers;

import java.util.concurrent.TimeUnit;

/**
 * Created by astra on 06.07.2015.
 */
public interface ReceiverConfig {

    long UPDATE_DELAY = 15 * 1000; //ms
    long INSTALL_DELAY = 10 * 1000;

    long MIN_INTERVAL_BETWEEN_UPDATE_CHECK = TimeUnit.MINUTES.toMillis(2);
    long MIN_INTERVAL_AFTER_UPDATE_CHECK_FAILURE = TimeUnit.MINUTES.toMillis(1);
}
