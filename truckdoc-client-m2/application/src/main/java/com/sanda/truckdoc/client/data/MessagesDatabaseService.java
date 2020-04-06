package com.sanda.truckdoc.client.data;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.table.TableUtils;
import com.sanda.truckdoc.client.api.AttachmentPojo;
import com.sanda.truckdoc.client.api.ClientToServerMessagePojo;
import com.sanda.truckdoc.client.api.v2.ServerToClientMessagePojoNew;
import com.sanda.truckdoc.client.api.v3.sync.routing.model.NamedPoint;
import com.sanda.truckdoc.client.data.model.AttachmentInfo;
import com.sanda.truckdoc.client.data.model.DbContactRecord;
import com.sanda.truckdoc.client.data.model.DbLocation;
import com.sanda.truckdoc.client.data.model.MessageFileRecord;
import com.sanda.truckdoc.client.data.model.ServerMessage;
import com.sanda.truckdoc.client.data.model.route.DbRouteAssignment;
import com.sanda.truckdoc.client.data.model.route.DbRoutePath;
import com.sanda.truckdoc.client.data.model.route.DbRoutePoint;
import com.sanda.truckdoc.client.util.FileHelper;
import com.sanda.truckdoc.client.util.RoleTypeMapper;
import com.sanda.truckdoc.client.util.UnexpectedException;
import com.sanda.truckdoc.client.util.commons.FileUtils;

import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static rx.Observable.from;
import static rx.Observable.just;

/**
 * @author Alexei Osipov
 */
@Singleton
public class MessagesDatabaseService {

    private final RuntimeExceptionDao<ServerMessage, Integer> serverMessagesDao;
    private final RuntimeExceptionDao<AttachmentInfo, Integer> attachmentDao;
    private final RuntimeExceptionDao<DbLocation, Long> locationDao;
    private final RuntimeExceptionDao<DbContactRecord, Long> contactRecordDao;
    private final RuntimeExceptionDao<MessageFileRecord, Long> messageFileDao;
    private final RuntimeExceptionDao<DbRouteAssignment, Long> routeAssignmentDao;
    private final RuntimeExceptionDao<DbRoutePoint, Long> routePointDao;
    private final RuntimeExceptionDao<DbRoutePath, Long> routePathDao;


    @Inject
    public MessagesDatabaseService(DatabaseHelper helper) {
        serverMessagesDao = helper.getRuntimeExceptionDao(ServerMessage.class);
        attachmentDao = helper.getRuntimeExceptionDao(AttachmentInfo.class);
        locationDao = helper.getRuntimeExceptionDao(DbLocation.class);
        contactRecordDao = helper.getRuntimeExceptionDao(DbContactRecord.class);
        messageFileDao = helper.getRuntimeExceptionDao(MessageFileRecord.class);
        routeAssignmentDao = helper.getRuntimeExceptionDao(DbRouteAssignment.class);
        routePointDao = helper.getRuntimeExceptionDao(DbRoutePoint.class);
        routePathDao = helper.getRuntimeExceptionDao(DbRoutePath.class);
    }

    public void initializeRoutePoints(DbRoutePath route, List<NamedPoint> points) {
        routePathDao.assignEmptyForeignCollection(route, "points");
        for (NamedPoint point : points) {
            DbRoutePoint dbRoutePoint = new DbRoutePoint(point);
            dbRoutePoint.setPath(route);
            routePointDao.create(dbRoutePoint);
        }
        routePathDao.update(route);
    }

    public Observable<Integer> markMessageAsDownloaded(ServerMessage message) {
        return Observable.defer(() -> {
            message.setDownloaded(true);
            return just(serverMessagesDao.update(message));
        });
    }

    public Observable<Integer> insertLocation(DbLocation location) {
        return Observable.defer(() -> {
            return just(locationDao.create(location));
        });
    }

    public DbRouteAssignment insertRouteAssignment(DbRouteAssignment routeAssignment) {
        return routeAssignmentDao.createIfNotExists(routeAssignment);
    }

