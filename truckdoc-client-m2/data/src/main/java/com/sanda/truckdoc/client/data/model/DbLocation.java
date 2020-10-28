package com.sanda.truckdoc.client.data.model;

import android.location.Location;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sanda.truckdoc.client.api.model.LocationRecord;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.Locale;

/**
 * Created by astra on 14.07.2015.
 */
@DatabaseTable(tableName = "locations")
public class DbLocation {

    @DatabaseField(generatedId = true)
    private long id;
    @DatabaseField
    private double latitude;
    @DatabaseField
    private double longitude;
    @DatabaseField
    private double altitude;
    @DatabaseField
    private float speed;
    @DatabaseField
    private float accuracy;
    @DatabaseField
    private long time;

    public DbLocation() {
    }

    public DbLocation(@NotNull Location l) {
        latitude = l.getLatitude();
        longitude = l.getLongitude();
        altitude = l.getAltitude();
        speed = l.getSpeed();
        accuracy = l.getAccuracy();
        time = l.getTime();
    }

    public DbLocation(double latitude, double longitude, double altitude, float speed, float accuracy, long time) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.speed = speed;
        this.accuracy = accuracy;
        this.time = time;
    }

    public long getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public float getSpeed() {
        return speed;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public long getTime() {
        return time;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DbLocation{");
        sb.append("id=").append(id);
        sb.append(", latitude=").append(latitude);
        sb.append(", longitude=").append(longitude);
        sb.append(", altitude=").append(altitude);
        sb.append(", speed=").append(speed);
        sb.append(", accuracy=").append(accuracy);
        sb.append(", time=").append(time);
        sb.append('}');
        return sb.toString();
    }

    public LocationRecord toLocationRecord() {
        LocationRecord l = new LocationRecord();
        l.setLat(String.format(Locale.US, "%.6f", latitude)); //привет
        l.setLng(String.format(Locale.US, "%.6f", longitude));
        l.setTime(new Date(time));
        return l;
    }
}
