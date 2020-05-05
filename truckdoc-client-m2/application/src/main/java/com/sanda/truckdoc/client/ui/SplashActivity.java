package com.sanda.truckdoc.client.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.sanda.truckdoc.client.Prefs;
import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.TruckDocApp;
import com.sanda.truckdoc.network.AppSettings;
import com.sanda.truckdoc.network.api.UserKey;

import org.androidannotations.annotations.EActivity;

import javax.inject.Inject;

import androidx.appcompat.app.AppCompatActivity;
import app.camera.tdoc.camera_library.PreferenceKeys;

/**
 * Created by sergeyz on 4/14/2017.
 */

@EActivity(R.layout.activity_splash)
public class SplashActivity extends AppCompatActivity {

    private AppSettings settings;
    UserKey userKey;
    @Inject
    Prefs prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TruckDocApp.get(this).appComponent().inject(this);
        if (!alreadyRegistered()) {
            RegisterActivity_.intent(this).start();
        } else if (isActive()) {
            DashboardActivity_.intent(this).start();
        } else {
            UnauthorizedActivity_.intent(this).start();
        }
    }

    private boolean isActive() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getInt(PreferenceKeys.getUserDeactivatedPreferenceKey(), 0) == 0;
    }

    private boolean alreadyRegistered() {
        TruckDocApp.get(this).appComponent().inject(this);
        settings = new AppSettings(this);
        userKey = settings.getUserKey();
        return (userKey != null && !TextUtils.isEmpty(userKey.getLogin()));
    }
}
