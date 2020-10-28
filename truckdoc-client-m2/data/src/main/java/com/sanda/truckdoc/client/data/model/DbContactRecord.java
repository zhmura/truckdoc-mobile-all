package com.sanda.truckdoc.client.data.model;

import android.graphics.Color;

import com.github.naixx.WithId;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sanda.truckdoc.client.api.model.ContactListAttribute;
import com.sanda.truckdoc.client.api.model.ContactRecord;

@DatabaseTable(tableName = "contact_records")
public class DbContactRecord implements WithId {

    @DatabaseField(id = true)
    private Long recipientId;
    @DatabaseField
    private String label;
    @DatabaseField
    private String recipientIdType;
    @DatabaseField
    private int color;
    @DatabaseField
    private String phone;

    public DbContactRecord() {
    }

    public DbContactRecord(Long recipientId, String label, String recipientIdType, int color, String phone) {
        this.recipientId = recipientId;
        this.label = label;
        this.recipientIdType = recipientIdType;
        this.color = color;
        this.phone = phone;
    }

    public DbContactRecord(ContactRecord contactRecord) {
        recipientId = contactRecord.getRecipientId();
        label = contactRecord.getLabel();
        recipientIdType = contactRecord.getRecipientIdType();
        if (contactRecord.getAttributes() != null && contactRecord.getAttributes().containsKey(ContactListAttribute.COLOR.getKey())) {
            color = Color.parseColor(contactRecord.getAttributes().get(ContactListAttribute.COLOR.getKey()));
        } else {
            color = Color.WHITE;
        }
    }

    public Long getRecipientId() {
        return recipientId;
    }

    @Override
    public long getId() {
        return recipientId;
    }

    public String getLabel() {
        return label;
    }

    public String getRecipientIdType() {
        return recipientIdType;
    }

    public int getColor() {
        return color;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
