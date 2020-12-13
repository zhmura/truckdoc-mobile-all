package com.sanda.truckdoc.client.updater.ui;

import android.Manifest;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import com.sanda.truckdoc.client.updater.Prefs;
import com.sanda.truckdoc.client.updater.R;
import com.sanda.truckdoc.client.updater.UpdaterApp;
import com.sanda.truckdoc.client.updater.receivers.CheckUpdateReceiver;
import com.sanda.truckdoc.client.updater.work.CheckUpdatesWorker;
import com.tbruyelle.rxpermissions.RxPermissions;

import javax.inject.Inject;

import androidx.work.WorkManager;

public class PrefsActivity extends AppCompatPreferenceActivity {

    @Inject Prefs prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UpdaterApp.get(this).appComponent().inject(this);
        CheckUpdateReceiver.restartCheckUpdateReceiver(this, true);
        new RxPermissions(this).request(Manifest.permission.READ_EXTERNAL_STORAGE).subscribe(aBoolean -> {
            if (aBoolean) {
                CheckUpdatesWorker.start(this);
            }
        });
        setupActionBar();
        addPreferencesFromResource(R.xml.pref_general);
        prefs.registerOnChangeListener((sharedPreferences, key) -> {

        });
        bindPreferenceSummaryToValue(findPreference("useWiFi"), (preference, newValue) -> {
            Boolean newVal = Boolean.valueOf(newValue.toString());
            if (newVal) {
                preference.setSummary(R.string.download_wifi_only);
            } else {
                preference.setSummary(R.string.download_3g);
            }
            boolean checked = ((SwitchCompatPreference) preference).isChecked();
            if (newVal != checked) WorkManager.getInstance(this).cancelAllWork();
            CheckUpdatesWorker.start(this);
            return true;
        });
    }

    private static void bindPreferenceSummaryToValue(Preference preference, Preference.OnPreferenceChangeListener listener) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(listener);

        // Trigger the listener immediately with the preference's
        // current value.
        listener.onPreferenceChange(preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getBoolean(preference.getKey(), false));
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
