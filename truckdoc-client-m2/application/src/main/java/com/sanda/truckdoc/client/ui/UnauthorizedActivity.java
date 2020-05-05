package com.sanda.truckdoc.client.ui;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;

import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.TruckDocApp;
import com.sanda.truckdoc.client.data.MessagesDatabaseService;
import com.sanda.truckdoc.network.AppSettings;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import app.camera.tdoc.camera_library.PreferenceKeys;

@EActivity(R.layout.unauthorized)
public class UnauthorizedActivity extends AppCompatActivity {

    @ViewById(R.id.btnLogout)
    Button logoutButton;
    @ViewById(R.id.btnContinue)
    Button continueButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Click(R.id.btnLogout)
    void onLogout() {
        triggerLogout();
    }

    private void triggerLogout() {
        turnSync(this, false);
        clearAppData(this);
        RegisterActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK).start();
        finishAffinity();
    }

    @Click(R.id.btnContinue)
    void onContinue() {
        turnSync(this, true);
        triggerContinue();
    }

    private void triggerContinue() {
        DashboardActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK).start();
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
        MessagesDatabaseService db = TruckDocApp.get(context).appComponent().db();
        db.deleteAllData();
    }

    @Override
    public void onBackPressed() {
        triggerContinue();
    }
}

