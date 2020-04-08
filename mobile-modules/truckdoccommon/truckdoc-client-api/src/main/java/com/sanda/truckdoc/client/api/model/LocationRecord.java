package com.sanda.truckdoc.client.api.model;

import java.util.Date;

/**
 * @author Alexei Osipov
 */
public class LocationRecord {
    private Date time;
    private String lng;
    private String lat;

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    @Override
    public String toString() {
        return "LocationRecord{" + "time=" + time +
                ", lng='" + lng + '\'' +
                ", lat='" + lat + '\'' +
                '}';
    }
}
