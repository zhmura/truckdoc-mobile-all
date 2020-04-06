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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.receivers.ServiceResultReceiver;
import com.sanda.truckdoc.client.service.MessageCheckService;
import com.sanda.truckdoc.client.service.NotificationHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import app.camera.tdoc.camera_library.PreferenceKeys;
import timber.log.Timber;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

@EActivity(R.layout.register)
public class RegisterActivity extends AppCompatActivity {

    @ViewById(R.id.registerToken)
    EditText registerToken;
    @ViewById(R.id.btnRegister)
    Button button;
    @ViewById(R.id.register_state)
    TextView textView;

    private ResponseReceiver receiver;

    public final static int SUCCESS_RETURN_CODE = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    protected void afterViews() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int isUserDeactivated = sharedPreferences.getInt(PreferenceKeys.getUserDeactivatedPreferenceKey(), 0);
        if (isUserDeactivated > 1) {
            textView.setText(R.string.you_was_deactivated);
        } else {
            textView.setText(R.string.you_need_to_register);
        }
        explicitPermissionsRequestIfRequired();
    }

    @Click(R.id.btnRegister)
    void onRegister() {
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
        //explicitPermissionsRequestIfRequired();
        IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_PROCESS_FINISHED);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ResponseReceiver();
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    private void triggerRegister() {
        button.setEnabled(false);
        final Intent intent = new Intent(MessageCheckService.ACTION_REGISTER, null, getApplicationContext(), MessageCheckService.class);
        if (registerToken.getText() == null
                || registerToken.getText().toString().trim().isEmpty()) {
            NotificationHelper.showNotificationMessage(getResources().getString(R.string.enter_registration_token), this);
            button.setEnabled(true);
        } else {
            intent.putExtra("registrationToken", registerToken.getText().toString());
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
            DashboardActivity_.intent(this).flags(FLAG_ACTIVITY_NEW_TASK).start();
        } else {
            button.setEnabled(true);
            NotificationHelper.showErrorMessage(getResources().getString(R.string.registration_failed) + errorMsg, this);
        }
    }

    public class ResponseReceiver extends ServiceResultReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ServiceResultReceiver.ACTION_PROCESS_FINISHED.equals(intent.getAction())) {
                button.setEnabled(true);

                Bundle bundle = intent.getBundleExtra(MessageCheckService.PARAM_OUT_DATA);

                boolean success = bundle == null || (bundle.getBoolean(MessageCheckService.REGISTRATION_SUCCESS, false));
                onRegistrationResult(success,
                        bundle != null ? bundle.getString(MessageCheckService.REGISTRATION_ERROR_MSG) : "");
            }
        }
    }
}

