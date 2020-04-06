package com.sanda.truckdoc.client.ui.message;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.TruckDocApp;
import com.sanda.truckdoc.client.data.MessagesDatabaseService;
import com.sanda.truckdoc.client.data.model.AttachmentInfo;
import com.sanda.truckdoc.client.data.model.DbContactRecord;
import com.sanda.truckdoc.client.data.model.ServerMessage;
import com.sanda.truckdoc.client.receivers.IncomeMessagesAlarmManager;
import com.sanda.truckdoc.client.receivers.ServiceResultReceiver;
import com.sanda.truckdoc.client.service.AppSettings;
import com.sanda.truckdoc.client.service.MessageCheckService;
import com.sanda.truckdoc.client.service.NewMessageService_;
import com.sanda.truckdoc.client.service.NotificationHelper;
import com.sanda.truckdoc.client.service.ResponseCheckHelper;
import com.sanda.truckdoc.client.service.SyncReason;
import com.sanda.truckdoc.client.ui.Dialogs;
import com.sanda.truckdoc.client.ui.TruckdocPreferenceActivity_;
import com.sanda.truckdoc.client.util.FileHelper;
import com.sanda.truckdoc.client.util.commons.FilenameUtils;
import com.sanda.truckdoc.network.api.AuthorizedNetworkModule;
import com.sanda.truckdoc.network.api.UserKey;

import net.tribe7.common.base.Optional;
import net.tribe7.common.collect.FluentIterable;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.Receiver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import rx.Observable;

import static net.tribe7.common.collect.FluentIterable.from;
import static org.androidannotations.annotations.Receiver.RegisterAt.OnResumeOnPause;
import static rx.android.schedulers.AndroidSchedulers.mainThread;
import static rx.schedulers.Schedulers.newThread;

@EFragment(R.layout.fragment_inbox)
@OptionsMenu(R.menu.inbox_menu)
public class InboxFragment extends Fragment implements MessageAdapter.ServiceMessageClickListener {

    // Progress Dialog
    private ProgressDialog pDialog;
    private MessageAdapter adapter;

