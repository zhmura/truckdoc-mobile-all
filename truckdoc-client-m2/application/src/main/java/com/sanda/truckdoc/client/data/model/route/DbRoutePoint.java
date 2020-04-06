package com.sanda.truckdoc.client.data.model.route;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sanda.truckdoc.client.api.v3.sync.routing.model.NamedPoint;

import org.jetbrains.annotations.NotNull;

/**
 *
 */
@DatabaseTable(tableName = "route_points")
public class DbRoutePoint {

    @DatabaseField(generatedId = true)
    private long id;
    @DatabaseField
    private double latitude;
    @DatabaseField
    private double longitude;
    @DatabaseField
    private String name;
    @DatabaseField
    private String serverId;


    @DatabaseField(canBeNull = false, foreign = true)
    private DbRoutePath path;

    public DbRoutePoint() {
    }

    public DbRoutePoint(@NotNull NamedPoint namedPoint) {
        latitude = namedPoint.getLat();
        longitude = namedPoint.getLng();
        name = namedPoint.getName();
        serverId = namedPoint.getId();
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DbRoutePath getPath() {
        return path;
    }

    public void setPath(DbRoutePath path) {
        this.path = path;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DBRoutePoint{");
        sb.append("id=").append(id);
        sb.append(", latitude=").append(latitude);
        sb.append(", longitude=").append(longitude);
        sb.append(", name=").append(name);
        sb.append(", serverId=").append(serverId);
        sb.append('}');
        return sb.toString();
    }
}
