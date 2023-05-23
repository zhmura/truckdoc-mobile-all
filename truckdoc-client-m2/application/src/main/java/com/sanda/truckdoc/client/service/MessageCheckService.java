package com.sanda.truckdoc.client.service;

import android.Manifest;
import android.app.IntentService;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanda.truckdoc.client.Prefs;
import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.TruckDocApp;
import com.sanda.truckdoc.client.adapters.SynchronizeRequestAdapter;
import com.sanda.truckdoc.client.api.ClientToServerMessagePojo;
import com.sanda.truckdoc.client.api.SynchronizeRequest;
import com.sanda.truckdoc.client.api.model.ContactListData;
import com.sanda.truckdoc.client.api.model.LocationRecord;
import com.sanda.truckdoc.client.api.v2.ServerToClientMessagePojoNew;
import com.sanda.truckdoc.client.api.v2.SynchronizeResponseNew;
import com.sanda.truckdoc.client.api.v3.configuration.api.RegisterRequest;
import com.sanda.truckdoc.client.api.v3.configuration.api.RegisterResponse;
import com.sanda.truckdoc.client.api.v3.configuration.api.UpdateRequest;
import com.sanda.truckdoc.client.api.v3.configuration.model.app.AppInfo;
import com.sanda.truckdoc.client.api.v3.sync.client.config.model.ClientConfig;
import com.sanda.truckdoc.client.api.v3.sync.client.config.model.ClientConfigWithVersion;
import com.sanda.truckdoc.client.api.v3.sync.client.config.model.Features;
import com.sanda.truckdoc.client.api.v3.sync.maintenance.model.MaintenanceConfigInfo;
import com.sanda.truckdoc.client.api.v3.sync.routing.model.RouteAssignmentInfo;
import com.sanda.truckdoc.client.api.v3.sync.routing.model.RoutePath;
import com.sanda.truckdoc.client.data.MessagesDatabaseService;
import com.sanda.truckdoc.client.data.model.AttachmentInfo;
import com.sanda.truckdoc.client.data.model.DbContactRecord;
import com.sanda.truckdoc.client.data.model.DbLocation;
import com.sanda.truckdoc.client.data.model.ServerMessage;
import com.sanda.truckdoc.client.data.model.route.DbRouteAssignment;
import com.sanda.truckdoc.client.receivers.GetNewMessagesAlarmManager;
import com.sanda.truckdoc.client.receivers.IncomeMessagesAlarmManager;
import com.sanda.truckdoc.client.receivers.LocationReceiver;
import com.sanda.truckdoc.client.receivers.NotificationReceiver_;
import com.sanda.truckdoc.client.receivers.ServiceResultReceiver;
import com.sanda.truckdoc.client.service.info.RegistrationInfoProvider;
import com.sanda.truckdoc.client.service.remote.MessageServiceClient;
import com.sanda.truckdoc.client.service.remote.QueryContext;
import com.sanda.truckdoc.client.service.remote.exceptions.CommunicationException;
import com.sanda.truckdoc.client.service.remote.exceptions.RemoteCallException;
import com.sanda.truckdoc.client.to.data.Model;
import com.sanda.truckdoc.client.to.utils.LocalStorage;
import com.sanda.truckdoc.client.ui.DashboardActivity_;
import com.sanda.truckdoc.client.ui.DialogActivity_;
import com.sanda.truckdoc.client.ui.RegisterActivity_;
import com.sanda.truckdoc.client.ui.message.InboxActivity;
import com.sanda.truckdoc.client.ui.utils.SoundUtils;
import com.sanda.truckdoc.client.util.FileHelper;
import com.sanda.truckdoc.client.util.PrefUtil;
import com.sanda.truckdoc.client.util.UnexpectedException;
import com.sanda.truckdoc.client.util.commons.FilenameUtils;
import com.sanda.truckdoc.client.util.commons.IOUtils;
import com.sanda.truckdoc.client.util.commons.StringUtils;
import com.sanda.truckdoc.client.util.timber.L;
import com.sanda.truckdoc.network.AuthorizedBackend;
import com.sanda.truckdoc.network.Backend;
import com.sanda.truckdoc.network.api.AuthorizedNetworkModule;
import com.sanda.truckdoc.network.api.SynchronizeCheckResponse;
import com.sanda.truckdoc.network.api.UserKey;

import net.tribe7.common.base.Optional;
import net.tribe7.common.collect.FluentIterable;
import net.tribe7.common.collect.ImmutableList;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import app.camera.tdoc.camera_library.PreferenceKeys;
import retrofit2.Response;
import rx.Observable;
import timber.log.Timber;

import static com.sanda.truckdoc.client.receivers.ServiceResultReceiver.NOTIFICATION_MESSAGE;

/**
 * @author Alexei Osipov
 */
public class MessageCheckService extends IntentService {

    private static final String TAG = "MessageCheckService";

    public static final String ACTION_REGISTER = "com.sanda.truckdoc.client.service.action.REGISTER";
    public static final String ACTION_GET_NEW_MESSAGES = "com.sanda.truckdoc.client.service.action.GET_NEW_MESSAGES";
    public static final String ACTION_SEND_TEXT_MESSAGE = "com.sanda.truckdoc.client.service.action.UPLOAD_FILE";

    /**
     * Root worksheet feed for online data source
     */

    public static final String PARAM_OUT_MSG = "OUT_TEXT";
    public static final String PARAM_OUT_DATA = "OUT_DATA";
    public static final String REGISTRATION_SUCCESS = "registration_success";
    public static final String REGISTRATION_ERROR_MSG = "registration_err_msg";

