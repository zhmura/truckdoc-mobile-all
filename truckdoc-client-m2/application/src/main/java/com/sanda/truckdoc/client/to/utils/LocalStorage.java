package com.sanda.truckdoc.client.to.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.sanda.truckdoc.client.util.StrictModeUtils;

/**
 * Created by k.natallie on 02.02.2016.
 */
public class LocalStorage {

    public static final String TRACK_NUMBER = "track_number";
    public static final String TENT_NUMBER = "tent_number";
    public static final String TENT_TYPE = "tent_type";
    public static final String TENT_TYPE_ID = "tent_type_id";
    public static final String CONFIG_INFO = "config_info";
    public static final String LATEST_RECEIVED_CONFIG_VERSION = "latest_config_version";
    public static final String TO_PROGRESS = "to_progress";
    public static final String TO_SEND_PROGRESS = "to_send_progress";
    public static final String LAST_MNT_REPORT = "last_mnt_report_sent_data";


    private SharedPreferences settings;
    private Context context;
    private static volatile LocalStorage instance;


    public static LocalStorage getInstance(Context context) {
        LocalStorage localInstance = instance;
        if (localInstance == null) {
            synchronized (LocalStorage.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new LocalStorage(context);
                }
            }
        }
        return localInstance;
    }

    private LocalStorage(Context context) {
        this.context = context;
        initSettings();
    }


    private void initSettings() {
        if (context != null) {
            settings = StrictModeUtils.allowDiskReads(() ->
                    context.getSharedPreferences("trackdocTO", Context.MODE_PRIVATE)
            );
        }
    }

    public void writeStringPreference(String name, String value) {
        if (settings == null) {
            initSettings();
        }
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(name, value);
        editor.apply();
    }

    public String readStringPreference(String name) {
        return readStringPreference(name, null);
    }

    public String readStringPreference(String name, String defValue) {
        if (settings == null) {
            initSettings();
        }
        return StrictModeUtils.allowDiskReads(() -> settings.getString(name, defValue));
    }

    public void writeIntPreference(String name, int value) {
        if (settings == null) {
            initSettings();
        }
        Editor editor = settings.edit();
        editor.putInt(name, value);
        editor.apply();
    }

    public void writeLongPreference(String name, long value) {
        if (settings == null) {
            initSettings();
        }
        Editor editor = settings.edit();
        editor.putLong(name, value);
        editor.apply();
    }


    public void writeBooleanPreference(String name, boolean value) {
        if (settings == null) {
            initSettings();
        }
        Editor editor = settings.edit();
        editor.putBoolean(name, value);
        editor.apply();
    }

    public int readIntPreference(String name) {
        return readIntPreference(name, 0);
    }


    public boolean readBooleanPreference(String name, boolean defaultValue) {
        if (settings == null) {
            initSettings();
        }
        return StrictModeUtils.allowDiskReads(() -> settings.getBoolean(name, defaultValue));
    }

    public int readIntPreference(String name, int defaultValue) {
        if (settings == null) {
            initSettings();
        }
        return StrictModeUtils.allowDiskReads(() -> settings.getInt(name, defaultValue));
    }

    public long readLongPreference(String name, long defaultValue) {
        if (settings == null) {
            initSettings();
        }
        return StrictModeUtils.allowDiskReads(() -> settings.getLong(name, defaultValue));
    }


    public SharedPreferences getSettings() {
        return settings;
    }

    public void setSettings(SharedPreferences settings) {
        this.settings = settings;
    }

    public void removePreference(String name) {
        if (settings != null) {
            Editor editor = settings.edit();
            editor.remove(name);
            editor.apply();
        }
    }
}
