package com.sanda.truckdoc.client.api;

import com.sanda.truckdoc.util.entity.EntityWithId;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.Collection;

/**
 * @author Alexei Osipov
 */
@Deprecated
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientToServerMessagePojo implements EntityWithId<Integer> {
    private Integer id;
    private String text;
    private boolean sent;
    private Integer recipientId;
    private Integer recipientRoleId;
    private Collection<AttachmentPojo> attachments;

    public ClientToServerMessagePojo() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public Collection<AttachmentPojo> getAttachments() {
        return attachments;
    }

    public void setAttachments(Collection<AttachmentPojo> attachments) {
        this.attachments = attachments;
    }

    public Integer getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Integer recipientId) {
        this.recipientId = recipientId;
    }

    public Integer getRecipientRoleId() {
        return recipientRoleId;
    }

    public void setRecipientRoleId(Integer recipientRoleId) {
        this.recipientRoleId = recipientRoleId;
    }
}