    public static final String INTENT_PARAM_DAEMON = "daemon";
    public static final String INTENT_PARAM_DIRECT_REFRESH = "directRefresh";
    public static final String INTENT_PARAM_SYNC_REASON = "syncReason";
    public static final String INTENT_PARAM_TIMESTAMP = "ts";


    //Notification message ID
    private static final int NEW_MESSAGE_NOTIFICATION = 1448;
    private static final int SYNC_RESULTS_NOTIFICATION_ID = 1337;
    private static final String SYNC_CHECK_URL = "https://mobile.aps-solver.com/mobile-api/v2/messages/syncCheck"; // TODO: Use api_service_path!
    private static final ImmutableList<String> DOC_TYPES = ImmutableList.of("INVOICE", "CARNET-TIR", "CMR", "COM-DESCR", "PACK-LIST", "EXPORT-DECL", "DKD");

    // Checks should not be done when time passed is less than this.
    private static final long MIN_CHECK_INTERVAL_MS = TimeUnit.MINUTES.toMillis(1);
    // Acceptable violation of time schedule. This is needed to handle situation when android fires timer earlier than needed.
    private static final long MAX_AHEAD_OF_TIME_CHECK_MS = TimeUnit.SECONDS.toMillis(60);
    private static final String NOTIFICATION_CHANNEL_ID = "messagecheck.ch1";

    @Nullable
    private HttpExecutor httpsExecutor = null;
    @NotNull
    private Properties resources;
    @NotNull
    private AppSettings settings;
    @Nullable
    private UserKey userKey;

    // This is static and volatile to survive service recreation.
    private static volatile boolean appVersionWasChecked = false;

    @Inject
    MessagesDatabaseService databaseService;
    @Inject
    NotificationHelper notificationHelper;

    private AuthorizedBackend authorizedBackend;
    @Inject
    Backend backend;
    @Inject
    Prefs prefs;

