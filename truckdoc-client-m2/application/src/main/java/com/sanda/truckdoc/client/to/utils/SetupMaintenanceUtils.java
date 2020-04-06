package com.sanda.truckdoc.client.to.utils;

import android.content.Context;

import com.sanda.truckdoc.client.to.data.Model;

/**
 * Created by k.natallie on 10.12.2016.
 */

public class SetupMaintenanceUtils {

    public static void restoreMaintenance(Context context) {
        LocalStorage storage = LocalStorage.getInstance(context);
        String typeId = storage.readStringPreference(LocalStorage.TENT_TYPE_ID);
        String toJson = storage.readStringPreference(LocalStorage.TO_PROGRESS, null);
        Model model = Model.getInstance(context);
        if (model.getResult() == null && toJson != null) {
            model.setupMaintenance(typeId);
        }
    }


}
