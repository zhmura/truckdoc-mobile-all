package com.sanda.truckdoc.client.util;

import android.os.Build;
import android.util.Log;

public class EmulatorDetector {

    private static final String TAG = "EmulatorDetector";

    private static int rating = -1;

    /**
     * Detects if app is currenly running on emulator, or real device.
     *
     * @return true for emulator, false for real devices
     */
    public static boolean isEmulator() {

        if (rating < 0) { // rating is not calculated yet
            int newRating = 0;

            if (Build.PRODUCT.equals("sdk") ||
                    Build.PRODUCT.equals("google_sdk") ||
                    Build.PRODUCT.equals("sdk_x86") ||
                    Build.PRODUCT.equals("vbox86p")) {
                newRating++;
            }

            if (Build.MANUFACTURER.equals("unknown") ||
                    Build.MANUFACTURER.toLowerCase().equals("genymotion")) {
                newRating++;
            }

            if (Build.BRAND.equals("generic") ||
                    Build.BRAND.equals("generic_x86")) {
                newRating++;
            }

            if (Build.DEVICE.startsWith("generic") ||
                    Build.DEVICE.startsWith("generic_x86") ||
                    Build.DEVICE.startsWith("vbox86p")) {
                newRating++;
            }

            if (Build.MODEL.equals("sdk") ||
                    Build.MODEL.startsWith("google_sdk") ||
                    Build.MODEL.startsWith("Android SDK built for x86")) {
                newRating++;
            }

            if (Build.HARDWARE.equals("goldfish") ||
                    Build.HARDWARE.equals("vbox86")) {
                newRating++;
            }

            if (Build.FINGERPRINT.contains("generic/sdk/generic") ||
                    Build.FINGERPRINT.contains("generic_x86") ||
                    Build.FINGERPRINT.contains("generic/google_sdk/generic") ||
                    Build.FINGERPRINT.contains("generic/vbox86p/vbox86p")) {
                newRating++;
            }

            rating = newRating;
        }

        return rating > 2;
    }

    /**
     * Returns string with human-readable listing of Build.* parameters used in {@link #isEmulator()} method.
     *
     * @return all involved Build.* parameters and its values
     */
    public static String getDeviceListing() {
        return "Build.PRODUCT: " + Build.PRODUCT + "\n" +
                "Build.MANUFACTURER: " + Build.MANUFACTURER + "\n" +
                "Build.BRAND: " + Build.BRAND + "\n" +
                "Build.DEVICE: " + Build.DEVICE + "\n" +
                "Build.MODEL: " + Build.MODEL + "\n" +
                "Build.HARDWARE: " + Build.HARDWARE + "\n" +
                "Build.FINGERPRINT: " + Build.FINGERPRINT;
    }

    /**
     * Prints all Build.* parameters used in {@link #isEmulator()} method to logcat.
     */
    public static void logcat() {
        Log.d(TAG, getDeviceListing());
    }

}
