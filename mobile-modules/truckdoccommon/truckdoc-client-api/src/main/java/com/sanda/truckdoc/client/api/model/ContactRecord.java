package com.sanda.truckdoc.client.api.model;

import java.util.Map;

/**
 * @author Alexei Osipov
 */
public class ContactRecord {

    /**
     * Label for contact.
     */
    private String label; // non null

    /**
     * Recipient Id that should be passed to server when a message is sent to this contact.
     */
    private Long recipientId; // non null

    /**
     * Recipient Id Type that should be passed to server when a message is sent to this contact.
     */
    private String recipientIdType; // non null

    /**
     * Additional contact attributes. Usually client-specific. Possible keys are defined in {@link ContactListAttribute}.
     */
    private Map<String, String> attributes;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public String getRecipientIdType() {
        return recipientIdType;
    }

    public void setRecipientIdType(String recipientIdType) {
        this.recipientIdType = recipientIdType;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return "ContactRecord{" +
               "label='" + label + '\'' +
               ", recipientId=" + recipientId +
               ", recipientIdType='" + recipientIdType + '\'' +
               ", attributes=" + attributes +
               '}';
    }
}
