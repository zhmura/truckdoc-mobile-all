package com.sanda.truckdoc.client.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.receivers.IncomeMessagesAlarmManager;
import com.sanda.truckdoc.client.service.MessageCheckService;
import com.sanda.truckdoc.client.ui.message.InboxActivity;
import com.sanda.truckdoc.client.util.ConnectionUtils;

/**
 * Created by User on 11.05.2015.
 */
public class DialogUtils {

    Dialog dialog;
    Vibrator vib;
    RelativeLayout rl;

    @SuppressWarnings("static-access")
    public Dialog dialog(final Context context, final String message, final long roleId, boolean quickReply, boolean repeat) {
        dialog = new Dialog(context, android.R.style.Theme_NoTitleBar);
        dialog.setContentView(R.layout.dialog_message_reminder);
        dialog.getWindow();
        dialog.setCancelable(false);
        TextView m = dialog.findViewById(R.id.dialogMessage);
        final Button no = dialog.findViewById(R.id.cancel_btn_id);
/*
        final Button ok = (Button) dialog.findViewById(R.id.ok_btn_id);
*/
        final Button see = dialog.findViewById(R.id.see_btn_id);
        m.setOnClickListener(arg0 -> {
            Intent intent = new Intent(context, InboxActivity.class);
            context.startActivity(intent);
            dialog.dismiss();
            ((Activity) context).finish();
        });
/*        ok.setOnClickListener(arg0 -> {
            Intent intent = new Intent(MessageCheckService.ACTION_SEND_TEXT_MESSAGE, null, context, MessageCheckService.class);
            Bundle b = new Bundle();
            b.putString("com/sanda/truckdoc/client/message", context.getResources().getString(R.string.dialog_new_messages_accept));
            b.putLong("mail.group", roleId);
            intent.putExtras(b);
            context.startService(intent);
            IncomeMessagesAlarmManager.cancelAlarm(context);
            dialog.dismiss();
            ((Activity) context).finish();
        });*/
        if (repeat) {
            no.setOnClickListener(arg0 -> {
                dialog.dismiss();
                ((Activity) context).finish();
            });
        } else {
            no.setOnClickListener(arg0 -> {
                ConnectionUtils.checkIfHaveInternetConnection(message, null, context);
                Intent intent = new Intent(MessageCheckService.ACTION_SEND_TEXT_MESSAGE, null, context, MessageCheckService.class);
                Bundle b = new Bundle();
                b.putString("com/sanda/truckdoc/client/message", context.getResources().getString(R.string.dialog_new_messages_cant_answer));
                b.putLong("mail.group", roleId);
                intent.putExtras(b);
                context.startService(intent);
                IncomeMessagesAlarmManager.cancelAlarm(context);
                IncomeMessagesAlarmManager.setAlarmWithDialog(context, IncomeMessagesAlarmManager.NOTIFY_INTERVAL_BUSY);
                dialog.dismiss();
                ((Activity) context).finish();
            });
        }
/*
        ok.setVisibility(View.GONE);
*/
        see.setVisibility(View.VISIBLE);
        see.setOnClickListener(arg0 -> {
            Intent intent2 = new Intent(context, InboxActivity.class);
            intent2.putExtra("messagesSeen", true);
            intent2.putExtra("roleId", roleId);
            context.startActivity(intent2);
            dialog.dismiss();
            ((Activity) context).finish();
        });
        m.setText((message.length() > 300 || !quickReply) ? message.substring(0, message.length() > 300 ? 250 : message.length() - 1) +
                "..." : message);
        dialog.show();
        vib = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);

        return dialog;
    }
}
