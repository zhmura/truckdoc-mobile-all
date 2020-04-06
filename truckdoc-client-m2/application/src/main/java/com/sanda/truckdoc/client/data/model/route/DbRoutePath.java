package com.sanda.truckdoc.client.data.model.route;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.sanda.truckdoc.client.api.v3.sync.routing.model.RoutePath;

import org.jetbrains.annotations.NotNull;

/**
 *
 */
@DatabaseTable(tableName = "route_pathes")
public class DbRoutePath {


    @DatabaseField(generatedId = true)
    private long id;
    @DatabaseField
    private String name;
    @DatabaseField
    private double serverRouteId;
    @ForeignCollectionField(eager = true)
    private ForeignCollection<DbRoutePoint> points;

    @DatabaseField(canBeNull = false, foreign = true)
    private DbRouteAssignment routeAssignment;

    public DbRoutePath() {
    }

    public DbRoutePath(@NotNull RoutePath routePath, @NotNull Long routeId) {
        this.name = routePath.getName();
        this.serverRouteId = routeId;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getServerRouteId() {
        return serverRouteId;
    }

    public void setServerRouteId(double serverRouteId) {
        this.serverRouteId = serverRouteId;
    }

    public ForeignCollection<DbRoutePoint> getPoints() {
        return points;
    }

    public void setPoints(ForeignCollection<DbRoutePoint> points) {
        this.points = points;
    }

    public DbRouteAssignment getRouteAssignment() {
        return routeAssignment;
    }

    public void setRouteAssignment(DbRouteAssignment routeAssignment) {
        this.routeAssignment = routeAssignment;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DbRoutePath{");
        sb.append("id=").append(id);
        sb.append(", name=").append(name);
        sb.append(", serverRouteId=").append(serverRouteId);
        sb.append('}');
        return sb.toString();
    }
}
