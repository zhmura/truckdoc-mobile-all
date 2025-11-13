package com.sanda.truckdoc.client

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.sanda.truckdoc.client.ui.DashboardActivity
import com.sanda.truckdoc.client.ui.SplashActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TruckDocAppIntegrationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val splashActivityRule = ActivityTestRule(SplashActivity::class.java, false, false)

    @get:Rule
    val dashboardActivityRule = ActivityTestRule(DashboardActivity::class.java, false, false)

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `test application context is available`() {
        // Given & When
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // Then
        assert(appContext != null)
        assert(appContext.packageName == "com.sanda.truckdoc.client")
    }

    @Test
    fun `test splash activity launches successfully`() {
        // Given
        val intent = splashActivityRule.intent

        // When
        splashActivityRule.launchActivity(intent)

        // Then
        assert(splashActivityRule.activity != null)
        assert(!splashActivityRule.activity.isFinishing)
    }

    @Test
    fun `test dashboard activity launches successfully`() {
        // Given
        val intent = dashboardActivityRule.intent

        // When
        dashboardActivityRule.launchActivity(intent)

        // Then
        assert(dashboardActivityRule.activity != null)
        assert(!dashboardActivityRule.activity.isFinishing)
    }

    @Test
    fun `test application resources are accessible`() {
        // Given & When
        val resources = context.resources

        // Then
        assert(resources != null)
        assert(resources.getString(R.string.app_name).isNotEmpty())
    }

    @Test
    fun `test application database is accessible`() {
        // Given & When
        val database = context.getDatabasePath("truckdoc_database")

        // Then
        // Database path should be accessible (may not exist yet)
        assert(database != null)
    }

    @Test
    fun `test application shared preferences are accessible`() {
        // Given & When
        val prefs = context.getSharedPreferences("truckdoc_prefs", Context.MODE_PRIVATE)

        // Then
        assert(prefs != null)
    }

    @Test
    fun `test application file directory is accessible`() {
        // Given & When
        val filesDir = context.filesDir

        // Then
        assert(filesDir != null)
        assert(filesDir.exists())
    }

    @Test
    fun `test application cache directory is accessible`() {
        // Given & When
        val cacheDir = context.cacheDir

        // Then
        assert(cacheDir != null)
        assert(cacheDir.exists())
    }

    @Test
    fun `test application external files directory is accessible`() {
        // Given & When
        val externalFilesDir = context.getExternalFilesDir(null)

        // Then
        assert(externalFilesDir != null)
        assert(externalFilesDir.exists())
    }

    @Test
    fun `test application package info is accessible`() {
        // Given & When
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

        // Then
        assert(packageInfo != null)
        assert(packageInfo.packageName == "com.sanda.truckdoc.client")
    }

    @Test
    fun `test application version info is accessible`() {
        // Given & When
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

        // Then
        assert(packageInfo.versionName != null)
        assert(packageInfo.versionCode > 0)
    }

    @Test
    fun `test application permissions are declared`() {
        // Given & When
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

        // Then
        assert(packageInfo.requestedPermissions != null)
        assert(packageInfo.requestedPermissions.isNotEmpty())
    }

    @Test
    fun `test application activities are declared`() {
        // Given & When
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

        // Then
        assert(packageInfo.activities != null)
        assert(packageInfo.activities.isNotEmpty())
    }

    @Test
    fun `test application services are declared`() {
        // Given & When
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

        // Then
        assert(packageInfo.services != null)
        assert(packageInfo.services.isNotEmpty())
    }

    @Test
    fun `test application receivers are declared`() {
        // Given & When
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

        // Then
        assert(packageInfo.receivers != null)
        assert(packageInfo.receivers.isNotEmpty())
    }

    @Test
    fun `test application providers are declared`() {
        // Given & When
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

        // Then
        assert(packageInfo.providers != null)
        assert(packageInfo.providers.isNotEmpty())
    }

    @Test
    fun `test application can access network state`() {
        // Given & When
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)

        // Then
        assert(connectivityManager != null)
    }

    @Test
    fun `test application can access notification service`() {
        // Given & When
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)

        // Then
        assert(notificationManager != null)
    }

    @Test
    fun `test application can access alarm service`() {
        // Given & When
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE)

        // Then
        assert(alarmManager != null)
    }

    @Test
    fun `test application can access location service`() {
        // Given & When
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE)

        // Then
        assert(locationManager != null)
    }

    @Test
    fun `test application can access telephony service`() {
        // Given & When
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE)

        // Then
        assert(telephonyManager != null)
    }

    @Test
    fun `test application can access power service`() {
        // Given & When
        val powerManager = context.getSystemService(Context.POWER_SERVICE)

        // Then
        assert(powerManager != null)
    }

    @Test
    fun `test application can access keyguard service`() {
        // Given & When
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE)

        // Then
        assert(keyguardManager != null)
    }

    @Test
    fun `test application can access wifi service`() {
        // Given & When
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE)

        // Then
        assert(wifiManager != null)
    }

    @Test
    fun `test application can access audio service`() {
        // Given & When
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE)

        // Then
        assert(audioManager != null)
    }

    @Test
    fun `test application can access vibrator service`() {
        // Given & When
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE)

        // Then
        assert(vibrator != null)
    }

    @Test
    fun `test application can access sensor service`() {
        // Given & When
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE)

        // Then
        assert(sensorManager != null)
    }

    @Test
    fun `test application can access camera service`() {
        // Given & When
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE)

        // Then
        assert(cameraManager != null)
    }

    @Test
    fun `test application can access storage service`() {
        // Given & When
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE)

        // Then
        assert(storageManager != null)
    }

    @Test
    fun `test application can access usb service`() {
        // Given & When
        val usbManager = context.getSystemService(Context.USB_SERVICE)

        // Then
        assert(usbManager != null)
    }

    @Test
    fun `test application can access input service`() {
        // Given & When
        val inputManager = context.getSystemService(Context.INPUT_SERVICE)

        // Then
        assert(inputManager != null)
    }

    @Test
    fun `test application can access window service`() {
        // Given & When
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE)

        // Then
        assert(windowManager != null)
    }

    @Test
    fun `test application can access layout inflater service`() {
        // Given & When
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)

        // Then
        assert(layoutInflater != null)
    }

    @Test
    fun `test application can access clipboard service`() {
        // Given & When
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE)

        // Then
        assert(clipboardManager != null)
    }

    @Test
    fun `test application can access search service`() {
        // Given & When
        val searchManager = context.getSystemService(Context.SEARCH_SERVICE)

        // Then
        assert(searchManager != null)
    }

    @Test
    fun `test application can access dropbox service`() {
        // Given & When
        val dropboxManager = context.getSystemService(Context.DROPBOX_SERVICE)

        // Then
        assert(dropboxManager != null)
    }

    @Test
    fun `test application can access device policy service`() {
        // Given & When
        val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE)

        // Then
        assert(devicePolicyManager != null)
    }

    @Test
    fun `test application can access ui mode service`() {
        // Given & When
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE)

        // Then
        assert(uiModeManager != null)
    }

    @Test
    fun `test application can access download service`() {
        // Given & When
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE)

        // Then
        assert(downloadManager != null)
    }

    @Test
    fun `test application can access nfc service`() {
        // Given & When
        val nfcManager = context.getSystemService(Context.NFC_SERVICE)

        // Then
        assert(nfcManager != null)
    }

    @Test
    fun `test application can access bluetooth service`() {
        // Given & When
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE)

        // Then
        assert(bluetoothManager != null)
    }

    @Test
    fun `test application can access wifi p2p service`() {
        // Given & When
        val wifiP2pManager = context.getSystemService(Context.WIFI_P2P_SERVICE)

        // Then
        assert(wifiP2pManager != null)
    }

    @Test
    fun `test application can access input method service`() {
        // Given & When
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE)

        // Then
        assert(inputMethodManager != null)
    }

    @Test
    fun `test application can access text services service`() {
        // Given & When
        val textServicesManager = context.getSystemService(Context.TEXT_SERVICES_MANAGER_SERVICE)

        // Then
        assert(textServicesManager != null)
    }

    @Test
    fun `test application can access accessibility service`() {
        // Given & When
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE)

        // Then
        assert(accessibilityManager != null)
    }

    @Test
    fun `test application can access account service`() {
        // Given & When
        val accountManager = context.getSystemService(Context.ACCOUNT_SERVICE)

        // Then
        assert(accountManager != null)
    }

    @Test
    fun `test application can access activity service`() {
        // Given & When
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE)

        // Then
        assert(activityManager != null)
    }

    @Test
    fun `test application can access package service`() {
        // Given & When
        val packageManager = context.getSystemService(Context.PACKAGE_SERVICE)

        // Then
        assert(packageManager != null)
    }

    @Test
    fun `test application can access app widget service`() {
        // Given & When
        val appWidgetManager = context.getSystemService(Context.APPWIDGET_SERVICE)

        // Then
        assert(appWidgetManager != null)
    }

    @Test
    fun `test application can access wall paper service`() {
        // Given & When
        val wallpaperManager = context.getSystemService(Context.WALLPAPER_SERVICE)

        // Then
        assert(wallpaperManager != null)
    }

    @Test
    fun `test application can access status bar service`() {
        // Given & When
        val statusBarManager = context.getSystemService(Context.STATUS_BAR_SERVICE)

        // Then
        assert(statusBarManager != null)
    }

    @Test
    fun `test application can access media router service`() {
        // Given & When
        val mediaRouter = context.getSystemService(Context.MEDIA_ROUTER_SERVICE)

        // Then
        assert(mediaRouter != null)
    }

    @Test
    fun `test application can access media session service`() {
        // Given & When
        val mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE)

        // Then
        assert(mediaSessionManager != null)
    }

    @Test
    fun `test application can access print service`() {
        // Given & When
        val printManager = context.getSystemService(Context.PRINT_SERVICE)

        // Then
        assert(printManager != null)
    }

    @Test
    fun `test application can access restriction service`() {
        // Given & When
        val restrictionManager = context.getSystemService(Context.RESTRICTIONS_SERVICE)

        // Then
        assert(restrictionManager != null)
    }

    @Test
    fun `test application can access app ops service`() {
        // Given & When
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE)

        // Then
        assert(appOpsManager != null)
    }

    @Test
    fun `test application can access captioning service`() {
        // Given & When
        val captioningManager = context.getSystemService(Context.CAPTIONING_SERVICE)

        // Then
        assert(captioningManager != null)
    }

    @Test
    fun `test application can access consumer ir service`() {
        // Given & When
        val consumerIrManager = context.getSystemService(Context.CONSUMER_IR_SERVICE)

        // Then
        assert(consumerIrManager != null)
    }

    @Test
    fun `test application can access tv input service`() {
        // Given & When
        val tvInputManager = context.getSystemService(Context.TV_INPUT_SERVICE)

        // Then
        assert(tvInputManager != null)
    }

    @Test
    fun `test application can access network score service`() {
        // Given & When
        val networkScoreManager = context.getSystemService(Context.NETWORK_SCORE_SERVICE)

        // Then
        assert(networkScoreManager != null)
    }

    @Test
    fun `test application can access usage stats service`() {
        // Given & When
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE)

        // Then
        assert(usageStatsManager != null)
    }

    @Test
    fun `test application can access job scheduler service`() {
        // Given & When
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE)

        // Then
        assert(jobScheduler != null)
    }

    @Test
    fun `test application can access persistent data block service`() {
        // Given & When
        val persistentDataBlockManager = context.getSystemService(Context.PERSISTENT_DATA_BLOCK_SERVICE)

        // Then
        assert(persistentDataBlockManager != null)
    }

    @Test
    fun `test application can access media projection service`() {
        // Given & When
        val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE)

        // Then
        assert(mediaProjectionManager != null)
    }

    @Test
    fun `test application can access midi service`() {
        // Given & When
        val midiManager = context.getSystemService(Context.MIDI_SERVICE)

        // Then
        assert(midiManager != null)
    }

    @Test
    fun `test application can access radio service`() {
        // Given & When
        val radioManager = context.getSystemService(Context.RADIO_SERVICE)

        // Then
        assert(radioManager != null)
    }

    @Test
    fun `test application can access hardware properties service`() {
        // Given & When
        val hardwarePropertiesManager = context.getSystemService(Context.HARDWARE_PROPERTIES_SERVICE)

        // Then
        assert(hardwarePropertiesManager != null)
    }

    @Test
    fun `test application can access shortcut service`() {
        // Given & When
        val shortcutManager = context.getSystemService(Context.SHORTCUT_SERVICE)

        // Then
        assert(shortcutManager != null)
    }

    @Test
    fun `test application can access connectivity diagnostics service`() {
        // Given & When
        val connectivityDiagnosticsManager = context.getSystemService(Context.CONNECTIVITY_DIAGNOSTICS_SERVICE)

        // Then
        assert(connectivityDiagnosticsManager != null)
    }

    @Test
    fun `test application can access cross profile apps service`() {
        // Given & When
        val crossProfileApps = context.getSystemService(Context.CROSS_PROFILE_APPS_SERVICE)

        // Then
        assert(crossProfileApps != null)
    }

    @Test
    fun `test application can access euicc service`() {
        // Given & When
        val euiccManager = context.getSystemService(Context.EUICC_SERVICE)

        // Then
        assert(euiccManager != null)
    }

    @Test
    fun `test application can access slice service`() {
        // Given & When
        val sliceManager = context.getSystemService(Context.SLICE_SERVICE)

        // Then
        assert(sliceManager != null)
    }

    @Test
    fun `test application can access role service`() {
        // Given & When
        val roleManager = context.getSystemService(Context.ROLE_SERVICE)

        // Then
        assert(roleManager != null)
    }

    @Test
    fun `test application can access people service`() {
        // Given & When
        val peopleManager = context.getSystemService(Context.PEOPLE_SERVICE)

        // Then
        assert(peopleManager != null)
    }

    @Test
    fun `test application can access companion device service`() {
        // Given & When
        val companionDeviceManager = context.getSystemService(Context.COMPANION_DEVICE_SERVICE)

        // Then
        assert(companionDeviceManager != null)
    }

    @Test
    fun `test application can access system update service`() {
        // Given & When
        val systemUpdateManager = context.getSystemService(Context.SYSTEM_UPDATE_SERVICE)

        // Then
        assert(systemUpdateManager != null)
    }

    @Test
    fun `test application can access adb service`() {
        // Given & When
        val adbManager = context.getSystemService(Context.ADB_SERVICE)

        // Then
        assert(adbManager != null)
    }

    @Test
    fun `test application can access app search service`() {
        // Given & When
        val appSearchManager = context.getSystemService(Context.APP_SEARCH_SERVICE)

        // Then
        assert(appSearchManager != null)
    }

    @Test
    fun `test application can access biometric service`() {
        // Given & When
        val biometricManager = context.getSystemService(Context.BIOMETRIC_SERVICE)

        // Then
        assert(biometricManager != null)
    }

    @Test
    fun `test application can access game service`() {
        // Given & When
        val gameManager = context.getSystemService(Context.GAME_SERVICE)

        // Then
        assert(gameManager != null)
    }

    @Test
    fun `test application can access health connect service`() {
        // Given & When
        val healthConnectManager = context.getSystemService(Context.HEALTH_CONNECT_SERVICE)

        // Then
        assert(healthConnectManager != null)
    }

    @Test
    fun `test application can access location manager service`() {
        // Given & When
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE)

        // Then
        assert(locationManager != null)
    }

    @Test
    fun `test application can access country detector service`() {
        // Given & When
        val countryDetector = context.getSystemService(Context.COUNTRY_DETECTOR)

        // Then
        assert(countryDetector != null)
    }

    @Test
    fun `test application can access network management service`() {
        // Given & When
        val networkManagementService = context.getSystemService(Context.NETWORKMANAGEMENT_SERVICE)

        // Then
        assert(networkManagementService != null)
    }

    @Test
    fun `test application can access network policy service`() {
        // Given & When
        val networkPolicyManager = context.getSystemService(Context.NETWORK_POLICY_SERVICE)

        // Then
        assert(networkPolicyManager != null)
    }

    @Test
    fun `test application can access network stats service`() {
        // Given & When
        val networkStatsManager = context.getSystemService(Context.NETWORK_STATS_SERVICE)

        // Then
        assert(networkStatsManager != null)
    }

    @Test
    fun `test application can access network suggestion service`() {
        // Given & When
        val networkSuggestionManager = context.getSystemService(Context.NETWORK_SUGGESTIONS_SERVICE)

        // Then
        assert(networkSuggestionManager != null)
    }

    @Test
    fun `test application can access network watchlist service`() {
        // Given & When
        val networkWatchlistManager = context.getSystemService(Context.NETWORK_WATCHLIST_SERVICE)

        // Then
        assert(networkWatchlistManager != null)
    }

    @Test
    fun `test application can access permission service`() {
        // Given & When
        val permissionManager = context.getSystemService(Context.PERMISSION_SERVICE)

        // Then
        assert(permissionManager != null)
    }

    @Test
    fun `test application can access permission controller service`() {
        // Given & When
        val permissionControllerManager = context.getSystemService(Context.PERMISSION_CONTROLLER_SERVICE)

        // Then
        assert(permissionControllerManager != null)
    }

    @Test
    fun `test application can access recovery service`() {
        // Given & When
        val recoveryManager = context.getSystemService(Context.RECOVERY_SERVICE)

        // Then
        assert(recoveryManager != null)
    }

    @Test
    fun `test application can access role controller service`() {
        // Given & When
        val roleControllerManager = context.getSystemService(Context.ROLE_CONTROLLER_SERVICE)

        // Then
        assert(roleControllerManager != null)
    }

    @Test
    fun `test application can access search ui service`() {
        // Given & When
        val searchUiManager = context.getSystemService(Context.SEARCH_UI_SERVICE)

        // Then
        assert(searchUiManager != null)
    }

    @Test
    fun `test application can access system health service`() {
        // Given & When
        val systemHealthManager = context.getSystemService(Context.SYSTEM_HEALTH_SERVICE)

        // Then
        assert(systemHealthManager != null)
    }

    @Test
    fun `test application can access time zone rules service`() {
        // Given & When
        val timeZoneRulesManager = context.getSystemService(Context.TIME_ZONE_RULES_SERVICE)

        // Then
        assert(timeZoneRulesManager != null)
    }

    @Test
    fun `test application can access time detector service`() {
        // Given & When
        val timeDetector = context.getSystemService(Context.TIME_DETECTOR_SERVICE)

        // Then
        assert(timeDetector != null)
    }

    @Test
    fun `test application can access time zone detector service`() {
        // Given & When
        val timeZoneDetector = context.getSystemService(Context.TIME_ZONE_DETECTOR_SERVICE)

        // Then
        assert(timeZoneDetector != null)
    }

    @Test
    fun `test application can access virtual device service`() {
        // Given & When
        val virtualDeviceManager = context.getSystemService(Context.VIRTUAL_DEVICE_SERVICE)

        // Then
        assert(virtualDeviceManager != null)
    }

    @Test
    fun `test application can access webkit service`() {
        // Given & When
        val webkitUpdateService = context.getSystemService(Context.WEBKIT_UPDATE_SERVICE)

        // Then
        assert(webkitUpdateService != null)
    }
} 
 