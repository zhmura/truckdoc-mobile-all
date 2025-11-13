package com.sanda.truckdoc.client.service.intent;

import android.content.Context;
import android.content.Intent;

import com.sanda.checker.Checker;
import com.sanda.truckdoc.client.data.MessagesDatabaseService;
import com.sanda.truckdoc.client.data.MessagesDatabaseServiceJavaCompat;
import com.sanda.truckdoc.client.HiltEntryPoint;
import com.sanda.truckdoc.client.TruckDocApp;
import com.sanda.truckdoc.client.ui.DashboardActivity;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

public class BootCompletedJobIntentService extends JobIntentService {

    public static final int JOB_ID = 0x01;

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, BootCompletedJobIntentService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        try {
            Checker.setupCheckConnectionAfterBootIfNeeded(getApplicationContext());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        
        // Get database service through Hilt
        HiltEntryPoint entryPoint = TruckDocApp.getEntryPoint(getApplicationContext());
        MessagesDatabaseService databaseService = entryPoint.messagesDatabaseService();
        MessagesDatabaseServiceJavaCompat.deleteOldMessagesBlocking(databaseService);
        
        startDashboardActivity();
    }

    private void startDashboardActivity() {
        Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(intent);
    }

}
