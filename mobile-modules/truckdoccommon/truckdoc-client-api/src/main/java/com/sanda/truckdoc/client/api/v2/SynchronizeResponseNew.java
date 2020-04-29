package com.sanda.truckdoc.client.api.v2;

import com.sanda.truckdoc.client.api.base.SynchronizeResponseBase;
import com.sanda.truckdoc.client.api.model.ContactListData;
import com.sanda.truckdoc.client.api.v3.sync.client.config.model.ClientConfigWithVersion;
import com.sanda.truckdoc.client.api.v3.sync.instructions.model.InstructionSetWithVersion;
import com.sanda.truckdoc.client.api.v3.sync.maintenance.model.MaintenanceConfigInfo;
import com.sanda.truckdoc.client.api.v3.sync.routing.model.RouteAssignmentInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * @author Alexei Osipov
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SynchronizeResponseNew implements SynchronizeResponseBase {
    private Long serverTimestamp;

    private List<ServerToClientMessagePojoNew> messagesForClient;

    /**
     * Updated contact list. Will be null if client not requested contact list update.
     * Will be nul if client requested contact list update but there is no changes in contact list.
     */
    private ContactListData contactList;

    /**
     * Contains information on current route assignment if it does not matches value from client.
     */
    private RouteAssignmentInfo routeAssignmentInfo;

    private MaintenanceConfigInfo maintenanceConfigInfo;

    private ClientConfigWithVersion clientConfigWithVersion;

    private InstructionSetWithVersion instructionSetWithVersion;


    /*Getter and setter*/

    public Long getServerTimestamp() {
        return serverTimestamp;
    }

    public void setServerTimestamp(Long serverTimestamp) {
        this.serverTimestamp = serverTimestamp;
    }

    public List<ServerToClientMessagePojoNew> getMessagesForClient() {
        return messagesForClient;
    }

    public void setMessagesForClient(List<ServerToClientMessagePojoNew> messagesForClient) {
        this.messagesForClient = messagesForClient;
    }

    public ContactListData getContactList() {
        return contactList;
    }

    public void setContactList(ContactListData contactList) {
        this.contactList = contactList;
    }

    public RouteAssignmentInfo getRouteAssignmentInfo() {
        return routeAssignmentInfo;
    }

    public void setRouteAssignmentInfo(RouteAssignmentInfo routeAssignmentInfo) {
        this.routeAssignmentInfo = routeAssignmentInfo;
    }

    public MaintenanceConfigInfo getMaintenanceConfigInfo() {
        return maintenanceConfigInfo;
    }

    public void setMaintenanceConfigInfo(MaintenanceConfigInfo maintenanceConfigInfo) {
        this.maintenanceConfigInfo = maintenanceConfigInfo;
    }

    public ClientConfigWithVersion getClientConfigWithVersion() {
        return clientConfigWithVersion;
    }

    public void setClientConfigWithVersion(ClientConfigWithVersion clientConfigWithVersion) {
        this.clientConfigWithVersion = clientConfigWithVersion;
    }

    public InstructionSetWithVersion getInstructionSetWithVersion() {
        return instructionSetWithVersion;
    }

    public void setInstructionSetWithVersion(InstructionSetWithVersion instructionSetWithVersion) {
        this.instructionSetWithVersion = instructionSetWithVersion;
    }

    @Override
    public String toString() {
        return "SynchronizeResponseNew{" +
                "serverTimestamp=" + serverTimestamp +
                ", messagesForClient=" + messagesForClient +
                ", contactList=" + contactList +
                ", routeAssignmentInfo=" + routeAssignmentInfo +
                ", maintenanceConfigInfo=" + maintenanceConfigInfo +
                ", clientConfigWithVersion=" + clientConfigWithVersion +
                ", instructionSetWithVersion=" + instructionSetWithVersion +
                '}';
    }
}
