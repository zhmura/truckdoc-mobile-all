package com.sanda.truckdoc.client.to.data.db;

import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.io.File;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Observable;

/**
 * Created by k.natallie on 07.08.2016.
 */
@Singleton
public class MntDbService {
    private final RuntimeExceptionDao<MaintenanceFileRecord, Long> mntFileDao;

    @Inject
    public MntDbService(MaintenaceDbHelper helper) {
        mntFileDao = helper.getRuntimeExceptionDao(MaintenanceFileRecord.class);
    }

    public Observable<MaintenanceFileRecord> getMntFiles() {
        return Observable.fromCallable(() -> {
            return mntFileDao.queryForAll();
        }).flatMapIterable(list -> list);
    }

    // Blocking method for Java compatibility
    public List<MaintenanceFileRecord> getMntFilesBlocking() {
        return mntFileDao.queryForAll();
    }

    public void createMessageFileRecord(MaintenanceFileRecord record) {
        mntFileDao.create(record);
    }

    public void deleteMessageFileRecords(List<MaintenanceFileRecord> records) {
        for (MaintenanceFileRecord record : records) {
            new File(record.getPath()).delete();
        }
        mntFileDao.delete(records);
    }

    public void deleteAllMntFileRecords() {
        List<MaintenanceFileRecord> records = mntFileDao.queryForAll();
        for (MaintenanceFileRecord record : records) {
            File f = new File(record.getPath());
            if (f.exists()) f.delete();
        }
        mntFileDao.delete(records);
    }

    public void updateMessageFileRecord(MaintenanceFileRecord record) {
        mntFileDao.update(record);
    }

}
