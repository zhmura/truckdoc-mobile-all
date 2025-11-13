package com.sanda.truckdoc.client.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import com.sanda.truckdoc.client.HiltEntryPoint;
import com.sanda.truckdoc.client.TruckDocApp;
import com.sanda.truckdoc.client.data.MessagesDatabaseService;
import com.sanda.truckdoc.client.data.MessagesDatabaseServiceJavaCompat;
import com.sanda.truckdoc.client.data.model.DbLocation;

import org.joda.time.DateTime;

import timber.log.Timber;

public class LocationReceiver extends BroadcastReceiver {
    public static final String ACTION_LOCATION_CHANGED = "com.sanda.truckdoc.client.ACTION_LOCATION_CHANGED";
    public static final long TURN_OFF_GPS_SPECIAL_VALUE = -1L;

    public static void requestLocationUpdates(Context context, Long interval) {
        // Implementation for requesting location updates
        // This is a placeholder - the actual implementation would depend on the location service
        Timber.d("Requesting location updates with interval: %d", interval);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_LOCATION_CHANGED.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                Location location = extras.getParcelable("location");
                if (location != null) {
                    try {
                        HiltEntryPoint entryPoint = TruckDocApp.getEntryPoint(context);
                        MessagesDatabaseService messagesDatabaseService = entryPoint.messagesDatabaseService();
                        
                        DbLocation record = new DbLocation(
                            0, // id will be auto-generated
                            location.getLatitude(),
                            location.getLongitude(),
                            new DateTime(location.getTime()),
                            location.getAccuracy(),
                            location.getSpeed(),
                            location.getBearing(),
                            location.getAltitude(),
                            location.getProvider()
                        );
                        
                        MessagesDatabaseServiceJavaCompat.insertLocationBlocking(messagesDatabaseService, record);
                    } catch (Exception e) {
                        Timber.e(e, "Error saving location");
                    }
                }
            }
        }
    }
}
