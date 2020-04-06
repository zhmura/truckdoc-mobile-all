package com.sanda.truckdoc.client.data.model.route;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sanda.truckdoc.client.api.v3.sync.routing.model.RouteAssignment;
import com.sanda.truckdoc.client.api.v3.sync.routing.model.RoutePath;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

/**
 * Created by astra on 14.07.2015.
 */
@DatabaseTable(tableName = "route_assignments")
public class DbRouteAssignment {

    @DatabaseField(generatedId = true)
    private long id;
    @DatabaseField
    private double serverAssignmentId;

    @DatabaseField(foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
    private DbRoutePath route;

    @DatabaseField(dataType = DataType.DATE_TIME) //
    private DateTime dateOfAssignment;

    public DbRouteAssignment() {
    }

    public DbRouteAssignment(@NotNull RouteAssignment routeAssignment, @NotNull RoutePath route) {
        this.serverAssignmentId = routeAssignment.getRouteAssignmentId();
        this.route = new DbRoutePath(route, routeAssignment.getRouteId());
        this.route.setRouteAssignment(this);
        this.dateOfAssignment = DateTime.now();
    }


    public long getId() {
        return id;
    }

    public double getServerAssignmentId() {
        return serverAssignmentId;
    }

    public void setServerAssignmentId(double serverAssignmentId) {
        this.serverAssignmentId = serverAssignmentId;
    }

    public DateTime getDateOfAssignment() {
        return dateOfAssignment;
    }

    public void setDateOfAssignment(DateTime dateOfAssignment) {
        this.dateOfAssignment = dateOfAssignment;
    }

    public DbRoutePath getRoute() {
        return route;
    }

    public void setRoute(DbRoutePath route) {
        this.route = route;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DbLocation{");
        sb.append("id=").append(id);
        sb.append(", routeAssignmentId=").append(serverAssignmentId);
        sb.append('}');
        return sb.toString();
    }
}
