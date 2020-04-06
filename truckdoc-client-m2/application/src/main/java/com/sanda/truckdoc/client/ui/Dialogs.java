package com.sanda.truckdoc.client.ui;

import android.app.AlertDialog;
import android.content.Context;

import com.sanda.truckdoc.client.R;

import rx.functions.Action0;

/**
 * Created by astra on 28.05.2015.
 */
public class Dialogs {

    public static void showCreateAnotherPhotoDialog(Context context, Action0 success, Action0 finish) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Сделать еще одно фото?")
                .setCancelable(true)
                .setPositiveButton(R.string.question_answer_y, (dialog, id) -> {
                    success.call();
                })
                .setNegativeButton(R.string.question_answer_n, (dialog, id) -> {
                    finish.call();
                });
        builder.create().show();
    }

    public static void showDeleteAllDialog(Context context, Action0 success) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Удалить все входящие и исходящие сообщения с прикрепленными файлами?")
                .setCancelable(true)
                .setPositiveButton(android.R.string.yes, (dialog, id) -> {
                    success.call();
                })
                .setNegativeButton(android.R.string.no, (dialog, id) -> {
                });
        builder.create().show();
    }

    public static void showDocumentOrImageDialogWithReply(Context context, Action0 document, Action0 image, Action0 reply) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.open_attach_image).setCancelable(true).setPositiveButton(R.string.document, (dialog, id1) -> {
            document.call();
        }).setNegativeButton(R.string.image, (dialog, id1) -> {
            image.call();
        }).setNeutralButton("Ответить", (dialog, id1) -> {
            reply.call();
        });
        builder.create().show();
    }

    public static void showClearAppDataDialog(Context context, Action0 success) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.clear_app_data_dialog).setCancelable(true).setPositiveButton(android.R.string.yes, (dialog, id) -> {
            success.call();
        }).setNegativeButton(android.R.string.no, (dialog, id) -> {
        });
        builder.create().show();
    }

    public static void showDialogWithReply(Context context, Action0 action, int attachmentType, Action0 reply) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(attachmentType > 1 ? R.string.open_attach_image : R.string.open_attach_doc).setCancelable(true).setPositiveButton(attachmentType == 1
                ? R.string.document : R.string.image, (dialog, id1) -> {
            action.call();
        }).setNeutralButton("Ответить", (dialog, id1) -> {
            reply.call();
        });
        builder.create().show();
    }
}
