package app.camera.tdoc.camera_library.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.location.Location;
import android.media.CamcorderProfile;
import android.net.Uri;
import android.util.Pair;
import android.view.MotionEvent;

import java.io.File;
import java.io.IOException;
import java.util.Date;


public interface ApplicationInterface {
    int VIDEOMETHOD_FILE = 0; // video will be saved to a file
    int VIDEOMETHOD_SAF = 1; // video will be saved using Android 5's Storage Access Framework
    int VIDEOMETHOD_URI = 2; // video will be written to the supplied Uri

    Context getContext();

    boolean useCamera2();

    Location getLocation(); // get current location - null if not available (or you don't care about geotagging)

    int createOutputVideoMethod(); // return a VIDEOMETHOD_* value to specify how to create a video file

    File createOutputVideoFile() throws IOException; // will be called if createOutputVideoUsingSAF() returns VIDEOMETHOD_FILE

    Uri createOutputVideoSAF() throws IOException; // will be called if createOutputVideoUsingSAF() returns VIDEOMETHOD_SAF

    Uri createOutputVideoUri() throws IOException; // will be called if createOutputVideoUsingSAF() returns VIDEOMETHOD_URI

    int getCameraIdPref();

    String getFlashPref();

    String getFocusPref(boolean is_video); // focus_mode_auto, focus_mode_infinity, focus_mode_macro, focus_mode_locked, focus_mode_fixed, focus_mode_manual2, focus_mode_edof, focus_mode_continuous_video

    boolean isVideoPref(); // start up in video mode?

    String getSceneModePref(); // "auto" for default (strings correspond to Android's scene mode constants in android.hardware.Camera.Parameters)

    String getColorEffectPref(); // "node" for default (strings correspond to Android's color effect constants in android.hardware.Camera.Parameters)

    String getWhiteBalancePref();

    String getISOPref();
    // "default" for default

    int getExposureCompensationPref(); // 0 for default

    Pair<Integer, Integer> getCameraResolutionPref(); // return null to let Preview choose size

    int getImageQualityPref(); // jpeg quality for taking photos; "90" is a recommended default

    String getVideoQualityPref(); // should be one of Preview.getSupportedVideoQuality() (use Preview.getCamcorderProfile() or Preview.getCamcorderProfileDescription() for details); or return "" to let Preview choose quality

    boolean getVideoStabilizationPref(); // whether to use video stabilization for video

    String getVideoBitratePref(); // return "default" to let Preview choose

    String getVideoFPSPref(); // return "default" to let Preview choose

    long getVideoMaxDurationPref(); // time in ms after which to automatically stop video recording (return 0 for off)

    int getVideoRestartTimesPref(); // number of times to restart video recording after hitting max duration (return 0 for never auto-restarting)

    long getVideoMaxFileSizePref(); // maximum file size in bytes for video (return 0 for device default)

    boolean getVideoRestartMaxFileSizePref(); // whether to restart on hitting max file size

    boolean getVideoFlashPref(); // option to switch flash on/off while recording video (should be false in most cases!)

    String getPreviewSizePref();

    String getPreviewRotationPref();

    String getLockOrientationPref();

    boolean getTouchCapturePref();

    boolean getDoubleTapCapturePref();

    boolean getPausePreviewPref();

    boolean getShutterSoundPref();

    boolean getStartupFocusPref();

    boolean getGeotaggingPref();

    boolean getRequireLocationPref();

    int getZoomPref();

    // Camera2 only modes:
    long getExposureTimePref(); // only called if getISOPref() is not "default"

    float getFocusDistancePref();

    boolean isTestAlwaysFocus(); // if true, pretend autofocus always successful

    void cameraSetup(); // called when the camera is (re-)set up - should update UI elements/parameters that depend on camera settings

    void touchEvent(MotionEvent event);

    void startingVideo(); // called just before video recording starts

    void stoppingVideo(); // called just before video recording stops

    void stoppedVideo(final int video_method, final Uri uri, final String filename); // called after video recording stopped (uri/filename will be null if video is corrupt or not created)

    void onFailedStartPreview(); // called if failed to start camera preview

    void onPhotoError(); // callback for failing to take a photo

    void onVideoInfo(int what, int extra); // callback for info when recording video (see MediaRecorder.OnInfoListener)

    void onVideoError(int what, int extra); // callback for errors when recording video (see MediaRecorder.OnErrorListener)

    void onVideoRecordStartError(CamcorderProfile profile);

    void onVideoRecordStopError(CamcorderProfile profile);

    void onFailedReconnectError();

    void onFailedCreateVideoFileError();

    void hasPausedPreview(boolean paused);

    void cameraInOperation(boolean in_operation);

    void cameraClosed();

    void layoutUI();

    void multitouchZoom(int new_zoom);

    void setCameraIdPref(int cameraId);

    void setFlashPref(String flash_value);

    void setFocusPref(String focus_value, boolean is_video);

    void setVideoPref(boolean is_video);

    void setColorEffectPref(String color_effect);

    void clearColorEffectPref();

    void setWhiteBalancePref(String white_balance);

    void clearWhiteBalancePref();

    void setISOPref(String iso);

    void clearISOPref();

    void setExposureCompensationPref(int exposure);

    void clearExposureCompensationPref();

    void setCameraResolutionPref(int width, int height);

    void setVideoQualityPref(String video_quality);

    void setZoomPref(int zoom);

    void setExposureTimePref(long exposure_time);

    void clearExposureTimePref();

    void setFocusDistancePref(float focus_distance);

    void onDrawPreview(Canvas canvas);

    boolean onPictureTaken(byte[] data, Date current_date);

    void onPictureCompleted();

    void onContinuousFocusMove(boolean start);
}
