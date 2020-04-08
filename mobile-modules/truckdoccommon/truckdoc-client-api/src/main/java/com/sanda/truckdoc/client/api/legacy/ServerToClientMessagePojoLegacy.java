package com.sanda.truckdoc.client.api.legacy;

import com.sanda.truckdoc.client.api.AttachmentPojo;
import com.sanda.truckdoc.util.entity.EntityWithId;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.Collection;

/**
 * @author Alexei Osipov
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerToClientMessagePojoLegacy implements EntityWithId<Integer> {

    private Integer id;
    private Integer senderRoleId;
    private String  text;

    private Collection<AttachmentPojo> attachments;

    public ServerToClientMessagePojoLegacy() {
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public Collection<AttachmentPojo> getAttachments() {
        return attachments;
    }

    public void setAttachments(Collection<AttachmentPojo> attachments) {
        this.attachments = attachments;
    }

    @Deprecated
    public Integer getSenderRoleId() {
        return senderRoleId;
    }

    @Deprecated
    public void setSenderRoleId(Integer senderRoleId) {
        this.senderRoleId = senderRoleId;
    }

    @Override
    public String toString() {
        return "ServerToClientMessagePojo{" + "id=" + id +
                ", senderRoleId=" + senderRoleId +
                ", text='" + text + '\'' +
                ", attachments=" + attachments +
                '}';
    }
}
