package com.sanda.truckdoc.client.data.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sanda.truckdoc.client.data.model.file.FileType;

import org.joda.time.DateTime;

import java.io.File;

import androidx.annotation.Nullable;

@DatabaseTable(tableName = "message_files")
public class MessageFileRecord {
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField
    private String name;
    @DatabaseField
    private String path;
    @DatabaseField
    private String type;
    @DatabaseField
    private String metadata;
    @DatabaseField
    private Long serverId;
    @DatabaseField
    private Long recipientId;
    @DatabaseField
    private boolean sent;
    @DatabaseField
    private boolean markForSend;

    @DatabaseField(dataType = DataType.DATE_TIME) //
    private DateTime creationTime = DateTime.now();

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getType() {
        return type;
    }

    public String getMetadata() {
        return metadata;
    }

    @Nullable
    public Long getServerId() {
        return serverId;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public DateTime getCreationTime() {
        return creationTime;
    }

    public void setPath(String path) {
        this.path = path;
        this.name = new File(path).getName();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(FileType type) {
        this.type = type.name();
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public void setServerId(Long serverId) {
        this.serverId = serverId;
    }

    @Override
    public String toString() {
        return "MessageFileRecord{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", type='" + type + '\'' +
                ", metadata='" + metadata + '\'' +
                ", serverId=" + serverId +
                ", recipientId=" + recipientId +
                ", creationTime=" + creationTime +
                '}';
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public boolean isReadyForSend() {
        return markForSend;
    }

    public void setReadyForSend(boolean markForSend) {
        this.markForSend = markForSend;
    }
}
