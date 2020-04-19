package com.sanda.truckdoc.client.api;

import com.sanda.truckdoc.util.entity.EntityWithId;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * @author Alexei Osipov
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AttachmentPojo implements EntityWithId<Integer> {
    private Integer id;
    private Long fileSize;
    private String fileName;
    private String mimeType;

    public AttachmentPojo() {
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}