package com.sanda.truckdoc.client.ui;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.TruckDocApp;
import com.sanda.truckdoc.client.data.MessagesDatabaseService;
import com.sanda.truckdoc.client.data.MessagesDatabaseServiceJavaCompat;
import com.sanda.truckdoc.client.databinding.UnauthorizedBinding;
import com.sanda.truckdoc.client.service.AppSettings;
import com.sanda.truckdoc.client.HiltEntryPoint;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import app.camera.tdoc.camera_library.PreferenceKeys;

public class UnauthorizedActivity extends AppCompatActivity {

    private UnauthorizedBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = UnauthorizedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.btnLogout.setOnClickListener(v -> onLogout());
        binding.btnContinue.setOnClickListener(v -> onContinue());
    }

    private void onLogout() {
        triggerLogout();
    }

    private void triggerLogout() {
        turnSync(this, false);
        clearAppData(this);
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finishAffinity();
    }

    private void onContinue() {
        turnSync(this, true);
        triggerContinue();
    }

    private void triggerContinue() {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finishAffinity();
    }

    private static void turnSync(Context context, boolean isOn) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putInt(PreferenceKeys.getUserDeactivatedPreferenceKey(), 0).apply();
        sharedPreferences.edit().putBoolean(PreferenceKeys.getSyncEnabledPreferenceKey(), isOn).apply();
    }

    private static void clearAppData(ContextWrapper context) {
        AppSettings settings = new AppSettings(context);
        settings.clearUserKey();
        HiltEntryPoint entryPoint = TruckDocApp.getEntryPoint(context);
        MessagesDatabaseService db = entryPoint.messagesDatabaseService();
        MessagesDatabaseServiceJavaCompat.deleteAllDataBlocking(db);
    }

    @Override
    public void onBackPressed() {
        triggerContinue();
    }
}

