package com.sanda.truckdoc.client.ui.message;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.TruckDocApp;
import com.sanda.truckdoc.client.data.MessagesDatabaseService;
import com.sanda.truckdoc.client.data.MessagesDatabaseServiceJavaCompat;
import com.sanda.truckdoc.client.data.model.AttachmentInfo;
import com.sanda.truckdoc.client.data.model.DbContactRecord;
import com.sanda.truckdoc.client.data.model.ServerMessage;
import com.sanda.truckdoc.client.databinding.FragmentInboxBinding;
import com.sanda.truckdoc.client.receivers.IncomeMessagesAlarmManager;
import com.sanda.truckdoc.client.receivers.ServiceResultReceiver;
import com.sanda.truckdoc.client.service.AppSettings;
import com.sanda.truckdoc.client.service.MessageCheckService;
import com.sanda.truckdoc.client.service.NewMessageService;
import com.sanda.truckdoc.client.service.NotificationHelper;
import com.sanda.truckdoc.client.service.ResponseCheckHelper;
import com.sanda.truckdoc.client.service.SyncReason;
import com.sanda.truckdoc.client.ui.Dialogs;
import com.sanda.truckdoc.client.ui.TruckdocPreferenceActivity;
import com.sanda.truckdoc.client.util.FileHelper;
import com.sanda.truckdoc.client.util.commons.FilenameUtils;
import com.sanda.truckdoc.network.api.UserKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.File;

import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static com.google.common.collect.FluentIterable.from;
import com.sanda.truckdoc.client.HiltEntryPoint;

@AndroidEntryPoint
public class InboxFragment extends Fragment implements MessageAdapter.ServiceMessageClickListener {

    private MessageAdapter adapter;
    private FragmentInboxBinding binding;
    private boolean showHidden = false;
    private ServiceResultReceiver receiver;

