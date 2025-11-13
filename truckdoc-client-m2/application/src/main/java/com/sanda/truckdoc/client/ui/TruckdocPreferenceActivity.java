package com.sanda.truckdoc.client.ui;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;

import com.sanda.truckdoc.client.Prefs;
import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.TruckDocApp;
import com.sanda.truckdoc.client.data.MessagesDatabaseService;
import com.sanda.truckdoc.client.data.MessagesDatabaseServiceJavaCompat;
import com.sanda.truckdoc.client.data.model.DbContactRecord;
import com.sanda.truckdoc.client.receivers.GetNewMessagesAlarmManager;
import com.sanda.truckdoc.client.ui.utils.AppCompatPreferenceActivity;
import com.sanda.truckdoc.client.HiltEntryPoint;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class TruckdocPreferenceActivity extends AppCompatPreferenceActivity {

    private ListPreference syncInterval;
    private Preference techSupportPhone;
    private Prefs prefs;
    private MessagesDatabaseService db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
        getPreferenceManager().setSharedPreferencesName(Prefs.FILENAME);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Use Hilt entry point pattern for AppCompatPreferenceActivity
        HiltEntryPoint entryPoint = TruckDocApp.getEntryPoint(this);
        prefs = entryPoint.prefs();
        db = entryPoint.messagesDatabaseService();

        // Initialize preferences
        syncInterval = (ListPreference) findPreference("syncInterval");
        techSupportPhone = findPreference("techSupportPhone");

        // Set up initial values
        techSupportPhone.setSummary(prefs.techSupportPhone());
        if (syncInterval.getValue() == null) {
            syncInterval.setValueIndex(syncInterval.findIndexOfValue(prefs.syncInterval()));
        }
        syncInterval.setSummary(prefs.syncInterval() + getResources().getString(R.string.syncInterval_mins));

        // Set up preference change listeners
        syncInterval.setOnPreferenceChangeListener((preference, newValue) -> {
            String syncIntervalTime = (String) newValue;
            prefs.syncInterval(syncIntervalTime);
            syncInterval.setValueIndex(syncInterval.findIndexOfValue(prefs.syncInterval()));
            syncInterval.setSummary(prefs.syncInterval() + getResources().getString(R.string.syncInterval_mins));
            GetNewMessagesAlarmManager.cancelGetMessagesAlarm(getApplicationContext());
            GetNewMessagesAlarmManager.setGetMessagesAlarm(getApplicationContext(), false);
            return true;
        });

        Preference timerStatus = findPreference(getString(R.string.timerStatus));
        timerStatus.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean isEnabled = (Boolean) newValue;
            if (isEnabled) {
                GetNewMessagesAlarmManager.setGetMessagesAlarm(getApplicationContext(), false);
            } else {
                GetNewMessagesAlarmManager.cancelGetMessagesAlarm(getApplicationContext());
            }
            return true;
        });

        // Load contact records using blocking method
        new Thread(() -> {
            try {
                List<DbContactRecord> contacts = MessagesDatabaseServiceJavaCompat.getContactRecordsBlocking(db);
                List<Preference> preferences = contacts.stream()
                        .map(this::createPhonePreference)
                        .collect(java.util.stream.Collectors.toList());
                runOnUiThread(() -> createPhonesCategory(preferences));
            } catch (Exception e) {
                // Handle error
            }
        }).start();
    }

    private Preference createPhonePreference(DbContactRecord record) {
        EditTextPreference p = new EditTextPreference(this);
        p.getEditText().setInputType(EditorInfo.TYPE_CLASS_PHONE);
        p.setTitle(record.getLabel());
        p.setSummary(record.getPhone());
        p.setOnPreferenceChangeListener((preference, newValue) -> {
            record.setPhone((String) newValue);
            MessagesDatabaseServiceJavaCompat.updateContactRecordBlocking(db, record);
            preference.setSummary(String.valueOf(newValue));
            return true;
        });
        return p;
    }

    private void createPhonesCategory(List<Preference> preferences) {
        PreferenceCategory category = new PreferenceCategory(this);
        category.setTitle(R.string.prefs_category_phones);
        getPreferenceScreen().addPreference(category);
        for (Preference preference : preferences) {
            category.addPreference(preference);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
