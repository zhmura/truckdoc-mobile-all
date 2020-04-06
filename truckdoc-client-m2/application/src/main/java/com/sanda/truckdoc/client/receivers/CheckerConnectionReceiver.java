package com.sanda.truckdoc.client.receivers;

import android.content.Context;
import android.content.Intent;

import com.sanda.checker.NoConnectionReceiver;
import com.sanda.truckdoc.client.ui.DialogActivity_;
import com.sanda.truckdoc.client.util.timber.L;

import androidx.annotation.NonNull;

public class CheckerConnectionReceiver extends NoConnectionReceiver {

    @Override
    protected void onCheck(@NonNull Context context, Result result) {
        L.v(result);
        DialogActivity_.intent(context).connectionProblem(true).result(result).flags(Intent.FLAG_ACTIVITY_NEW_TASK).start();
    }
}
