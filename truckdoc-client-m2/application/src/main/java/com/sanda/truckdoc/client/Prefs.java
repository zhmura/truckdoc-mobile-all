package com.sanda.truckdoc.client;

import de.devland.esperandro.annotations.Default;
import de.devland.esperandro.annotations.SharedPreferences;

@SharedPreferences(name = Prefs.FILENAME)
public interface Prefs {

    String FILENAME = "PREFS_KEY";

    String DEFAULT_NOTIFY_INTERVAL_STRING_MINUTE_VALUE = "5";
    int DEFAULT_NOTIFY_INTERVAL_MINUTE_VALUE = Integer.valueOf(DEFAULT_NOTIFY_INTERVAL_STRING_MINUTE_VALUE);
    long LOCATION_WRITE_INTERVAL = 600000L;

    boolean timerStatus();

    @Default(ofString = DEFAULT_NOTIFY_INTERVAL_STRING_MINUTE_VALUE)
    String syncInterval();

    void syncInterval(String interval);

    void timerStatus(boolean b);

    String columnPhone();

    String mechanicPhone();

    String expediterPhone();

    String techSupportPhone();

    void accidentState(String s);

    String accidentState();

    int accidentSelectedPage();

    void accidentSelectedPage(int value);

    @Default(ofLong = LOCATION_WRITE_INTERVAL)
    long gpsDataSyncTime();

    void gpsDataSyncTime(long value);

    boolean sendMessage();

    void sendMessage(boolean send);

    void contactListVersion(long versionNumber);

    long contactListVersion();

    long currentRouteAssignment();

    void currentRouteAssignment(long id);

    /**
     * @return timestamp of most recent sync access
     */
    long lastSyncAttemptTs();

    void lastSyncAttemptTs(long timestamp);

    /**
     * @return timestamp of most recent successful synchronization
     */
    long lastSuccessfulSyncTs();

    void lastSuccessfulSyncTs(long timestamp);

    long lastNetworkChangeNotificationTs();

    void lastNetworkChangeNotificationTs(long timestamp);

    /**
     * @return last saved version code of client application
     */
    long lastSavedClientVersionCode();

    void lastSavedClientVersionCode(long versionCode);

    long lastKnownRouteAssignment();

    void lastKnownRouteAssignment(long lastKnownRouteAssigment);

    long lastKnownMaintenanceConfigVersion();

    void lastKnownMaintenanceConfigVersion(long lastKnownMaintenanceConfirVersion);

    long lastKnownClientConfigVersion();

    void lastKnownClientConfigVersion(long lastKnownClientConfigVersion);

    boolean hasMaintenance();

    void hasMaintenance(boolean hasMaintenance);
}
