package com.sanda.truckdoc.client.api.v2;

import com.sanda.truckdoc.client.api.legacy.ServerToClientMessagePojoLegacy;
import com.sanda.truckdoc.client.api.v3.sync.message.model.MessageAttributes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Alexei Osipov
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerToClientMessagePojoNew extends ServerToClientMessagePojoLegacy {
    private Long sentTimestamp;
    private Long senderUserId;
    private Long senderVirtualGroupId;
    private String senderName;
    private MessageAttributes messageAttributes;

    public Long getSentTimestamp() {
        return sentTimestamp;
    }

    public void setSentTimestamp(Long sentTimestamp) {
        this.sentTimestamp = sentTimestamp;
    }

    public Long getSenderUserId() {
        return senderUserId;
    }

    public void setSenderUserId(Long senderUserId) {
        this.senderUserId = senderUserId;
    }

    public Long getSenderVirtualGroupId() {
        return senderVirtualGroupId;
    }

    public void setSenderVirtualGroupId(Long senderVirtualGroupId) {
        this.senderVirtualGroupId = senderVirtualGroupId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public MessageAttributes getMessageAttributes() {
        return messageAttributes;
    }

    public void setMessageAttributes(MessageAttributes messageAttributes) {
        this.messageAttributes = messageAttributes;
    }

    @Override
    public String toString() {
        return "ServerToClientMessagePojoNew{" +
                "sentTimestamp=" + sentTimestamp +
                ", senderUserId=" + senderUserId +
                ", senderVirtualGroupId=" + senderVirtualGroupId +
                ", senderName='" + senderName + '\'' +
                ", messageAttributes=" + messageAttributes +
                "} " + super.toString();
    }
}
