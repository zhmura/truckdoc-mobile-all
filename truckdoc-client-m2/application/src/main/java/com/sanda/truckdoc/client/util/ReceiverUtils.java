package com.sanda.truckdoc.client.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

/**
 * Utility methods for registering broadcast receivers with proper flags for Android 13+
 */
public class ReceiverUtils {
    
    /**
     * Register a broadcast receiver with proper export flag for Android 13+
     * Use this for app-internal receivers that should NOT be exported
     * 
     * @param context Context to register receiver with
     * @param receiver The BroadcastReceiver to register
     * @param filter IntentFilter for the receiver
     * @return The registered Intent (same as Context.registerReceiver)
     */
    public static Intent registerReceiverNotExported(Context context, BroadcastReceiver receiver, IntentFilter filter) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            return context.registerReceiver(receiver, filter);
        }
    }
    
    /**
     * Register a broadcast receiver with proper export flag for Android 13+
     * Use this for receivers that need to receive broadcasts from other apps
     * 
     * @param context Context to register receiver with
     * @param receiver The BroadcastReceiver to register
     * @param filter IntentFilter for the receiver
     * @return The registered Intent (same as Context.registerReceiver)
     */
    public static Intent registerReceiverExported(Context context, BroadcastReceiver receiver, IntentFilter filter) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            return context.registerReceiver(receiver, filter);
        }
    }
}

