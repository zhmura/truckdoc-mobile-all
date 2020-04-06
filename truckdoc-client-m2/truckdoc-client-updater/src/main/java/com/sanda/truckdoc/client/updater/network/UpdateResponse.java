package com.sanda.truckdoc.client.updater.network;

import com.google.gson.annotations.SerializedName;

/**
 * Created by astra on 05.07.2015.
 */
public class UpdateResponse {

    @SerializedName("updateAvailable")
    public boolean updateAvailable;

    @SerializedName("updateUrl")
    public String url;

    @SerializedName("updateVersionCode")
    public int versionCode;
}
