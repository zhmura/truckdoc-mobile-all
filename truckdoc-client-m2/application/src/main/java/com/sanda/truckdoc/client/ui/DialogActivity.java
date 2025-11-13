package com.sanda.truckdoc.client.ui;

import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Window;
import android.view.WindowManager;

import com.sanda.checker.NoConnectionReceiver;
import com.sanda.truckdoc.client.util.timber.L;

import androidx.appcompat.app.AppCompatActivity;

public class DialogActivity extends AppCompatActivity {

    public static final String ACTION_FINISH = "com.sanda.truckdoc.client.ui.DialogActivity.ACTION_FINISH";
    public static final String TAG = "noConnection";
    
    // Constants for intent extras
    public static final String EXTRA_CONNECTION_PROBLEM = "connectionProblem";
    public static final String EXTRA_RESULT = "result";
    public static final String EXTRA_REMINDER_MESSAGE = "reminderMessage";
    public static final String EXTRA_SENDER_ROLE_ID = "senderRoleId";
    public static final String EXTRA_QUICK_REPLY = "quickReply";
    public static final String EXTRA_REPEAT_REMINDER = "repeatReminder";

    private Dialog dialog;
    private KeyguardManager.KeyguardLock keyguardLock;
    private boolean connectionProblem;
    private String reminderMessage;
    private long senderRoleId;
    private boolean quickReply;
    private boolean repeatReminder;
    private NoConnectionReceiver.Result result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L.v();
        
        // Get extras from intent
        Intent intent = getIntent();
        connectionProblem = intent.getBooleanExtra("connectionProblem", false);
        reminderMessage = intent.getStringExtra("reminderMessage");
        senderRoleId = intent.getLongExtra("senderRoleId", 0);
        quickReply = intent.getBooleanExtra("quickReply", false);
        repeatReminder = intent.getBooleanExtra("repeatReminder", false);
        result = intent.getParcelableExtra("result");

        Window window = this.getWindow();

        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        final PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE, "Truckdoc_WakeLock:");
        wakeLock.acquire();

        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        processDialog();

        wakeLock.release();
    }

    private void processDialog() {
        if (!connectionProblem) {
            dialog = new DialogUtils().dialog(this, reminderMessage, senderRoleId, quickReply, repeatReminder);
        } else {
            if (dialog != null) {
                dialog.hide();
            }
            NoConnectionFragment noConnectionFragment;
            noConnectionFragment = (NoConnectionFragment) getSupportFragmentManager().findFragmentByTag(TAG);
            if (noConnectionFragment == null) {
                noConnectionFragment = NoConnectionFragment.newInstance(result);
                noConnectionFragment.setCancelable(false);
                noConnectionFragment.show(getSupportFragmentManager(), TAG);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        processDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        L.v();
    }

    @Override
    protected void onDestroy() {
        L.v();
        if (dialog != null) {
            dialog.dismiss();
        }
        super.onDestroy();
    }
}

