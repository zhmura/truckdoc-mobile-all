package app.camera.tdoc.camera_library;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Location;
import android.media.CamcorderProfile;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import app.camera.tdoc.camera_library.Controllers.CameraController;
import app.camera.tdoc.camera_library.Views.ApplicationInterface;
import app.camera.tdoc.camera_library.Views.UI.DrawPreview;

public class MyApplicationInterface implements ApplicationInterface {

    private CamActivity main_activity = null;
    private LocationSupplier locationSupplier = null;
    private StorageUtils storageUtils = null;
    private DrawPreview drawPreview = null;
    private ImageSaver imageSaver = null;

    private Rect text_bounds = new Rect();

    private boolean last_images_saf = false;


    private static class LastImage {
        boolean share = false;
        public String name = null;
        public Uri uri = null;

        public String getName() {
            return name;
        }

        public Uri getUri() {
            return uri;
        }

        LastImage(Uri uri, boolean share) {
            this.name = null;
            this.uri = uri;
            this.share = share;
        }

        LastImage(String filename, boolean share) {
            this.name = filename;
            this.uri = Uri.parse("file://" + this.name);
            this.share = share;
        }
    }

    private List<LastImage> last_images = new Vector<LastImage>();
    private int cameraId = 0;
    private int zoom_factor = 0;
    private float focus_distance = 0.0f;

    MyApplicationInterface(CamActivity main_activity, Bundle savedInstanceState) {
        this.main_activity = main_activity;
        this.locationSupplier = new LocationSupplier(main_activity);
        this.storageUtils = new StorageUtils(main_activity);
        this.drawPreview = new DrawPreview(main_activity, this);

        this.imageSaver = new ImageSaver(main_activity);
        this.imageSaver.start();

        if (savedInstanceState != null) {
            cameraId = savedInstanceState.getInt("cameraId", 0);
            zoom_factor = savedInstanceState.getInt("zoom_factor", 0);
            focus_distance = savedInstanceState.getFloat("focus_distance", 0.0f);
        }

    }

    void onSaveInstanceState(Bundle state) {
        state.putInt("cameraId", cameraId);
        state.putInt("zoom_factor", zoom_factor);
        state.putFloat("focus_distance", focus_distance);
    }

    protected void onDestroy() {
        if (imageSaver != null) {
            imageSaver.onDestroy();
        }
    }

    LocationSupplier getLocationSupplier() {
        return locationSupplier;
    }

    StorageUtils getStorageUtils() {
        return storageUtils;
    }

    ImageSaver getImageSaver() {
        return imageSaver;
    }

    @Override
    public Context getContext() {
        return main_activity;
    }

