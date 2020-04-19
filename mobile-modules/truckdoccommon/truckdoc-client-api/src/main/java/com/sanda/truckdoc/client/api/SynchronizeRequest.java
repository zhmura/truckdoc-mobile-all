package com.sanda.truckdoc.client.api;


import com.sanda.truckdoc.client.api.model.LocationRecord;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;


/**
 * @author Alexei Osipov
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SynchronizeRequest {
    public static final String GET_NEW_MESSAGES = "nmes"; // Request server for new messages
    public static final String GET_CONTACT_LIST_UPDATES = "clu"; // Request server for contact list updates
    public static final String GET_ROUTE_ASSIGNMENT = "ra";
    public static final String GET_MAINTENANCE_CONFIG = "mnt";
    public static final String GET_CLIENT_CONFIG = "ccfg";
    public static final String GET_INSTRUCTION_SET = "ins";

    /**
     * List of data slices that client wants to get from server.
     * Possible elements in list: "nmes", "clu"
     */
    private List<String> dataToGet; // non null

    /**
     * Time when the client initiated the request. In case of request re-attempts this value should remain same.
     */
    private Date currentClientTime; // non null

    private Long knownContactListVersion; // nullable - null means that client never updated contact list yet or does not want to update contact

    private List<LocationRecord> locationHistory; // nullable - list with recorded geo coordinates, null means that there is no data to send

    private Long lastKnownRouteAssignment; // nullable - null means that client never received route assignment or does not want to update it

    private Long lastKnownMaintenanceConfigVersion; // nullable - null means that client never received maintenance config or does not want to update it

    private Long lastKnownClientConfigVersion; // nullable

    private Long lastKnownInstructionSetVersion; // nullable

    public List<String> getDataToGet() {
        return dataToGet;
    }

    public void setDataToGet(@NotNull List<String> dataToGet) {
        this.dataToGet = dataToGet;
    }

    public Date getCurrentClientTime() {
        return currentClientTime;
    }

    public void setCurrentClientTime(@NotNull Date currentClientTime) {
        this.currentClientTime = currentClientTime;
    }

    public List<LocationRecord> getLocationHistory() {
        return locationHistory;
    }

    public void setLocationHistory(List<LocationRecord> locationHistory) {
        this.locationHistory = locationHistory;
    }

    @JsonIgnore //TODO
    public Long getKnownContactListVersion() {
        return knownContactListVersion;
    }

    public void setKnownContactListVersion(Long knownContactListVersion) {
        this.knownContactListVersion = knownContactListVersion;
    }

    public Long getLastKnownRouteAssignment() {
        return lastKnownRouteAssignment;
    }

    public void setLastKnownRouteAssignment(Long lastKnownRouteAssignment) {
        this.lastKnownRouteAssignment = lastKnownRouteAssignment;
    }

    public Long getLastKnownMaintenanceConfigVersion() {
        return lastKnownMaintenanceConfigVersion;
    }

    public void setLastKnownMaintenanceConfigVersion(Long lastKnownMaintenanceConfigVersion) {
        this.lastKnownMaintenanceConfigVersion = lastKnownMaintenanceConfigVersion;
    }

    public Long getLastKnownClientConfigVersion() {
        return lastKnownClientConfigVersion;
    }

    public void setLastKnownClientConfigVersion(Long lastKnownClientConfigVersion) {
        this.lastKnownClientConfigVersion = lastKnownClientConfigVersion;
    }

    @Override
    public String toString() {
        return "SynchronizeRequest{" +
                "dataToGet=" + dataToGet +
                ", currentClientTime=" + currentClientTime +
                ", knownContactListVersion=" + knownContactListVersion +
                ", locationHistory=" + locationHistory +
                ", lastKnownRouteAssignment=" + lastKnownRouteAssignment +
                ", lastKnownMaintenanceConfigVersion=" + lastKnownMaintenanceConfigVersion +
                ", lastKnownClientConfigVersion=" + lastKnownClientConfigVersion +
                '}';
    }
}
