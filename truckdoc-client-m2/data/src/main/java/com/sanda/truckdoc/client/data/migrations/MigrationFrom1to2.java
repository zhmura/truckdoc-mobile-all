package com.sanda.truckdoc.client.data.migrations;

import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.sanda.truckdoc.client.data.model.ServerMessage;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

/**
 * Created by astra on 04.06.2015.
 */
public class MigrationFrom1to2 {

    private RuntimeExceptionDao<ServerMessage, Integer> serverMessagesDao;

    public MigrationFrom1to2(RuntimeExceptionDao<ServerMessage, Integer> serverMessagesDao) {
        this.serverMessagesDao = serverMessagesDao;
    }

    public void execute() throws Exception {
        GenericRawResults<ServerMessage> messages = serverMessagesDao.queryRaw("SELECT id, saved FROM server_message", serverMessagesDao.getRawRowMapper());
        serverMessagesDao.executeRaw("ALTER TABLE server_message ADD COLUMN savedDate BIGINT");

        for (ServerMessage message : messages.getResults()) {
            String stringDate = message.getSaved();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MMMM-dd HH:mm:ss", new Locale("ru"));
            Date date = format.parse(stringDate);
            UpdateBuilder<ServerMessage, Integer> upd = serverMessagesDao.updateBuilder();
            upd.where().eq("id", message.getId());
            upd.updateColumnValue("savedDate", new DateTime(date));
            upd.update();
        }
        Timber.i("Migrate from version 1 successful");
    }
}
