package app.camera.tdoc.camera_library;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.renderscript.RenderScript;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import app.camera.tdoc.camera_library.Controllers.CameraControllerManager2;
import app.camera.tdoc.camera_library.Views.Preview;
import app.camera.tdoc.camera_library.Views.UI.CameraUI;

import static app.camera.tdoc.camera_library.ServiceResultReceiver.ACTION_PROCESS_FINISHED;

public class CamActivity extends Activity {

    public static final int GALLERY_REQUEST_CODE = 1290;
    private SensorManager mSensorManager = null;
    private Sensor mSensorAccelerometer = null;
    private Sensor mSensorMagnetic = null;
    private CameraUI mainUI = null;
    private MyApplicationInterface applicationInterface = null;
    private Preview preview = null;
    private TextView sessionCount = null;
    private OrientationEventListener orientationEventListener = null;
    private boolean supports_auto_stabilise = false;
    private boolean supports_camera2 = false;
    private SaveLocationHistory save_location_history = null;
    private SaveLocationHistory save_location_history_saf = null;
    private GestureDetector gestureDetector;
    private boolean screen_is_locked = false;
    private Map<Integer, Bitmap> preloaded_bitmap_resources = new Hashtable<Integer, Bitmap>();
    private ValueAnimator gallery_save_anim = null;
    public boolean is_test = false;
    public static final int OPEN_SAF_REQUEST_CODE = 42;
    public Bitmap gallery_bitmap = null;
    public boolean test_low_memory = false;
    public boolean test_have_angle = false;
    public float test_angle = 0.0f;
    public String test_last_saved_image = null;
    private String mBaseFolderName;
    private String mGalleryFolderName;
    private boolean isTrashBtnEnable;
    private boolean isServiceCameraModeEnabled;
    private boolean isSendBtnEnable;
    private boolean isSettingBtnEnable;
    private boolean isVideoBtnEnable;
    private boolean isExposureBtnEnable;
    private boolean isWhiteBalanceEnable;
    private boolean isColorEffectsEnable;
    private boolean isResolutionEnable;
    private boolean isBordersEnable;
    private boolean isIsoEnable;
    private boolean isFocusEnable;
    private boolean isFlashEnable;
    private boolean isAutoStabiliseEnable;
    private boolean isTimeStampEnable;
    private boolean isLocationStampEnable;
    private boolean isPrefixEnable;
    private ArrayList<PrefixList> mImagePrefixList;
    private FrameLayout mProgressLayout;
    private ImageType imageType = ImageType.SCENE_PHOTO;
    private ResponseReceiver receiver = new ResponseReceiver();

    public long getRecipientId() {
        return recipientId;
    }

    public ImageType getImageType() {
        return imageType;
    }

    private long recipientId;

    public static void start(Context context) {
        Intent starter = new Intent(context, CamActivity.class);
        context.startActivity(starter);
    }

