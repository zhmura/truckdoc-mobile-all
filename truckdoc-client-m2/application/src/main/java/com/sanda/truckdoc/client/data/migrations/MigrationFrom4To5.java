package com.sanda.truckdoc.client.data.migrations;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.sanda.truckdoc.client.data.model.DbContactRecord;

import java.sql.SQLException;

/**
 * Created by astra on 14.07.2015.
 */
public class MigrationFrom4To5 {

    public void execute(ConnectionSource connectionSource) {
        try {
            TableUtils.createTableIfNotExists(connectionSource, DbContactRecord.class);
        } catch (SQLException e) {
            throw new RuntimeException("Can't create DbContactRecord table");
        }
    }
}