    @Inject
    MessagesDatabaseService databaseService;
    @Nullable
    private UserKey userKey;
    @Inject
    NotificationHelper notificationHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState != null) {
            showHidden = savedInstanceState.getBoolean("showHidden", false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentInboxBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Context context = requireActivity();
        @NotNull AppSettings settings = new AppSettings(requireActivity());
        userKey = settings.getUserKey();
        // No authorized backend is used in this fragment; avoid keeping a stub initializer around.
        adapter = new MessageAdapter(this);
        binding.recyclerView.setAdapter(adapter);
        adapter.attachSwipeCallback(binding.recyclerView);
        loadInbox();
    }

    @Override
    public void onStart() {
        super.onStart();
        loadInbox();
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ServiceResultReceiver.ACTION_PROCESS_FINISHED);
        filter.addAction(ServiceResultReceiver.ACTION_LIST_UPDATE_START);
        filter.addAction(ServiceResultReceiver.ACTION_LIST_UPDATED);
        com.sanda.truckdoc.client.util.ReceiverUtils.registerReceiverNotExported(requireActivity(), receiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        requireActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("showHidden", showHidden);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void loadInbox() {
        ProgressHelper.showDialog(requireActivity(), getResources().getString(R.string.loading));

        IncomeMessagesAlarmManager.cancelAlarm(requireActivity());
        IncomeMessagesAlarmManager.cancelAlarmWithDialog(requireActivity());

        // Use compatibility layer for blocking calls
        new Thread(() -> {
            try {
                List<DbContactRecord> contacts = MessagesDatabaseServiceJavaCompat.getContactRecordsBlocking(databaseService);
                List<ServerMessage> messages = MessagesDatabaseServiceJavaCompat.getMessagesBlocking(databaseService, showHidden);
                Pair<List<ServerMessage>, List<DbContactRecord>> pair = Pair.create(messages, contacts);
                requireActivity().runOnUiThread(() -> {
                    adapter.swapItems(pair);
                    ProgressHelper.dismissDialog();
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(ProgressHelper::dismissDialog);
            }
        }).start();
    }

    @Override
    public void onServiceMessageClicked(ServerMessage sm) {
        final Integer messageId = sm.getId();

        List<AttachmentInfo> messageAttachments = sm.getAttachments().stream().filter(AttachmentInfo::isDownloaded).collect(Collectors.toList());
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
                Dialogs.showDocumentOrImageDialogWithReply(requireActivity(),
                        () -> showDocument(messageId),
                        () -> showImage(messageId),
                        () -> replyToSender(sm));
            } else {
                if (pdfExists) {
                    Dialogs.showDialogWithReply(requireActivity(),
                            () -> showDocument(messageId),
                            1,
                            () -> replyToSender(sm));
                }
                if (picsExists) {
                    Dialogs.showDialogWithReply(requireActivity(),
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
        MessagesDatabaseServiceJavaCompat.markHiddenBlocking(databaseService, sm);
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

                Uri uri = FileProvider.getUriForFile(requireActivity(), "com.sanda.truckdoc.client.provider", pic);
                intent.setDataAndType(uri, "image/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            } catch (Exception e) {
                showMessageToast(getResources().getString(R.string.cannot_open_file));
            }
        }
    }

    private void replyToSender(ServerMessage sm) {
        Long recipientId = null;
        List<DbContactRecord> contacts = MessagesDatabaseServiceJavaCompat.getContactRecordsBlocking(databaseService);
        List<DbContactRecord> contactRecords = contacts;
        if (sm.getSenderUserId() != null || sm.getSenderVirtualGroupId() != null) {
            if (sm.getSenderVirtualGroupId() != null) {
                Optional<DbContactRecord> contact = contactRecords.stream().filter(record -> record.getRecipientId() == sm.getSenderVirtualGroupId().longValue()).findFirst();
                recipientId = contact.isPresent() ? contact.get().getRecipientId() : null;
            } else {
                Optional<DbContactRecord> contact = contactRecords.stream().filter(record -> record.getRecipientId() == sm.getSenderUserId().longValue()).findFirst();
                recipientId = contact.isPresent() ? contact.get().getRecipientId() : null;
            }
        } else if (sm.getRecipientId() != null) {
            Optional<DbContactRecord> contact = contactRecords.stream().filter(record -> record.getRecipientId() == sm.getRecipientId().longValue()).findFirst();
            recipientId = contact.isPresent() ? contact.get().getRecipientId() : null;
        }
        if (recipientId != null) {
            Bundle args = new Bundle();
            args.putLong("recipientId", recipientId);
            ((InboxActivity) requireActivity()).setCurrentTab(1, args);
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
                Uri uri = FileProvider.getUriForFile(requireActivity(), "com.sanda.truckdoc.client.provider", apk);
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
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
            File pdf = new File(directory, Objects.requireNonNull(list)[0]);
            intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = FileProvider.getUriForFile(requireActivity(), "com.sanda.truckdoc.client.provider", pdf);
            intent.setData(uri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            showMessageToast(getResources().getString(R.string.cannot_open_file));
        }
    }

    @Override
    public void onDestroy() {
        ProgressHelper.dismissDialog();
        super.onDestroy();
    }

    private void showMessageToast(String s) {
        LayoutInflater inflater = LayoutInflater.from(requireActivity());
        View layout = inflater.inflate(R.layout.toast_layout, null);

        TextView text = layout.findViewById(R.id.text_toast);
        text.setTextSize(20.0f);
        text.setText(s);

        Toast toast = new Toast(requireActivity());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    private void showProgressBar(@Nullable String header, @Nullable String message, int status) {
        if (status > 0) {
            ProgressHelper.showDialog(requireActivity(), message);
        } else {
            ProgressHelper.dismissDialog();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull android.view.MenuInflater inflater) {
        inflater.inflate(R.menu.inbox_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_show_hidden).setChecked(showHidden);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_delete_all) {
            Dialogs.showDeleteAllDialog(requireActivity(), () -> {
                MessagesDatabaseServiceJavaCompat.deleteAllMessagesBlocking(databaseService);
                loadInbox();
            });
            return true;
        } else if (id == R.id.menu_clear_app_data) {
            Dialogs.showClearAppDataDialog(requireActivity(), () -> {
                ResponseCheckHelper.response401Action(requireActivity(), true);
            });
            return true;
        } else if (id == R.id.menu_settings) {
            openPreferences();
            return true;
        } else if (id == R.id.menu_refresh) {
            Context context = requireActivity().getApplicationContext();
            MessageCheckService.executeGetNewMessagesAction(context, false, true, SyncReason.USER_DIRECT);
            return true;
        } else if (id == R.id.menu_show_hidden) {
            item.setChecked(!item.isChecked());
            showHidden = item.isChecked();
            loadInbox();
            return true;
        } else if (id == R.id.log_report) {
            sendLogReport();
            return true;
        } else if (id == R.id.menu_export) {
            new Thread(() -> {
                try {
                    List<ServerMessage> messages = MessagesDatabaseServiceJavaCompat.getMessagesBlocking(databaseService, true);
                    requireActivity().runOnUiThread(() -> {
                        FileHelper.exportMessages(requireActivity(), messages);
                        Toast.makeText(requireActivity(), R.string.file_exported, Toast.LENGTH_SHORT).show();
                    });
                } catch (Exception e) {
                    requireActivity().runOnUiThread(() -> 
                        Toast.makeText(requireActivity(), "Export failed", Toast.LENGTH_SHORT).show());
                }
            }).start();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openPreferences() {
        Intent intent = new Intent(requireContext(), TruckdocPreferenceActivity.class);
        startActivity(intent);
    }

    private void sendLogReport() {
        Intent intent = new Intent(requireActivity(), NewMessageService.class);
        intent.setAction(NewMessageService.ACTION_SEND_LOG_REPORT);
        requireActivity().startService(intent);
    }
}
