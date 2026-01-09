package com.sanda.truckdoc.client.service.intent;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Replacement for deprecated {@code IntentService}.
 *
 * <p>Processes incoming intents sequentially on a single background thread.
 * Each intent is handled by {@link #handleIntent(Intent)}.
 */
public abstract class SingleThreadService extends Service {
    private ExecutorService executor;

    @Override
    public void onCreate() {
        super.onCreate();
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if (intent == null) {
            return getNullIntentStartMode();
        }
        executor.execute(() -> {
            try {
                handleIntent(intent);
            } finally {
                stopSelf(startId);
            }
        });
        return getStartMode();
    }

    /**
     * Called on a background thread. Implement your intent processing here.
     */
    protected abstract void handleIntent(Intent intent);

    /**
     * Return value for {@link #onStartCommand(Intent, int, int)} when intent is non-null.
     */
    protected int getStartMode() {
        return START_NOT_STICKY;
    }

    /**
     * Return value for {@link #onStartCommand(Intent, int, int)} when intent is null.
     */
    protected int getNullIntentStartMode() {
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (executor != null) {
            executor.shutdownNow();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

