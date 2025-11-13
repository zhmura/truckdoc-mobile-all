package com.sanda.truckdoc.client.service;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.TruckDocApp;
import com.sanda.truckdoc.client.data.MessagesDatabaseService;
import com.sanda.truckdoc.client.data.MessagesDatabaseServiceJavaCompat;
import com.sanda.truckdoc.client.ui.SplashActivity;
import com.sanda.truckdoc.client.HiltEntryPoint;

import app.camera.tdoc.camera_library.PreferenceKeys;
import retrofit2.Response;
import timber.log.Timber;

import static com.sanda.truckdoc.client.service.NotificationHelper.showErrorMessage;

/**
 * Created by Asus on 2/20/2017.
 */
public class ResponseCheckHelper {

    public static final String X_AUTH_ERROR_ACTION_HEADER = "X-Auth-Error-Action";

    public static boolean check401Error(Response response, ContextWrapper context, String methodCode, boolean ifInvcred) {
        if (ifInvcred) {
            response401Action(context, true);
        } else {
            response401Action(context, false);
        }
        return true;
    }

    public static boolean checkIfError(Response response, ContextWrapper context, String methodCode, boolean showToast) {
        if (response == null || !response.isSuccessful()) {
            int errorMessage;
            int code = response == null ? 532 : response.code();
            switch (code) {
                case 400:
                    errorMessage = R.string.common400response;
                    break;
                case 401:
                    String authErrorHeader = response.headers().get(X_AUTH_ERROR_ACTION_HEADER);
                    response401Action(context, authErrorHeader != null
                            && authErrorHeader.equalsIgnoreCase("Invalidate-Credentials"));
                    errorMessage = R.string.common401response;
                    break;
                case 402:
                    errorMessage = R.string.common402response;
                    break;
                case 403:
                    errorMessage = R.string.common403response;
                    break;
                case 404:
                    errorMessage = R.string.common404response;
                    break;
                case 500:
                    errorMessage = R.string.common500response;
                    break;
                case 531:
                    errorMessage = R.string.common531response;
                    break;
                case 532:
                    errorMessage = R.string.common532response;
                    break;
                default:
                    errorMessage = R.string.commonUnexpectedResponse;
            }
            if (showToast) {
                showErrorMessage(context, errorMessage, methodCode);
            }
            Timber.e("Response error: Code:" + (response != null ? response.code() : "undefined") + "; Method code:" + methodCode + "; Error message:" + errorMessage);
            return true;
        }
        return false;
    }

    public static void response401Action(ContextWrapper context, boolean invalidateCredentials) {
        disableSync(context);
        if (invalidateCredentials) {
            clearAppData(context);
            deactivateClient(context, 2);
        } else {
            deactivateClient(context, 1);
        }
        startSplashActivity(context);
    }

    private static void disableSync(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putBoolean(PreferenceKeys.getSyncEnabledPreferenceKey(), false).apply();
    }

    private static void deactivateClient(Context context, int status) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putInt(PreferenceKeys.getUserDeactivatedPreferenceKey(), status).apply();
    }


    private static void clearAppData(ContextWrapper context) {
        HiltEntryPoint entryPoint = TruckDocApp.getEntryPoint(context);
        MessagesDatabaseService db = entryPoint.messagesDatabaseService();
        MessagesDatabaseServiceJavaCompat.deleteAllDataBlocking(db);
        AppSettings settings = new AppSettings(context);
        settings.clearUserKey();
    }

    private static void startSplashActivity(Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            Intent intent = new Intent(activity, SplashActivity.class);
            activity.startActivity(intent);
        }
    }
}