    public static Builder newBuilder() {
        return new CamActivity().new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public Builder setFolderName(String baseFolderName) {
            CamActivity.this.mBaseFolderName = baseFolderName;
            return this;
        }

        public Builder setGalleryFolderName(String galleryFolderName) {
            CamActivity.this.mGalleryFolderName = galleryFolderName;
            return this;
        }


        public Builder setImagePrefixList(ArrayList<PrefixList> mImagePrefixList) {
            CamActivity.this.mImagePrefixList = mImagePrefixList;
            return this;
        }

        public Builder setDeleteButtonVisibility(boolean isTrashBtnEnable) {
            CamActivity.this.isTrashBtnEnable = isTrashBtnEnable;
            return this;
        }

        public Builder setServiceCameraModeEnabled(boolean isServiceCameraModeEnabled) {
            CamActivity.this.isServiceCameraModeEnabled = isServiceCameraModeEnabled;
            return this;
        }

        public Builder setSendButtonVisibility(boolean isSendBtnEnable) {
            CamActivity.this.isSendBtnEnable = isSendBtnEnable;
            return this;
        }

        public Builder setSettingButtonVisibility(boolean isSettingBtnEnable) {
            CamActivity.this.isSettingBtnEnable = isSettingBtnEnable;
            return this;
        }

        public Builder setVideoButtonVisibility(boolean isVideoBtnEnable) {
            CamActivity.this.isVideoBtnEnable = isVideoBtnEnable;
            return this;
        }

        public Builder setExposureEnable(boolean isExposureBtnEnable) {
            CamActivity.this.isExposureBtnEnable = isExposureBtnEnable;
            return this;
        }

        public Builder setWhiteBalanceEnable(boolean isWhiteBalanceEnable) {
            CamActivity.this.isWhiteBalanceEnable = isWhiteBalanceEnable;
            return this;
        }

        public Builder setColorEffectsEnable(boolean isColorEffectsEnable) {
            CamActivity.this.isColorEffectsEnable = isColorEffectsEnable;
            return this;
        }

        public Builder setBordersOptionEnable(boolean isBordersEnable) {
            CamActivity.this.isBordersEnable = isBordersEnable;
            return this;
        }

        public Builder setResolutionOptionEnable(boolean isResolutionEnable) {
            CamActivity.this.isResolutionEnable = isResolutionEnable;
            return this;
        }

        public Builder setIsoOptionEnable(boolean isIsoEnable) {
            CamActivity.this.isIsoEnable = isIsoEnable;
            return this;
        }

        public Builder setFocusOptionEnable(boolean isFocusEnable) {
            CamActivity.this.isFocusEnable = isFocusEnable;
            return this;
        }

        public Builder setFlashOptionEnable(boolean isFlashEnable) {
            CamActivity.this.isFlashEnable = isFlashEnable;
            return this;
        }

        public Builder setAutoStabiliseOptionEnable(boolean isAutoStabiliseEnable) {
            CamActivity.this.isAutoStabiliseEnable = isAutoStabiliseEnable;
            return this;
        }

        public Builder setTimeStampeEnable(boolean isTimeStampEnable) {
            CamActivity.this.isTimeStampEnable = isTimeStampEnable;
            return this;
        }

        public Builder setLocationStampEnable(boolean isLocationStampEnable) {
            CamActivity.this.isLocationStampEnable = isLocationStampEnable;
            return this;
        }

        public Builder setPrefixEnable(boolean isPrefixEnable) {
            CamActivity.this.isPrefixEnable = isPrefixEnable;
            return this;
        }

        public Builder setRecipient(long recipientId) {
            CamActivity.this.recipientId = recipientId;
            return this;
        }

        public Builder setImageType(ImageType imageType) {
            CamActivity.this.imageType = imageType;
            return this;
        }

        public Intent build(Context ctx) {
            Intent i = new Intent(ctx, CamActivity.class);

            //file settings
            i.putExtra(PreferenceKeys.getBaseFolderPathKey(), mBaseFolderName); //base folder // Environment.DIRECTORY_DCIM
            i.putExtra(PreferenceKeys.getGalleryFolderPathKey(), mGalleryFolderName);  //gallery folder (for images and video)
            i.putExtra(PreferenceKeys.getPrefixImageListKey(), mImagePrefixList);  // image prefix list
            i.putExtra(PreferenceKeys.getPrefixEnable(), isPrefixEnable);  // image prefix
            i.putExtra(PreferenceKeys.getServiceCameraModeEnabledPreferenceKey(), isServiceCameraModeEnabled);  // to mode

            //params
            i.putExtra(PreferenceKeys.getImageTypeKey(), imageType.name());
            i.putExtra(PreferenceKeys.getRecipientKey(), recipientId);

            //buttons
            i.putExtra(PreferenceKeys.getTrashBtnEnableKey(), isTrashBtnEnable);  //trash button
            i.putExtra(PreferenceKeys.getSendBtnEnableKey(), isSendBtnEnable);  //send images button
            i.putExtra(PreferenceKeys.getSettingEnableKey(), isSettingBtnEnable);  //settiogs button
            i.putExtra(PreferenceKeys.getVideoEnableKey(), isVideoBtnEnable);  //video photo switch button
            i.putExtra(PreferenceKeys.getExposureEnableKey(), isExposureBtnEnable); //exposure button

            //options
            i.putExtra(PreferenceKeys.getWhiteBalanceKey(), isWhiteBalanceEnable);  //WHITE BALANCE
            i.putExtra(PreferenceKeys.getColorEffectsKey(), isColorEffectsEnable);  //COLOR EFFECTS
            i.putExtra(PreferenceKeys.getResolutionKey(), isResolutionEnable);  //RESOLUTION
            i.putExtra(PreferenceKeys.getBordersKey(), isBordersEnable); //BORDERS
            i.putExtra(PreferenceKeys.getIsoKey(), isIsoEnable); //ISO
            i.putExtra(PreferenceKeys.getFocusOptionKey(), isFocusEnable); //FOCUS OPTIONS
            i.putExtra(PreferenceKeys.getFlashOptionKey(), isFlashEnable); //FLASH OPTIONS
            i.putExtra(PreferenceKeys.getAutostabiliseKey(), isAutoStabiliseEnable); //AUTO STABILISE

            i.putExtra(PreferenceKeys.getTimeStampEnable(), isTimeStampEnable); //timestamp on image options
            i.putExtra(PreferenceKeys.getLocationStampEnable(), isLocationStampEnable); //locationStamp on image options
            return i;
        }
    }

