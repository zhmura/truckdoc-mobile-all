package com.sanda.checker;

import com.sanda.checker.deprecated.Defaults;

import de.devland.esperandro.SharedPreferenceActions;
import de.devland.esperandro.annotations.Default;
import de.devland.esperandro.annotations.SharedPreferences;

@SharedPreferences(name = "checker_prefs")
public interface CheckerPrefs extends SharedPreferenceActions {

    String NAME = "apnName";
    String APN = "apn";

    @Default(ofString = Defaults.APN_NAME)
    String apnName();

    void apnName(String value);

    @Default(ofString = Defaults.APN)
    String apn();

    void apn(String value);

    void lastTime(String millis);

    String lastTime();

    void networkOperator(String name);

    String networkOperator();

    boolean wasMobileDataEnabled();

    void wasMobileDataEnabled(boolean value);

    boolean wasRoamingDataEnabled();

    void wasRoamingDataEnabled(boolean value);

    boolean wasAirplaneModeEnabled();

    void wasAirplaneModeEnabled(boolean value);
}
