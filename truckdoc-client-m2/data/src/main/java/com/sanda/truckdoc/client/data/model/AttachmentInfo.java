package com.sanda.truckdoc.client.data.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sanda.truckdoc.client.api.AttachmentPojo;
import com.sanda.truckdoc.util.entity.EntityWithId;

/**
 * @author Alexei Osipov
 */
@DatabaseTable(tableName = "attachment_info")
public class AttachmentInfo implements EntityWithId {

    @DatabaseField(generatedId = true)
    private Integer id;
    @DatabaseField
    private Integer serverId;
    @DatabaseField
    private Long fileSize;
    @DatabaseField
    private String fileName;
    @DatabaseField
    private String mimeType;
    @DatabaseField
    private boolean downloaded = false;

    @DatabaseField(canBeNull = false, foreign = true)
    private ServerMessage message;

    @SuppressWarnings("UnusedDeclaration")
    public AttachmentInfo() {
        // ORMLite needs a no-arg constructor
    }

    public AttachmentInfo(AttachmentPojo pojo) {
        id = pojo.getId();
        serverId = pojo.getId();
        fileSize = pojo.getFileSize();
        fileName = pojo.getFileName();
        mimeType = pojo.getMimeType();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public ServerMessage getMessage() {
        return message;
    }

    public void setMessage(ServerMessage message) {
        this.message = message;
    }

    public Integer getServerId() {
        return serverId;
    }

    public void setServerId(Integer serverId) {
        this.serverId = serverId;
    }
}
