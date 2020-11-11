package com.sanda.truckdoc.client.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.data.model.AttachmentInfo;
import com.sanda.truckdoc.client.data.model.ServerMessage;
import com.sanda.truckdoc.client.ui.Dialogs;
import com.sanda.truckdoc.client.util.commons.FilenameUtils;

import net.tribe7.common.collect.FluentIterable;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import androidx.core.content.FileProvider;
import app.messages2.OnMessageClicked;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class ShowAttachments implements OnMessageClicked {

    private final Activity activity;

    @Inject
    public ShowAttachments(Activity activity) {this.activity = activity;}

    @Override
    public void show(@NotNull ServerMessage sm, @NotNull List<AttachmentInfo> attachments, @NotNull Function0<Unit> onReply) {
        final Integer messageId = sm.getId();

        FluentIterable<AttachmentInfo> messageAttachments = FluentIterable.from(attachments).filter(AttachmentInfo::isDownloaded);
        if (attachments.size() == 0) {
            onReply.invoke();
        }
        if (attachments.size() > 0) {
            boolean pdfExists = false;
            boolean picsExists = false;
            boolean apkExists = false;
            for (AttachmentInfo attachmentInfo : messageAttachments) {

                String extension = FilenameUtils.getExtension(attachmentInfo.getFileName()).toLowerCase();

                if (extension.equals("pdf")) {
                    pdfExists = true;
                }
                if (extension.equals("jpeg") || extension.equals("jpg") || extension.equals("png")) {
                    picsExists = true;
                }
                if (extension.equals("apk")) {
                    apkExists = true;
                }
            }
            if (pdfExists && picsExists) {
                Dialogs.showDocumentOrImageDialogWithReply(activity,
                        () -> showDocument(messageId),
                        () -> showImage(messageId),
                        onReply::invoke);
            } else {
                if (pdfExists) {
                    Dialogs.showDialogWithReply(activity, () -> showDocument(messageId), 1, () -> onReply.invoke());
                }
                if (picsExists) {
                    Dialogs.showDialogWithReply(activity, () -> showImage(messageId), 2, () -> onReply.invoke());
                }
                if (apkExists) {
                    installApk(messageId);
                }
            }
        }
    }

    private void replyToSender(ServerMessage sm) {
        /* TODO
        Long recipientId = null;
        Observable<DbContactRecord> contacts = databaseService.getContactRecords();
        FluentIterable<DbContactRecord> contactRecords = FluentIterable.from(contacts.toList().toBlocking().single());
        if (sm.getSenderUserId() != null || sm.getSenderVirtualGroupId() != null) {
            if (sm.getSenderVirtualGroupId() != null) {
                Optional<DbContactRecord> contact = contactRecords.firstMatch(record -> record.getRecipientId() == sm
                .getSenderVirtualGroupId().longValue());
                recipientId = contact.isPresent() ? contact.get().getRecipientId() : null;
            } else {
                Optional<DbContactRecord> contact = contactRecords.firstMatch(record -> record.getRecipientId() == sm.getSenderUserId()
                .longValue());
                recipientId = contact.isPresent() ? contact.get().getRecipientId() : null;
            }
        } else if (sm.getRecipientId() != null) {
            Optional<DbContactRecord> contact = contactRecords.firstMatch(record -> record.getRecipientId() == sm.getRecipientId()
            .longValue());
            recipientId = contact.isPresent() ? contact.get().getRecipientId() : null;
        }
        if (recipientId != null) {
            Bundle args = new Bundle();
            args.putLong("recipientId", recipientId);
            ((InboxActivity) getActivity()).setCurrentTab(1, args);
        }*/

    }

    private void showImage(Integer messageId) {
        File directory = FileHelper.getIncomeDirectory(messageId, "jpeg");
        String[] list = directory.list();
        if (list != null && list.length > 0) {
            try {
                File pic = new File(directory, list[0]);
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                Uri uri = FileProvider.getUriForFile(Objects.requireNonNull(this.activity), "com.sanda.truckdoc.client.provider", pic);
                intent.setDataAndType(uri, "image/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                activity.startActivity(intent);
            } catch (Exception e) {
                showMessageToast(activity.getResources().getString(R.string.cannot_open_file));
            }
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
                    Uri uri = FileProvider.getUriForFile(Objects.requireNonNull(this.activity), "com.sanda.truckdoc.client.provider", apk);
                    intent.setDataAndType(uri, "application/vnd.android.package-archive");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } else {
                    intent.setDataAndType(Uri.fromFile(apk), "application/vnd.android.package-archive");
                }
                activity.startActivity(intent);
            } catch (Exception e) {
                showMessageToast(activity.getResources().getString(R.string.cannot_open_file));
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
                Uri uri = FileProvider.getUriForFile(Objects.requireNonNull(this.activity), "com.sanda.truckdoc.client.provider", pdf);
                intent.setData(uri);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                intent.setDataAndType(Uri.parse(pdf.getAbsolutePath()), "application/pdf");
                intent = Intent.createChooser(intent, "Open File");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            activity.startActivity(intent);
        } catch (Exception e) {
            showMessageToast(activity.getResources().getString(R.string.cannot_open_file));
        }
    }

    private void showMessageToast(String s) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View layout = inflater.inflate(R.layout.toast_layout, null);

        TextView text = layout.findViewById(R.id.text_toast);
        text.setTextSize(20.0f);
        text.setText(s);

        Toast toast = new Toast(activity);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }
}
