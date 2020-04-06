package com.sanda.checker;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import org.joda.time.DateTime;

import de.devland.esperandro.Esperandro;

public class Checker {

    private static final int MIN_HOUR = 7;
    private static final int MAX_HOUR = 20;
    public static final String AFTER_BOOT_ACTION = "com.sanda.checker.Checker.AFTER_BOOT_ACTION";
    public static final int AFTER_BOOT_DELAY = 30 * 1000;

    private final Context context;
    private final CheckerPrefs prefs;

/*    public static void checkDataEnabled(final Context context) throws ClassNotFoundException {
        new Checker(context).check();
    }*/

    public static void setupCheckConnectionAfterBootIfNeeded(final Context context) throws ClassNotFoundException {
        Log.d("Checker", "setupCheckAfterBoot");
        if (wasConnectionLastTime(context)) {
            return;
        }

        Intent alarmIntent = getAfterBootIntent(context);
        boolean isAlarmUp = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_NO_CREATE) != null; //No creation
        if (!isAlarmUp) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0); //Creation
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    /*start time immediately*/ 0, AFTER_BOOT_DELAY, pendingIntent);
        }
    }

    /**
     * Checks if there was connection last time
     */
    public static boolean wasConnectionLastTime(final Context context) {
        CheckerPrefs prefs = Esperandro.getPreferences(CheckerPrefs.class, context);
        return TextUtils.isEmpty(prefs.lastTime());
    }

    public static void cancelCheckConnectionAfterBoot(final Context context) throws ClassNotFoundException {
        Log.d("Checker", "cancelCheckConnectionAfterBoot");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = getAfterBootIntent(context);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        alarmManager.cancel(pendingIntent);
    }

    private static Intent getAfterBootIntent(final Context context) throws ClassNotFoundException {
        Intent intent = new Intent(context, Class.forName("com.sanda.truckdoc.client.receivers.CheckConnectionAfterBootReceiver"));
        intent.setAction(AFTER_BOOT_ACTION);
        return intent;
    }

    private Checker(final Context context) {
        this.context = context;
        prefs = Esperandro.getPreferences(CheckerPrefs.class, context);
    }

    private boolean isMobileDataEnabled() {
        return Settings.Secure.getInt(context.getContentResolver(), "mobile_data", 0) == 1;
    }

    private boolean isRoamingDataEnabled() {
        return Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.DATA_ROAMING, 0) == 1;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public boolean isAirplaneModeEnabled() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            return Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }
    }

/*
    private void check() throws ClassNotFoundException {
        if (isConnected()) {
            prefs.lastTime(null);
            prefs.networkOperator(getNetworkOperatorName());
            prefs.wasMobileDataEnabled(isMobileDataEnabled());
            prefs.wasRoamingDataEnabled(isRoamingDataEnabled());
            prefs.wasAirplaneModeEnabled(isAirplaneModeEnabled());
            connectionRestored();
            return;
        }

        int hourOfDay = LocalDateTime.now().getHourOfDay();
        if (MIN_HOUR <= hourOfDay && hourOfDay <= MAX_HOUR) {
            if (!TextUtils.isEmpty(prefs.lastTime())) {
                DateTime lastTime = DateTime.parse(prefs.lastTime());
                if (lastTime != null) {
                    if (isExpired(lastTime)) {
                        noConnectionForLongTime();
                    }
                } else {
                    prefs.lastTime(DateTime.now().toString());
                }
            } else {
                prefs.lastTime(DateTime.now().toString());
            }
        }
    }
*/

    private String getNetworkOperatorName() {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getNetworkOperatorName();
    }

    private boolean isExpired(DateTime lastTime) {
        return lastTime.isBefore(BuildConfig.DEBUG ? DateTime.now().minusSeconds(5) : DateTime.now().minusHours(2));
    }

    private void noConnectionForLongTime() throws ClassNotFoundException {
        NoConnectionReceiver.start(context,
                isMobileDataEnabled(),
                isRoamingDataEnabled(),
                isAirplaneModeEnabled(),
                hasNetworkOperatorChanged(),
                prefs.wasMobileDataEnabled(),
                prefs.wasRoamingDataEnabled(),
                prefs.wasAirplaneModeEnabled());
    }

    private void connectionRestored() {
        ConnectionRestoredReceiver.start(context);
    }

    boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private boolean hasNetworkOperatorChanged() {
        String operator = prefs.networkOperator();
        return operator != null && !operator.equalsIgnoreCase(getNetworkOperatorName());
    }
}
