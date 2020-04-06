package com.sanda.checker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public abstract class NoConnectionReceiver extends BroadcastReceiver {

    public static class Result implements Parcelable {

        public final boolean mobileDataEnabled;
        public final boolean roamingDataEnabled;
        public final boolean airplaneModeEnabled;
        public final boolean networkOperatorChanged;
        public final boolean wasMobileDataEnabled;
        public final boolean wasRoamingDataEnabled;
        public final boolean wasAirplaneModeEnabled;

        public Result(boolean mobileDataEnabled,
                      boolean roamingDataEnabled,
                      boolean airplaneModeEnabled,
                      boolean networkOperatorChanged,
                      boolean wasMobileDataEnabled,
                      boolean wasRoamingDataEnabled,
                      boolean wasAirplaneModeEnabled) {
            this.mobileDataEnabled = mobileDataEnabled;
            this.roamingDataEnabled = roamingDataEnabled;
            this.airplaneModeEnabled = airplaneModeEnabled;
            this.networkOperatorChanged = networkOperatorChanged;
            this.wasMobileDataEnabled = wasMobileDataEnabled;
            this.wasRoamingDataEnabled = wasRoamingDataEnabled;
            this.wasAirplaneModeEnabled = wasAirplaneModeEnabled;
        }


        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("Result{");
            sb.append("mobileDataEnabled=").append(mobileDataEnabled);
            sb.append(", roamingDataEnabled=").append(roamingDataEnabled);
            sb.append(", airplaneModeEnabled=").append(airplaneModeEnabled);
            sb.append(", networkOperatorChanged=").append(networkOperatorChanged);
            sb.append(", wasMobileDataEnabled=").append(wasMobileDataEnabled);
            sb.append(", wasRoamingDataEnabled=").append(wasRoamingDataEnabled);
            sb.append(", wasAirplaneModeEnabled=").append(wasAirplaneModeEnabled);
            sb.append('}');
            return sb.toString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeByte(mobileDataEnabled ? (byte) 1 : (byte) 0);
            dest.writeByte(roamingDataEnabled ? (byte) 1 : (byte) 0);
            dest.writeByte(airplaneModeEnabled ? (byte) 1 : (byte) 0);
            dest.writeByte(networkOperatorChanged ? (byte) 1 : (byte) 0);
            dest.writeByte(wasMobileDataEnabled ? (byte) 1 : (byte) 0);
            dest.writeByte(wasRoamingDataEnabled ? (byte) 1 : (byte) 0);
            dest.writeByte(wasAirplaneModeEnabled ? (byte) 1 : (byte) 0);
        }

        protected Result(Parcel in) {
            this.mobileDataEnabled = in.readByte() != 0;
            this.roamingDataEnabled = in.readByte() != 0;
            this.airplaneModeEnabled = in.readByte() != 0;
            this.networkOperatorChanged = in.readByte() != 0;
            this.wasMobileDataEnabled = in.readByte() != 0;
            this.wasRoamingDataEnabled = in.readByte() != 0;
            this.wasAirplaneModeEnabled = in.readByte() != 0;
        }

        public static final Parcelable.Creator<Result> CREATOR = new Parcelable.Creator<Result>() {
            public Result createFromParcel(Parcel source) {
                return new Result(source);
            }

            public Result[] newArray(int size) {
                return new Result[size];
            }
        };
    }

    public static final String ACTION = "com.sanda.checker.NoConnectionReceiver.ACTION";

    private static final String MOBILE = "mobile";
    private static final String ROAMING = "roaming";
    private static final String AIRPLANE = "airplane";
    private static final String WAS_MOBILE = "was_mobile";
    private static final String WAS_ROAMING = "was_roaming";
    private static final String WAS_AIRPLANE = "was_airplane";
    private static final String OPERATOR = "operator";

    public static void start(final @NonNull Context context,
                             boolean mobileDataEnabled,
                             boolean roamingDataEnabled,
                             boolean airplaneModeEnabled,
                             boolean networkOperatorChanged,
                             boolean wasMobileDataEnabled,
                             boolean wasRoamingDataEnabled,
                             boolean wasAirplaneModeEnabled) throws ClassNotFoundException {
        Intent intent = new Intent(context, Class.forName("com.sanda.truckdoc.client.receivers.CheckerConnectionReceiver"));
        intent.setAction(ACTION);
        intent.putExtra(MOBILE, mobileDataEnabled);
        intent.putExtra(ROAMING, roamingDataEnabled);
        intent.putExtra(AIRPLANE, airplaneModeEnabled);
        intent.putExtra(OPERATOR, networkOperatorChanged);
        intent.putExtra(WAS_MOBILE, wasMobileDataEnabled);
        intent.putExtra(WAS_ROAMING, wasRoamingDataEnabled);
        intent.putExtra(WAS_AIRPLANE, wasAirplaneModeEnabled);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        onCheck(context,
                new Result(intent.getBooleanExtra(MOBILE, false),
                        intent.getBooleanExtra(ROAMING, false),
                        intent.getBooleanExtra(AIRPLANE, false),
                        intent.getBooleanExtra(OPERATOR, false),
                        intent.getBooleanExtra(WAS_MOBILE, false),
                        intent.getBooleanExtra(WAS_ROAMING, false),
                        intent.getBooleanExtra(WAS_AIRPLANE, false)));
    }

    protected abstract void onCheck(@NonNull Context context, Result result);
}
