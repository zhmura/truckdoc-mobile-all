package com.sanda.truckdoc.client.updater.receivers;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.sanda.truckdoc.client.updater.Prefs;
import com.sanda.truckdoc.client.updater.UpdaterApp;
import com.sanda.truckdoc.client.updater.utils.NotificationHelper;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

import javax.inject.Inject;

import androidx.annotation.Nullable;

/**
 * Created by astra on 05.07.2015.
 */
public class DownloadCompleteReceiver extends BroadcastReceiver {

    @Inject
    Prefs prefs;

    @Inject
    NotificationHelper notificationHelper;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            UpdaterApp.get(context).appComponent().inject(this);
            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(prefs.downloadId());
            DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Cursor c = dm.query(query);
            if (c.moveToFirst()) {
                int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                    String path;
                    //apk locations column, expected according to api
                    path = getPath(c, DownloadManager.COLUMN_LOCAL_URI, true);
                    if (path == null) {
                        path = getPath(c, DownloadManager.COLUMN_LOCAL_FILENAME, false);
                    }
                    //check other columns
                    if (path == null) {
                        for (int i = 0; i < c.getColumnCount(); i++) {
                            path = getString(c, true, i);
                            if (path != null) {
                                break;
                            }
                        }
                    }
                    if (StringUtils.isNotEmpty(path)) {
                        prefs.apkPath(path);
                        notificationHelper.notifyAppDownloaded();
                        CheckInstallReceiver.startCheckInstallReceiver(context);
                    }
                }
            }
        }
    }

    @Nullable
    private String getPath(Cursor c, String columnName, boolean isUri) {
        String path = null;
        int index = c.getColumnIndex(columnName);
        if (index >= 0) {
            path = getString(c, isUri, index);
        }
        return path;
    }

    @Nullable
    private String getString(Cursor c, boolean isUri, int index) {
        String path = null;
        try {
            path = isUri ? Uri.parse(c.getString(index)).getPath() : c.getString(index);
            if (StringUtils.isEmpty(path) || !fileExists(path)) {
                path = null;
            }
        } catch (Exception ignored) {
        }
        return path;
    }

    private boolean fileExists(String path) {
        File file = new File(path);
        return file.exists();
    }
}
