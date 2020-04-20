package com.sanda.truckdoc.network.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sanda.truckdoc.client.api.SynchronizeRequest;


/**
 * Synchronization operation response from server. The set of fields depends on {@link SynchronizeRequest#dataToGet}.
 *
 * @author Alexei Osipov
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SynchronizeCheckResponse {
    /**
     * Flag indicating if there are new messages for client. May be 1 or 0 if messages are not requested.
     */
    private Number hasNewMessages;

    public SynchronizeCheckResponse(Integer hasNewMessages) {
        this.hasNewMessages = hasNewMessages;
    }

    public Number getHasNewMessages() {
        return hasNewMessages;
    }

    public void setHasNewMessages(Number hasNewMessages) {
        this.hasNewMessages = hasNewMessages;
    }

    @Override
    public String toString() {
        return "SynchronizeCheckResponse{" +
                "hasNewMessagesForClient=" + hasNewMessages +
                '}';
    }
}
