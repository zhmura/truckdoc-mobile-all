package app.camera.tdoc.camera_library;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.core.content.ContextCompat;

public class LocationSupplier {

    private Context context = null;
    private LocationManager locationManager = null;
    private MyLocationListener[] locationListeners = null;

    LocationSupplier(Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public Location getLocation() {
        if (locationListeners == null)
            return null;
        for (int i = 0; i < locationListeners.length; i++) {
            Location location = locationListeners[i].getLocation();
            if (location != null)
                return location;
        }
        return null;
    }

    private static class MyLocationListener implements LocationListener {
        private Location location = null;
        public boolean test_has_received_location = false;

        Location getLocation() {
            return location;
        }

        public void onLocationChanged(Location location) {
            if (location != null) {
                this.test_has_received_location = true;
                if (location.getLatitude() != 0.0d || location.getLongitude() != 0.0d) {
                    this.location = location;
                }
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.OUT_OF_SERVICE:
                case LocationProvider.TEMPORARILY_UNAVAILABLE: {
                    this.location = null;
                    this.test_has_received_location = false;
                    break;
                }
                default:
                    break;
            }
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
            this.location = null;
            this.test_has_received_location = false;
        }
    }


    boolean setupLocationListener() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean store_location = sharedPreferences.getBoolean(PreferenceKeys.getLocationStampEnable(), true);
        if (store_location && locationListeners == null) {
            boolean has_coarse_location_permission = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            boolean has_fine_location_permission = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            if (!has_coarse_location_permission && !has_fine_location_permission) {
                return false;
            }
            locationListeners = new MyLocationListener[2];
            locationListeners[0] = new MyLocationListener();
            locationListeners[1] = new MyLocationListener();

            if (locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER)) {
                if (has_coarse_location_permission) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListeners[1]);
                }
            }
            if (locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER)) {
                if (has_fine_location_permission) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListeners[0]);
                }
            }
        } else if (!store_location) {
            freeLocationListeners();
        }
        return true;
    }

    void freeLocationListeners() {
        if (locationListeners != null) {
            for (int i = 0; i < locationListeners.length; i++) {
                locationManager.removeUpdates(locationListeners[i]);
                locationListeners[i] = null;
            }
            locationListeners = null;
        }
    }


    public static String locationToDMS(double coord) {
        String sign = (coord < 0.0) ? "-" : "";
        boolean is_zero = true;
        coord = Math.abs(coord);
        int intPart = (int) coord;
        is_zero = is_zero && (intPart == 0);
        String degrees = String.valueOf(intPart);
        double mod = coord - intPart;

        coord = mod * 60;
        intPart = (int) coord;
        is_zero = is_zero && (intPart == 0);
        mod = coord - intPart;
        String minutes = String.valueOf(intPart);

        coord = mod * 60;
        intPart = (int) coord;
        is_zero = is_zero && (intPart == 0);
        String seconds = String.valueOf(intPart);

        if (is_zero) {
            // so we don't show -ve for coord that is -ve but smaller than 1"
            sign = "";
        }

        // use unicode rather than degrees symbol, due to Android Studio warning - see https://sourceforge.net/p/opencamera/tickets/107/
        return sign + degrees + "\u00b0" + minutes + "'" + seconds + "\"";
    }
}
