package com.sanda.truckdoc.client.service;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.os.StrictMode;

import com.sanda.truckdoc.network.api.UserKey;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexei Osipov
 */
public class AppSettings {

    private static final String PREFS_NAME = "TruckDocPrefs";

    private static final String PREF_USER_NAME = "userName";
    private static final String PREF_USER_LOGIN = "userLogin";
    private static final String PREF_USER_KEY = "userKey";

    private ContextWrapper context;

    public AppSettings(@NotNull ContextWrapper context) {
        this.context = context;
    }

    private SharedPreferences getPrefs() {
        StrictMode.ThreadPolicy oldPolicy = StrictMode.allowThreadDiskReads();
        try {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        } finally {
            StrictMode.setThreadPolicy(oldPolicy);
        }
    }

    @Nullable
    public UserKey getUserKey() {
        SharedPreferences settings = getPrefs();
        String name = settings.getString(PREF_USER_NAME, null);
        String login = settings.getString(PREF_USER_LOGIN, null);
        String key = settings.getString(PREF_USER_KEY, null);
        return login != null ? new UserKey(name, login, key) : null;
    }

    public void saveUserKey(UserKey userKey) {
        SharedPreferences.Editor editor = getPrefs().edit();
        editor.putString(PREF_USER_NAME, userKey.getName());
        editor.putString(PREF_USER_LOGIN, userKey.getLogin());
        editor.putString(PREF_USER_KEY, userKey.getSecret());
        editor.commit();
    }

    public void clearUserKey() {
        SharedPreferences.Editor editor = getPrefs().edit();
        editor.putString(PREF_USER_NAME, null);
        editor.putString(PREF_USER_LOGIN, null);
        editor.putString(PREF_USER_KEY, null);
        editor.commit();
    }
}