    @Inject
    MessagesDatabaseService databaseService;
    @InstanceState
    boolean showHidden = false;
    @Nullable
    private UserKey userKey;
    @Inject
    NotificationHelper notificationHelper;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        final Context context = getActivity();
        TruckDocApp.get(context).appComponent().inject(this);
        @NotNull AppSettings settings = new AppSettings(this.getActivity());
        userKey = settings.getUserKey();
        if (userKey != null) {
            createAuthorizedBackend();
        }
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        adapter = new MessageAdapter(this);
        recyclerView.setAdapter(adapter);
        adapter.attachSwipeCallback(recyclerView);
        loadInbox();
    }

    private void createAuthorizedBackend() {
        assert userKey != null;
        TruckDocApp.get(this.getActivity())
                .appComponent()
                .plus(new AuthorizedNetworkModule(userKey))
                .authorizedBackend();
    }

    private void loadInbox() {
        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage(getResources().getString(R.string.loading));
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

        IncomeMessagesAlarmManager.cancelAlarm(getActivity());
        IncomeMessagesAlarmManager.cancelAlarmWithDialog(getActivity());

        Observable<List<DbContactRecord>> contacts = databaseService.getContactRecords().toList().cache();

        databaseService.getMessages(showHidden)
                .doOnTerminate(pDialog::dismiss)
                .toList()
                .zipWith(contacts, Pair::create)
                .subscribe(adapter::swapItems); //TODO bind activity
    }

    @Receiver(actions = ServiceResultReceiver.ACTION_PROCESS_FINISHED,
            registerAt = OnResumeOnPause)
    void onMessagesLoaded(Intent intent) {
        //String text = intent.getStringExtra(MessageCheckService.PARAM_OUT_MSG);
        showProgressBar(null, null, 0);
        //NotificationHelper.showNotificationMessage(text, this.getContext());
        loadInbox();
    }

    @Receiver(actions = ServiceResultReceiver.ACTION_PROCESS_START,
            registerAt = OnResumeOnPause)
    void onMessagesLoadStarted() {
        showProgressBar(getResources().getString(R.string.loading),
                getResources().getString(R.string.wait_for_loading),
                1);
    }

    @Override
    public void onServiceMessageClicked(ServerMessage sm) {
        final Integer messageId = sm.getId();

        FluentIterable<AttachmentInfo> messageAttachments = from(sm.getAttachments()).filter(AttachmentInfo::isDownloaded);
        if (sm.getAttachments().size() == 0) {
            replyToSender(sm);
        }
        if (sm.getAttachments().size() > 0) {
            boolean pdfExists = false;
            boolean picsExists = false;
            boolean apkExists = false;
            for (AttachmentInfo attachmentInfo : messageAttachments) {

                String extension = FilenameUtils.getExtension(attachmentInfo.getFileName()).toLowerCase();

                if (extension.equals("pdf")) {
                    pdfExists = true;
                }
                if (extension.equals("jpeg") ||
                        extension.equals("jpg") ||
                        extension.equals("png")) {
                    picsExists = true;
                }
                if (extension.equals("apk")) {
                    apkExists = true;
                }
            }
            if (pdfExists && picsExists) {
                Dialogs.showDocumentOrImageDialogWithReply(getActivity(),
                        () -> showDocument(messageId),
                        () -> showImage(messageId),
                        () -> replyToSender(sm));
            } else {
                if (pdfExists) {
                    Dialogs.showDialogWithReply(getActivity(),
                            () -> showDocument(messageId),
                            1,
                            () -> replyToSender(sm));
                }
                if (picsExists) {
                    Dialogs.showDialogWithReply(getActivity(),
                            () -> showImage(messageId),
                            2,
                            () -> replyToSender(sm));
                }
                if (apkExists) {
                    installApk(messageId);
                }
            }
        }
    }

    @Override
    public boolean onServiceMessageDismissed(ServerMessage sm) {
        databaseService.markHidden(sm);
        return showHidden;
    }

    private void showImage(Integer messageId) {
        File directory = FileHelper.getIncomeDirectory(messageId, "jpeg");
        String[] list = directory.list();
        if (list != null && list.length > 0) {
            try {
                File pic = new File(directory, list[0]);
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Uri uri = FileProvider.getUriForFile(Objects.requireNonNull(this.getActivity()), "com.sanda.truckdoc.client.provider", pic);
                    intent.setDataAndType(uri, "image/*");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } else {
                    intent.setDataAndType(Uri.parse(pic.getAbsolutePath()), "image/*");
                    intent = Intent.createChooser(intent, "Open File");
                }
                startActivity(intent);
            } catch (Exception e) {
                showMessageToast(getResources().getString(R.string.cannot_open_file));
            }
        }
    }

    private void replyToSender(ServerMessage sm) {
        Long recipientId = null;
        Observable<DbContactRecord> contacts = databaseService.getContactRecords();
        FluentIterable<DbContactRecord> contactRecords = FluentIterable.from(contacts.toList().toBlocking().single());
        if (sm.getSenderUserId() != null || sm.getSenderVirtualGroupId() != null) {
            if (sm.getSenderVirtualGroupId() != null) {
                Optional<DbContactRecord> contact = contactRecords.firstMatch(record -> record.getRecipientId() == sm.getSenderVirtualGroupId().longValue());
                recipientId = contact.isPresent() ? contact.get().getRecipientId() : null;
            } else {
                Optional<DbContactRecord> contact = contactRecords.firstMatch(record -> record.getRecipientId() == sm.getSenderUserId().longValue());
                recipientId = contact.isPresent() ? contact.get().getRecipientId() : null;
            }
        } else if (sm.getRecipientId() != null) {
            Optional<DbContactRecord> contact = contactRecords.firstMatch(record -> record.getRecipientId() == sm.getRecipientId().longValue());
            recipientId = contact.isPresent() ? contact.get().getRecipientId() : null;
        }
        if (recipientId != null) {
            Bundle args = new Bundle();
            args.putLong("recipientId", recipientId);
            ((InboxActivity) getActivity()).setCurrentTab(1, args);
        }

    }

    private void installApk(Integer messageId) {
        File directory = FileHelper.getIncomeDirectory(messageId, "apk");
        String[] list = directory.list();
        if (list != null && list.length > 0) {
            try {
                File apk = new File(directory, list[0]);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Uri uri = FileProvider.getUriForFile(Objects.requireNonNull(this.getActivity()), "com.sanda.truckdoc.client.provider", apk);
                    intent.setDataAndType(uri, "application/vnd.android.package-archive");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } else {
                    intent.setDataAndType(Uri.fromFile(apk), "application/vnd.android.package-archive");
                }
                startActivity(intent);
            } catch (Exception e) {
                showMessageToast(getResources().getString(R.string.cannot_open_file));
            }
        }
    }

    private void showDocument(Integer messageId) {
        File directory = FileHelper.getIncomeDirectory(messageId, "pdf");
        String[] list = directory.list();
        Intent intent;
        try {
            File pdf = new File(directory, list[0]);
            intent = new Intent(Intent.ACTION_VIEW);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri uri = FileProvider.getUriForFile(Objects.requireNonNull(this.getActivity()), "com.sanda.truckdoc.client.provider", pdf);
                intent.setData(uri);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                intent.setDataAndType(Uri.parse(pdf.getAbsolutePath()), "application/pdf");
                intent = Intent.createChooser(intent, "Open File");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            startActivity(intent);
        } catch (Exception e) {
            showMessageToast(getResources().getString(R.string.cannot_open_file));
        }
    }

    @Override
    public void onDestroy() {
        pDialog.cancel();
        super.onDestroy();
    }

    private void showMessageToast(String s) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View layout = inflater.inflate(R.layout.toast_layout, null);

        TextView text = layout.findViewById(R.id.text_toast);
        text.setTextSize(20.0f);
        text.setText(s);

        Toast toast = new Toast(getActivity());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    private void showProgressBar(@Nullable String header, @Nullable String message, int status) {
        pDialog.setTitle(header);
        pDialog.setMessage(message);
        pDialog.setIndeterminate(true);
        pDialog.setProgress(50);
        pDialog.setCancelable(true);
        if (status > 0) {
            pDialog.show();
        } else {
            pDialog.cancel();
        }
    }

    @OptionsItem
    void menuDeleteAll() {
        Dialogs.showDeleteAllDialog(getActivity(), () -> {
            databaseService.deleteAllMessages().observeOn(mainThread()).subscribe(integer -> loadInbox());
        });
    }

    @OptionsItem
    void menuClearAppData() {
        Dialogs.showClearAppDataDialog(getActivity(), () -> {
            ResponseCheckHelper.response401Action(getActivity(), true);
        });
    }

    @OptionsItem
    void menuSettings() {
        TruckdocPreferenceActivity_.intent(this).start();
    }

    @OptionsItem
    void menuRefresh() {
        Context context = getActivity().getApplicationContext();
        MessageCheckService.executeGetNewMessagesAction(context, false, true, SyncReason.USER_DIRECT);
    }

    @OptionsItem
    void menuShowHidden(MenuItem item) {
        item.setChecked(!item.isChecked());
        showHidden = item.isChecked();
        loadInbox();
    }

    @OptionsItem
    void logReport() {
        NewMessageService_.intent(this.getActivity()).sendLogReport().start();
    }

    @OptionsItem
    void menuExport() {
        databaseService.getMessages(true)
                .subscribeOn(newThread())
                .observeOn(mainThread())
                .toList()
                .doOnNext((messages1) -> FileHelper.exportMessages(getActivity(), messages1))
                .subscribe(messages -> Toast.makeText(getActivity(), R.string.file_exported, Toast.LENGTH_SHORT)
                        .show());
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_show_hidden).setChecked(showHidden);
    }
}
