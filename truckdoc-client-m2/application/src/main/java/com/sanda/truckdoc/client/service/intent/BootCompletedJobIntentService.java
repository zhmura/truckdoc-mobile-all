package com.sanda.truckdoc.client.service.intent;

import android.content.Context;
import android.content.Intent;

import com.sanda.checker.Checker;
import com.sanda.truckdoc.client.data.MessagesDatabaseService;
import com.sanda.truckdoc.client.ui.DashboardActivity_;

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
        MessagesDatabaseService.deleteOldMessages(getApplicationContext());
        DashboardActivity_.intent(getApplicationContext()).flags(Intent.FLAG_ACTIVITY_NEW_TASK).start();
    }

}
