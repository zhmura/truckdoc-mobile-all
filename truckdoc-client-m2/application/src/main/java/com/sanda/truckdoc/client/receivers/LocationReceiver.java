package com.sanda.truckdoc.client.receivers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;

import com.sanda.truckdoc.client.BuildConfig;
import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.TruckDocApp;
import com.sanda.truckdoc.client.data.model.DbLocation;
import com.sanda.truckdoc.client.service.CustomToast;
import com.sanda.truckdoc.client.util.timber.L;

import org.androidannotations.annotations.EReceiver;
import org.androidannotations.annotations.ReceiverAction;
import org.androidannotations.api.support.content.AbstractBroadcastReceiver;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import timber.log.Timber;

@EReceiver
public class LocationReceiver extends AbstractBroadcastReceiver {

    public static final String ACTION_LOCATION_CHANGED = "com.sanda.truckdoc.client.receivers.LOCATION_CHANGED";

    public static final long DEFAULT_GPS_CHECK_INTERVAL = BuildConfig.DEBUG ? TimeUnit.SECONDS.toMillis(60) : TimeUnit.MINUTES.toMillis(10);

    public static final long TURN_OFF_GPS_SPECIAL_VALUE = 0;

    /**
     * @param timeMs 0 means turn off GPS
     */
    public static void requestLocationUpdates(@NonNull Context context, long timeMs) {
        Intent intent = new Intent(context, LocationReceiver_.class);
        intent.setAction(ACTION_LOCATION_CHANGED);
        PendingIntent pendingIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        }
        if (timeMs <= 0L) {
            if (pendingIntent != null) {
                pendingIntent.cancel();
            }
            return;
        }
        final Criteria locationCriteria = new Criteria();

        locationCriteria.setAccuracy(Criteria.ACCURACY_FINE);
        locationCriteria.setPowerRequirement(Criteria.POWER_MEDIUM);

        if (pendingIntent != null) {
            L.v("Starting location updates");
            try {
                final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                if (locationManager != null) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        locationManager.requestLocationUpdates(timeMs, 0f, locationCriteria, pendingIntent);
                    }
                }
            } catch (Exception e) {
                Timber.e(e, "Location update failed");
                CustomToast.showToast(context, context.getResources().getText(R.string.no_location_providers_found).toString());
            }
        } else {
            L.v("Location updates already started");
        }
    }

    @ReceiverAction(actions = ACTION_LOCATION_CHANGED)
    void onLocation(@Nullable @org.androidannotations.annotations.ReceiverAction.Extra(LocationManager.KEY_LOCATION_CHANGED) Location location,
                    @NonNull Context context) {
        //@Nullable @org.androidannotations.annotations.ReceiverAction.Extra(LocationManager.KEY_LOCATION_CHANGED) Location location,
        //@NotNull Context context) {
        L.v(location);
        if (location != null) {
            TruckDocApp.get(context).appComponent().db().insertLocation(new DbLocation(location)).subscribe(integer -> {
            }, L::e);
        }
    }

}