    @Override
    public boolean useCamera2() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return main_activity.supportsCamera2() && sharedPreferences.getBoolean(PreferenceKeys.getUseCamera2PreferenceKey(), false);
    }

    @Override
    public Location getLocation() {
        return locationSupplier.getLocation();
    }

    @Override
    public int createOutputVideoMethod() {
        String action = main_activity.getIntent().getAction();
        if (MediaStore.ACTION_VIDEO_CAPTURE.equals(action)) {
            Bundle myExtras = main_activity.getIntent().getExtras();
            if (myExtras != null) {
                Uri intent_uri = myExtras.getParcelable(MediaStore.EXTRA_OUTPUT);
                if (intent_uri != null) {
                    return VIDEOMETHOD_URI;
                }
            }
            return VIDEOMETHOD_FILE;
        }
        boolean using_saf = storageUtils.isUsingSAF();
        return using_saf ? VIDEOMETHOD_SAF : VIDEOMETHOD_FILE;
    }

    @Override
    public File createOutputVideoFile() throws IOException {
        return storageUtils.createOutputMediaFile(StorageUtils.MEDIA_TYPE_VIDEO, "", "mp4", new Date());
    }

    @Override
    public Uri createOutputVideoSAF() throws IOException {
        return storageUtils.createOutputMediaFileSAF(StorageUtils.MEDIA_TYPE_VIDEO, "", "mp4", new Date());
    }

    @Override
    public Uri createOutputVideoUri() throws IOException {
        String action = main_activity.getIntent().getAction();
        if (MediaStore.ACTION_VIDEO_CAPTURE.equals(action)) {
            Bundle myExtras = main_activity.getIntent().getExtras();
            if (myExtras != null) {
                Uri intent_uri = myExtras.getParcelable(MediaStore.EXTRA_OUTPUT);
                if (intent_uri != null) {
                    return intent_uri;
                }
            }
        }
        throw new RuntimeException(); // programming error if we arrived here
    }

    @Override
    public int getCameraIdPref() {
        return cameraId;
    }

    @Override
    public String getFlashPref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getString(PreferenceKeys.getFlashPreferenceKey(cameraId), "");
    }

    @Override
    public String getFocusPref(boolean is_video) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getString(PreferenceKeys.getFocusPreferenceKey(cameraId, is_video), "");
    }

    @Override
    public boolean isVideoPref() {
        String action = main_activity.getIntent().getAction();
        if (MediaStore.INTENT_ACTION_VIDEO_CAMERA.equals(action) || MediaStore.ACTION_VIDEO_CAPTURE.equals(action)) {
            return true;
        } else if (MediaStore.ACTION_IMAGE_CAPTURE.equals(action) || MediaStore.ACTION_IMAGE_CAPTURE_SECURE.equals(action) || MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA.equals(action) || MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE.equals(action)) {
            return false;
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getBoolean(PreferenceKeys.getIsVideoPreferenceKey(), false);
    }

    @Override
    public String getSceneModePref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getString(PreferenceKeys.getSceneModePreferenceKey(), "auto");
    }

    @Override
    public String getColorEffectPref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getString(PreferenceKeys.getColorEffectPreferenceKey(), "none");
    }

    @Override
    public String getWhiteBalancePref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getString(PreferenceKeys.getWhiteBalancePreferenceKey(), "auto");
    }

    @Override
    public String getISOPref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getString(PreferenceKeys.getISOPreferenceKey(), "auto");
    }

    @Override
    public int getExposureCompensationPref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String value = sharedPreferences.getString(PreferenceKeys.getExposurePreferenceKey(), "0");
        int exposure = 0;
        try {
            exposure = Integer.parseInt(value);
        } catch (NumberFormatException exception) {
        }
        return exposure;
    }

    @Override
    public Pair<Integer, Integer> getCameraResolutionPref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String resolution_value = sharedPreferences.getString(PreferenceKeys.getResolutionPreferenceKey(cameraId), "");
        if (resolution_value.length() > 0) {
            // parse the saved size, and make sure it is still valid
            int index = resolution_value.indexOf(' ');
            if (index == -1) {
            } else {
                String resolution_w_s = resolution_value.substring(0, index);
                String resolution_h_s = resolution_value.substring(index + 1);
                try {
                    int resolution_w = Integer.parseInt(resolution_w_s);
                    int resolution_h = Integer.parseInt(resolution_h_s);
                    return new Pair<Integer, Integer>(resolution_w, resolution_h);
                } catch (NumberFormatException exception) {
                }
            }
        }
        return null;
    }

    @Override
    public int getImageQualityPref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String image_quality_s = sharedPreferences.getString(PreferenceKeys.getQualityPreferenceKey(), "90");
        int image_quality = 0;
        try {
            image_quality = Integer.parseInt(image_quality_s);
        } catch (NumberFormatException exception) {
            image_quality = 90;
        }
        return image_quality;
    }


    @Override
    public String getVideoQualityPref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getString(PreferenceKeys.getVideoQualityPreferenceKey(cameraId), "");
    }

    @Override
    public boolean getVideoStabilizationPref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getBoolean(PreferenceKeys.getVideoStabilizationPreferenceKey(), false);
    }


    @Override
    public String getVideoBitratePref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getString(PreferenceKeys.getVideoBitratePreferenceKey(), "default");
    }

    @Override
    public String getVideoFPSPref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getString(PreferenceKeys.getVideoFPSPreferenceKey(), "default");
    }

    @Override
    public long getVideoMaxDurationPref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String video_max_duration_value = sharedPreferences.getString(PreferenceKeys.getVideoMaxDurationPreferenceKey(), "0");
        long video_max_duration = 0;
        try {
            video_max_duration = (long) Integer.parseInt(video_max_duration_value) * 1000;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            video_max_duration = 0;
        }
        return video_max_duration;
    }

    @Override
    public int getVideoRestartTimesPref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String restart_value = sharedPreferences.getString(PreferenceKeys.getVideoRestartPreferenceKey(), "0");
        int remaining_restart_video = 0;
        try {
            remaining_restart_video = Integer.parseInt(restart_value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            remaining_restart_video = 0;
        }
        return remaining_restart_video;
    }

    @Override
    public long getVideoMaxFileSizePref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String video_max_filesize_value = sharedPreferences.getString(PreferenceKeys.getVideoMaxFileSizePreferenceKey(), "0");
        long video_max_filesize = 0;
        try {
            video_max_filesize = Integer.parseInt(video_max_filesize_value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            video_max_filesize = 0;
        }
        return video_max_filesize;
    }

    @Override
    public boolean getVideoRestartMaxFileSizePref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getBoolean(PreferenceKeys.getVideoRestartMaxFileSizePreferenceKey(), true);
    }

    @Override
    public boolean getVideoFlashPref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getBoolean(PreferenceKeys.getVideoFlashPreferenceKey(), false);
    }

    @Override
    public String getPreviewSizePref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getString(PreferenceKeys.getPreviewSizePreferenceKey(), "preference_preview_size_wysiwyg");
    }

    @Override
    public String getPreviewRotationPref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getString(PreferenceKeys.getRotatePreviewPreferenceKey(), "0");
    }

    @Override
    public String getLockOrientationPref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getString(PreferenceKeys.getLockOrientationPreferenceKey(), "none");
    }

    @Override
    public boolean getTouchCapturePref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String value = sharedPreferences.getString(PreferenceKeys.getTouchCapturePreferenceKey(), "none");
        return value.equals("single");
    }

    @Override
    public boolean getDoubleTapCapturePref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String value = sharedPreferences.getString(PreferenceKeys.getTouchCapturePreferenceKey(), "none");
        return value.equals("double");
    }

    @Override
    public boolean getPausePreviewPref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getBoolean(PreferenceKeys.getPausePreviewPreferenceKey(), false);
    }

    @Override
    public boolean getShutterSoundPref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getBoolean(PreferenceKeys.getShutterSoundPreferenceKey(), true);
    }

    @Override
    public boolean getStartupFocusPref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getBoolean(PreferenceKeys.getStartupFocusPreferenceKey(), true);
    }

    @Override
    public boolean getGeotaggingPref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getBoolean(PreferenceKeys.getLocationStampEnable(), true);
    }

    @Override
    public boolean getRequireLocationPref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getBoolean(PreferenceKeys.getRequireLocationPreferenceKey(), true);
    }


    private boolean getAutoStabilisePref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean auto_stabilise = sharedPreferences.getBoolean(PreferenceKeys.getAutoStabilisePreferenceKey(), false);
        return auto_stabilise && main_activity.supportsAutoStabilise();
    }

    private String getStampPref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getString(PreferenceKeys.getStampPreferenceKey(), "preference_stamp_no");
    }

    private String getStampDateFormatPref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getString(PreferenceKeys.getStampDateFormatPreferenceKey(), "preference_stamp_dateformat_default");
    }

    private String getStampTimeFormatPref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getString(PreferenceKeys.getStampTimeFormatPreferenceKey(), "preference_stamp_timeformat_default");
    }

    private String getStampGPSFormatPref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getString(PreferenceKeys.getStampGPSFormatPreferenceKey(), "preference_stamp_gpsformat_default");
    }

    private boolean getStampNeed() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getBoolean(PreferenceKeys.getTimeStampEnable(), true);
    }


    @Override
    public int getZoomPref() {
        return zoom_factor;
    }

    @Override
    public long getExposureTimePref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getLong(PreferenceKeys.getExposureTimePreferenceKey(), CameraController.EXPOSURE_TIME_DEFAULT);
    }

    @Override
    public float getFocusDistancePref() {
        return focus_distance;
    }


    @Override
    public boolean isTestAlwaysFocus() {
        return main_activity.is_test;
    }

    @Override
    public void stoppedVideo(final int video_method, final Uri uri, final String filename) {
        boolean done = false;
        if (video_method == VIDEOMETHOD_FILE) {
            if (filename != null) {
                File file = new File(filename);
                storageUtils.broadcastFile(file, false, true, true);
                done = true;
            }
        } else {
            if (uri != null) {
                File real_file = storageUtils.getFileFromDocumentUriSAF(uri);
                if (real_file != null) {
                    storageUtils.broadcastFile(real_file, false, true, true);
                    main_activity.test_last_saved_image = real_file.getAbsolutePath();
                } else {
                    storageUtils.announceUri(uri, false, true);
                }
                done = true;
            }
        }

        String action = main_activity.getIntent().getAction();
        if (MediaStore.ACTION_VIDEO_CAPTURE.equals(action)) {
            if (!(done && video_method == VIDEOMETHOD_FILE)) {
                Intent output = null;
                if (done) {
                    // may need to pass back the Uri we saved to, if the calling application didn't specify a Uri
                    // set note above for VIDEOMETHOD_FILE
                    // n.b., currently this code is not used, as we always switch to VIDEOMETHOD_FILE if the calling application didn't specify a Uri, but I've left this here for possible future behaviour
                    if (video_method == VIDEOMETHOD_SAF) {
                        output = new Intent();
                        output.setData(uri);
                    }
                }
                main_activity.setResult(done ? Activity.RESULT_OK : Activity.RESULT_CANCELED, output);
                main_activity.finish();
            }
        } else if (done) {
            // create thumbnail
            long time_s = System.currentTimeMillis();
            Bitmap thumbnail = null;
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                if (video_method == VIDEOMETHOD_FILE) {
                    File file = new File(filename);
                    retriever.setDataSource(file.getPath());
                } else {
                    ParcelFileDescriptor pfd_saf = getContext().getContentResolver().openFileDescriptor(uri, "r");
                    retriever.setDataSource(pfd_saf.getFileDescriptor());
                }
                thumbnail = retriever.getFrameAtTime(-1);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (RuntimeException e) {
                e.printStackTrace();
            } finally {
                try {
                    retriever.release();
                } catch (RuntimeException | java.io.IOException ex) {
                }
            }
            if (thumbnail != null) {
                ImageButton galleryButton = main_activity.findViewById(R.id.gallery);
                int width = thumbnail.getWidth();
                int height = thumbnail.getHeight();
                if (width > galleryButton.getWidth()) {
                    float scale = (float) galleryButton.getWidth() / width;
                    int new_width = Math.round(scale * width);
                    int new_height = Math.round(scale * height);
                    Bitmap scaled_thumbnail = Bitmap.createScaledBitmap(thumbnail, new_width, new_height, true);
                    // careful, as scaled_thumbnail is sometimes not a copy!
                    if (scaled_thumbnail != thumbnail) {
                        thumbnail.recycle();
                        thumbnail = scaled_thumbnail;
                    }
                }
                final Bitmap thumbnail_f = thumbnail;
                main_activity.runOnUiThread(new Runnable() {
                    public void run() {
                        updateThumbnail(thumbnail_f);
                    }
                });
            }
        }
    }

    @Override
    public void cameraSetup() {
        main_activity.cameraSetup();
        drawPreview.clearContinuousFocusMove();
    }

    @Override
    public void onContinuousFocusMove(boolean start) {
        drawPreview.onContinuousFocusMove(start);
    }

    @Override
    public void touchEvent(MotionEvent event) {
        main_activity.getMainUI().clearSeekBar();
        main_activity.getMainUI().closePopup();
    }

    @Override
    public void startingVideo() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (sharedPreferences.getBoolean(PreferenceKeys.getLockVideoPreferenceKey(), false)) {
            main_activity.lockScreen();
        }

        ImageButton view = main_activity.findViewById(R.id.take_photo);
        view.setImageResource(R.drawable.ic_take_video_rec);
        view.setContentDescription(getContext().getResources().getString(R.string.stop_video));
        view.setTag(R.drawable.ic_take_video_rec); // for testing
    }

    @Override
    public void stoppingVideo() {
        main_activity.unlockScreen();
        ImageButton view = main_activity.findViewById(R.id.take_photo);
        view.setImageResource(R.drawable.take_video_selector);
        view.setContentDescription(getContext().getResources().getString(R.string.start_video));
        view.setTag(R.drawable.take_video_selector); // for testing
    }

    @Override
    public void onVideoInfo(int what, int extra) {
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED || what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
            int message_id = 0;
            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                message_id = R.string.video_max_duration;
            } else {
                message_id = R.string.video_max_filesize;
            }
            String debug_value = "info_" + what + "_" + extra;
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("last_video_error", debug_value);
            editor.apply();
        }
    }

    @Override
    public void onFailedStartPreview() {
    }

    @Override
    public void onPhotoError() {
    }

    @Override
    public void onVideoError(int what, int extra) {
        int message_id = R.string.video_error_unknown;
        if (what == MediaRecorder.MEDIA_ERROR_SERVER_DIED) {
            message_id = R.string.video_error_server_died;
        }
        String debug_value = "error_" + what + "_" + extra;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("last_video_error", debug_value);
        editor.apply();
    }

    @Override
    public void onVideoRecordStartError(CamcorderProfile profile) {
        String error_message = "";
        String features = main_activity.getPreview().getErrorFeatures(profile);
        if (features.length() > 0) {
            error_message = getContext().getResources().getString(R.string.sorry) + ", " + features + " " + getContext().getResources().getString(R.string.not_supported);
        } else {
            error_message = getContext().getResources().getString(R.string.failed_to_record_video);
        }
        ImageButton view = main_activity.findViewById(R.id.take_photo);
        view.setImageResource(R.drawable.take_video_selector);
        view.setContentDescription(getContext().getResources().getString(R.string.start_video));
        view.setTag(R.drawable.take_video_selector); // for testing
    }

    @Override
    public void onVideoRecordStopError(CamcorderProfile profile) {
        String features = main_activity.getPreview().getErrorFeatures(profile);
        String error_message = getContext().getResources().getString(R.string.video_may_be_corrupted);
        if (features.length() > 0) {
            error_message += ", " + features + " " + getContext().getResources().getString(R.string.not_supported);
        }
    }

    @Override
    public void onFailedReconnectError() {
    }

    @Override
    public void onFailedCreateVideoFileError() {
        ImageButton view = main_activity.findViewById(R.id.take_photo);
        view.setImageResource(R.drawable.take_video_selector);
        view.setContentDescription(getContext().getResources().getString(R.string.start_video));
        view.setTag(R.drawable.take_video_selector);
    }

    @Override
    public void hasPausedPreview(boolean paused) {

    }

    @Override
    public void cameraInOperation(boolean in_operation) {
        drawPreview.cameraInOperation(in_operation);
        main_activity.getMainUI().showGUI(!in_operation);
    }

    @Override
    public void onPictureCompleted() {
        drawPreview.cameraInOperation(false);
    }

    @Override
    public void cameraClosed() {
        main_activity.getMainUI().clearSeekBar();
        main_activity.getMainUI().destroyPopup();
        drawPreview.clearContinuousFocusMove();
    }

    void updateThumbnail(Bitmap thumbnail) {
        //main_activity.updateGalleryIcon(thumbnail);
    }

    @Override
    public void layoutUI() {
        main_activity.getMainUI().layoutUI();
    }

    @Override
    public void multitouchZoom(int new_zoom) {
        main_activity.getMainUI().setSeekbarZoom();
    }

    @Override
    public void setCameraIdPref(int cameraId) {
        this.cameraId = cameraId;
    }

    @Override
    public void setFlashPref(String flash_value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PreferenceKeys.getFlashPreferenceKey(cameraId), flash_value);
        editor.apply();
    }

    @Override
    public void setFocusPref(String focus_value, boolean is_video) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PreferenceKeys.getFocusPreferenceKey(cameraId, is_video), focus_value);
        editor.apply();
        final int visibility = main_activity.getPreview().getCurrentFocusValue() != null && main_activity.getPreview().getCurrentFocusValue().equals("focus_mode_manual2") ? View.VISIBLE : View.INVISIBLE;
        View focusSeekBar = main_activity.findViewById(R.id.focus_seekbar);
        focusSeekBar.setVisibility(visibility);
    }

    @Override
    public void setVideoPref(boolean is_video) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PreferenceKeys.getIsVideoPreferenceKey(), is_video);
        editor.apply();
    }

    @Override
    public void setColorEffectPref(String color_effect) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PreferenceKeys.getColorEffectPreferenceKey(), color_effect);
        editor.apply();
    }

    @Override
    public void clearColorEffectPref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(PreferenceKeys.getColorEffectPreferenceKey());
        editor.apply();
    }

    @Override
    public void setWhiteBalancePref(String white_balance) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PreferenceKeys.getWhiteBalancePreferenceKey(), white_balance);
        editor.apply();
    }

    @Override
    public void clearWhiteBalancePref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(PreferenceKeys.getWhiteBalancePreferenceKey());
        editor.apply();
    }

    @Override
    public void setISOPref(String iso) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PreferenceKeys.getISOPreferenceKey(), iso);
        editor.apply();
    }

    @Override
    public void clearISOPref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(PreferenceKeys.getISOPreferenceKey());
        editor.apply();
    }

    @Override
    public void setExposureCompensationPref(int exposure) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PreferenceKeys.getExposurePreferenceKey(), "" + exposure);
        editor.apply();
    }

    @Override
    public void clearExposureCompensationPref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(PreferenceKeys.getExposurePreferenceKey());
        editor.apply();
    }

    @Override
    public void setCameraResolutionPref(int width, int height) {
        String resolution_value = width + " " + height;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PreferenceKeys.getResolutionPreferenceKey(cameraId), resolution_value);
        editor.apply();
    }

    @Override
    public void setVideoQualityPref(String video_quality) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PreferenceKeys.getVideoQualityPreferenceKey(cameraId), video_quality);
        editor.apply();
    }

    @Override
    public void setZoomPref(int zoom) {
        this.zoom_factor = zoom;
    }

    @Override
    public void setExposureTimePref(long exposure_time) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(PreferenceKeys.getExposureTimePreferenceKey(), exposure_time);
        editor.apply();
    }

    @Override
    public void clearExposureTimePref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(PreferenceKeys.getExposureTimePreferenceKey());
        editor.apply();
    }

    @Override
    public void setFocusDistancePref(float focus_distance) {
        this.focus_distance = focus_distance;
    }


    @Override
    public void onDrawPreview(Canvas canvas) {
        drawPreview.onDrawPreview(canvas);
    }

    public void drawTextWithBackground(Canvas canvas, Paint paint, String text, int foreground, int background, int location_x, int location_y) {
        drawTextWithBackground(canvas, paint, text, foreground, background, location_x, location_y, false);
    }

    public void drawTextWithBackground(Canvas canvas, Paint paint, String text, int foreground, int background, int location_x, int location_y, boolean align_top) {
        drawTextWithBackground(canvas, paint, text, foreground, background, location_x, location_y, align_top, null, true);
    }

    public void drawTextWithBackground(Canvas canvas, Paint paint, String text, int foreground, int background, int location_x, int location_y, boolean align_top, String ybounds_text, boolean shadow) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(background);
        paint.setAlpha(64);
        int alt_height = 0;
        if (ybounds_text != null) {
            paint.getTextBounds(ybounds_text, 0, ybounds_text.length(), text_bounds);
            alt_height = text_bounds.bottom - text_bounds.top;
        }
        paint.getTextBounds(text, 0, text.length(), text_bounds);
        if (ybounds_text != null) {
            text_bounds.bottom = text_bounds.top + alt_height;
        }
        final int padding = (int) (2 * scale + 0.5f); // convert dps to pixels
        if (paint.getTextAlign() == Paint.Align.RIGHT || paint.getTextAlign() == Paint.Align.CENTER) {
            float width = paint.measureText(text); // n.b., need to use measureText rather than getTextBounds here
            /*if( MyDebug.LOG )
				 TAG, "width: " + width);*/
            if (paint.getTextAlign() == Paint.Align.CENTER)
                width /= 2.0f;
            text_bounds.left -= width;
            text_bounds.right -= width;
        }
		/*if( MyDebug.LOG )
			 TAG, "text_bounds left-right: " + text_bounds.left + " , " + text_bounds.right);*/
        text_bounds.left += location_x - padding;
        text_bounds.right += location_x + padding;
        if (align_top) {
            int height = text_bounds.bottom - text_bounds.top + 2 * padding;
            // unclear why we need the offset of -1, but need this to align properly on Galaxy Nexus at least
            int y_diff = -text_bounds.top + padding - 1;
            text_bounds.top = location_y - 1;
            text_bounds.bottom = text_bounds.top + height;
            location_y += y_diff;
        } else {
            text_bounds.top += location_y - padding;
            text_bounds.bottom += location_y + padding;
        }
        if (shadow) {
            canvas.drawRect(text_bounds, paint);
        }
        paint.setColor(foreground);
        canvas.drawText(text, location_x, location_y, paint);
    }

    private boolean saveInBackground(boolean image_capture_intent) {
        boolean do_in_background = true;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (!sharedPreferences.getBoolean(PreferenceKeys.getBackgroundPhotoSavingPreferenceKey(), true))
            do_in_background = false;
        else if (image_capture_intent)
            do_in_background = false;
        else if (getPausePreviewPref())
            do_in_background = false;
        return do_in_background;
    }

    private boolean isImageCaptureIntent() {
        boolean image_capture_intent = false;
        String action = main_activity.getIntent().getAction();
        if (MediaStore.ACTION_IMAGE_CAPTURE.equals(action) || MediaStore.ACTION_IMAGE_CAPTURE_SECURE.equals(action)) {
            image_capture_intent = true;
        }
        return image_capture_intent;
    }

    private boolean saveImage(boolean is_hdr, boolean save_expo, List<byte[]> images, Date current_date) {

        System.gc();

        boolean image_capture_intent = isImageCaptureIntent();
        Uri image_capture_intent_uri = null;
        if (image_capture_intent) {
            Bundle myExtras = main_activity.getIntent().getExtras();
            if (myExtras != null) {
                image_capture_intent_uri = myExtras.getParcelable(MediaStore.EXTRA_OUTPUT);
            }
        }

        boolean using_camera2 = main_activity.getPreview().usingCamera2API();
        int image_quality = getImageQualityPref();
        boolean do_auto_stabilise = getAutoStabilisePref() && main_activity.getPreview().hasLevelAngle();
        double level_angle = do_auto_stabilise ? main_activity.getPreview().getLevelAngle() : 0.0;
        if (do_auto_stabilise && main_activity.test_have_angle)
            level_angle = main_activity.test_angle;
        if (do_auto_stabilise && main_activity.test_low_memory)
            level_angle = 45.0;
        boolean is_front_facing = main_activity.getPreview().getCameraController() != null && main_activity.getPreview().getCameraController().isFrontFacing();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String pref_style = sharedPreferences.getString(PreferenceKeys.getStampStyleKey(), "preference_stamp_style_shadowed");
        String preference_stamp_dateformat = this.getStampDateFormatPref();
        String preference_stamp_timeformat = this.getStampTimeFormatPref();
        String preference_stamp_gpsformat = this.getStampGPSFormatPref();
        boolean needStamp = this.getStampNeed();
        boolean store_location = getGeotaggingPref() && getLocation() != null;
        Location location = store_location ? getLocation() : null;
        boolean do_in_background = saveInBackground(image_capture_intent);

        boolean success = imageSaver.saveImageJpeg(do_in_background, save_expo, images,
                image_capture_intent, image_capture_intent_uri,
                using_camera2, image_quality,
                do_auto_stabilise, level_angle,
                is_front_facing,
                current_date,
                pref_style, preference_stamp_dateformat, preference_stamp_timeformat, preference_stamp_gpsformat,
                store_location, location, needStamp);


        return success;
    }

    @Override
    public boolean onPictureTaken(byte[] data, Date current_date) {

        List<byte[]> images = new ArrayList<byte[]>();
        images.add(data);

        boolean success = saveImage(false, false, images, current_date);
        return success;
    }

    void addLastImage(File file, boolean share) {
        last_images_saf = false;
        LastImage last_image = new LastImage(file.getAbsolutePath(), share);
        last_images.add(last_image);
    }

    void addLastImageSAF(Uri uri, boolean share) {
        last_images_saf = true;
        LastImage last_image = new LastImage(uri, share);
        last_images.add(last_image);
    }

    void clearLastImages() {
        last_images_saf = false;
        last_images.clear();
    }
}
