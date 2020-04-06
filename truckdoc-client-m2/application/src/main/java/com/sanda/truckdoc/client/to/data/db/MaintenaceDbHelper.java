package com.sanda.truckdoc.client.to.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.sanda.truckdoc.client.util.timber.L;

import java.sql.SQLException;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Created by k.natallie on 07.08.2016.
 */

public class MaintenaceDbHelper extends OrmLiteSqliteOpenHelper {
    // name of the database file for your application -- change to something appropriate for your app
    private static final String DATABASE_NAME = "mnt_truckdoc.db";
    // any time you make changes to your database objects, you may have to increase the database version
    private static final int DATABASE_VERSION = 1;


    private static final Class<?>[] ENTITIES = //
            {MaintenanceFileRecord.class};
    private final Context context;


    @Inject
    public MaintenaceDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION); //TODO add config
        //  serverMessagesDao = getRuntimeExceptionDao(ServerMessage.class);
        //   attachmentDao = getRuntimeExceptionDao(AttachmentInfo.class);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            L.i();
            for (Class<?> entity : ENTITIES) {
                TableUtils.createTableIfNotExists(connectionSource, entity);
            }
        } catch (SQLException e) {
            Timber.e(e, "Mnt DB create issue");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {

    }

    /**
     * Close the database connections and clear any cached DAOs.
     */
    @Override
    public void close() {
        super.close();
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
            Timber.e(e, "Mnt DB recreate issue");
            throw new RuntimeException(e);
        }
        onCreate(db, connectionSource);
    }

}
