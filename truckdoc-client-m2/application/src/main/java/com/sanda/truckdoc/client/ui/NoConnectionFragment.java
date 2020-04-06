package com.sanda.truckdoc.client.ui;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.sanda.checker.NoConnectionReceiver;
import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.ui.floating.ApnHelpWindow;
import com.sanda.truckdoc.client.ui.floating.HelpWindow;
import com.sanda.truckdoc.client.ui.utils.TwoLineTextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import timber.log.Timber;

@EFragment(R.layout.dialog_fragment_no_connection)
public class NoConnectionFragment extends DialogFragment {

    @FragmentArg
    NoConnectionReceiver.Result result;

    @ViewById
    View dataLayout, roamingDataLayout, airplaneLayout, operatorLayout, apnLayout;
    @ViewById
    TwoLineTextView mobileDataText, roamingDataText, airplaneText;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @AfterViews
    void afterViews() {
        if (!result.mobileDataEnabled) {
            dataLayout.setVisibility(View.VISIBLE);
            if (result.wasMobileDataEnabled) {
                mobileDataText.setText2Visibility(View.VISIBLE);
            }
        }
        if (!result.roamingDataEnabled) {
            roamingDataLayout.setVisibility(View.VISIBLE);
            if (result.wasRoamingDataEnabled) {
                roamingDataText.setText2Visibility(View.VISIBLE);
            }
        }
        if (result.airplaneModeEnabled) {
            airplaneLayout.setVisibility(View.VISIBLE);
            if (!result.wasAirplaneModeEnabled) {
                airplaneText.setText2Visibility(View.VISIBLE);
            }
        }
        if (result.networkOperatorChanged) {
            operatorLayout.setVisibility(View.VISIBLE);
        }

        if (result.mobileDataEnabled && result.roamingDataEnabled && !result.airplaneModeEnabled) {
            apnLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() == null) {
            return;
        }
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Click(R.id.close)
    void onCloseBtn() {
        getActivity().finish();
    }

    @Click
    void onMobileData() {
        HelpWindow.start(getActivity(), getString(R.string.help_enable_mobile_data));

        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.android.settings",
                "com.android.settings.Settings$DataUsageSummaryActivity"));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Timber.e(e, "No connection handler - activity not found");
            startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
        }
    }

    @Click
    void onRoamingData() {
        HelpWindow.start(getActivity(), getString(R.string.help_enable_roaming_data));
        startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
    }

    @Click
    void onAirplane() {
        HelpWindow.start(getActivity(), getString(R.string.help_airplane_mode));
        startActivity(new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS));
    }

    @Click
    void onApn() {
        ApnHelpWindow.start(getActivity());
        startActivity(new Intent(Settings.ACTION_APN_SETTINGS));
    }
}