    public List<DbRouteAssignment> findRouteAssignmentByServerId(Long serverRouteAssignmentId) {
        return routeAssignmentDao.queryForEq("serverAssignmentId", serverRouteAssignmentId);
    }

    public DbRouteAssignment findRouteAssignmentById(Long routeAssignmentId) {
        return routeAssignmentDao.queryForId(routeAssignmentId);
    }


    public void addSmsMessage(ServerMessage serverMessage) {
        serverMessagesDao.create(serverMessage);
    }

    public void markHidden(ServerMessage serverMessage) {
        serverMessage.setHidden(true);
        serverMessagesDao.update(serverMessage);
    }

    public List<DbLocation> getLocations() {
        try {
            return locationDao.queryBuilder().limit(100L).query();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Observable<DbContactRecord> getContactRecords() {
        return Observable.defer(() -> from(contactRecordDao.queryForAll()));
    }

    public Observable<MessageFileRecord> getMessageFiles() {
        return Observable.fromCallable(messageFileDao::queryForAll).flatMap(Observable::from);
    }

    public Observable<MessageFileRecord> getNotSentMessageFiles() {
        return Observable.fromCallable(() -> messageFileDao.queryForEq("sent", false)).flatMap(Observable::from);
    }

    public Observable<MessageFileRecord> getNotUploadedMessageFiles() {
        return Observable.fromCallable(() -> messageFileDao.queryBuilder().where().isNull("serverId").query()).flatMap(Observable::from);
    }

    public void createMessageFileRecord(MessageFileRecord record) {
        messageFileDao.create(record);
    }

    public void deleteMessageFileRecords(List<MessageFileRecord> records) {
        for (MessageFileRecord record : records) {
            new File(record.getPath()).delete();
        }
        messageFileDao.delete(records);
    }

    public void deleteAllMessageFileRecords() {
        List<MessageFileRecord> records = messageFileDao.queryForAll();
        for (MessageFileRecord record : records) {
            File f = new File(record.getPath());
            if (f.exists()) f.delete();
        }
        messageFileDao.delete(records);
    }

    public void updateMessageFileRecord(MessageFileRecord record) {
        messageFileDao.update(record);
    }

    public boolean replaceContactRecords(List<DbContactRecord> newContacts) {
        try {
            TransactionManager.callInTransaction(contactRecordDao.getConnectionSource(), () -> {
                TableUtils.clearTable(contactRecordDao.getConnectionSource(), DbContactRecord.class);
                for (DbContactRecord contact : newContacts) {
                    contactRecordDao.create(contact);
                }
                return null;
            });
            return true;
        } catch (SQLException e) {
            Timber.e(e, "Replace Contact Records failed");
            return false;
        }
    }

    public Observable<Integer> updateContactRecord(DbContactRecord record) {
        return Observable.defer(() -> just(contactRecordDao.update(record)));
    }

    public void deleteAssignments() {
        for (DbRouteAssignment dbRouteAssignment : routeAssignmentDao.queryForAll()) {
            routePointDao.delete(dbRouteAssignment.getRoute().getPoints());
            routePathDao.delete(dbRouteAssignment.getRoute());
            routeAssignmentDao.delete(dbRouteAssignment);
        }
    }

    public void deleteLocations(List<DbLocation> locations) {
        locationDao.delete(locations);
    }

    public Observable<Integer> markFileAsDownloaded(final AttachmentInfo attachment) {
        return Observable.defer(() -> {
            attachment.setDownloaded(true);
            return just(attachmentDao.update(attachment));
        });
    }

    public Observable<Integer> deleteAllMessages() {
        return Observable.defer(() -> {
            QueryBuilder<ServerMessage, Integer> qb = serverMessagesDao.queryBuilder();
            qb.orderBy("saved", true);
            List<ServerMessage> list = null;
            try {
                list = serverMessagesDao.query(qb.prepare());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            for (ServerMessage message : list) {
                try {
                    FileUtils.deleteDirectory(FileHelper.getIncomeDirectory(message.getId(), "*"));
                } catch (IOException e) {
                    //
                }
                serverMessagesDao.delete(message);
            }
            return just(0);
        }).subscribeOn(Schedulers.newThread());
    }

    //called in bg
    public static List<ServerMessage> getNotDownloadedMessages(Context context) {
        return new DatabaseOperation<List<ServerMessage>, ServerMessage>() {
            @Override
            public List<ServerMessage> impl(Dao<ServerMessage, Integer> dao) throws SQLException {
                return dao.queryForEq("downloaded", false);
            }
        }.execute(context, ServerMessage.class);
    }

    //called in bg
    public static List<AttachmentInfo> getNotDownloadedFiles(Context context, final ServerMessage message) {
        return new DatabaseOperation<List<AttachmentInfo>, AttachmentInfo>() {
            @Override
            public List<AttachmentInfo> impl(Dao<AttachmentInfo, Integer> dao) throws SQLException {
                QueryBuilder<AttachmentInfo, Integer> qb = dao.queryBuilder();
                return dao.query(qb.where().eq("message_id", message.getId()).and().eq("downloaded", false).prepare());
            }
        }.execute(context, AttachmentInfo.class);
    }

    public static void saveMessageIfNotExist(final Context context, final ServerToClientMessagePojoNew messagePojo) {
        new DatabaseOperation<Void, ServerMessage>() {
            @Override
            public Void impl(Dao<ServerMessage, Integer> dao) throws SQLException {
                List<ServerMessage> oldMessages = dao.queryForEq("serverMessageId", messagePojo.getId());
                if (oldMessages.size() == 0) {
                    ServerMessage message = new ServerMessage(messagePojo);
                    message.setSavedDate(DateTime.now());
                    message.setServerMessageId(messagePojo.getId());

                    if (messagePojo.getAttachments().size() > 0) {
                        message.setDownloaded(false);
                    }
                    dao.create(message);
                    saveFiles(context, messagePojo.getAttachments(), message);
                }
                return null;
            }
        }.execute(context, ServerMessage.class);
    }

    public static void saveOutgoingMessages(final Context context,
                                            final Collection<ClientToServerMessagePojo> messagePojos) {
        new DatabaseOperation<Void, ServerMessage>() {
            @Override
            public Void impl(Dao<ServerMessage, Integer> dao) throws SQLException {
                for (ClientToServerMessagePojo messagePojo : messagePojos) {
                    ServerMessage oldMessage = null;
                    if (messagePojo.getId() != null) {
                        oldMessage = dao.queryForId(messagePojo.getId());
                        oldMessage.setSent(messagePojo.isSent());
                        dao.update(oldMessage);
                    }
                    if (oldMessage == null) {
                        ServerMessage message = new ServerMessage(messagePojo);
                        message.setSavedDate(DateTime.now());
                        message.setOutgoing(true);
                        message.setSent(messagePojo.isSent());
                        message.setProcessed(false);
                        message.setDownloaded(true);
                        message.setRecipientId(messagePojo.getRecipientId());
                        dao.create(message);
                        saveFiles(context, messagePojo.getAttachments(), message);
                    }
                }
                return null;
            }
        }.execute(context, ServerMessage.class);
    }

    public static void saveFiles(Context context,
                                 final Collection<AttachmentPojo> attachmentPojos,
                                 final ServerMessage message) {
        new DatabaseOperation<Void, AttachmentInfo>() {
            @Override
            public Void impl(Dao<AttachmentInfo, Integer> dao) throws SQLException {
                for (AttachmentPojo attachmentPojo : attachmentPojos) {
                    AttachmentInfo attachmentInfo = new AttachmentInfo(attachmentPojo);
                    attachmentInfo.setMessage(message);
                    dao.create(attachmentInfo);
                }
                return null;
            }
        }.execute(context, AttachmentInfo.class);
    }

    public Observable<ServerMessage> getMessages(boolean showHidden) {
        //for debug
        ServerMessage sm = new ServerMessage();
        sm.setDownloaded(true);
        sm.setSavedDate(DateTime.now());
        sm.setRecipientId(RoleTypeMapper.IN_COLUMN);
        sm.setText("text");
        sm.setSenderRoleId(RoleTypeMapper.OUT_EXPEDITER);
        sm.setOutgoing(false);
        sm.setId(888000);

        return Observable.defer(() -> {
            //SELECT * FROM server_message WHERE (outgoing=1 OR (outgoing=0 AND downloaded=1)) AND hidden?=0
            QueryBuilder<ServerMessage, Integer> q = serverMessagesDao.queryBuilder();
            Where<ServerMessage, Integer> where = q.where();
            try {
                where.or(where.eq("outgoing", true), where //
                        .eq("outgoing", false).and().eq("downloaded", true));
                if (!showHidden) {
                    where.and(where, where.eq("hidden", showHidden));
                }
                q.orderBy("savedDate", false);
                q.limit(50L);
                return from(serverMessagesDao.query(q.prepare()));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static List<ServerMessage> getPendingOutMessages(Context context) {
        return new DatabaseOperation<List<ServerMessage>, ServerMessage>() {
            @Override
            public List<ServerMessage> impl(Dao<ServerMessage, Integer> dao) throws SQLException {
                Map<String, Object> map = new HashMap<>();
                map.put("sent", false);
                map.put("outgoing", true);
                return dao.queryForFieldValuesArgs(map);
            }
        }.execute(context, ServerMessage.class);
    }

    public static void deleteOldMessages(Context context) {
        new DatabaseOperation<Void, ServerMessage>() {
            @Override
            public Void impl(Dao<ServerMessage, Integer> dao) throws SQLException {

                QueryBuilder<ServerMessage, Integer> qb = dao.queryBuilder();
                qb.orderBy("saved", true);
                List<ServerMessage> list = dao.query(qb.prepare());
                for (ServerMessage message : list) {
                    DateTime date = message.getSavedDate();
                    try {
                        if (date.isBefore(DateTime.now().minusMonths(2))) {
                            FileUtils.deleteDirectory(FileHelper.getIncomeDirectory(message.getId(), "*"));
                            dao.delete(message);
                        }
                    } catch (IOException e) {
                        //
                    }
                }
                return null;
            }
        }.execute(context, ServerMessage.class);
    }

    public void deleteAllData() {
        try {
            FileHelper.deleteAllAppFiles();
            TableUtils.clearTable(attachmentDao.getConnectionSource(), attachmentDao.getDataClass());
            TableUtils.clearTable(serverMessagesDao.getConnectionSource(), serverMessagesDao.getDataClass());
            TableUtils.clearTable(locationDao.getConnectionSource(), routeAssignmentDao.getDataClass());
            TableUtils.clearTable(contactRecordDao.getConnectionSource(), contactRecordDao.getDataClass());
            TableUtils.clearTable(messageFileDao.getConnectionSource(), messageFileDao.getDataClass());
            TableUtils.clearTable(routeAssignmentDao.getConnectionSource(), routeAssignmentDao.getDataClass());
            TableUtils.clearTable(routePointDao.getConnectionSource(), routePointDao.getDataClass());
            TableUtils.clearTable(routePathDao.getConnectionSource(), routePathDao.getDataClass());
        } catch (Exception e) {
            //throw new RuntimeException(e);
        }

    }

    private static abstract class DatabaseOperation<R, T> {

        public abstract R impl(Dao<T, Integer> dao) throws SQLException;

        public R execute(Context context, Class<T> entityClass) {
            DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
            try {
                @SuppressWarnings("unchecked") Dao<T, Integer> dao = helper.getDao(entityClass);
                return impl(dao);
            } catch (SQLException e) {
                throw new UnexpectedException("Failed to execute database operation", e);
            } finally {
                OpenHelperManager.releaseHelper();
            }
        }
    }
}
