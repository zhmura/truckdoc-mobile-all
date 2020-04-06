package com.sanda.truckdoc.client.to.data;

/**
 * Created by k.natallie on 27.03.2016.
 */
public class TrailerType {
    String id;
    String name;


    public TrailerType(String name, String id) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
