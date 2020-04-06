package com.sanda.truckdoc.client.util;

import com.sanda.truckdoc.client.Prefs;

import java.util.concurrent.TimeUnit;

/**
 * @author Alexei Osipov
 */
public class PrefUtil {

    public static long getSyncIntervalMs(Prefs prefs) {
        int syncInterval = Integer.valueOf(prefs.syncInterval());
        if (syncInterval < 1) {
            syncInterval = Prefs.DEFAULT_NOTIFY_INTERVAL_MINUTE_VALUE;
        }
        return TimeUnit.MINUTES.toMillis(syncInterval);
    }
}
