package com.sanda.truckdoc.client.adapters;

import com.sanda.truckdoc.client.api.SynchronizeRequest;
import com.sanda.truckdoc.client.api.model.LocationRecord;

import net.tribe7.common.collect.Lists;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * Created by astra on 10.07.2015.
 */
public class SynchronizeRequestAdapter {

    private static final ArrayList<String> MESSAGES = Lists.newArrayList(SynchronizeRequest.GET_NEW_MESSAGES);
    private static final ArrayList<String> MESSAGES_AND_UPDATES = Lists.newArrayList(SynchronizeRequest.GET_NEW_MESSAGES,
            SynchronizeRequest.GET_CONTACT_LIST_UPDATES,
            SynchronizeRequest.GET_CLIENT_CONFIG,
            SynchronizeRequest.GET_ROUTE_ASSIGNMENT);

    public static SynchronizeRequest withNewMessagesAndUpdates(List<LocationRecord> locationHistory,
                                                               Long knownContactListVersion,
                                                               Long lastKnownRouteAssignment,
                                                               Long lastKnownMaintenanceConfigVersion) {
        SynchronizeRequest r = createRequest(locationHistory, MESSAGES_AND_UPDATES);
        if (knownContactListVersion >= 0) {
            r.setKnownContactListVersion(knownContactListVersion);
        }
        if (lastKnownRouteAssignment >= 0) {
            r.setLastKnownRouteAssignment(lastKnownRouteAssignment);
        }
        if (lastKnownMaintenanceConfigVersion >= 0) {
            r.setLastKnownMaintenanceConfigVersion(lastKnownMaintenanceConfigVersion);
        }
        return r;
    }

    @NonNull
    private static SynchronizeRequest createRequest(List<LocationRecord> locationHistory,

                                                    ArrayList<String> dataToGet) {
        SynchronizeRequest r = new SynchronizeRequest();
        r.setDataToGet(dataToGet);
        r.setCurrentClientTime(DateTime.now().toDate());
        r.setLocationHistory(locationHistory);
        return r;
    }

    public static SynchronizeRequest autoCheckClientInfoUpdates(List<LocationRecord> locationHistory) {
        SynchronizeRequest r = new SynchronizeRequest();
        r.setDataToGet(Lists.newArrayList(SynchronizeRequest.GET_NEW_MESSAGES,
                SynchronizeRequest.GET_ROUTE_ASSIGNMENT,
                SynchronizeRequest.GET_CONTACT_LIST_UPDATES,
                SynchronizeRequest.GET_CLIENT_CONFIG));
        r.setCurrentClientTime(DateTime.now().toDate());
        r.setLocationHistory(locationHistory);
        return r;
    }

    public static SynchronizeRequest synchronizeOnly(List<LocationRecord> locationHistory) {
        SynchronizeRequest r = new SynchronizeRequest();
        r.setCurrentClientTime(DateTime.now().toDate());
        r.setLocationHistory(locationHistory);
        return r;
    }

    public static SynchronizeRequest synchronizeMaintenance(List<LocationRecord> locationHistory) {
        SynchronizeRequest r = new SynchronizeRequest();
        r.setDataToGet(Lists.newArrayList(SynchronizeRequest.GET_MAINTENANCE_CONFIG));
        r.setCurrentClientTime(DateTime.now().toDate());
        r.setLocationHistory(locationHistory);
        return r;
    }
}
