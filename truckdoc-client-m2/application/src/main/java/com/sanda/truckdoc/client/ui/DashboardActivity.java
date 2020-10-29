package com.sanda.truckdoc.client.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.sanda.truckdoc.client.Prefs;
import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.TruckDocApp;
import com.sanda.truckdoc.client.data.DbRouteAssignmentWithPath;
import com.sanda.truckdoc.client.data.MessagesDatabaseService;
import com.sanda.truckdoc.client.data.DbPathWithPoints;
import com.sanda.truckdoc.client.data.model.DbContactRecord;
import com.sanda.truckdoc.client.data.model.route.DbRoutePoint;
import com.sanda.truckdoc.client.receivers.ServiceResultReceiver;
import com.sanda.truckdoc.client.service.MessageCheckService;
import com.sanda.truckdoc.client.to.EnterTruckDataActivity;
import com.sanda.truckdoc.client.to.data.Model;
import com.sanda.truckdoc.client.to.service.NewMntService;
import com.sanda.truckdoc.client.to.service.NewMntService_;
import com.sanda.truckdoc.client.to.utils.LocalStorage;
import com.sanda.truckdoc.client.to.utils.SetupMaintenanceUtils;
import com.sanda.truckdoc.client.ui.message.InboxActivity;
import com.sanda.truckdoc.client.util.NotificationHelper;
import com.sanda.truckdoc.network.api.UserKey;

import net.tribe7.common.collect.FluentIterable;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewsById;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import app.camera.tdoc.camera_library.CamActivity;
import app.camera.tdoc.camera_library.PrefixList;
import app.instructions.InstructionsActivity;
import rx.functions.Action1;
import timber.log.Timber;

import static app.camera.tdoc.camera_library.ImageType.SCENE_PHOTO;

/**
 * TruckDoc mobile client class
 *
 * @author Siarhei Zhmura
 */
@EActivity(R.layout.activity_home)
public class DashboardActivity extends AppCompatActivity {
    private static final int REGISTER_USER_REQEST_CODE = 1;
    @ViewsById({R.id.btnMaps, R.id.btnMessages, R.id.btnScan, R.id.btnLandscapePhoto, R.id.btnMaintain})

    List<View> buttons;

    private ResponseReceiver receiver = new ResponseReceiver();
    private ProgressDialog dialog;

    @Inject
    Provider<UserKey> userKey;
    @Inject
    MessagesDatabaseService databaseService;
    @Inject
    Prefs prefs;

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

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onDestroy() {
        super.onDestroy();
    }

    @AfterViews
    void afterViews() {
        TruckDocApp.get(this).appComponent().inject(this);
        explicitPermissionsRequestIfRequired();
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.icon);
        dialog = new ProgressDialog(this);
        triggerCheckMaintenance();
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    @Click(R.id.btnMaps)
    void onMapsBtn() {
        try {
            Intent intent = getPackageManager().getLaunchIntentForPackage("com.mapfactor.navigator");
            startActivity(intent);
            Thread.sleep(5000);
            new NavigatorTask().execute();
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка при старте навигатора", Toast.LENGTH_SHORT).show();
        }
    }

    @Click(R.id.btnMessages)
    void onMessagesBtn() {
        Intent intent = new Intent(this, InboxActivity.class);
        DashboardActivity.this.startActivity(intent);
    }

    @Click(R.id.btnScan)
    void onScanBtn() {
        populateRecipientDialog(recipientId -> {
            ScannerActivity_.intent(this).recipientId(recipientId).start();
        });
    }

    @Click(R.id.btnMaintain)
    void onMaintenanceBtn() {
        Intent intent = new Intent(getApplicationContext(), EnterTruckDataActivity.class);
        startActivity(intent);
    }

    @Click(R.id.btnLandscapePhoto)
    void onLandscapePhotoBtn() {
        populateRecipientDialog(recipientId -> {
            startCameraActivity(SCENE_PHOTO, recipientId);
        });
    }

    @Click(R.id.btnInstructions)
    void onInstructions() {
        startActivity(new Intent(this, InstructionsActivity.class));
    }

