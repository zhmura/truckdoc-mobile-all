package com.sanda.truckdoc.client.api;

import com.sanda.truckdoc.util.entity.EntityWithId;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * @author Alexei Osipov
 */
@Deprecated
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageRecipientPojo implements EntityWithId<Integer> {
    private Integer id;
    private String name;

    public MessageRecipientPojo() {
    }

    public MessageRecipientPojo(int id, String name) {
        this.id = id;
        this.name = name;
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
}