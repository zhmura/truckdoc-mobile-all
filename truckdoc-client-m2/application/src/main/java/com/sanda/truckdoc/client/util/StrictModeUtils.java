package com.sanda.truckdoc.client.util;

import android.os.StrictMode;

/**
 * Helper to silence StrictMode disk-read warnings around unavoidable preference reads during startup.
 *
 * <p>Use sparingly: the long-term fix is to move I/O off the main thread.</p>
 */
public final class StrictModeUtils {
    private StrictModeUtils() {}

    public interface Supplier<T> {
        T get();
    }

    public static <T> T allowDiskReads(Supplier<T> supplier) {
        StrictMode.ThreadPolicy oldPolicy = StrictMode.allowThreadDiskReads();
        try {
            return supplier.get();
        } finally {
            StrictMode.setThreadPolicy(oldPolicy);
        }
    }
}

