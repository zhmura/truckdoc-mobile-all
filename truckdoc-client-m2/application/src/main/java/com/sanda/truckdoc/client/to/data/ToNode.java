package com.sanda.truckdoc.client.to.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sanda.truckdoc.client.api.v3.sync.checklist.model.ChecklistResultNode;

import java.io.Serializable;

import androidx.annotation.NonNull;

/**
 * Created by k.natallie on 19.03.2016.
 */
public class ToNode extends ChecklistResultNode implements Serializable {
    private ToNode parent;
    private boolean isLastChild;
    private String icon;

    @JsonIgnore
    public ToNode getParent() {
        return parent;
    }

    @JsonIgnore
    public void setParent(ToNode parent) {
        this.parent = parent;
    }

    @JsonIgnore
    public String getTitleText() {
        ToNode parent = getTopParent(this);
        String result = parent.getName();

        if (!result.equals(getName()) && getParent() != null) {
            if ((result).equals(getParent().getName())) {
                return result + " > " + getName();
            }
            return result + "> ... >" + getName();
        }

        return getName();
    }

    /**
     * returnt parent of item if available, or returns itself if no parent
     *
     * @param item
     * @return
     */
    private ToNode getTopParent(@NonNull ToNode item) {
        if (item.getParent() == null) {
            return item;

        }
        return getTopParent(item.getParent());
    }

    @JsonIgnore
    public boolean isLastChild() {
        return isLastChild;
    }

    @JsonIgnore
    public void setLastChild(boolean lastChild) {
        isLastChild = lastChild;
    }

    @JsonIgnore
    public String getIcon() {
        return icon;
    }

    @JsonIgnore
    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public String toString() {
        return "ToNode{" +
                "parent=" + parent +
                "attachedFiles" + getAttachedFiles() +
                ", isLastChild=" + isLastChild +
                ", icon='" + icon + '\'' +
                '}';
    }
}
