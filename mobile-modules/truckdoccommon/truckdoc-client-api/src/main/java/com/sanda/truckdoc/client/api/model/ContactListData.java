package com.sanda.truckdoc.client.api.model;

import java.util.List;

/**
 * @author Alexei Osipov
 */
public class ContactListData {
    /**
     * List of all contact records defined for this client.
     */
    private List<ContactRecord> contactRecords;

    /**
     * Current contact list version.
     */
    private Long contactListVersion;

    public List<ContactRecord> getContactRecords() {
        return contactRecords;
    }

    public void setContactRecords(List<ContactRecord> contactRecords) {
        this.contactRecords = contactRecords;
    }

    public Long getContactListVersion() {
        return contactListVersion;
    }

    public void setContactListVersion(Long contactListVersion) {
        this.contactListVersion = contactListVersion;
    }
}
