package com.sanda.truckdoc.client.data.migrations;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.sanda.truckdoc.client.data.model.ServerMessage;

import timber.log.Timber;

/**
 * Created by astra on 04.06.2015.
 */
public class MigrationFrom3to4 {

    private RuntimeExceptionDao<ServerMessage, Integer> serverMessagesDao;

    public MigrationFrom3to4(RuntimeExceptionDao<ServerMessage, Integer> serverMessagesDao) {
        this.serverMessagesDao = serverMessagesDao;
    }

    public void execute() throws Exception {

        serverMessagesDao.executeRaw("ALTER TABLE server_message ADD COLUMN hidden INTEGER DEFAULT 0");

        Timber.i("Migrate from version 3 successful");
    }
}
