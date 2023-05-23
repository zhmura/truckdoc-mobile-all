package com.sanda.truckdoc.client.to.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by k.natallie on 03.02.2016.
 */
@JsonIgnoreProperties(value = { "parent", "id"})
public class ToSubItem implements Serializable {
    private String id;
    private TOState value;
    private String name;
    private List<ToSubItem> children;
    private ToSubItem parent;
    private String comment;


    public ToSubItem(TOState toState, @NonNull String name, @Nullable List<ToSubItem> children, ToSubItem parent) {
        id = UUID.randomUUID().toString();
        this.value = toState;
        this.name = name;
        this.children = children;
        this.parent = parent;
    }

    public ToSubItem(String name, List<ToSubItem> subparts, ToSubItem parent) {
        id = UUID.randomUUID().toString();
        this.value = TOState.NOT_CHECKED;
        this.name = name;
        this.children = subparts;
        this.parent = parent;
    }

    public ToSubItem(String id, String name, List<ToSubItem> subparts, ToSubItem parent) {
        this.id = id;
        this.value = TOState.NOT_CHECKED;
        this.name = name;
        this.children = subparts;
        this.parent = parent;
    }

    public TOState getValue() {
        return value;
    }

    public void setValue(TOState value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ToSubItem> getChildren() {
        return children;
    }

    public void setChildren(List<ToSubItem> children) {
        this.children = children;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ToSubItem toSubItem = (ToSubItem) o;

        return !(id != null ? !id.equals(toSubItem.id) : toSubItem.id != null);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public ToSubItem getParent() {
        return parent;
    }

    /**
     * returnt parent of item if available, or returns itself if no parent
     *
     * @param item
     * @return
     */
    private ToSubItem getTopParent(@NonNull ToSubItem item) {
        if (item.getParent() == null) {
            return item;

        }
        return getTopParent(item.getParent());
    }


    public void setParent(ToSubItem parent) {
        this.parent = parent;
    }

    @JsonIgnore
    public String getTitleText() {
        ToSubItem parent = getTopParent(this);
        String result = parent.getName();

     /*   if (getParent() == null) {
            return null;
        }
*/
        if (!result.equals(getName()) && getParent() != null) {
            if ((result).equals(getParent().getName())) {
                return result + " > " + getName();
            }
            return result + "> ... >" + getName();
        }

        return getName();
    }

    @JsonIgnore
    public String getFullTitleText() {
        boolean isTop = false;

        ToSubItem item = getParent();
        if (item == null) {
            return getName();
        }
        String title = getName();
        while (!isTop) {
            title = item.getName() + ">" + title;
            item = item.getParent();
            if (item == null) {
                isTop = true;
            }
        }
        return title;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
