package com.sanda.truckdoc.client.to.data.db;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.DateTime;

import java.io.File;

/**
 * Created by k.natallie on 06.08.2016.
 */
@DatabaseTable(tableName = "mnt_files")
public class MaintenanceFileRecord {
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
    private boolean sent;
    @DatabaseField
    private boolean markForSend;
    @DatabaseField
    private String nodeName;

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

    public Long getServerId() {
        return serverId;
    }

    public boolean isSent() {
        return sent;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public DateTime getCreationTime() {
        return creationTime;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
        this.name = new File(path).getName();
    }


    public void setType(String type) {
        this.type = type;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public void setServerId(Long serverId) {
        this.serverId = serverId;
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

    public void setCreationTime(DateTime creationTime) {
        this.creationTime = creationTime;
    }

    @Override
    public String toString() {
        return "MaintenanceFileRecord{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", type='" + type + '\'' +
                ", metadata='" + metadata + '\'' +
                ", serverId=" + serverId +
                ", sent=" + sent +
                ", markForSend=" + markForSend +
                ", creationTime=" + creationTime +
                '}';
    }
}
