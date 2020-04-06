package com.sanda.truckdoc.client.ui;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.view.inputmethod.EditorInfo;

import com.sanda.truckdoc.client.Prefs;
import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.TruckDocApp;
import com.sanda.truckdoc.client.data.MessagesDatabaseService;
import com.sanda.truckdoc.client.data.model.DbContactRecord;
import com.sanda.truckdoc.client.receivers.GetNewMessagesAlarmManager;
import com.sanda.truckdoc.client.ui.utils.AppCompatPreferenceActivity;

import org.androidannotations.annotations.AfterPreferences;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.PreferenceByKey;
import org.androidannotations.annotations.PreferenceChange;
import org.androidannotations.annotations.PreferenceScreen;

import java.util.List;

import javax.inject.Inject;

@PreferenceScreen(R.xml.prefs)
@EActivity
public class TruckdocPreferenceActivity extends AppCompatPreferenceActivity {

    @PreferenceByKey
    ListPreference syncInterval;
    @PreferenceByKey
    Preference techSupportPhone;

    @Inject
    Prefs prefs;
    @Inject
    MessagesDatabaseService db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(Prefs.FILENAME);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @AfterPreferences
    void afterPrefs() {
        TruckDocApp.get(this).appComponent().inject(this);
        techSupportPhone.setSummary(prefs.techSupportPhone());
        if (syncInterval.getValue() == null) {
            syncInterval.setValueIndex(syncInterval.findIndexOfValue(prefs.syncInterval()));
        }
        syncInterval.setSummary(prefs.syncInterval()
                + getResources().getString(R.string.syncInterval_mins));
        db.getContactRecords().map(this::createPhonePreference).toList()
                .subscribe(this::createPhonesCategory);
    }

    private Preference createPhonePreference(DbContactRecord record) {
        EditTextPreference p = new EditTextPreference(this);
        p.getEditText().setInputType(EditorInfo.TYPE_CLASS_PHONE);
        p.setTitle(record.getLabel());
        p.setSummary(record.getPhone());
        p.setOnPreferenceChangeListener((preference, newValue) -> {
            record.setPhone((String) newValue);
            db.updateContactRecord(record).subscribe();
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

    @PreferenceChange(R.string.timerStatus)
    void onChange(boolean b) {
        if (b) {
            GetNewMessagesAlarmManager.setGetMessagesAlarm(getApplicationContext(), false);
        } else {
            GetNewMessagesAlarmManager.cancelGetMessagesAlarm(getApplicationContext());
        }
    }

    @PreferenceChange
    void syncInterval(String syncIntervalTime) {
        prefs.syncInterval(syncIntervalTime);
        syncInterval.setValueIndex(syncInterval.findIndexOfValue(prefs.syncInterval()));
        syncInterval.setSummary(prefs.syncInterval() + getResources().getString(R.string.syncInterval_mins));
        GetNewMessagesAlarmManager.cancelGetMessagesAlarm(getApplicationContext());
        GetNewMessagesAlarmManager.setGetMessagesAlarm(getApplicationContext(), false);
    }

    @OptionsItem(android.R.id.home)
    void onHome() {
        finish();
    }

/*    @Receiver(actions = ServiceResultReceiver.NOTIFICATION_MESSAGE,
            registerAt = OnResumeOnPause)
    void onNotificationMessage(Intent intent) {
        String message = intent.getStringExtra(NotificationHelper.PARAM_MSG);
        Boolean isError = intent.getBooleanExtra(NotificationHelper.PARAM_IS_ERROR, false);
        if (isError) {
            NotificationHelper.showErrorMessage(message, this);
        } else {
            NotificationHelper.showNotificationMessage(message, this);
        }
    }*/
}
