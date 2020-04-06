package com.sanda.truckdoc.client.data.migrations;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.sanda.truckdoc.client.data.model.route.DbRouteAssignment;
import com.sanda.truckdoc.client.data.model.route.DbRoutePath;
import com.sanda.truckdoc.client.data.model.route.DbRoutePoint;

import java.sql.SQLException;

/**
 * Created by szhmura
 */
public class MigrationFrom6To7 {

    public void execute(ConnectionSource connectionSource) {
        try {
            TableUtils.createTableIfNotExists(connectionSource, DbRoutePoint.class);
            TableUtils.createTableIfNotExists(connectionSource, DbRoutePath.class);
            TableUtils.createTableIfNotExists(connectionSource, DbRouteAssignment.class);
        } catch (SQLException e) {
            throw new RuntimeException("Can't create DbRoute tables");
        }
    }
}
