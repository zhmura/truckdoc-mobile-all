package com.sanda.truckdoc.client.updater.receivers;

import android.app.AlarmManager;

import java.util.concurrent.TimeUnit;

/**
 * Created by astra on 06.07.2015.
 */
public interface ReceiverConfig {

    long UPDATE_DELAY  = AlarmManager.INTERVAL_HOUR;
    long INSTALL_DELAY = AlarmManager.INTERVAL_HALF_HOUR;

    long MIN_INTERVAL_BETWEEN_UPDATE_CHECK = TimeUnit.HOURS.toMillis(23);
    long MIN_INTERVAL_AFTER_UPDATE_CHECK_FAILURE = TimeUnit.MINUTES.toMillis(50);
}
