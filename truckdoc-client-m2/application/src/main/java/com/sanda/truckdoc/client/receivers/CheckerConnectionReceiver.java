package com.sanda.truckdoc.client.receivers;

import android.content.Context;
import android.content.Intent;

import com.sanda.checker.NoConnectionReceiver;
import com.sanda.truckdoc.client.ui.DialogActivity;
import com.sanda.truckdoc.client.util.timber.L;

import androidx.annotation.NonNull;

public class CheckerConnectionReceiver extends NoConnectionReceiver {

    @Override
    protected void onCheck(@NonNull Context context, Result result) {
        L.v(result);
        startDialogActivity(context, true, result.toString());
    }

    private void startDialogActivity(Context context, boolean connectionProblem, String result) {
        Intent intent = new Intent(context, DialogActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(DialogActivity.EXTRA_CONNECTION_PROBLEM, connectionProblem);
        intent.putExtra(DialogActivity.EXTRA_RESULT, result);
        context.startActivity(intent);
    }
}
