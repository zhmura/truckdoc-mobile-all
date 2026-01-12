package com.sanda.truckdoc.client.ui;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.databinding.RegisterBinding;
import com.sanda.truckdoc.client.receivers.ServiceResultReceiver;
import com.sanda.truckdoc.client.service.MessageCheckService;
import com.sanda.truckdoc.client.service.NotificationHelper;
import com.sanda.truckdoc.client.util.StrictModeUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import app.camera.tdoc.camera_library.PreferenceKeys;
import timber.log.Timber;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class RegisterActivity extends AppCompatActivity {

    private RegisterBinding binding;
    private ResponseReceiver receiver;

    public final static int SUCCESS_RETURN_CODE = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = RegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupViews();
        setupClickListeners();
    }

    private void setupViews() {
        int isUserDeactivated = StrictModeUtils.allowDiskReads(() -> {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            return sharedPreferences.getInt(PreferenceKeys.getUserDeactivatedPreferenceKey(), 0);
        });
        if (isUserDeactivated > 1) {
            binding.registerState.setText(R.string.you_was_deactivated);
        } else {
            binding.registerState.setText(R.string.you_need_to_register);
        }
        explicitPermissionsRequestIfRequired();
    }

    private void setupClickListeners() {
        binding.btnRegister.setOnClickListener(v -> onRegister());
    }

    private void onRegister() {
        triggerRegister();
    }

    private void explicitPermissionsRequestIfRequired() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int PERMISSION_ALL = 1;
            PackageInfo info;
            try {
                info = getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
                if (!hasPermissions(this, info.requestedPermissions)) {
                    ActivityCompat.requestPermissions(this, info.requestedPermissions, PERMISSION_ALL);
                }
            } catch (Exception e) {
                Timber.e(e, "Request permission handler failed");
            }
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_PROCESS_FINISHED);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ResponseReceiver();
        // Use utility for Android 13+ compatibility
        com.sanda.truckdoc.client.util.ReceiverUtils.registerReceiverNotExported(this, receiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    private void triggerRegister() {
        binding.btnRegister.setEnabled(false);
        final Intent intent = new Intent(MessageCheckService.ACTION_REGISTER, null, getApplicationContext(), MessageCheckService.class);
        if (binding.registerToken.getText() == null
                || binding.registerToken.getText().toString().trim().isEmpty()) {
            NotificationHelper.showNotificationMessage(getResources().getString(R.string.enter_registration_token), this);
            binding.btnRegister.setEnabled(true);
        } else {
            intent.putExtra("registrationToken", binding.registerToken.getText().toString());
            startService(intent);
        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    private void onRegistrationResult(boolean success, String errorMsg) {
        if (success) {
            this.setResult(SUCCESS_RETURN_CODE);
            this.finish();
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            binding.btnRegister.setEnabled(true);
            NotificationHelper.showErrorMessage(getResources().getString(R.string.registration_failed) + errorMsg, this);
        }
    }

    public class ResponseReceiver extends ServiceResultReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ServiceResultReceiver.ACTION_PROCESS_FINISHED.equals(intent.getAction())) {
                binding.btnRegister.setEnabled(true);

                Bundle bundle = intent.getBundleExtra(MessageCheckService.PARAM_OUT_DATA);

                boolean success = bundle == null || (bundle.getBoolean(MessageCheckService.REGISTRATION_SUCCESS, false));
                onRegistrationResult(success,
                        bundle != null ? bundle.getString(MessageCheckService.REGISTRATION_ERROR_MSG) : "");
            }
        }
    }
}

