package com.sanda.truckdoc.client.api.model;

/**
 * @author Alexei Osipov
 */
public enum ContactListAttribute {
    COLOR("color");

    private final String key;

    ContactListAttribute(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
