package com.sanda.truckdoc.client.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.sanda.truckdoc.client.TruckDocApp;
import com.sanda.truckdoc.client.data.MessagesDatabaseService;
import com.sanda.truckdoc.client.data.model.DbContactRecord;
import com.sanda.truckdoc.client.data.model.ServerMessage;
import com.sanda.truckdoc.client.service.MessageCheckService;
import com.sanda.truckdoc.client.service.SyncReason;
import com.sanda.truckdoc.client.util.Option;
import com.sanda.truckdoc.client.util.timber.L;

import net.tribe7.common.base.Optional;
import net.tribe7.common.base.Strings;
import net.tribe7.common.collect.FluentIterable;

import org.joda.time.DateTime;

import androidx.annotation.NonNull;
import rx.Observable;
import timber.log.Timber;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        //L.v();
        try {
            Bundle bundle = intent.getExtras();
            Object[] messages = (Object[]) bundle.get("pdus");
            assert messages != null;
            SmsMessage[] sms = new SmsMessage[messages.length];
            // Create messages for each incoming PDU
            for (int n = 0; n < messages.length; n++) {
                sms[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
            }
            MessagesDatabaseService db = TruckDocApp.get(context).appComponent().db();
            Observable<DbContactRecord> contacts = db.getContactRecords().cache();
            FluentIterable<DbContactRecord> contactRecords = FluentIterable.from(contacts.toList().toBlocking().single());

            boolean needSync = false;
            for (SmsMessage msg : sms) {
                String smsPhone = msg.getDisplayOriginatingAddress();
                L.v(sms);
                if (!Strings.isNullOrEmpty(smsPhone)) {

                    Option<Integer> role = Option.empty();
                    Optional<DbContactRecord> record = contactRecords.firstMatch(r -> r.getPhone().equals(smsPhone));

                    L.v(role);
                    if (record.isPresent()) {
                        ServerMessage sm = new ServerMessage();
                        sm.setDownloaded(true);
                        sm.setSavedDate(new DateTime(msg.getTimestampMillis()));
                        sm.setText(msg.getMessageBody());
                        sm.setRecipientId(record.get().getRecipientId().intValue());
                        sm.setOutgoing(false);
                        sm.setHidden(true);
                        db.addSmsMessage(sm);
                        needSync = true;
                    }
                }
            }
            if (needSync) {
                // TODO: Think if we really need to do sync on SMS and why
                MessageCheckService.executeGetNewMessagesAction(context, false, false, SyncReason.GOT_SMS);
            }
        } catch (Exception e) {
            Timber.e(e, "Sms receiving failed.");
        }
    }
}
