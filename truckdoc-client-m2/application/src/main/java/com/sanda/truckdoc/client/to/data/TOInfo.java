package com.sanda.truckdoc.client.to.data;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by k.natallie on 01.02.2016.
 */
public class TOInfo implements Serializable {
    public enum TOItemTypes {
        NONE, TRACK, TRAILER
    }

    private List<ToSubItem> items;


    public List<ToSubItem> getItems() {
        return items;
    }

    public void setItems(List<ToSubItem> items) {
        this.items = items;
    }

    public void addItemType(ToSubItem subItem) {
        if (items == null) {
            items = new LinkedList<>();
        }

        items.add(subItem);
    }

}
