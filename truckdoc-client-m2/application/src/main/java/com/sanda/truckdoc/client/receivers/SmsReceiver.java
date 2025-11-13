package com.sanda.truckdoc.client.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import android.util.Log;
import android.os.Bundle;

import com.google.common.base.Strings;
import com.sanda.truckdoc.client.TruckDocApp;
import com.sanda.truckdoc.client.data.MessagesDatabaseService;
import com.sanda.truckdoc.client.data.MessagesDatabaseServiceJavaCompat;
import com.sanda.truckdoc.client.data.model.DbContactRecord;
import com.sanda.truckdoc.client.data.model.ServerMessage;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import com.sanda.truckdoc.client.HiltEntryPoint;

/**
 * Created by astra on 21.10.2015.
 */
public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null) {
                    for (Object pdu : pdus) {
                        SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                        String smsPhone = smsMessage.getDisplayOriginatingAddress();
                        String smsBody = smsMessage.getMessageBody();

                        if (!Strings.isNullOrEmpty(smsPhone) && !Strings.isNullOrEmpty(smsBody)) {
                            try {
                                HiltEntryPoint entryPoint = TruckDocApp.getEntryPoint(context);
                                MessagesDatabaseService db = entryPoint.messagesDatabaseService();
                                List<DbContactRecord> contacts = MessagesDatabaseServiceJavaCompat.getContactRecordsBlocking(db);
                                List<DbContactRecord> contactRecords = contacts;
                                
                                Optional<DbContactRecord> record = contactRecords.stream().filter(r -> r.getPhone().equals(smsPhone)).findFirst();
                                if (record.isPresent()) {
                                    ServerMessage sm = new ServerMessage(
                                        0, // id will be auto-generated
                                        0, // serverMessageId
                                        smsBody, // message
                                        "", // sender
                                        "", // recipient
                                        DateTime.now(), // savedDate
                                        null, // sentDate
                                        false, // outgoing
                                        false, // sent
                                        false, // downloaded
                                        false, // hidden
                                        "SMS", // type
                                        0, // priority
                                        false, // read
                                        null, // tags
                                        null, // senderUserId
                                        null, // senderVirtualGroupId
                                        record.get().getRecipientId(), // recipientId
                                        new ArrayList<>() // attachments
                                    );
                                    MessagesDatabaseServiceJavaCompat.addSmsMessageBlocking(db, sm);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error processing SMS", e);
                            }
                        }
                    }
                }
            }
        }
    }
}
