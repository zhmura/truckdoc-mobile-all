package com.sanda.truckdoc.client.data.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.sanda.truckdoc.client.api.AttachmentPojo;
import com.sanda.truckdoc.client.api.ClientToServerMessagePojo;
import com.sanda.truckdoc.client.api.v2.ServerToClientMessagePojoNew;
import com.sanda.truckdoc.util.entity.EntityWithId;

import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Alexei Osipov
 */
@DatabaseTable(tableName = "server_message")
public class ServerMessage implements EntityWithId {

    @DatabaseField(generatedId = true) //
    private Integer id;
    @DatabaseField
    private Integer recipientId;
    @DatabaseField
    private Integer serverMessageId;
    @DatabaseField
    private Integer senderRoleId;
    @DatabaseField
    private Long senderUserId;
    @DatabaseField
    private Long senderVirtualGroupId;
    @DatabaseField
    private String senderName;
    @DatabaseField
    private String text;
    @DatabaseField
    private boolean downloaded;
    @DatabaseField
    private boolean outgoing;
    @DatabaseField
    private boolean sent;
    @DatabaseField
    private boolean processed;
    @DatabaseField
    private boolean hidden;
    @DatabaseField
    private String saved;
    @DatabaseField(dataType = DataType.DATE_TIME) //
    private DateTime savedDate;
    @ForeignCollectionField(eager = true)//
    private ForeignCollection<AttachmentInfo> attachments;

    @SuppressWarnings("UnusedDeclaration")
    public ServerMessage() {
        // ORMLite needs a no-arg constructor
    }

    public ServerMessage(ServerToClientMessagePojoNew pojo) {
        this.id = pojo.getId();
        this.text = pojo.getText();
        this.serverMessageId = pojo.getId();
        this.senderRoleId = pojo.getSenderRoleId();
        this.senderUserId = pojo.getSenderUserId();
        this.senderVirtualGroupId = pojo.getSenderVirtualGroupId();
        this.senderName = pojo.getSenderName();

        Collection<AttachmentPojo> pojoAttachments = pojo.getAttachments();
        for (AttachmentPojo attachmentPojo : pojoAttachments) {
            AttachmentInfo attachmentInfo = new AttachmentInfo(attachmentPojo);
            attachmentInfo.setMessage(this);
        }
    }

    public ServerMessage(ClientToServerMessagePojo pojo) {
        this.id = pojo.getId();
        this.text = pojo.getText();
        this.sent = pojo.isSent();
        this.recipientId = pojo.getRecipientId();
        Collection<AttachmentPojo> pojoAttachments = pojo.getAttachments();
        for (AttachmentPojo attachmentPojo : pojoAttachments) {
            AttachmentInfo attachmentInfo = new AttachmentInfo(attachmentPojo);
            attachmentInfo.setMessage(this);
        }
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

    public boolean isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public Collection<AttachmentInfo> getAttachments() {
        if (attachments == null) {
            return Collections.emptyList();
        }
        return attachments;
    }

    public void setAttachments(ForeignCollection<AttachmentInfo> attachments) {
        this.attachments = attachments;
    }

    @Deprecated
    public String getSaved() {
        return saved;
    }

    public boolean isOutgoing() {
        return outgoing;
    }

    public void setOutgoing(boolean outgoing) {
        this.outgoing = outgoing;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
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

    public Integer getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Integer recipientId) {
        this.recipientId = recipientId;
    }

    public Integer getServerMessageId() {
        return serverMessageId;
    }

    public void setServerMessageId(Integer serverMessageId) {
        this.serverMessageId = serverMessageId;
    }

    public Integer getSenderRoleId() {
        return senderRoleId;
    }

    public void setSenderRoleId(Integer senderRoleId) {
        this.senderRoleId = senderRoleId;
    }

    public DateTime getSavedDate() {
        return savedDate;
    }

    public void setSavedDate(DateTime savedDate) {
        this.savedDate = savedDate;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
