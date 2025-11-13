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
import com.sanda.truckdoc.client.databinding.DialogFragmentNoConnectionBinding;
import com.sanda.truckdoc.client.ui.floating.ApnHelpWindow;
import com.sanda.truckdoc.client.ui.floating.HelpWindow;
import com.sanda.truckdoc.client.ui.utils.TwoLineTextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import timber.log.Timber;

public class NoConnectionFragment extends DialogFragment {

    private DialogFragmentNoConnectionBinding binding;
    private NoConnectionReceiver.Result result;

    public static NoConnectionFragment newInstance(NoConnectionReceiver.Result result) {
        NoConnectionFragment fragment = new NoConnectionFragment();
        fragment.result = result;
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews();
        setupClickListeners();
    }

    private void setupViews() {
        if (!result.mobileDataEnabled) {
            binding.dataLayout.setVisibility(View.VISIBLE);
            if (result.wasMobileDataEnabled) {
                binding.mobileDataText.setText2Visibility(View.VISIBLE);
            }
        }
        if (!result.roamingDataEnabled) {
            binding.roamingDataLayout.setVisibility(View.VISIBLE);
            if (result.wasRoamingDataEnabled) {
                binding.roamingDataText.setText2Visibility(View.VISIBLE);
            }
        }
        if (result.airplaneModeEnabled) {
            binding.airplaneLayout.setVisibility(View.VISIBLE);
            if (!result.wasAirplaneModeEnabled) {
                binding.airplaneText.setText2Visibility(View.VISIBLE);
            }
        }
        if (result.networkOperatorChanged) {
            binding.operatorLayout.setVisibility(View.VISIBLE);
        }

        if (result.mobileDataEnabled && result.roamingDataEnabled && !result.airplaneModeEnabled) {
            binding.apnLayout.setVisibility(View.VISIBLE);
        }
    }

    private void setupClickListeners() {
        binding.close.setOnClickListener(v -> onCloseBtn());
        binding.onMobileData.setOnClickListener(v -> onMobileData());
        binding.onRoamingData.setOnClickListener(v -> onRoamingData());
        binding.onAirplane.setOnClickListener(v -> onAirplane());
        binding.onApn.setOnClickListener(v -> onApn());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() == null) {
            return;
        }
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void onCloseBtn() {
        getActivity().finish();
    }

    private void onMobileData() {
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

    private void onRoamingData() {
        HelpWindow.start(getActivity(), getString(R.string.help_enable_roaming_data));
        startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
    }

    private void onAirplane() {
        HelpWindow.start(getActivity(), getString(R.string.help_airplane_mode));
        startActivity(new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS));
    }

    private void onApn() {
        ApnHelpWindow.start(getActivity());
        startActivity(new Intent(Settings.ACTION_APN_SETTINGS));
    }
}
