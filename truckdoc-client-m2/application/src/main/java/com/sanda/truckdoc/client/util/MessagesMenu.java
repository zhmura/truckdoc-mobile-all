package com.sanda.truckdoc.client.util;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.data.MessagesDatabaseService;
import com.sanda.truckdoc.client.service.MessageCheckService;
import com.sanda.truckdoc.client.service.NewMessageService_;
import com.sanda.truckdoc.client.service.ResponseCheckHelper;
import com.sanda.truckdoc.client.service.SyncReason;
import com.sanda.truckdoc.client.ui.Dialogs;
import com.sanda.truckdoc.client.ui.TruckdocPreferenceActivity_;

import javax.inject.Inject;

import app.messages2.OnMessagesMenu;

import static rx.android.schedulers.AndroidSchedulers.mainThread;
import static rx.schedulers.Schedulers.newThread;

public class MessagesMenu implements OnMessagesMenu {

    private final Activity                activity;
    private final MessagesDatabaseService databaseService;

    @Inject
    public MessagesMenu(Activity activity, MessagesDatabaseService databaseService) {
        this.activity = activity;
        this.databaseService = databaseService;
    }

    @Override
    public void deleteAll() {
        Dialogs.showDeleteAllDialog(activity, () -> {
            databaseService.deleteAllMessages().observeOn(mainThread()).subscribe(integer -> {});
        });
    }

    @Override
    public void clearAppData() {
        Dialogs.showClearAppDataDialog(activity, () -> {
            ResponseCheckHelper.response401Action(activity, true);
        });
    }

    @Override
    public void showSettings() {
        TruckdocPreferenceActivity_.intent(activity).start();

    }

    @Override
    public void refresh() {
        Context context = activity.getApplicationContext();
        MessageCheckService.executeGetNewMessagesAction(context, false, true, SyncReason.USER_DIRECT);
    }

    @Override
    public void export() {
        databaseService.getMessages(true)
                .subscribeOn(newThread())
                .observeOn(mainThread())

                .doOnNext((messages1) -> FileHelper.exportMessages(activity, messages1))
                .subscribe(messages -> Toast.makeText(activity, R.string.file_exported, Toast.LENGTH_SHORT)
                        .show());
    }

    @Override
    public void logReport() {
        NewMessageService_.intent(activity).sendLogReport().start();

    }
}
