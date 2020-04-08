package com.sanda.truckdoc.client.api;

/**
 * @author Alexei Osipov
 */
@Deprecated
public enum RecipientType {

    OWN_LEADERS(1),
    OWN_EXPEDITORS_AND_LEADERS(2),
    OWN_MECHANICS_AND_LEADERS(3),
    OWN_EPI(4);

    public static final int MAX_PREDEFINED_ID = 1000 - 1; // 1000 and above will be handles using new logic

    private int recipientTypeId;

    RecipientType(int recipientTypeId) {
        this.recipientTypeId = recipientTypeId;
    }

    public int getRecipientTypeId() {
        return recipientTypeId;
    }
}
