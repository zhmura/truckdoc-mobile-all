package com.sanda.truckdoc.client.updater;

import de.devland.esperandro.SharedPreferenceActions;
import de.devland.esperandro.annotations.SharedPreferences;

/**
 * Created by astra on 07.06.2015.
 */
@SharedPreferences
public interface Prefs extends SharedPreferenceActions {

    String APK_PATH                   = "apkPath";
    String VERSION_CODE               = "versionCode";
    String LAST_SUCCESSFUL_CHECK_DATE = "lastSuccessfulCheck";
    String LAST_CHECK_DATE            = "lastCheck";

    String targetPackage();

    void targetPackage(String targetPackage);

    boolean useWiFi();

    void useWifi(boolean useWifi);
}