    protected void putToPreference(String key, String value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putString(key, value).apply();
    }

    protected void putToPreference(String key, boolean value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    protected void putToPreference(String key, long value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putLong(key, value).apply();
    }

    protected void putToPreference(String key, ArrayList<PrefixList> prefixLists) {
        Set<String> set = new HashSet<String>();
        for (PrefixList pl : prefixLists) {
            set.add((pl.isEmptyLabel() ? getResources().getString(R.string.save_as_label) : pl.getLabel()) + "," + pl.getPrefix());
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putStringSet(key, set).apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity_main);

        Intent i = getIntent();
        putToPreference(PreferenceKeys.getBaseFolderPathKey(), i.getStringExtra(PreferenceKeys.getBaseFolderPathKey()));
        putToPreference(PreferenceKeys.getGalleryFolderPathKey(), i.getStringExtra(PreferenceKeys.getGalleryFolderPathKey()));
        putToPreference(PreferenceKeys.getPrefixImageKey(), i.getStringExtra(PreferenceKeys.getPrefixImageKey()));
        putToPreference(PreferenceKeys.getPrefixImageListKey(), (ArrayList<PrefixList>) i.getSerializableExtra(PreferenceKeys.getPrefixImageListKey()));
        putToPreference(PreferenceKeys.getPrefixEnable(), i.getBooleanExtra(PreferenceKeys.getPrefixEnable(), false));
        putToPreference(PreferenceKeys.getServiceCameraModeEnabledPreferenceKey(), i.getBooleanExtra(PreferenceKeys.getServiceCameraModeEnabledPreferenceKey(), false));

        putToPreference(PreferenceKeys.getTrashBtnEnableKey(), i.getBooleanExtra(PreferenceKeys.getTrashBtnEnableKey(), true));
        putToPreference(PreferenceKeys.getSendBtnEnableKey(), i.getBooleanExtra(PreferenceKeys.getSendBtnEnableKey(), true));
        putToPreference(PreferenceKeys.getSettingEnableKey(), i.getBooleanExtra(PreferenceKeys.getSettingEnableKey(), true));
        putToPreference(PreferenceKeys.getWhiteBalanceKey(), i.getBooleanExtra(PreferenceKeys.getWhiteBalanceKey(), true));
        putToPreference(PreferenceKeys.getColorEffectsKey(), i.getBooleanExtra(PreferenceKeys.getColorEffectsKey(), true));
        putToPreference(PreferenceKeys.getResolutionKey(), i.getBooleanExtra(PreferenceKeys.getResolutionKey(), true));
        putToPreference(PreferenceKeys.getImageTypeKey(), i.getStringExtra(PreferenceKeys.getImageTypeKey()));
        putToPreference(PreferenceKeys.getRecipientKey(), i.getLongExtra(PreferenceKeys.getRecipientKey(), 1L));
        putToPreference(PreferenceKeys.getBordersKey(), i.getBooleanExtra(PreferenceKeys.getBordersKey(), true));
        putToPreference(PreferenceKeys.getIsoKey(), i.getBooleanExtra(PreferenceKeys.getIsoKey(), true));
        putToPreference(PreferenceKeys.getFocusOptionKey(), i.getBooleanExtra(PreferenceKeys.getFocusOptionKey(), true));
        putToPreference(PreferenceKeys.getFlashOptionKey(), i.getBooleanExtra(PreferenceKeys.getFlashOptionKey(), true));
        putToPreference(PreferenceKeys.getExposureEnableKey(), i.getBooleanExtra(PreferenceKeys.getExposureEnableKey(), true));
        putToPreference(PreferenceKeys.getAutostabiliseKey(), i.getBooleanExtra(PreferenceKeys.getAutostabiliseKey(), true));
        putToPreference(PreferenceKeys.getVideoEnableKey(), i.getBooleanExtra(PreferenceKeys.getVideoEnableKey(), true));
        putToPreference(PreferenceKeys.getTimeStampEnable(), i.getBooleanExtra(PreferenceKeys.getTimeStampEnable(), true));
        putToPreference(PreferenceKeys.getLocationStampEnable(), i.getBooleanExtra(PreferenceKeys.getLocationStampEnable(), true));

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        if (getIntent() != null && getIntent().getExtras() != null) {
            is_test = getIntent().getExtras().getBoolean("test_project");
        }

        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        if (activityManager.getLargeMemoryClass() >= 128) {
            supports_auto_stabilise = true;
        }

        mainUI = new CameraUI(this);
        applicationInterface = new MyApplicationInterface(this, savedInstanceState);

        initCamera2Support();
        setWindowFlagsForCamera();

        save_location_history = new SaveLocationHistory(this, "save_location_history", getStorageUtils().getSaveLocation());
        if (applicationInterface.getStorageUtils().isUsingSAF()) {
            save_location_history_saf = new SaveLocationHistory(this, "save_location_history_saf", getStorageUtils().getSaveLocationSAF());
        }

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            mSensorMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }

        mainUI.clearSeekBar();
        mProgressLayout = findViewById(R.id.progress_layout);
        sessionCount = findViewById(R.id.sessionCount);
        preview = new Preview(this, applicationInterface, savedInstanceState, ((ViewGroup) this.findViewById(R.id.preview)));
        orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                CamActivity.this.mainUI.onOrientationChanged(orientation);
            }
        };

        gestureDetector = new GestureDetector(this, new MyGestureDetector());

        preloadIcons(R.array.flash_icons);
        preloadIcons(R.array.focus_mode_icons);

    }

    private void preloadIcons(int icons_id) {
        String[] icons = getResources().getStringArray(icons_id);
        for (int i = 0; i < icons.length; i++) {
            int resource = getResources().getIdentifier(icons[i], null, this.getApplicationContext().getPackageName());
            Bitmap bm = BitmapFactory.decodeResource(getResources(), resource);
            this.preloaded_bitmap_resources.put(resource, bm);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initCamera2Support() {
        supports_camera2 = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CameraControllerManager2 manager2 = new CameraControllerManager2(this);
            supports_camera2 = manager2.getNumberOfCameras() != 0;
            for (int i = 0; i < manager2.getNumberOfCameras() && supports_camera2; i++) {
                if (!manager2.allowCamera2Support(i)) {
                    supports_camera2 = false;
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onDestroy() {
        if (applicationInterface != null) {
            applicationInterface.onDestroy();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            RenderScript.releaseAllContexts();
        }
        for (Map.Entry<Integer, Bitmap> entry : preloaded_bitmap_resources.entrySet()) {
            entry.getValue().recycle();
        }
        preloaded_bitmap_resources.clear();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @SuppressWarnings("deprecation")
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {

            case KeyEvent.KEYCODE_MENU: {
                // openSettings();
                return true;
            }
            case KeyEvent.KEYCODE_CAMERA: {
                if (event.getRepeatCount() == 0) {
                    takePicture();
                    return true;
                }
            }
            case KeyEvent.KEYCODE_FOCUS: {
                if (event.getDownTime() == event.getEventTime() && !preview.isFocusWaiting()) {
                    preview.requestAutoFocus();
                }
                return true;
            }
            case KeyEvent.KEYCODE_ZOOM_IN: {
                this.zoomIn();
                return true;
            }
            case KeyEvent.KEYCODE_ZOOM_OUT: {
                this.zoomOut();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    public void zoomIn() {
        mainUI.changeSeekbar(R.id.zoom_seekbar, -1);
    }

    public void zoomOut() {
        mainUI.changeSeekbar(R.id.zoom_seekbar, 1);
    }


    private SensorEventListener accelerometerListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            preview.onAccelerometerSensorChanged(event);
        }
    };

    private SensorEventListener magneticListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            preview.onMagneticSensorChanged(event);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().getRootView().setBackgroundColor(Color.BLACK);
        mSensorManager.registerListener(accelerometerListener, mSensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(magneticListener, mSensorMagnetic, SensorManager.SENSOR_DELAY_NORMAL);
        orientationEventListener.enable();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int photoCount = sharedPreferences.getInt(PreferenceKeys.getSessionPhotoCountPreferenceKey(), 0);
        sessionCount.setText(MessageFormat.format("( {0} )", photoCount));
        initLocation();
        mainUI.layoutUI();
        updateGalleryIcon();
        preview.onResume();
        mProgressLayout.setVisibility(View.GONE);

        IntentFilter filter = new IntentFilter(ACTION_PROCESS_FINISHED);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(receiver, filter);
    }

    public static final String PARAM_OUT_MSG = "OUT_TEXT";

    public class ResponseReceiver extends ServiceResultReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_PROCESS_FINISHED.equals(intent.getAction())) {
                String message = intent.getStringExtra(PARAM_OUT_MSG);
                if (message != null && !message.isEmpty()) {
                    NotificationHelper.showNotificationMessage(message, context);
                }
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void onPause() {
        waitUntilImageQueueEmpty(); // so we don't risk losing any images
        super.onPause();
        mainUI.destroyPopup();
        mSensorManager.unregisterListener(accelerometerListener);
        mSensorManager.unregisterListener(magneticListener);
        orientationEventListener.disable();
        applicationInterface.getLocationSupplier().freeLocationListeners();
        applicationInterface.clearLastImages(); // this should happen when pausing the preview, but call explicitly just to be safe
        preview.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        preview.setCameraDisplayOrientation();
        super.onConfigurationChanged(newConfig);
    }

    public void waitUntilImageQueueEmpty() {
        applicationInterface.getImageSaver().waitUntilDone();
    }

    public void clickedTakePhoto(View view) {
        this.takePicture();
    }


/*    public void clickedSwitchVideo(View view) {
        this.closePopup();
        View switchVideoButton = findViewById(R.id.switch_video);
        switchVideoButton.setEnabled(false);
        this.preview.switchVideo(false);
        switchVideoButton.setEnabled(true);
        mainUI.setTakePhotoIcon();
    }*/

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void clickedExposure(View view) {
        mainUI.toggleExposureUI();
    }

    private static double seekbarScaling(double frac) {
        return (Math.pow(100.0, frac) - 1.0) / 99.0;
    }

    private static double seekbarScalingInverse(double scaling) {
        return Math.log(99.0 * scaling + 1.0) / Math.log(100.0);
    }

    private void setProgressSeekbarScaled(SeekBar seekBar, double min_value, double max_value, double value) {
        seekBar.setMax(100);
        double scaling = (value - min_value) / (max_value - min_value);
        double frac = CamActivity.seekbarScalingInverse(scaling);
        int percent = (int) (frac * 100.0 + 0.5); // add 0.5 for rounding
        if (percent < 0)
            percent = 0;
        else if (percent > 100)
            percent = 100;
        seekBar.setProgress(percent);
    }

    public boolean popupIsOpen() {
        return mainUI.popupIsOpen();
    }


    public void closePopup() {
        mainUI.closePopup();
    }

    public Bitmap getPreloadedBitmap(int resource) {
        return this.preloaded_bitmap_resources.get(resource);
    }

    public void clickedPopupSettings(View view) {
        mainUI.togglePopupSettings();
    }


    public void updateForSettings(String toast_message) {

        String saved_focus_value = null;
        if (preview.getCameraController() != null && preview.isVideo() && !preview.focusIsVideo()) {
            saved_focus_value = preview.getCurrentFocusValue(); // n.b., may still be null
            preview.updateFocusForVideo(false);
        }

        save_location_history.updateFolderHistory(getStorageUtils().getSaveLocation(), true);
        boolean need_reopen = false;
        if (preview.getCameraController() != null) {
            String scene_mode = preview.getCameraController().getSceneMode();
            String key = PreferenceKeys.getSceneModePreferenceKey();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String value = sharedPreferences.getString(key, preview.getCameraController().getDefaultSceneMode());
            if (!value.equals(scene_mode)) {
                need_reopen = true;
            }
        }

        mainUI.layoutUI();
        initLocation();

        if (need_reopen || preview.getCameraController() == null) {
            preview.onPause();
            preview.onResume();
        } else {
            preview.setCameraDisplayOrientation();
            preview.pausePreview();
            preview.setupCamera(false);
        }

        if (saved_focus_value != null) {
            preview.updateFocus(saved_focus_value, true, false);
        }
    }


    @Override
    public void onBackPressed() {
        if (popupIsOpen()) {
            closePopup();
            return;
        }
        super.onBackPressed();
    }


    public void setWindowFlagsForCamera() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        if (sharedPreferences.getBoolean(PreferenceKeys.getKeepDisplayOnPreferenceKey(), true)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        if (sharedPreferences.getBoolean(PreferenceKeys.getShowWhenLockedPreferenceKey(), true)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }

        {
            WindowManager.LayoutParams layout = getWindow().getAttributes();
            if (sharedPreferences.getBoolean(PreferenceKeys.getMaxBrightnessPreferenceKey(), true)) {
                layout.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
            } else {
                layout.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            }
            getWindow().setAttributes(layout);
        }

    }

    public void setWindowFlagsForSettings() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        {
            WindowManager.LayoutParams layout = getWindow().getAttributes();
            layout.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            getWindow().setAttributes(layout);
        }

    }

    public void showPreview(boolean show) {
        final ViewGroup container = findViewById(R.id.hide_container);
        container.setBackgroundColor(Color.BLACK);
        container.setAlpha(show ? 0.0f : 1.0f);
    }

    public void updateGalleryIconToBlank() {
        ImageButton galleryButton = this.findViewById(R.id.gallery);
        int bottom = galleryButton.getPaddingBottom();
        int top = galleryButton.getPaddingTop();
        int right = galleryButton.getPaddingRight();
        int left = galleryButton.getPaddingLeft();
        galleryButton.setImageBitmap(null);
        galleryButton.setImageResource(R.drawable.ic_gallery);
        galleryButton.setPadding(left, top, right, bottom);
        gallery_bitmap = null;
    }

    void updateGalleryIcon(Bitmap thumbnail) {
        ImageButton galleryButton = this.findViewById(R.id.gallery);
        galleryButton.setImageBitmap(thumbnail);
        gallery_bitmap = thumbnail;
    }

    @SuppressLint("StaticFieldLeak")
    public void updateGalleryIcon() {

        new AsyncTask<Void, Void, Bitmap>() {

            protected Bitmap doInBackground(Void... params) {
                StorageUtils.Media media = applicationInterface.getStorageUtils().getLatestMedia();
                Bitmap thumbnail = null;
                KeyguardManager keyguard_manager = (KeyguardManager) CamActivity.this.getSystemService(Context.KEYGUARD_SERVICE);
                boolean is_locked = keyguard_manager != null && keyguard_manager.inKeyguardRestrictedInputMode();
                if (media != null && getContentResolver() != null && !is_locked) {
                    if (media.video) {
                        thumbnail = MediaStore.Video.Thumbnails.getThumbnail(getContentResolver(), media.id, MediaStore.Video.Thumbnails.MINI_KIND, null);
                    } else {
                        thumbnail = MediaStore.Images.Thumbnails.getThumbnail(getContentResolver(), media.id, MediaStore.Images.Thumbnails.MINI_KIND, null);
                    }
                    if (thumbnail != null) {
                        if (media.orientation != 0) {
                            Matrix matrix = new Matrix();
                            matrix.setRotate(media.orientation, thumbnail.getWidth() * 0.5f, thumbnail.getHeight() * 0.5f);
                            try {
                                Bitmap rotated_thumbnail = Bitmap.createBitmap(thumbnail, 0, 0, thumbnail.getWidth(), thumbnail.getHeight(), matrix, true);
                                if (rotated_thumbnail != thumbnail) {
                                    thumbnail.recycle();
                                    thumbnail = rotated_thumbnail;
                                }
                            } catch (Throwable ignored) {
                            }
                        }
                    }
                }
                return thumbnail;
            }

            protected void onPostExecute(Bitmap thumbnail) {
                applicationInterface.getStorageUtils().clearLastMediaScanned();
                if (thumbnail != null) {
                    updateGalleryIcon(thumbnail);
                } else {
                    updateGalleryIconToBlank();
                }
            }
        }.execute();

    }

    public void startGalleryActivityForResult() {
        Intent intent = new Intent(this, GalleryActivity.class);
        intent.putExtra(PreferenceKeys.getLastPhotoModeKey(), true);
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
        mProgressLayout.setVisibility(View.GONE);
    }

    void savingImage(final boolean started) {

        this.runOnUiThread(new Runnable() {
            public void run() {

                final ImageButton galleryButton = findViewById(R.id.gallery);
                if (started) {
                    mProgressLayout.setVisibility(View.VISIBLE);
                    if (gallery_save_anim == null) {
                        gallery_save_anim = ValueAnimator.ofInt(Color.argb(200, 255, 255, 255), Color.argb(63, 255, 255, 255));
                        gallery_save_anim.setEvaluator(new ArgbEvaluator());
                        gallery_save_anim.setRepeatCount(ValueAnimator.INFINITE);
                        gallery_save_anim.setRepeatMode(ValueAnimator.REVERSE);
                        gallery_save_anim.setDuration(500);
                    }
                    gallery_save_anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            galleryButton.setColorFilter((Integer) animation.getAnimatedValue(), PorterDuff.Mode.MULTIPLY);
                        }
                    });
                    gallery_save_anim.start();
                } else if (gallery_save_anim != null) {
                    gallery_save_anim.cancel();
                    startGalleryActivityForResult();
                }
                galleryButton.setColorFilter(null);
            }
        });
    }

    public void clickedExit(View view) {
        finish();
    }


    public void clickedGallery(View view) {
        if (!getPreview().isVideo()) {
            Intent i = new Intent(this, GalleryActivity.class);
            startActivityForResult(i, GALLERY_REQUEST_CODE);
        } else {
            Uri uri = applicationInterface.getStorageUtils().getLastMediaScanned();
            if (uri == null) {
                StorageUtils.Media media = applicationInterface.getStorageUtils().getLatestMedia();
                if (media != null) {
                    uri = media.uri;
                }
            }

            if (uri != null) {
                try {
                    ContentResolver cr = getContentResolver();
                    ParcelFileDescriptor pfd = cr.openFileDescriptor(uri, "r");
                    if (pfd == null) {
                        uri = null;
                    } else {
                        pfd.close();
                    }
                } catch (IOException e) {
                    uri = null;
                }
            }
            if (uri == null) {
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            }
            if (!is_test) {
                final String REVIEW_ACTION = "com.android.camera.action.REVIEW";
                try {
                    Intent intent = new Intent(REVIEW_ACTION, uri);
                    this.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        this.startActivity(intent);
                    }
                }
            }
        }
    }


    public void updateFolderHistorySAF(String save_folder) {
        if (save_location_history_saf == null) {
            save_location_history_saf = new SaveLocationHistory(this, "save_location_history_saf", save_folder);
        }
        save_location_history_saf.updateFolderHistory(save_folder, true);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == GALLERY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                resultData.getStringExtra(PreferenceKeys.getKeyForNewImagePath());
                Toast.makeText(this, "Файл сохранен", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == OPEN_SAF_REQUEST_CODE) {
            if (resultCode == RESULT_OK && resultData != null) {
                Uri treeUri = resultData.getData();
                final int takeFlags = resultData.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(Objects.requireNonNull(treeUri), takeFlags);
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(PreferenceKeys.getSaveLocationSAFPreferenceKey(), treeUri.toString());
                editor.apply();
                updateFolderHistorySAF(treeUri.toString());
            } else {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                String uri = sharedPreferences.getString(PreferenceKeys.getSaveLocationSAFPreferenceKey(), "");
                if (uri.length() == 0) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(PreferenceKeys.getUsingSAFPreferenceKey(), false);
                    editor.apply();
                }
            }
            boolean saf_dialog_from_preferences = false;
            if (!saf_dialog_from_preferences) {
                setWindowFlagsForCamera();
                showPreview(true);
            }
        }
    }


    private void takePicture() {
        closePopup();
        this.preview.takePicturePressed();
    }

    void lockScreen() {
        findViewById(R.id.locker).setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
        screen_is_locked = true;
    }

    void unlockScreen() {
        findViewById(R.id.locker).setOnTouchListener(null);
        screen_is_locked = false;
    }

    public boolean isScreenLocked() {
        return screen_is_locked;
    }

    class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                final ViewConfiguration vc = ViewConfiguration.get(CamActivity.this);
                final float scale = getResources().getDisplayMetrics().density;
                final int swipeMinDistance = (int) (160 * scale + 0.5f);
                final int swipeThresholdVelocity = vc.getScaledMinimumFlingVelocity();
                float xdist = e1.getX() - e2.getX();
                float ydist = e1.getY() - e2.getY();
                float dist2 = xdist * xdist + ydist * ydist;
                float vel2 = velocityX * velocityX + velocityY * velocityY;
                if (dist2 > swipeMinDistance * swipeMinDistance && vel2 > swipeThresholdVelocity * swipeThresholdVelocity) {
                    unlockScreen();
                }
            } catch (Exception ignored) {
            }
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        if (this.preview != null) {
            preview.onSaveInstanceState(state);
        }
        if (this.applicationInterface != null) {
            applicationInterface.onSaveInstanceState(state);
        }
    }

    public boolean supportsExposureButton() {
        if (preview.getCameraController() == null)
            return false;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String iso_value = sharedPreferences.getString(PreferenceKeys.getISOPreferenceKey(), preview.getCameraController().getDefaultISO());
        boolean manual_iso = !iso_value.equals(preview.getCameraController().getDefaultISO());
        return preview.supportsExposures() || (manual_iso && preview.supportsISORange());
    }

    void cameraSetup() {


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        {
            SeekBar zoomSeekBar = findViewById(R.id.zoom_seekbar);

            if (preview.supportsZoom()) {
                zoomSeekBar.setOnSeekBarChangeListener(null);
                zoomSeekBar.setMax(preview.getMaxZoom());
                zoomSeekBar.setProgress(preview.getMaxZoom() - preview.getCameraController().getZoom());
                zoomSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        preview.zoomTo(preview.getMaxZoom() - progress);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

                if (!sharedPreferences.getBoolean(PreferenceKeys.getShowZoomSliderControlsPreferenceKey(), true)) {
                    zoomSeekBar.setVisibility(View.INVISIBLE);
                }
            } else {
                zoomSeekBar.setVisibility(View.GONE);
            }
        }
        {
            SeekBar focusSeekBar = findViewById(R.id.focus_seekbar);
            focusSeekBar.setOnSeekBarChangeListener(null);
            setProgressSeekbarScaled(focusSeekBar, 0.0, preview.getMinimumFocusDistance(), preview.getCameraController().getFocusDistance());
            focusSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    double frac = progress / 100.0;
                    double scaling = CamActivity.seekbarScaling(frac);
                    float focus_distance = (float) (scaling * preview.getMinimumFocusDistance());
                    preview.setFocusDistance(focus_distance);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
            final int visibility = preview.getCurrentFocusValue() != null && this.getPreview().getCurrentFocusValue().equals("focus_mode_manual2") ? View.VISIBLE : View.INVISIBLE;
            focusSeekBar.setVisibility(visibility);
        }
        {
            if (preview.supportsISORange()) {
                SeekBar iso_seek_bar = findViewById(R.id.iso_seekbar);
                iso_seek_bar.setOnSeekBarChangeListener(null);
                setProgressSeekbarScaled(iso_seek_bar, preview.getMinimumISO(), preview.getMaximumISO(), preview.getCameraController().getISO());
                iso_seek_bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        double frac = progress / 100.0;
                        double scaling = CamActivity.seekbarScaling(frac);
                        int min_iso = preview.getMinimumISO();
                        int max_iso = preview.getMaximumISO();
                        int iso = min_iso + (int) (scaling * (max_iso - min_iso));
                        preview.setISO(iso);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });
                if (preview.supportsExposureTime()) {
                    SeekBar exposure_time_seek_bar = findViewById(R.id.exposure_time_seekbar);
                    exposure_time_seek_bar.setOnSeekBarChangeListener(null);
                    setProgressSeekbarScaled(exposure_time_seek_bar, preview.getMinimumExposureTime(), preview.getMaximumExposureTime(), preview.getCameraController().getExposureTime());
                    exposure_time_seek_bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            double frac = progress / 100.0;
                            double scaling = CamActivity.seekbarScaling(frac);
                            long min_exposure_time = preview.getMinimumExposureTime();
                            long max_exposure_time = preview.getMaximumExposureTime();
                            long exposure_time = min_exposure_time + (long) (scaling * (max_exposure_time - min_exposure_time));
                            preview.setExposureTime(exposure_time);
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    });
                }
            }
        }
        {
            if (preview.supportsExposures()) {
                final int min_exposure = preview.getMinimumExposure();
                SeekBar exposure_seek_bar = findViewById(R.id.exposure_seekbar);
                exposure_seek_bar.setOnSeekBarChangeListener(null);
                exposure_seek_bar.setMax(preview.getMaximumExposure() - min_exposure);
                exposure_seek_bar.setProgress(preview.getCurrentExposure() - min_exposure);
                exposure_seek_bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        preview.setExposure(min_exposure + progress);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });


            }
        }
        mainUI.setPopupIcon();
        mainUI.setTakePhotoIcon();
    }

    public boolean supportsAutoStabilise() {
        return this.supports_auto_stabilise;
    }


    public boolean supportsCamera2() {
        return this.supports_camera2;
    }


    public Preview getPreview() {
        return this.preview;
    }

    public CameraUI getMainUI() {
        return this.mainUI;
    }

    public MyApplicationInterface getApplicationInterface() {
        return this.applicationInterface;
    }

    public StorageUtils getStorageUtils() {
        return this.applicationInterface.getStorageUtils();
    }


    private void initLocation() {
        if (!applicationInterface.getLocationSupplier().setupLocationListener()) {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(PreferenceKeys.getLocationStampEnable(), true);
            editor.apply();
        }
    }
}
