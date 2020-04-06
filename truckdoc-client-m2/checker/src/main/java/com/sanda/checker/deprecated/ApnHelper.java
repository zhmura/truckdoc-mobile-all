package com.sanda.checker.deprecated;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.sanda.checker.CheckerPrefs;

import androidx.annotation.NonNull;
import de.devland.esperandro.Esperandro;

class ApnHelper {

    private static final String TAG = ApnHelper.class.getSimpleName();
    private static final Uri APN_URI = Uri.parse("content://telephony/carriers");
    private static final Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");
    private final Cursor c;

//    public static Single<Boolean> validate(@NonNull Context context) {
//        return Single.create(subscriber -> {
//            CheckerPrefs prefs = Esperandro.getPreferences(CheckerPrefs.class, context);
//            String apnName = prefs.apnName();
//            String apnPref = prefs.apn();
//            if (TextUtils.isEmpty(apnName) || TextUtils.isEmpty(apnPref)) {
//                subscriber.onError(new IllegalStateException("Checker preferences are not defined!"));
//            } else {
//                try {
//                    Apn apn = new ApnHelper(context).read();
//                    Log.v(TAG, apn.toString());
//                    subscriber.onSuccess(apnName.equals(apn.name) && apnPref.equals(apn.apn));
//                    Log.v(TAG, String.format("Found name %s, expected %s, found apn %s, expected %s", apn.name, apnName, apn.apn, apnPref));
//                } catch (Exception e) {
//                    subscriber.onError(e);
//                }
//            }
//        });
//    }

    public static void setDefaults(@NonNull Context context, @NonNull String apnName, @NonNull String apn) {
        CheckerPrefs prefs = Esperandro.getPreferences(CheckerPrefs.class, context);
        prefs.apn(apn);
        prefs.apnName(apnName);
    }

    private ApnHelper(@NonNull Context context) {
        c = context.getContentResolver().query(PREFERRED_APN_URI, null, null, null, null);
    }

    private String string(String columnName) {
        return c.getString(c.getColumnIndex(columnName));
    }

    private Apn read() {
        return Apn.create()
                .apn(string("apn"))
                .name(string("name"))
                .type(string("type"))
                .proxy(string("proxy"))
                .port(string("port"))
                .user(string("user"))
                .password(string("password"))
                .server(string("server"))
                .mmsc(string("mmsc"))
                .mmsproxy(string("mmsproxy"))
                .mmsport(string("mmsport"))
                .mcc(string("mcc"))
                .mnc(string("mnc"))
                .numeric(string("numeric"))
                .build();
    }
}
