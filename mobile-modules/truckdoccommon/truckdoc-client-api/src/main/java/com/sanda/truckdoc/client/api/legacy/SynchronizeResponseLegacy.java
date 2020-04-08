package com.sanda.truckdoc.client.api.legacy;

import com.sanda.truckdoc.client.api.SynchronizeRequest;
import com.sanda.truckdoc.client.api.base.SynchronizeResponseBase;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

/**
 * Synchronization operation response from server. The set of fields depends on {@link SynchronizeRequest#dataToGet}.
 *
 * @author Alexei Osipov
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SynchronizeResponseLegacy implements SynchronizeResponseBase {
    /**
     * New messages for client. May be null if messages are not requested.
     */
    private List<ServerToClientMessagePojoLegacy> messagesForClient;


    public List<ServerToClientMessagePojoLegacy> getMessagesForClient() {
        return messagesForClient;
    }

    public void setMessagesForClient(List<ServerToClientMessagePojoLegacy> messagesForClient) {
        this.messagesForClient = messagesForClient;
    }

    @Override
    public String toString() {
        return "SynchronizeResponse{" +
                "messagesForClient=" + messagesForClient +
                '}';
    }
}
