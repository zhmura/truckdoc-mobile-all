package com.sanda.truckdoc.client.updater.ui;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import com.sanda.truckdoc.client.updater.R;
import com.sanda.truckdoc.client.updater.receivers.CheckUpdateReceiver;

public class PrefsActivity extends AppCompatPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CheckUpdateReceiver.restartCheckUpdateReceiver(this, true);
        setupActionBar();
        addPreferencesFromResource(R.xml.pref_general);

        bindPreferenceSummaryToValue(findPreference("useWiFi"), (preference, newValue) -> {
            if (Boolean.valueOf(newValue.toString())) {
                preference.setSummary(R.string.download_wifi_only);
            } else {
                preference.setSummary(R.string.download_3g);
            }
            return true;
        });
    }

    private static void bindPreferenceSummaryToValue(Preference preference,
                                                     Preference.OnPreferenceChangeListener listener) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(listener);

        // Trigger the listener immediately with the preference's
        // current value.
        listener.onPreferenceChange(preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                        .getBoolean(preference.getKey(), false));
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