    public MessageCheckService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startCommand();
        TruckDocApp.get(this).appComponent().inject(this);
        resources = loadProperties();
        settings = new AppSettings(this);
        userKey = settings.getUserKey();
        if (userKey != null) {
            createAuthorizedBackend();
        }
        Timber.i("Service was created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent == null) {
            return START_NOT_STICKY;
        }
        startCommand();
        return START_STICKY;
    }


    private void startCommand() {
        startInForeground();
    }

    private void startInForeground() {
        Intent notificationIntent = new Intent(this, MessageCheckService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_action_refresh)
                .setContentTitle("truckdoc")
                .setContentText("messaging")
                .setTicker("truckdoc")
                .setContentIntent(pendingIntent);
        Notification notification = builder.build();
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "messagecheck.channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Truckdoc message channel");
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
        try {
            startForeground((int) (System.currentTimeMillis() % 10000), notification);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

/*
    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName) {
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification buildForegroundNotification() {
        NotificationCompat.Builder b=new NotificationCompat.Builder(this, createNotificationChannel("message_check", "Truckdoc message service"));

        b.setOngoing(true)
                .setOngoing(true)
                .setPriority(PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentTitle(getString(R.string.message_check))
                .setContentText("message check service")
                .setSmallIcon(android.R.drawable.ic_popup_sync);
        try {
            return b.build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
*/

    private void createAuthorizedBackend() {
        assert userKey != null;
        authorizedBackend = TruckDocApp.get(this)
                .appComponent()
                .plus(new AuthorizedNetworkModule(userKey))
                .authorizedBackend();
    }

    @NotNull
    private HttpExecutor getHttpsExecutor() {
        if (httpsExecutor == null) {
            httpsExecutor = HttpExecutorFactory.getExecutor(this, true);
        }
        return httpsExecutor;
    }

    @Override
    public void onDestroy() {
        Timber.i("Destroying service");
        //TODO workaround for NetworkOnMainThreadException
        if (httpsExecutor != null) {
            new Thread(httpsExecutor::shutdown).start();
        }
        this.stopSelf();
        super.onDestroy();
    }

    private Properties loadProperties() {
        Resources resources = this.getResources();
        try {
            InputStream inputStream = resources.openRawResource(R.raw.service);
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        } catch (IOException e) {
            throw new RuntimeException("Configuration file is missing", e);
        }
    }

    private boolean register(String registrationToken) throws IOException, RemoteCallException {
        RegisterRequest registerRequest = new RegisterRequest();
        TelephonyManager tMgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss", Locale.US);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return false;
        }
        String line1Number = tMgr.getLine1Number();
        String generatedName = !TextUtils.isEmpty(line1Number) ? line1Number : "CLIENT_TRUCKDOC_" + sdf.format(new Date());

        AppInfo clientAppInfo = RegistrationInfoProvider.getClientAppInfo(this);
        registerRequest.setSimInfo(RegistrationInfoProvider.getSimInfo(this, tMgr));
        registerRequest.setDeviceInfo(RegistrationInfoProvider.getClientDeviceInfo(this));

        registerRequest.setAppInfo(clientAppInfo);
        registerRequest.setGeneratedName(generatedName);
        registerRequest.setRegistrationToken(registrationToken.toUpperCase()); // Note: toUpperCase is temporary fix
        Response registerResponse = backend.register(registerRequest).executeUnchecked();
        if (ResponseCheckHelper.checkIfError(registerResponse, this, "M1", false)) {
            return false;
        }
        RegisterResponse responseBody = (RegisterResponse) registerResponse.body();
        userKey = new UserKey(responseBody.getName(), responseBody.getLoginKey(), responseBody.getSecretKey());
        settings.saveUserKey(userKey);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putInt(PreferenceKeys.getUserDeactivatedPreferenceKey(), 0).apply();
        prefs.lastSavedClientVersionCode(clientAppInfo.getAppVersionCode());
        createAuthorizedBackend();
        enableSync(this);
        processGetNewMessagesAction(true, false, SyncReason.NETWORK_AVAILABLE);
        return true;
    }

    private static void enableSync(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putBoolean(PreferenceKeys.getSyncEnabledPreferenceKey(), true).apply();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            //Checker.checkDataEnabled(this);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            boolean syncEnabled = sharedPreferences.getBoolean(PreferenceKeys.getSyncEnabledPreferenceKey(), false);
            String action = intent.getAction();
            Bundle bundle = intent.getExtras();
            userKey = settings.getUserKey();
            Long recipientId = null;
            String recipientIdType = null;
            if (bundle != null && bundle.get("mail.group") != null) {
                recipientId = bundle.getLong("mail.group");
                recipientIdType = bundle.getString("mail.group.type");
            }
            if (ACTION_REGISTER.equals(action)) {
                registerAction(intent.getStringExtra("registrationToken"));
            } else {
                if (authorizedBackend != null && syncEnabled) {
                    if (ACTION_GET_NEW_MESSAGES.equals(action)) {
                        processGetNewMessagesActionIfNecessary(intent);
                    } else if (ACTION_SEND_TEXT_MESSAGE.equals(action) && (intent.getStringExtra("com/sanda/truckdoc/client/message") != null)) {
                        sendMessageAction(intent.getStringExtra("com/sanda/truckdoc/client/message"), recipientId, recipientIdType);
                    }
                }
            }
        } catch (Exception e) {
            Timber.e(e, "Handle intent issue with action:" + intent.getAction());
        }
    }

    private boolean isDirectRefresh(Intent intent) {
        return intent.getBooleanExtra(INTENT_PARAM_DIRECT_REFRESH, false);
    }

    @NotNull
    private SyncReason getSyncReason(Intent intent) {
        SyncReason result = (SyncReason) intent.getSerializableExtra(INTENT_PARAM_SYNC_REASON);
        return result != null ? result : SyncReason.UNKNOWN;
    }

    private boolean autoCheckClientInfoUpdates() throws IOException {
        triggerSyncNotification();
        List<DbLocation> dbLocations = databaseService.getLocations();
        List<LocationRecord> locationRecordList = Observable.from(dbLocations)
                .map(DbLocation::toLocationRecord)
                .toList()
                .toBlocking()
                .first();
        SynchronizeRequest request = SynchronizeRequestAdapter.autoCheckClientInfoUpdates(locationRecordList);
        Long configVersion = prefs.lastKnownClientConfigVersion();
        if (!configVersion.equals(0L)) {
            request.setLastKnownClientConfigVersion(configVersion);
        }
        Long routeAssignment = prefs.currentRouteAssignment();
        if (!routeAssignment.equals(0L)) {
            request.setLastKnownRouteAssignment(routeAssignment);
        }
        Long contactListVersion = prefs.contactListVersion();
        if (!contactListVersion.equals(0L)) {
            request.setKnownContactListVersion(contactListVersion);
        }
        Response response = authorizedBackend.synchronizeCheck(
                SYNC_CHECK_URL,
                request).executeUnchecked();
        if (ResponseCheckHelper.checkIfError(response, this, "M3", false)) {
            return false;
        }

        int hasNewMessages = ((SynchronizeCheckResponse) response.body()).getHasNewMessages().intValue();
        databaseService.deleteLocations(dbLocations);
        if (hasNewMessages == 0) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.cancel(NEW_MESSAGE_NOTIFICATION);
        }
        return hasNewMessages == 1;
    }

    private void sendMessageAction(String message, Long recipientId, String recipientIdtype) {
        ClientToServerMessagePojo pojo = new ClientToServerMessagePojo();
        pojo.setText(message);
        pojo.setRecipientId(recipientId.intValue());
        pojo.setAttachments(new ArrayList<>());
        List<ClientToServerMessagePojo> list = new ArrayList<>();
        list.add(pojo);
        try {
            Response response = authorizedBackend
                    .sendMessage(message, recipientIdtype, HttpExecutorFactory.PROTOCOL_VERSION, recipientId).executeUnchecked();
            if (ResponseCheckHelper.checkIfError(response, this, "M9", true)) {
                pojo.setSent(false);
                return;
            }
            pojo.setSent(true);
            MessagesDatabaseService.saveOutgoingMessages(this, list);
            sendNotificationMessageonUI(getResources().getString(R.string.messages_sent), false);
            SoundUtils.soundNotification(this, false);
        } catch (IOException e) {
            Timber.e(e, "Sending messages failed");
            pojo.setSent(false);
            sendNotificationMessageonUI(NotificationHelper.getErrorMessage(e, this, "M9"), true);
            SoundUtils.soundNotification(this, true);
        }
    }

    private void registerAction(String registrationToken) {
        try {
            boolean result = register(registrationToken);
            if (result) {
                sendNotificationMessageonUI(getResources().getString(R.string.userRegisteredSuccessfully), false);
                DashboardActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_NEW_TASK).start();
            } else {
                RegisterActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_NEW_TASK).start();
            }
        } catch (Exception e) {
            Timber.e(e, "Sending messages failed");
            sendNotificationMessageonUI(NotificationHelper.getErrorMessage(e, this, "M1"), true);
        }
        stopSelf();
    }

    /**
     * Check and update client info
     *
     * @return false is update failed and true otherwise
     */
    private boolean checkAndUpdateClientInfo() {
        try {
            Long savedClientVersionCode = prefs.lastSavedClientVersionCode();
            AppInfo clientAppInfo = RegistrationInfoProvider.getClientAppInfo(this);
            Long currentVersionCode = clientAppInfo.getAppVersionCode();
            if (!currentVersionCode.equals(savedClientVersionCode)) {
                TelephonyManager tMgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
                UpdateRequest updateRequest = new UpdateRequest();
                updateRequest.setSimInfo(RegistrationInfoProvider.getSimInfo(this, tMgr));
                updateRequest.setDeviceInfo(RegistrationInfoProvider.getClientDeviceInfo(this));
                updateRequest.setAppInfo(clientAppInfo);
                updateRequest.setCurrentClientTime(new Date().getTime());
                Response response = authorizedBackend.update(updateRequest).executeUnchecked();
                if (!ResponseCheckHelper.checkIfError(response, this, "M6", true)) {
                    sendNotificationMessageonUI(getResources().getString(R.string.update_client_info), false);
                    prefs.lastSavedClientVersionCode(currentVersionCode);
                }
            }
        } catch (IOException e) {
            sendNotificationMessageonUI(NotificationHelper.getErrorMessage(e, this, "M6"), true);
            Timber.e(e, "Check and update Client info failed");
            return false;
        }
        return true;
    }

    private void processGetNewMessagesActionIfNecessary(Intent intent) throws RemoteCallException, IOException {
        if (!appVersionWasChecked) {
            if (checkAndUpdateClientInfo()) {
                appVersionWasChecked = true;
            } else {
                // Check failed. Abort.
                return;
            }
        }

        SyncReason syncReason = getSyncReason(intent);
        //long intentTimestamp = getIntentTimestamp(intent);

        boolean directRefresh = isDirectRefresh(intent);
        if (directRefresh) {
            // Refresh initiated by user. Do it now.
            processGetNewMessagesAction(true, true, syncReason);
            return;
        }

        long currentTimeMillis = System.currentTimeMillis();
        long lastSyncAttemptTs = prefs.lastSyncAttemptTs();
        long lastSuccessfulSyncTs = prefs.lastSuccessfulSyncTs();
/*        if (intentTimestamp < lastSyncAttemptTs) {
            L.d("Intent was created before starting last attempt. Ignore it.");
            return;
        }*/

        final long configuredSyncInterval = PrefUtil.getSyncIntervalMs(prefs);
        final long lastSyncOrAttempt = Math.max(lastSyncAttemptTs, lastSuccessfulSyncTs);

        long minTimePassedToSyncNow = configuredSyncInterval; // How much time should have been passed to permit sync right now
        boolean lastSyncWasSuccessful = lastSyncAttemptTs <= lastSuccessfulSyncTs;

        boolean updateAlarmIfNoSync = false; // Should we reschedule next alarm trigger if we decide to not sync right now?
        boolean onUpdateUseFullInterval = true; // Should we use full interval or minimum allowed interval if we not sync right now?
        switch (syncReason) {
            case GOT_SMS:
                // Got SMS => reduce interval requirements to a minimum.
                minTimePassedToSyncNow = MIN_CHECK_INTERVAL_MS;
                updateAlarmIfNoSync = true;
                onUpdateUseFullInterval = false;
                break;

            case NETWORK_AVAILABLE:
                // Got network status change. We might update earlier if our recent attempt was not successful.
                if (!lastSyncWasSuccessful) {
                    minTimePassedToSyncNow = MIN_CHECK_INTERVAL_MS;
                    updateAlarmIfNoSync = true;
                    onUpdateUseFullInterval = false;
                }
                break;

            case USER_DIRECT:
                assert false;
                break;

            case PERIODIC_CHECK:
                // Allow an early checkIfError
                minTimePassedToSyncNow = Math.max(MIN_CHECK_INTERVAL_MS, configuredSyncInterval - MAX_AHEAD_OF_TIME_CHECK_MS);
                updateAlarmIfNoSync = true;
                break;

            case ALARM_CHANGED:
                // Allow an early checkIfError
                minTimePassedToSyncNow = MIN_CHECK_INTERVAL_MS;
                updateAlarmIfNoSync = true;
                break;
            default:
        }

        long nextAllowedAttempt = lastSyncOrAttempt + minTimePassedToSyncNow;
        long nextAllowedAttemptWithFullInterval = lastSyncOrAttempt + configuredSyncInterval;
        boolean tooEarly = nextAllowedAttempt > currentTimeMillis;

        if (tooEarly) {
            Timber.i("Last sync (or attempt) was done too recently. SyncReason: %s. Time passed: %ds", syncReason, msToSec(currentTimeMillis - lastSuccessfulSyncTs));
            // We decided to not do sync right now. However we may decide to change time for next sync.
            if (updateAlarmIfNoSync) {
                Timber.i("Updating alarm without sync. AheadOfTime: %ds. SyncReason: %s", msToSec(nextAllowedAttemptWithFullInterval - currentTimeMillis), syncReason);
                long nextTriggerTs = onUpdateUseFullInterval ? nextAllowedAttemptWithFullInterval : nextAllowedAttempt;
                GetNewMessagesAlarmManager.setMessageAlarmStartingAtTime(getApplicationContext(), nextTriggerTs);
            }
            return;
        }

        boolean updateAlarm = syncReason != SyncReason.PERIODIC_CHECK && syncReason != SyncReason.ALARM_CHANGED;
        processGetNewMessagesAction(false, updateAlarm, syncReason);

    }

    private static long msToSec(long duration) {
        return TimeUnit.MILLISECONDS.toSeconds(duration);
    }

    /**
     * @param directRefresh indicates that sync was triggered by direct action of user
     * @param updateAlarm   alarm should be rescheduled for new time
     */
    private void processGetNewMessagesAction(boolean directRefresh, boolean updateAlarm, SyncReason syncReason) throws RemoteCallException, IOException {
        long syncStartTs = System.currentTimeMillis();
        long prevSyncAttemptTs = prefs.lastSyncAttemptTs();
        prefs.lastSyncAttemptTs(syncStartTs);
        Timber.i("Time since update: %ds", msToSec(syncStartTs - prevSyncAttemptTs));
        if (updateAlarm) {
            L.d("Updating alarm. Current ts: %d", System.currentTimeMillis());
            GetNewMessagesAlarmManager.setGetMessagesAlarm(getApplicationContext(), false);
        }

        boolean hasToDoFullSync;
        boolean successfulCheck = false;

        if (directRefresh) {
            // Note: autoCheckClientInfoUpdates() doesn't work because it will not display notification about absence of new messages.
            // TODO: use autoCheckClientInfoUpdates() and show message if no updates present
            hasToDoFullSync = true;
        } else {
            Timber.i("Checking messages. Reason: %s", syncReason);
            if (autoCheckClientInfoUpdates()) {
                Timber.d("New data available");
                hasToDoFullSync = true;
            } else {
                Timber.d("No new data available");
                hasToDoFullSync = false;
                successfulCheck = true;
            }
        }

        if (hasToDoFullSync) {
            successfulCheck = getNewMessagesAction(directRefresh, syncReason);
        }

        if (prefs.hasMaintenance()) {
            checkMaintenance();
        }

        if (successfulCheck) {
            prefs.lastSuccessfulSyncTs(System.currentTimeMillis());
        }
    }

    private void routeUpdate(SynchronizeResponseNew responseNew) {
        try {
            RouteAssignmentInfo routeAssignmentInfo = responseNew.getRouteAssignmentInfo();
            if (routeAssignmentInfo != null
                    && routeAssignmentInfo.getRouteAssignment() != null
                    && routeAssignmentInfo.getRouteAssignment().getRouteId() != null
                    && databaseService.findRouteAssignmentByServerId(routeAssignmentInfo.getRouteAssignment().getRouteAssignmentId()).size() == 0) {
                Response route = authorizedBackend.getRoute(routeAssignmentInfo.getRouteAssignment().getRouteId()).executeUnchecked();
                if (ResponseCheckHelper.checkIfError(route, this, "M11", true)) {
                    return;
                }
                RoutePath routePath = (RoutePath) route.body();
                DbRouteAssignment routeAssignment = new DbRouteAssignment(routeAssignmentInfo.getRouteAssignment(), routePath);
                DbRouteAssignment assignment = databaseService.insertRouteAssignment(routeAssignment);
                databaseService.initializeRoutePoints(assignment.getRoute(), routePath.getPoints());
                prefs.currentRouteAssignment(assignment.getId());
            }
        } catch (IOException e) {
            Timber.e(e, "Route update failed");
            sendNotificationMessageonUI(NotificationHelper.getErrorMessage(e, this, "M11"), true);
        }
    }


    private void checkMaintenance() throws IOException {

        List<DbLocation> dbLocations = databaseService.getLocations();
        List<LocationRecord> locationRecordList = Observable.from(dbLocations)
                .map(DbLocation::toLocationRecord)
                .toList()
                .toBlocking()
                .first();
        SynchronizeRequest request = SynchronizeRequestAdapter.synchronizeMaintenance(locationRecordList);
        long versionMnt = prefs.lastKnownMaintenanceConfigVersion();
        request.setLastKnownMaintenanceConfigVersion((versionMnt >= 0) ? versionMnt : null);


        Response response = authorizedBackend.synchronize(request).executeUnchecked();
        if (ResponseCheckHelper.checkIfError(response, this, "M2", true)) {
            return;
        }
        maintenanceConfigUpdate((SynchronizeResponseNew) response.body());
    }


    private void maintenanceConfigUpdate(SynchronizeResponseNew responseNew) throws IOException {
        MaintenanceConfigInfo info = responseNew.getMaintenanceConfigInfo();
        if (info != null) {
            prefs.lastKnownMaintenanceConfigVersion(info.getConfigVersion());
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(info);
            Model model = Model.getInstance(getApplicationContext());
            model.setMaintenanceConfigInfo(info);
            LocalStorage.getInstance(getApplicationContext()).writeStringPreference(LocalStorage.CONFIG_INFO, json);
            //sendNotificationMessageonUI(getResources().getString(R.string.to_settings_received), false);
        }
    }

    /**
     * @param directRefresh indicates that sync was triggered by direct action of user
     * @return true if there were no errors
     * @throws RemoteCallException
     */
    private boolean getNewMessagesAction(boolean directRefresh, SyncReason syncReason) throws RemoteCallException {
        Timber.i("Getting new messages: %s", syncReason);
        QueryContext queryContext = getQueryContext();
        triggerSyncNotification();
        // Get messages
        notifyActivity(getResources().getString(R.string.load_new_mess), null, true);
        //Log.v(this.getClass().getName(), "Новых сообщений: " + newMessages.size());
        int totalMessageCount = 0;
        int totalAttachmentCount = 0;
        int receivedAttachmentCount = 0;
        boolean withError = false;

        /*
        1. Получить все новые сообщения
        2. Загрузить из базы все незагруженные сообщения
        3. Загрузить все неагруженные аттачменты для всех этих сообщений
        4. Пометить сообщения как загруженные
        5. Отправить пометку на сервер
         */
        // Save messages
        Set<Integer> receivedMessages = new HashSet<>();
        List<ServerToClientMessagePojoNew> newMessages = new ArrayList<>();
        try {
            List<DbLocation> dbLocations = databaseService.getLocations();
            List<LocationRecord> locationRecordList = Observable.from(dbLocations)
                    .map(DbLocation::toLocationRecord)
                    .toList()
                    .toBlocking()
                    .first();
            SynchronizeRequest request = SynchronizeRequestAdapter.withNewMessagesAndUpdates(
                    locationRecordList,
                    prefs.contactListVersion(),
                    prefs.lastKnownRouteAssignment(),
                    prefs.lastKnownMaintenanceConfigVersion());

            Long version = prefs.lastKnownClientConfigVersion();
            request.setLastKnownClientConfigVersion(version > 0 ? version : null);
            Response responseWrapper = authorizedBackend.synchronize(request).executeUnchecked();

            if (ResponseCheckHelper.checkIfError(responseWrapper, this, "M2", true)) {
                return false;
            }
            SynchronizeResponseNew responseNew = (SynchronizeResponseNew) responseWrapper.body();
            processContactList(responseNew.getContactList());
            newMessages = responseNew.getMessagesForClient();
            routeUpdate(responseNew);
            databaseService.deleteLocations(dbLocations);

            ClientConfigWithVersion clientConfigWithVersion = responseNew.getClientConfigWithVersion();
            if (clientConfigWithVersion != null) {
                ClientConfig clientConfig = clientConfigWithVersion.getClientConfig();
                prefs.lastKnownClientConfigVersion(clientConfigWithVersion.getClientConfigVersion());
                Features features = clientConfig.getFeatures();
                boolean hasMaintenance = features != null ? features.getMaintenanceReports() : false;
                prefs.hasMaintenance(hasMaintenance);

                Long gpsSyncInterval = clientConfig.getGpsCheckIntervalMs();
                if (gpsSyncInterval == null) {
                    // 0 means turn off GPS
                    gpsSyncInterval = LocationReceiver.TURN_OFF_GPS_SPECIAL_VALUE;
                }
                if (prefs.gpsDataSyncTime() != gpsSyncInterval) {
                    prefs.gpsDataSyncTime(gpsSyncInterval <= 0 ? LocationReceiver.TURN_OFF_GPS_SPECIAL_VALUE : gpsSyncInterval);
                    LocationReceiver.requestLocationUpdates(this, gpsSyncInterval);
                }
            }

            databaseService.deleteLocations(dbLocations);

            for (ServerToClientMessagePojoNew messagePojo : newMessages) {
                MessagesDatabaseService.saveMessageIfNotExist(this, messagePojo);
                if (messagePojo.getAttachments().size() == 0) {
                    receivedMessages.add(messagePojo.getId());
                }
            }
            List<ServerMessage> notDownLoadedMessages = MessagesDatabaseService.getNotDownloadedMessages(this);
            totalMessageCount = notDownLoadedMessages.size();
            for (ServerMessage message : notDownLoadedMessages) {
                // Mark messages on server as received
                List<AttachmentInfo> notDownloadedFiles = MessagesDatabaseService.getNotDownloadedFiles(this, message);
                for (AttachmentInfo attachmentInfo : notDownloadedFiles) {
                    if (!attachmentInfo.isDownloaded()) {
                        totalAttachmentCount++;
                        InputStream stream = MessageServiceClient.getFileBinaryData(queryContext,
                                attachmentInfo.getServerId());
                        saveFile(attachmentInfo, stream);
                        databaseService.markFileAsDownloaded(attachmentInfo).toBlocking().last();
                        receivedAttachmentCount++;
                    }
                }
                receivedMessages.add(message.getServerMessageId());
                databaseService.markMessageAsDownloaded(message).toBlocking().last();
            }
        } catch (Exception e) {
            sendNotificationMessageonUI(NotificationHelper.getErrorMessage(e, this, "M2"), true);
            withError = true;
            Timber.e(e, "Get messages action failed");
        }
        List<Integer> receivedList = new ArrayList<>();
        receivedList.addAll(receivedMessages);
        if (receivedMessages.size() > 0) {
            MessageServiceClient.markMessagesAsReceived(queryContext, receivedList);
        }

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(NEW_MESSAGE_NOTIFICATION);
        notifyActivity(receivedMessages, newMessages, directRefresh, totalMessageCount, withError, totalAttachmentCount, receivedAttachmentCount);
        notifyActivity("", null, false);

        return !withError;
    }

    private void processContactList(ContactListData contactList) {
        if (contactList != null && contactList.getContactListVersion() != null) {
            ImmutableList<DbContactRecord> records = FluentIterable.from(contactList.getContactRecords())
                    .transform(DbContactRecord::new)
                    .toList();
            if (databaseService.replaceContactRecords(records)) {
                prefs.contactListVersion(contactList.getContactListVersion());
                Timber.i("Contacts replaced");
            }
        } else {
            Timber.i("No new contacts");
        }
    }

    private void notifyActivity(Set<Integer> receivedMessages, List<ServerToClientMessagePojoNew> newMessages, boolean directRefresh, int totalMessageCount, boolean withError, int totalAttachmentCount,
                                int receivedAttachmentCount) {
        boolean hasNew = false;
        String notificationMessageText = "";
        Long responseUserId = null;

        boolean allMessagesReceived = receivedMessages.size() == totalMessageCount;
        boolean allAttachmentsReceived = totalAttachmentCount == receivedAttachmentCount;
        boolean receivedWithErrors = !allMessagesReceived || !allAttachmentsReceived;
        String dialogMessage = "";
        if (receivedMessages.size() == 1) {
            playNotification();
            hasNew = true;
            notificationMessageText = getResources().getString(R.string.get_1_mess) +
                    (receivedWithErrors ? "\n" + getResources().getString(R.string.get_mess_err) : "");
            sendNotificationMessageonUI(notificationMessageText, receivedWithErrors);
            ServerToClientMessagePojoNew finalMessage = newMessages.get(0);
            dialogMessage = finalMessage.getText().substring(0, finalMessage.getText().length() > 301 ? 300 : finalMessage.getText().length() - 1);
            Observable<DbContactRecord> contacts = databaseService.getContactRecords();
            FluentIterable<DbContactRecord> contactRecords = FluentIterable.from(contacts.toList().toBlocking().single());
            if (finalMessage.getSenderUserId() != null || finalMessage.getSenderVirtualGroupId() != null) {
                if (finalMessage.getSenderVirtualGroupId() != null) {
                    Optional<DbContactRecord> contact = contactRecords.firstMatch(record -> record.getRecipientId() == finalMessage.getSenderVirtualGroupId().longValue());
                    responseUserId = contact.isPresent() ? contact.get().getRecipientId() : null;
                } else {
                    Optional<DbContactRecord> contact = contactRecords.firstMatch(record -> record.getRecipientId() == finalMessage.getSenderUserId().longValue());
                    responseUserId = contact.isPresent() ? contact.get().getRecipientId() : null;
                }
            }
        }
        if (receivedMessages.size() >= 2) {
            playNotification();
            hasNew = true;
            notificationMessageText = getResources().getString(R.string.get_n_mess) +
                    totalMessageCount +
                    getResources().getString(R.string.get_n_mess_2) +
                    (receivedWithErrors ? "\n" + getResources().getString(R.string.get_mess_err) : "");
            sendNotificationMessageonUI(notificationMessageText, receivedWithErrors);
        }
        if (receivedMessages.size() == 0 && directRefresh) {
            notificationMessageText = getResources().getString(R.string.no_new_mess_got) +
                    (withError ? "\n" + getResources().getString(R.string.error_no_mess) : "");
            sendNotificationMessageonUI(notificationMessageText, withError);
            notificationMessageText = null;
        }
        if (hasNew) {
            IncomeMessagesAlarmManager.setAlarm(getApplicationContext(),
                    receivedMessages.size() > 0 && allMessagesReceived,
                    IncomeMessagesAlarmManager.NOTIFY_INTERVAL);
            triggerNotification(receivedMessages.size() > 0 && receivedWithErrors, withError);
        }
        KeyguardManager keyguardManager = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        //L.v("Should send to dialog %updateAlarm  - %updateAlarm", keyguardManager.inKeyguardRestrictedInputMode(), !pm.isScreenOn());
        if (hasNew && (keyguardManager.inKeyguardRestrictedInputMode() || !pm.isScreenOn()) && notificationMessageText != null) {
            //L.v("Sending to dialog %s", message);
            try {
                unlockDevice();
                updateMessagesWidget(receivedMessages.size() > 1 ? notificationMessageText : dialogMessage,
                        responseUserId,
                        receivedMessages.size() == 1 && newMessages.get(0).getAttachments().size() == 0);
            } catch (Exception e) {
                Timber.e("", e);
            }
        }
    }

    private void unlockDevice() {
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = km.newKeyguardLock("TAG");

        PowerManager pm2 = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        final PowerManager.WakeLock wakeLock = pm2.newWakeLock(PowerManager.FULL_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE, "MyWakeLock:");
        wakeLock.acquire();
        keyguardLock.disableKeyguard();

        wakeLock.release();
    }

    private void updateMessagesWidget(String message, Long recipientId, boolean quickReply) {
        try {
            DialogActivity_.intent(this)
                    .connectionProblem(false)
                    .reminderMessage(message)
                    .senderRoleId(recipientId)
                    .quickReply(quickReply)
                    .repeatReminder(false)
                    .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .start();
        } catch (Exception e) {
            L.e(e);
        }
    }

    public void triggerSyncNotification() {
        //Counter
        int count = 0;
        //Create NotificationManager  object
        NotificationManager notifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //Instantiate notification with icon and ticker message
        //PendingIntent to launch our activity if the user selects it
        PendingIntent i = PendingIntent.getActivity(this, 0, new Intent(this, InboxActivity.class), PendingIntent.FLAG_IMMUTABLE);
        //Set the info that show in the notification panel
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "syncchannel");

        builder.setContentTitle(getResources().getString(R.string.mess_sync_finished));
        builder.setContentText(getResources().getString(R.string.mess_sync_finished));
        builder.setContentIntent(i);
        builder.setChannelId("syncchannel");
        builder.setSmallIcon(R.drawable.refresh);
        builder.setTicker(getResources().getString(R.string.mess_sync));
        builder.setNumber(++count);
        Notification notifyObj = builder.build();

        //Value indicates the current number of events represented by the notification
        //notifyObj.number = ++count;
        //Set default notification sound
        notifyObj.defaults |= Notification.DEFAULT_LIGHTS;
        //Clear the status notification when the user selects it
        notifyObj.flags |= Notification.FLAG_AUTO_CANCEL;
        notifyObj.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
        //Send notification
        notifyMgr.notify(NEW_MESSAGE_NOTIFICATION, notifyObj);
    }

    public void triggerNotification(boolean unloaded, boolean withError) {
        //Counter
        int count = 0;
        //Create NotificationManager  object
        NotificationManager notifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //Instantiate notification with icon and ticker message
        int icon = R.drawable.done_square;
        if (withError) {
            icon = R.drawable.stop_round;
        }
        if (unloaded) {
            icon = R.drawable.warning_triangle;
        }

        //PendingIntent to launch our activity if the user selects it
        PendingIntent i = PendingIntent.getActivity(this, 0, new Intent(this, InboxActivity.class), PendingIntent.FLAG_IMMUTABLE);
        //Set the info that show in the notification panel
        Notification.Builder builder = new Notification.Builder(this);

        int title;
        int text;
        if (!unloaded && !withError) {
            title = R.string.new_mess_got;
            text = R.string.new_mess_got;

        } else {
            title = R.string.new_mess_got_err;
            text = R.string.click_for_view;
        }

        builder.setContentTitle(getResources().getString(title));
        builder.setContentText(getResources().getString(text));
        builder.setContentIntent(i);
        builder.setSmallIcon(icon);
        builder.setTicker(getResources().getString(R.string.new_mess_status));
        builder.setNumber(++count);
        Notification notifyObj = builder.build();


        //Value indicates the current number of events represented by the notification
        notifyObj.number = ++count;
        //Set default vibration
        notifyObj.defaults |= Notification.DEFAULT_VIBRATE;
        //Set default notification sound
        notifyObj.defaults |= Notification.DEFAULT_SOUND;
        //Clear the status notification when the user selects it
        notifyObj.flags |= Notification.FLAG_AUTO_CANCEL;
        //Send notification
        notifyMgr.notify(SYNC_RESULTS_NOTIFICATION_ID, notifyObj);
    }

    public void playNotification() {
        //Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        //Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        //r.play();
    }

    private void saveFile(AttachmentInfo attachmentInfo, InputStream is) throws CommunicationException {
        String originalFileName = attachmentInfo.getFileName();
        String extension = FilenameUtils.getExtension(originalFileName);

        File directory = FileHelper.getIncomeDirectory(attachmentInfo.getMessage().getId(), extension);

        File file = new File(directory, "TruckDoc_" +
                formatMessageId(attachmentInfo.getMessage().getId()) +
                "_" +
                formatAttachmentId(attachmentInfo.getId()) +
                "." +
                extension);
        OutputStream os;
        try {
            os = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new UnexpectedException("Failed to open file for writing");
        }
        try {
            IOUtils.copy(is, os);
        } catch (IOException e) {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
            throw new CommunicationException(getResources().getString(R.string.error_loading), e);
        }
    }

    private String formatMessageId(Integer messageId) {
        return formatNumber(messageId, 6);
    }

    private String formatAttachmentId(Integer attachmentId) {
        return formatNumber(attachmentId, 6);
    }

    private String formatNumber(Integer number, int size) {
        return StringUtils.leftPad(number.toString(), size, '0');
    }

    private String getApiServicePath() {
        return resources.getProperty("api_service_path");
    }

    public QueryContext getQueryContext() {
        return new QueryContext(getHttpsExecutor(), userKey, getApiServicePath());
    }

    private void sendNotificationMessageonUI(String message, boolean isError) {
        Intent broadcastIntent = new Intent(this, NotificationReceiver_.class);
        broadcastIntent.setAction(NOTIFICATION_MESSAGE);
        broadcastIntent.putExtra(NotificationHelper.PARAM_IS_ERROR, isError);
        broadcastIntent.putExtra(NotificationHelper.PARAM_MSG, message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }

    private void notifyActivity(String message, @Nullable Parcelable result, boolean isStart) {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager.isScreenOn()) {
            Intent broadcastIntent = new Intent();
            if (isStart) {
                broadcastIntent.setAction(ServiceResultReceiver.ACTION_PROCESS_START);
            } else {
                broadcastIntent.setAction(ServiceResultReceiver.ACTION_PROCESS_FINISHED);
            }
            broadcastIntent.putExtra(MessageCheckService.PARAM_OUT_MSG, message);
            broadcastIntent.putExtra(MessageCheckService.PARAM_OUT_DATA, result);
            LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
        }
    }

    public static void executeGetNewMessagesAction(Context context, boolean daemon, boolean directRefresh, @Nullable SyncReason syncReason) {
        assert !(daemon && directRefresh);
        Intent intent = new Intent(MessageCheckService.ACTION_GET_NEW_MESSAGES, null, context, MessageCheckService.class);
        if (daemon) {
            intent.putExtra(INTENT_PARAM_DAEMON, true);
        }
        if (directRefresh) {
            intent.putExtra(INTENT_PARAM_DIRECT_REFRESH, true);
        }
        if (syncReason != null) {
            intent.putExtra(INTENT_PARAM_SYNC_REASON, syncReason);
        }
        //intent.putExtra(INTENT_PARAM_TIMESTAMP, System.currentTimeMillis());
        ContextCompat.startForegroundService(
                context,
                intent
        );
    }
}
