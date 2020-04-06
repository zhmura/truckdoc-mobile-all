package com.sanda.truckdoc.client.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.sanda.truckdoc.client.R;

import timber.log.Timber;

/**
 * Created by astra on 19.08.2015.
 */
public class SmsHelper {

    private static void sendSms(String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }

    public static void sendSmsToOperatorNoConnection(final Context context) {
        sendSms("9111", context.getString(R.string.sms_operator_no_connection));
    }

    public static boolean sendSmsIntent(final Context context, final String phone, final String message) {
        try {
            Intent smsIntent = new Intent(Intent.ACTION_VIEW);
            smsIntent.setType("vnd.android-dir/mms-sms");
            smsIntent.putExtra("address", phone);
            smsIntent.putExtra("sms_body", message);
            context.startActivity(smsIntent);
            return true;
        } catch (ActivityNotFoundException e) {
            Timber.e(e, "Error sending sms");
            Toast.makeText(context, R.string.error_sms_app_not_found, Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
