package com.sanda.truckdoc.client.updater.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.telephony.TelephonyManager;

import java.io.File;
import java.util.UUID;

import androidx.core.content.FileProvider;
import timber.log.Timber;

import static com.sanda.truckdoc.client.updater.BuildConfig.APPLICATION_ID;

/**
 * Created by astra on 05.07.2015.
 */
public class DeviceHelper {

    public static DeviceIds getDeviceId(Context c) {
        final TelephonyManager tm = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);

        final String androidId;
        androidId = android.provider.Settings.Secure.getString(c.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = UUID.nameUUIDFromBytes(androidId.getBytes());

        DeviceIds ids = new DeviceIds();
        ids.deviceId = null;
        ids.androidId = androidId;
        ids.deviceUuid = deviceUuid.toString();

        return ids;
    }

    public static int findUpdaterVersionCode(Context c) {
        try {
            return c.getPackageManager().getPackageInfo(APPLICATION_ID, 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

    public static int findVersionCode(String targetPackage, Context c) {
        try {
            return c.getPackageManager().getPackageInfo(targetPackage, 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

    public static int getFileVersionCode(Context c, String filePath) {
        PackageInfo packageArchiveInfo = c.getPackageManager().getPackageArchiveInfo(filePath, 0);
        if (packageArchiveInfo != null) {
            return packageArchiveInfo.versionCode;
        }
        throw new RuntimeException("Can't parse file");
    }

    public static String findVersionName(String targetPackage, Context c) {
        try {
            return c.getPackageManager().getPackageInfo(targetPackage, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void installFile(Context context, File file) {
        Timber.i("Install file " + file);
        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
        NotificationHelper.soundNotification(context);
    }
}
