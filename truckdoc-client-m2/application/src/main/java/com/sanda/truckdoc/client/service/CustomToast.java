package com.sanda.truckdoc.client.service;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sanda.truckdoc.client.R;

/**
 * Created by Sergey Zhmura on 2/23/2017.
 */

public class CustomToast {

    public static void showToast(Context context, String message) {
        Toast customToast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        customToast.setDuration(Toast.LENGTH_LONG);
        customToast.show();
    }

    // Red toast
    public static void showRed(Context context, String message) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customToastRoot = inflater.inflate(R.layout.red_toast, null);
        TextView txt = customToastRoot.findViewById(R.id.textViewRedToast);
        txt.setText(message);
        Toast customToast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        customToast.setView(customToastRoot);
        customToast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        customToast.show();
    }

    // Yellow toast
    public static void showYellow(Context context, String message) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customToastRoot = inflater.inflate(R.layout.yellow_toast, null);
        TextView txt = customToastRoot.findViewById(R.id.textViewYellowToast);
        txt.setText(message);
        Toast customToast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        customToast.setView(customToastRoot);
        customToast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        customToast.setDuration(Toast.LENGTH_LONG);
        customToast.show();
    }

    // Blue toast
    public static void showBlue(Context context, String message) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customToastRoot = inflater.inflate(R.layout.blue_toast, null);
        TextView txt = customToastRoot.findViewById(R.id.textViewBlueToast);
        txt.setText(message);
        Toast customToast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        customToast.setView(customToastRoot);
        customToast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        customToast.setDuration(Toast.LENGTH_LONG);
        customToast.show();
    }
}
