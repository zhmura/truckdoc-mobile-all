package com.sanda.truckdoc.client.updater.service;

import android.app.DownloadManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.widget.Toast;

import com.sanda.truckdoc.client.updater.Prefs;
import com.sanda.truckdoc.client.updater.R;
import com.sanda.truckdoc.client.updater.UpdaterApp;
import com.sanda.truckdoc.client.updater.network.Backend;
import com.sanda.truckdoc.client.updater.network.UpdateResponse;
import com.sanda.truckdoc.client.updater.receivers.CheckInstallReceiver;
import com.sanda.truckdoc.client.updater.receivers.CheckUpdateReceiver;
import com.sanda.truckdoc.client.updater.receivers.ConnectionChangeReceiver;
import com.sanda.truckdoc.client.updater.utils.DeviceHelper;
import com.sanda.truckdoc.client.updater.utils.DeviceIds;
import com.sanda.truckdoc.client.updater.utils.L;
import com.sanda.truckdoc.client.updater.utils.NotificationHelper;
import com.sanda.truckdoc.client.updater.utils.PropertiesHelper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import rx.Observable;
import timber.log.Timber;

import com.sanda.truckdoc.client.updater.utils.DeviceHelper;

import static com.sanda.truckdoc.client.updater.utils.DeviceHelper.findUpdaterVersionCode;
import static com.sanda.truckdoc.client.updater.utils.DeviceHelper.findVersionCode;
import static com.sanda.truckdoc.client.updater.utils.DeviceHelper.findVersionName;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class UpdaterIntentService extends IntentService {

    private static final String ACTION_CHECK_UPDATE = "com.sanda.truckdoc.client.updater.service.action.CHECK_UPDATE";
    public static final String ACTION_CHECK_INSTALL = "com.sanda.truckdoc.client.updater.service.action.CHECK_INSTALL";
    public static final int COLUMN_LOCATION = 8;
    private static final String NOTIFICATION_CHANNEL_ID = "updater.channel";

    public static void startUpdateCheck(Context context) {
        Intent intent = new Intent(context, UpdaterIntentService.class);
        intent.setAction(ACTION_CHECK_UPDATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void startInstallCheck(Context context) {
        Intent intent = new Intent(context, UpdaterIntentService.class);
        intent.setAction(ACTION_CHECK_INSTALL);
        ContextCompat.startForegroundService(
                context,
                intent
        );
    }

    @Inject
    Backend backend;
    @Inject
    Prefs prefs;
    @Inject
    NotificationHelper notificationHelper;

    public UpdaterIntentService() {
        super("UpdaterIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        UpdaterApp.get(this).appComponent().inject(this);
        if (StringUtils.isEmpty(prefs.targetPackage())) {
            prefs.targetPackage(new PropertiesHelper().getTargetPackageForUpdate());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startInForeground();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startInForeground() {
        Intent notificationIntent = new Intent(this, UpdaterIntentService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("truckdoc")
                .setContentText("messaging")
                .setTicker("truckdoc")
                .setContentIntent(pendingIntent);
        Notification notification = builder.build();
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "messagecheck.channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Truckdoc message channel");
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
        try {
            startForeground((int) (System.currentTimeMillis() % 10000), notification);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            try {
                final String action = intent.getAction();
                if (ACTION_CHECK_UPDATE.equals(action)) {
                    handleCheckUpdate();
                } else if (ACTION_CHECK_INSTALL.equals(action)) {
                    handleCheckInstall();
                }
            } catch (Exception e) {
                Timber.e(e, "Updater intent handle error with intent action " + intent.getAction());
            }
        }
    }

    public void turnConnectionChangeBroadcastReceiver(boolean on) {
        ComponentName receiver = new ComponentName(this, ConnectionChangeReceiver.class);
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                on ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    private void handleCheckUpdate() {
        L.v("Starting update check...");
        if (!isOnline()) {
            Timber.i("Allowed network unavailable");
            CheckUpdateReceiver.restartCheckUpdateReceiver(this, true);
            return;
        }

        prefs.lastCheck(System.currentTimeMillis());

        if (StringUtils.isEmpty(prefs.targetPackage())) {
            Toast.makeText(this, "Target package not set", Toast.LENGTH_SHORT).show();
            Timber.e("Target package not set for updater. Skipping update request...");
        } else {
            DeviceIds ids = DeviceHelper.getDeviceId(this);
            backend.checkUpdates(prefs.targetPackage(),
                    findVersionName(prefs.targetPackage(), this),
                    android.os.Build.VERSION.SDK_INT,
                    findVersionCode(prefs.targetPackage(), this),
                    findUpdaterVersionCode(this),
                    ids.deviceId,
                    ids.androidId,
                    ids.deviceUuid)
                    .filter(updateResponse -> {
                        Toast.makeText(this, "Доступно ли обновление? Ответ: " + updateResponse.updateAvailable, Toast.LENGTH_SHORT).show();
                        prefs.lastSuccessfulCheck(System.currentTimeMillis());
                        boolean updateAvailable = updateResponse.updateAvailable;
                        turnConnectionChangeBroadcastReceiver(false);
                        Timber.i("Update available: %s", updateAvailable);
                        return updateAvailable;
                    })
                    .subscribe(this::startDownloadService, this::updateErrorHandle);

        }
    }

    private void updateErrorHandle(Throwable e) {
        turnConnectionChangeBroadcastReceiver(true);
        Toast.makeText(this, "Ошибка при проверке обновления " + e.getMessage(), Toast.LENGTH_SHORT).show();
        Timber.e(e, "Update check failed");
    }

    private void startDownloadService(UpdateResponse updateResponse) {
        Timber.i("Start download service for updater");
        int versionCode = updateResponse.versionCode;
        prefs.versionCode(versionCode);
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        //we query download manager for all downloaded paths. But we also store one in prefs(if download queue is cleared)
        // then we filter them for validness and take only one valid(lazily)
        queryDownloadQueue(downloadManager, versionCode) //
                .startWith(prefs.apkPath())
                .filter(this::fileExistsAndValid)
                .take(1)
                .defaultIfEmpty(null)
                .subscribe(s -> {
                    // s - truly valid and existed apk path
                    if (TextUtils.isEmpty(s)) {
                        L.v("Adding request to download");
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(updateResponse.url));
                        request.setTitle(getName(versionCode));
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalPublicDir("/Truckdoc/update/", getName(versionCode));
                        Toast.makeText(this, "Добавляем задачу в менеджер загрузок", Toast.LENGTH_SHORT).show();
                        long enqueue = downloadManager.enqueue(request);
                        prefs.downloadId(enqueue);
                    } else {
                        L.v("File already downloaded, starting install: " + s);
                        prefs.apkPath(s);
                        CheckInstallReceiver.startCheckInstallReceiver(this);
                    }
                });
    }

    private boolean fileExistsAndValid(String path) {
        try {
            if (StringUtils.isNotEmpty(path)) {
                DeviceHelper.getFileVersionCode(this, path);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            Timber.e(e, "Error on file existence and validity check");
            return false;
        }
    }

    static class DownloadEntry {
        final String name;
        final String path;
        final int status;

        DownloadEntry(String name, String path, int status) {
            this.name = name;
            this.path = path;
            this.status = status;
        }
    }

    //returns a sequence of all valid downloaded paths
    private Observable<String> queryDownloadQueue(DownloadManager downloadManager, int versionCode) {
        DownloadManager.Query query = new DownloadManager.Query();
        Cursor c = downloadManager.query(new DownloadManager.Query());
        List<DownloadEntry> result = new ArrayList<>();
        if (c != null) {
            L.v("totalEntries = " + c.getCount());
            for (int i = 0; i < c.getCount(); i++) {
                c.moveToPosition(i);
                int columnStatus = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                int columnTitle = c.getColumnIndex(DownloadManager.COLUMN_TITLE);
                String location = StringUtils.isEmpty(c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))) ?
                        c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)) :
                        c.getString(COLUMN_LOCATION);
                String path = Uri.parse(location).getPath();
                int status = c.getInt(columnStatus);
                String downloadName = c.getString(columnTitle);
                boolean namesEqual = getName(versionCode).equalsIgnoreCase(downloadName);
                L.v(downloadName + " namesEqual=" + namesEqual + " status=" + status + " path=" + path);

                if (namesEqual && //
                        (DownloadManager.STATUS_SUCCESSFUL == status ||
                                DownloadManager.STATUS_RUNNING == status ||
                                DownloadManager.STATUS_PENDING == status)) {
                    result.add(new DownloadEntry(downloadName, path, status));
                }
            }
            c.close();

        }
        return Observable.from(result).map(downloadEntry -> downloadEntry.path);
    }

    private String getName(int versionCode) {
        return prefs.targetPackage() + "-" + versionCode + ".apk";
    }

    private void handleCheckInstall() {
        Timber.i("Check updater install handle");
        try {
            if (StringUtils.isNotEmpty(prefs.apkPath())) {
                int versionCode = DeviceHelper.getFileVersionCode(this, prefs.apkPath());
                File file = new File(prefs.apkPath());
                if (versionCode > findVersionCode(prefs.targetPackage(), this)) {
                    if (file.exists()) {
                        DeviceHelper.installFile(this, file);
                    }
                } else if (versionCode == findVersionCode(prefs.targetPackage(), this)) {
                    alreadyInstalled(file);
                }
            }
        } catch (Throwable e) {
            Timber.e(e, "Error on updater install check");
        }
    }

    private void alreadyInstalled(File file) {
        Timber.i("File already installed " + file.getName());
        Toast.makeText(this, R.string.file_installed, Toast.LENGTH_SHORT).show();
        notificationHelper.hide();
        prefs.remove(Prefs.VERSION_CODE);
        prefs.remove(Prefs.APK_PATH);
        FileUtils.deleteQuietly(file);
        CheckInstallReceiver.stopCheckInstallReceiver(this);
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (prefs.useWiFi()) {
            NetworkInfo netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return netInfo != null && netInfo.isConnectedOrConnecting();
        } else {
            DeviceIds ids = DeviceHelper.getDeviceId(this);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        }
    }

    @Override
    public void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        }
        stopSelf();
    }
}
