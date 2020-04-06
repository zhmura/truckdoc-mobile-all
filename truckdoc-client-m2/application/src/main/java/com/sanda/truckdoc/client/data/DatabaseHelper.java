package com.sanda.truckdoc.client.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.AndroidDatabaseConnection;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableUtils;
import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.data.migrations.MigrationFrom1to2;
import com.sanda.truckdoc.client.data.migrations.MigrationFrom2To3;
import com.sanda.truckdoc.client.data.migrations.MigrationFrom3to4;
import com.sanda.truckdoc.client.data.migrations.MigrationFrom4To5;
import com.sanda.truckdoc.client.data.migrations.MigrationFrom5To6;
import com.sanda.truckdoc.client.data.migrations.MigrationFrom6To7;
import com.sanda.truckdoc.client.data.model.AttachmentInfo;
import com.sanda.truckdoc.client.data.model.Contact;
import com.sanda.truckdoc.client.data.model.DbContactRecord;
import com.sanda.truckdoc.client.data.model.DbLocation;
import com.sanda.truckdoc.client.data.model.MessageFileRecord;
import com.sanda.truckdoc.client.data.model.ServerMessage;
import com.sanda.truckdoc.client.data.model.route.DbRouteAssignment;
import com.sanda.truckdoc.client.data.model.route.DbRoutePath;
import com.sanda.truckdoc.client.data.model.route.DbRoutePoint;
import com.sanda.truckdoc.client.util.timber.L;

import net.tribe7.common.collect.Lists;

import java.sql.SQLException;
import java.util.ArrayList;

import javax.inject.Inject;

/**
 * @author Alexei Osipov
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    // name of the database file for your application -- change to something appropriate for your app
    private static final String DATABASE_NAME = "td_messages.db";
    // any time you make changes to your database objects, you may have to increase the database version
    private static final int DATABASE_VERSION = 6;

    private static final Class<?>[] ENTITIES = //
            {ServerMessage.class,
                    AttachmentInfo.class,
                    Contact.class,
                    DbLocation.class,
                    DbContactRecord.class,
                    MessageFileRecord.class,
                    DbRoutePoint.class,
                    DbRoutePath.class,
                    DbRouteAssignment.class};

    // the DAO object we use to access the SimpleData table
    private final RuntimeExceptionDao<ServerMessage, Integer> serverMessagesDao;
    private final RuntimeExceptionDao<AttachmentInfo, Integer> attachmentDao;
    private final Context context;

    @Inject
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION); //TODO add config
        serverMessagesDao = getRuntimeExceptionDao(ServerMessage.class);
        attachmentDao = getRuntimeExceptionDao(AttachmentInfo.class);
        this.context = context;
    }

    /**
     * This is called when the database is first created. Usually you should call createTable statements here to create
     * the tables that will store your data.
     */
    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            L.i();
            for (Class<?> entity : ENTITIES) {
                TableUtils.createTableIfNotExists(connectionSource, entity);
            }
            createDefaultButtons();
        } catch (SQLException e) {
            L.e(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
     * the various data to match the new version number.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        L.i("onUpgrade, oldVersion=[%s], newVersion=[%s]", oldVersion, newVersion);
        try {

            db.beginTransaction();
            try {
                switch (oldVersion) {
                    case 1:
                        new MigrationFrom1to2(serverMessagesDao).execute();
                    case 2:
                        new MigrationFrom2To3().execute(connectionSource);
                    case 3:
                        new MigrationFrom3to4(serverMessagesDao).execute();
                    case 4:
                        new MigrationFrom4To5().execute(connectionSource);
                        createDefaultButtons();
                    case 5:
                        new MigrationFrom5To6().execute(connectionSource);
                    case 6:
                        new MigrationFrom6To7().execute(connectionSource);
                }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } catch (final Exception e) {
            L.e(e, "Can't migrate databases, bootstrap database, data will be lost");
            recreateDatabase(db, connectionSource);
        }
    }

    public final void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        ConnectionSource cs = getConnectionSource();
        /*
         * The method is called by Android database helper's get-database calls when Android detects that we need to
         * create or update the database. So we have to use the database argument and save a connection to it on the
         * AndroidConnectionSource, otherwise it will go recursive if the subclass calls getConnectionSource().
         */
        DatabaseConnection conn = cs.getSpecialConnection();
        boolean clearSpecial = false;
        if (conn == null) {
            conn = new AndroidDatabaseConnection(db, true);
            try {
                cs.saveSpecialConnection(conn);
                clearSpecial = true;
            } catch (SQLException e) {
                throw new IllegalStateException("Could not save special connection", e);
            }
        }
        try {
            recreateDatabase(db, cs);
        } finally {
            if (clearSpecial) {
                cs.clearSpecialConnection(conn);
            }
        }
    }

    private void recreateDatabase(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            L.i();
            for (Class<?> entity : ENTITIES) {
                TableUtils.dropTable(connectionSource, entity, true);
            }

            // after we drop the old databases, we create the new ones
            onCreate(db, connectionSource);
        } catch (SQLException e) {
            L.e(e);
            throw new RuntimeException(e);
        }
        onCreate(db, connectionSource);
    }

    private void createDefaultButtons() {
        ArrayList<DbContactRecord> records = Lists.newArrayList(//
                new DbContactRecord(1l,
                        "Колонный",
                        null,
                        context.getResources().getColor(R.color.button_default_green),
                        null),
                new DbContactRecord(2l,
                        "Экспедитор",
                        null,
                        context.getResources().getColor(R.color.button_default_blue),
                        null),
                new DbContactRecord(3l,
                        "Оператор",
                        null,
                        context.getResources().getColor(R.color.button_default_red),
                        null));
        RuntimeExceptionDao<DbContactRecord, Long> contactRecordDao = getRuntimeExceptionDao(DbContactRecord.class);
        for (DbContactRecord record : records) {
            contactRecordDao.createIfNotExists(record);
        }
    }

    /**
     * Close the database connections and clear any cached DAOs.
     */
    @Override
    public void close() {
        super.close();
    }
}
