package com.sanda.truckdoc.client.service.info;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.sanda.truckdoc.client.BuildConfig;
import com.sanda.truckdoc.client.api.v3.configuration.model.app.AppFeatures;
import com.sanda.truckdoc.client.api.v3.configuration.model.app.AppInfo;
import com.sanda.truckdoc.client.api.v3.configuration.model.app.features.GeoFeatures;
import com.sanda.truckdoc.client.api.v3.configuration.model.app.features.MessageFeatures;
import com.sanda.truckdoc.client.api.v3.configuration.model.device.DeviceInfo;
import com.sanda.truckdoc.client.api.v3.configuration.model.device.Dimensions;
import com.sanda.truckdoc.client.api.v3.configuration.model.sim.SimInfo;

import com.google.common.collect.ImmutableList;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

/**
 * @author Alexei Osipov
 */
public class RegistrationInfoProvider {

    @NonNull
    public static SimInfo getSimInfo(Context context, TelephonyManager tMgr) {
        SimInfo simInfo = new SimInfo();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            simInfo.setPhoneNumber(tMgr.getLine1Number());
            simInfo.setSimSerialNumber(tMgr.getSimSerialNumber());
        }
        return simInfo;
    }

    @NonNull
    public static DeviceInfo getClientDeviceInfo(ContextWrapper contextWrapper) {
        TelephonyManager tMgr = (TelephonyManager) contextWrapper.getSystemService(Context.TELEPHONY_SERVICE);

        DeviceInfo deviceInfo = new DeviceInfo();
        String androidId = Settings.Secure.getString(contextWrapper.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        deviceInfo.setAndroidId(androidId);
        deviceInfo.setDeviceId(androidId);
        deviceInfo.setPhoneModel(android.os.Build.MODEL);
        deviceInfo.setPhoneManufacturer(android.os.Build.MANUFACTURER);
        deviceInfo.setAndroidVersion(android.os.Build.VERSION.RELEASE);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) contextWrapper.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displaymetrics);
        Dimensions screenSize = new Dimensions();
        screenSize.setX((long) displaymetrics.heightPixels);
        screenSize.setY((long) displaymetrics.widthPixels);
        deviceInfo.setScreenSize(screenSize);
        return deviceInfo;
    }


    /**
     * Generate client app info to inform server about current versions and features
     */
    @NonNull
    public static AppInfo getClientAppInfo(ContextWrapper contextWrapper) {
        AppInfo appInfo = new AppInfo();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        appInfo.setAppBuildDate(df.format(new Date(BuildConfig.TIMESTAMP))); // This will be deprecated in favor of appBuildTimestamp
        appInfo.setAppBuildTimestamp(BuildConfig.TIMESTAMP);
        appInfo.setAppId(BuildConfig.APPLICATION_ID);
        String packageName = contextWrapper.getPackageName();
        appInfo.setCustomBuildCompanyId(packageName.substring(packageName.lastIndexOf('.') + 1)); // TODO: Wrong!
        appInfo.setAppBuildType(BuildConfig.BUILD_TYPE);
        appInfo.setAppFlavor(BuildConfig.FLAVOR);
        appInfo.setIsDev(BuildConfig.DEV);
        appInfo.setIsDebug(BuildConfig.DEBUG);


        try {
            PackageManager packageManager = contextWrapper.getPackageManager();
            PackageInfo pInfo = packageManager.getPackageInfo(packageName, 0);
            appInfo.setAppVersion(pInfo.versionName);
            appInfo.setAppVersionCode((long) pInfo.versionCode);
            appInfo.setAppPackage(pInfo.packageName);
            ApplicationInfo ai = packageManager.getApplicationInfo(packageName, 0);
            appInfo.setAppName(ai != null ? String.valueOf(packageManager.getApplicationLabel(ai)) : "(unknown)");
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        appInfo.setSupportedFeatures(getClientSupportedFeatures(contextWrapper));
        return appInfo;
    }

    @NonNull
    /**
     * In this method we should set all features current client version supports.
     *
     */
    private static AppFeatures getClientSupportedFeatures(ContextWrapper contextWrapper) {
        AppFeatures supportedFeatures = new AppFeatures();
        GeoFeatures geoFeatures = new GeoFeatures();

        PackageManager packageManager = contextWrapper.getPackageManager();
        boolean hasGPS = packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
        geoFeatures.setCanSendLocation(hasGPS);
        geoFeatures.setConfigurableLocationSendInterval(true);
        geoFeatures.setRouteNavigation(isAppPackageInstalled(contextWrapper, "com.mapfactor.navigator"));
        supportedFeatures.setGeoFeatures(geoFeatures);

        supportedFeatures.setInsuranceReport(false);
        supportedFeatures.setMaintenanceReports(true);

        MessageFeatures messageFeatures = new MessageFeatures();
        messageFeatures.setCanReceiveMessages(true);
        messageFeatures.setCanSendMessage(true);
        messageFeatures.setSupportedFileExtensions(ImmutableList.of("pdf", "jpeg", "png", "jpg"));
        messageFeatures.setContactListSupport(true);
        supportedFeatures.setMessageFeatures(messageFeatures);

        return supportedFeatures;
    }

    private static boolean isAppPackageInstalled(ContextWrapper contextWrapper, String packageName) {
        PackageManager pm = contextWrapper.getPackageManager();
        boolean appInstalled;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            appInstalled = true;
        } catch (PackageManager.NameNotFoundException e) {
            appInstalled = false;
        }
        return appInstalled;
    }
}
