package com.sanda.truckdoc.client.api;

import com.sanda.truckdoc.util.entity.EntityWithId;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.Collection;

/**
 * @author Alexei Osipov
 */
@Deprecated
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContactListPojo implements EntityWithId<Integer> {
    private Integer id;
    private Collection<MessageRecipientPojo> contacts;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Collection<MessageRecipientPojo> getContacts() {
        return contacts;
    }

    public void setContacts(Collection<MessageRecipientPojo> contacts) {
        this.contacts = contacts;
    }
}