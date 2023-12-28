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

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

import androidx.appcompat.app.AppCompatActivity;

@EActivity
public class DialogActivity extends AppCompatActivity {

    public static final String ACTION_FINISH = "com.sanda.truckdoc.client.ui.DialogActivity.ACTION_FINISH";
    public static final String TAG = "noConnection";

    private Dialog dialog;

    private KeyguardManager.KeyguardLock keyguardLock;

    @Extra
    boolean connectionProblem;
    @Extra
    String reminderMessage;
    @Extra
    long senderRoleId;
    @Extra
    boolean quickReply;
    @Extra
    boolean repeatReminder;
    @Extra
    NoConnectionReceiver.Result result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L.v();
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
                noConnectionFragment = NoConnectionFragment_.builder().result(result).build();
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

    protected void onDestroy() {
        L.v();
        if (dialog != null) {
            dialog.dismiss();
        }
        super.onDestroy();
    }
}