    public void startCameraActivity(app.camera.tdoc.camera_library.ImageType type, Long recipientId) {
        ArrayList<PrefixList> prefixes = new ArrayList<>();
        prefixes.add(new PrefixList("Обычное фото", "PHOTO"));
        Intent i = CamActivity.newBuilder()
                .setFolderName("TruckDoc")
                .setGalleryFolderName("Gallery")
                .setImagePrefixList(prefixes)
                .setPrefixEnable(true)
                .setRecipient(recipientId)
                .setImageType(type)
                .setDeleteButtonVisibility(true)
                .setSendButtonVisibility(true)
                .setSettingButtonVisibility(true)
                .setVideoButtonVisibility(false)
                .setExposureEnable(true)
                .setWhiteBalanceEnable(true)
                .setColorEffectsEnable(true)
                .setBordersOptionEnable(true)
                .setResolutionOptionEnable(true)
                .setIsoOptionEnable(true)
                .setFocusOptionEnable(true)
                .setFlashOptionEnable(true)
                .setAutoStabiliseOptionEnable(true)
                .setTimeStampeEnable(!type.isForDoc())
                .setLocationStampEnable(!type.isForDoc())
                .build(this);
        startActivity(i);
    }

    private void populateRecipientDialog(Action1<Long> action) {
        databaseService.getContactRecords().toList().subscribe(dbContactRecords -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1,
                    FluentIterable.from(dbContactRecords).transform(DbContactRecord::getLabel).toList()) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    int color = dbContactRecords.get(position).getColor();
                    view.setBackgroundColor(color & 0x55ffffff);
                    return view;
                }
            };
            new AlertDialog.Builder(this).setAdapter( //
                    adapter, (dialog1, which) -> action.call(dbContactRecords.get(which).getRecipientId())).show();
        });
    }

