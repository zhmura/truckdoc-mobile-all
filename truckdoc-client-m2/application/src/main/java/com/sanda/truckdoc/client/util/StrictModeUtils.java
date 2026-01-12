package com.sanda.truckdoc.client.util;

import android.os.StrictMode;

/**
 * Helper to silence StrictMode disk I/O warnings around unavoidable preference access during startup.
 *
 * <p>Use sparingly: the long-term fix is to move I/O off the main thread.</p>
 */
public final class StrictModeUtils {
    private StrictModeUtils() {}

    public interface Supplier<T> {
        T get();
    }

    public static <T> T allowDiskReads(Supplier<T> supplier) {
        // SharedPreferences can touch disk on first access (e.g. create dirs / chmod), so we permit both reads and writes.
        StrictMode.ThreadPolicy oldPolicy = StrictMode.getThreadPolicy();
        StrictMode.setThreadPolicy(
                new StrictMode.ThreadPolicy.Builder(oldPolicy)
                        .permitDiskReads()
                        .permitDiskWrites()
                        .build()
        );
        try {
            return supplier.get();
        } finally {
            StrictMode.setThreadPolicy(oldPolicy);
        }
    }
}

