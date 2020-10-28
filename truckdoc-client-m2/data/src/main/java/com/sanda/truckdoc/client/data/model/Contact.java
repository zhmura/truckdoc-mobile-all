package com.sanda.truckdoc.client.data.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sanda.truckdoc.client.api.MessageRecipientPojo;
import com.sanda.truckdoc.util.entity.EntityWithId;

/**
 * User: SBTPC
 * Date: 9.4.13
 * Time: 18.09
 */
@DatabaseTable(tableName = "contacts")
@Deprecated() //unused, but can exist in older databases
public class Contact implements EntityWithId {

    @DatabaseField(id = true)
    private Integer id;
    @DatabaseField
    private String name;
    @DatabaseField
    private Integer version;

    @SuppressWarnings("UnusedDeclaration")
    public Contact() {
        // ORMLite needs a no-arg constructor
    }

    public Contact(MessageRecipientPojo recipientPojo, int version) {
        this.id = recipientPojo.getId();
        this.name = recipientPojo.getName();
        this.version = version;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
