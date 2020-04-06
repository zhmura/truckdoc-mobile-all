package app.camera.tdoc.camera_library;

import java.io.Serializable;

public class PrefixList implements Serializable {
    private String label;
    private String prefix;

    public PrefixList(String label, String prefix) {
        this.label = label;
        this.prefix = prefix;
    }

    public String getLabel() {
        return label;
    }

    public boolean isEmptyLabel() {
        return label.equals("");
    }

    public String getPrefix() {
        return prefix;
    }
}
