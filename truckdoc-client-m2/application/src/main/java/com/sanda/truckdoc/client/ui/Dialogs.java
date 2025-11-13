package com.sanda.truckdoc.client.ui;

import android.app.AlertDialog;
import android.content.Context;

import com.sanda.truckdoc.client.R;

import io.reactivex.rxjava3.functions.Action;

/**
 * Created by astra on 28.05.2015.
 */
public class Dialogs {

    public static void showCreateAnotherPhotoDialog(Context context, Action success, Action finish) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Сделать еще одно фото?")
                .setCancelable(true)
                .setPositiveButton(R.string.question_answer_y, (dialog, id) -> {
                    try {
                        success.run();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                })
                .setNegativeButton(R.string.question_answer_n, (dialog, id) -> {
                    try {
                        finish.run();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                });
        builder.create().show();
    }

    public static void showDeleteAllDialog(Context context, Action success) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Удалить все входящие и исходящие сообщения с прикрепленными файлами?")
                .setCancelable(true)
                .setPositiveButton(android.R.string.yes, (dialog, id) -> {
                    try {
                        success.run();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                })
                .setNegativeButton(android.R.string.no, (dialog, id) -> {
                });
        builder.create().show();
    }

    public static void showDocumentOrImageDialogWithReply(Context context, Action document, Action image, Action reply) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.open_attach_image).setCancelable(true).setPositiveButton(R.string.document, (dialog, id1) -> {
            try {
                document.run();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }).setNegativeButton(R.string.image, (dialog, id1) -> {
            try {
                image.run();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }).setNeutralButton("Ответить", (dialog, id1) -> {
            try {
                reply.run();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        builder.create().show();
    }

    public static void showClearAppDataDialog(Context context, Action success) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.clear_app_data_dialog).setCancelable(true).setPositiveButton(android.R.string.yes, (dialog, id) -> {
            try {
                success.run();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }).setNegativeButton(android.R.string.no, (dialog, id) -> {
        });
        builder.create().show();
    }

    public static void showDialogWithReply(Context context, Action action, int attachmentType, Action reply) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(attachmentType > 1 ? R.string.open_attach_image : R.string.open_attach_doc).setCancelable(true).setPositiveButton(attachmentType == 1
                ? R.string.document : R.string.image, (dialog, id1) -> {
            try {
                action.run();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }).setNeutralButton("Ответить", (dialog, id1) -> {
            try {
                reply.run();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        builder.create().show();
    }
}
