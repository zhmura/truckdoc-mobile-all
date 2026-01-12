package com.sanda.truckdoc.client.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.sanda.truckdoc.client.Prefs;
import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.TruckDocApp;
import com.sanda.truckdoc.client.service.AppSettings;
import com.sanda.truckdoc.client.util.StrictModeUtils;
import com.sanda.truckdoc.network.api.UserKey;

import androidx.appcompat.app.AppCompatActivity;
import app.camera.tdoc.camera_library.PreferenceKeys;
import dagger.hilt.android.AndroidEntryPoint;

/**
 * Created by sergeyz on 4/14/2017.
 */

@AndroidEntryPoint
public class SplashActivity extends AppCompatActivity {

    private AppSettings settings;
    private UserKey userKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        if (!alreadyRegistered()) {
            startActivity(new Intent(this, RegisterActivity.class));
        } else if (isActive()) {
            startActivity(new Intent(this, DashboardActivity.class));
        } else {
            startActivity(new Intent(this, UnauthorizedActivity.class));
        }
        finish();
    }

    private boolean isActive() {
        return StrictModeUtils.allowDiskReads(() -> {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            return sharedPreferences.getInt(PreferenceKeys.getUserDeactivatedPreferenceKey(), 0) == 0;
        });
    }

    private boolean alreadyRegistered() {
        return StrictModeUtils.allowDiskReads(() -> {
            settings = new AppSettings(this);
            userKey = settings.getUserKey();
            return (userKey != null && !TextUtils.isEmpty(userKey.getLogin()));
        });
    }
}
