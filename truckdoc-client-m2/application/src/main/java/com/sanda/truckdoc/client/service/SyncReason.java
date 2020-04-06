package com.sanda.truckdoc.client.service;

/**
 * @author Alexei Osipov
 */
public enum SyncReason {
    PERIODIC_CHECK, // Periodic message sync
    ALARM_CHANGED, // Alarm was reset
    NETWORK_AVAILABLE, // Network become available
    GOT_SMS, // Got new SMS
    USER_DIRECT, // User pressed refresh button

    UNKNOWN // Do not use as reason!
}