//    private void turnGPSOn() {
//        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
//
//        if (!provider.contains("gps")) { //if gps is disabled
//            final Intent poke = new Intent();
//            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
//            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
//            poke.setData(Uri.parse("3"));
//            sendBroadcast(poke);
//        }
//    }
//
//    private void turnGPSOff() {
//        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
//
//        if (provider.contains("gps")) { //if gps is enabled
//            final Intent poke = new Intent();
//            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
//            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
//            poke.setData(Uri.parse("3"));
//            sendBroadcast(poke);
//        }
//    }

    private void triggerCheckMaintenance() {
        //  final Intent intent = new Intent(MaintenaceService.ACTION_GET_MAINTENANCE, null, this, MaintenaceService.class);
        //  startService(intent);

        String toProgress = LocalStorage.getInstance(getApplicationContext()).readStringPreference(LocalStorage.TO_SEND_PROGRESS);
        if (toProgress != null) {
            NewMntService.Status status = NewMntService.Status.valueOf(toProgress);
            switch (status) {
                case SEND_ATTEMTP:
                case SEND_ERROR:
                case UPLOAD_FILES: {
                    SetupMaintenanceUtils.restoreMaintenance(getApplicationContext());
                    if (Model.getInstance(getApplicationContext()).isFullFilled()) {
                        NewMntService_.intent(getApplicationContext()).messageSend().start();
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (prefs.hasMaintenance()) {
            buttons.get(4).setVisibility(View.VISIBLE);
        } else {
            buttons.get(4).setVisibility(View.GONE);
        }
        IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_PROCESS_FINISHED);
        filter.addAction(ResponseReceiver.ACTION_PRINT_RESP);
        filter.addAction(ResponseReceiver.NOTIFICATION_MESSAGE);
        filter.addAction(ServiceResultReceiver.ACTION_SENT_MAINTENANCE_OK);
        filter.addAction(ServiceResultReceiver.ACTION_SENT_MAINTENANCE_ERROR);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    private void setButtonState(boolean enable) {
        for (View v : buttons) {
            v.setEnabled(enable);
        }
    }

    private void disableButtons() {
        setButtonState(false);
    }

    private void enableButtons() {
        setButtonState(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REGISTER_USER_REQEST_CODE) {
            if (resultCode == RegisterActivity.SUCCESS_RETURN_CODE) {
                enableButtons();
            }
        }
    }

    private void triggerSms() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setType("vnd.android-dir/mms-sms");
        DashboardActivity.this.startActivity(intent);
    }

    public class ResponseReceiver extends ServiceResultReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ServiceResultReceiver.ACTION_PROCESS_FINISHED.equals(intent.getAction())) {
                //String text = intent.getStringExtra(MessageCheckService.PARAM_OUT_MSG);
                showProgressBar(null, null, 0);
                //if (text != null) { showMessageToast(text); }
                enableButtons();
            }
            if (ServiceResultReceiver.NOTIFICATION_MESSAGE.equals(intent.getAction())) {
                showProgressBar(null, null, 0);
            }
            if (ServiceResultReceiver.ACTION_SENT_MAINTENANCE_ERROR.equals(intent.getAction())) {
                String text = intent.getStringExtra(MessageCheckService.PARAM_OUT_MSG);
                NotificationHelper.showErrorMessage(text, getApplicationContext());
            }
            if (ServiceResultReceiver.ACTION_SENT_MAINTENANCE_OK.equals(intent.getAction())) {
                String text = intent.getStringExtra(MessageCheckService.PARAM_OUT_MSG);
                NotificationHelper.showNotificationMessage(text, getApplicationContext());
            }
        }
    }

    private void showProgressBar(@Nullable String header, @Nullable String message, int status) {
        dialog.setTitle(header);
        dialog.setMessage(message);
        dialog.setIndeterminate(true);
        dialog.setProgress(50);
        dialog.setCancelable(true);
        if (status > 0) {
            dialog.show();
        } else {
            dialog.cancel();
        }
    }


    private class NavigatorTask extends AsyncTask<Void, Void, Void> {

        private Socket mSocket = null;
        private BufferedReader mIn = null;
        private PrintWriter mOut = null;
        private boolean connected = false;
        private Context context = null;


        public void connect() {
            try {
                mSocket = new Socket("", 4242);
                mIn = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                mOut = new PrintWriter(mSocket.getOutputStream(), true);
                connected = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DashboardActivity.this, "Соединение с навигатором установлено!", Toast.LENGTH_LONG).
                                show();
                    }
                });
            } catch (Exception e) {
                connected = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DashboardActivity.this, "Не могу подключиться к MapFactor! Ошибка: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

        public void runCommand(DbPathWithPoints routePath) {
            StringBuilder route = new StringBuilder("$destination=");
            int i = 1;
            for (DbRoutePoint point : routePath.getPoints()) {
                route.append(new DecimalFormat("#.######").format(point.getLatitude()).replace(',', '.'))
                        .append(",")
                        .append(new DecimalFormat("#.######").format(point.getLongitude()).replace(',', '.'))
                        .append(",")
                        .append("\\\"")
                        .append(point.getName() != null ? point.getName() : i)
                        .append("\\\";");
                i++;
            }
            route.append("instant;navigate;");

            try {
                mOut.println(route.toString());
                final String res = mIn.readLine();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DashboardActivity.this, "Ответ навигатора: " + res.toUpperCase(),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(DashboardActivity.this, "Ошибка: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

        public void disconnect() {
            if (mSocket == null)
                return;

            runOnUiThread(() -> {
                try {
                    mSocket.close();
                } catch (Exception e) {
                    Toast.makeText(DashboardActivity.this, "Ответ навигатора: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (prefs.currentRouteAssignment() != 0) {
                DbRouteAssignmentWithPath assignment = databaseService.findRouteAssignmentById(prefs.currentRouteAssignment());
                if (assignment != null) {
                    connect();
                    DbPathWithPoints route = assignment.getPathWithPoints();
                    runCommand(route);
                    disconnect();
                }
            } else {
                runOnUiThread(() -> Toast.makeText(DashboardActivity.this, "Вам не назначен ни один маршрут",
                        Toast.LENGTH_LONG).show());

            }

            return null;

        }
    }


}

