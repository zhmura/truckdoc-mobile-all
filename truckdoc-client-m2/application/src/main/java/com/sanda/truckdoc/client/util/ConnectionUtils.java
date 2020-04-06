package com.sanda.truckdoc.client.util;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.sanda.truckdoc.client.R;

public class ConnectionUtils {

    /**
     * Checks if has internet connection.
     * Shows notification dialog in case phone is not registered in mobile network
     * Shows network settings in case of internet connection absence
     *
     * @param context - app context
     * @return <code>true</code> if connection available and <code>false</code> otherwise
     */
    public static boolean checkIfHaveInternetConnection(final Context context) {
        if (isNotRegisteredInMobileNetwork(context)) {
            return createNotRegisteredInMobileNetworkDialog(context);
        } else if (isInternetConnectionNotAvailable(context)) {
            return createNetworkSettingsDialog(context);
        }
        return true;
    }

    private static boolean createNetworkSettingsDialog(Context context) {
        TelephonyManager telephonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final boolean[] dialogChoose = {false};
        builder.setMessage(telephonyManager.getNetworkOperatorName() +
                context.getResources().getString(R.string.no_internet))
                .setCancelable(true)
                .setPositiveButton(R.string.question_answer_y, (dialog, id) -> {
                    try {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.setClassName("com.android.phone", "com.android.phone.NetworkSetting");
                        context.startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Log.e("TruckDoc", "Activity not found, android settings not launched");
                    }
                })
                .setNeutralButton(context.getResources().getString(R.string.check_anyway), (dialog, id) -> {
                    dialogChoose[0] = true;
                    dialog.cancel();
                })
                .setNegativeButton(R.string.question_answer_n, (dialog, id) -> {
                    dialog.cancel();
                });
        builder.create().show();
        return dialogChoose[0];
    }

    private static boolean createNotRegisteredInMobileNetworkDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final boolean[] dialogChoose = {false};
        builder.setCancelable(true)
                .setMessage(context.getResources().getString(R.string.no_network))
                .setPositiveButton("Ок", (dialog, id) -> {
                    dialog.cancel();
                })
                .setNeutralButton(context.getResources().getString(R.string.check_anyway), (dialog, id) -> {
                    dialogChoose[0] = true;
                    dialog.cancel();
                });
        builder.create().show();
        return dialogChoose[0];
    }

    /**
     * Checks if phone is registered in any mobile operator's network
     *
     * @param context - app context
     * @return <code>true</code> if registered and networks operator name is not empty and <code>false</code> otherwise
     */
    private static boolean isNotRegisteredInMobileNetwork(Context context) {
        TelephonyManager tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return (tel.getNetworkOperator() != null && tel.getNetworkOperator().equals(""));
    }

    /**
     * Checks if internet connection available via any source (2g, 3g, wifi etc)
     *
     * @param context - app context
     * @return <code>true</code> if connection available and <code>false</code> otherwise
     */
    private static boolean isInternetConnectionNotAvailable(final Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo == null || !netInfo.isConnectedOrConnecting();
    }

    /**
     * Checks if has internet connection.
     * Shows notification dialog in case phone is not registered in mobile network
     * Shows suggestion dialog to switch to sms messages in case of internet connection absence
     *
     * @param message - message to be converted to sms in case it can not be sent via http
     * @param phone   - phone number to sent sms in case internet connection is not available
     * @param context - app context
     * @return <code>true</code> if connection available and <code>false</code> otherwise
     */
    public static boolean checkIfHaveInternetConnection(final String message, final String phone, final Context context) {
        if (isInternetConnectionNotAvailable(context)) {
            if (isNotRegisteredInMobileNetwork(context)) {
                return createNotRegisteredInMobileNetworkDialog(context);
            } else {
                createSmsSuggestDialog(message, phone, context);
                return false;
            }
        }
        return true;
    }

    private static void createSmsSuggestDialog(String message, String phone, Context context) {
        TelephonyManager telephonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
        String notificationMessage = telephonyManager.getNetworkOperatorName() + ": " +
                context.getResources().getString(R.string.no_internet_sms);
        new AlertDialog.Builder(context).setMessage(notificationMessage)
                .setCancelable(true)
                .setNeutralButton(R.string.sms, (dialog, id) -> {
                    SmsHelper.sendSmsIntent(context, phone, message);
                    dialog.cancel();
                })
                .create()
                .show();
    }
}
