package app.camera.tdoc.camera_library.Controllers;

import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.location.Location;
import android.media.MediaRecorder;
import android.view.SurfaceHolder;

import java.util.List;


public abstract class CameraController {
    int cameraId = 0;

    public static final long EXPOSURE_TIME_DEFAULT = 1000000000l / 30;

    public int count_camera_parameters_exception = 0;
    public int count_precapture_timeout = 0;
    public boolean test_wait_capture_result = false;

    public static class CameraFeatures {
        public boolean is_zoom_supported = false;
        public int max_zoom = 0;
        public List<Integer> zoom_ratios = null;
        public boolean supports_face_detection = false;
        public List<CameraController.Size> picture_sizes = null;
        public List<CameraController.Size> video_sizes = null;
        public List<CameraController.Size> preview_sizes = null;
        public List<String> supported_flash_values = null;
        public List<String> supported_focus_values = null;
        public int max_num_focus_areas = 0;
        public float minimum_focus_distance = 0.0f;
        public boolean is_exposure_lock_supported = false;
        public boolean is_video_stabilization_supported = false;
        public boolean supports_iso_range = false;
        public int min_iso = 0;
        public int max_iso = 0;
        public boolean supports_exposure_time = false;
        public long min_exposure_time = 0l;
        public long max_exposure_time = 0l;
        public int min_exposure = 0;
        public int max_exposure = 0;
        public float exposure_step = 0.0f;
        public boolean can_disable_shutter_sound = false;
        public boolean supports_hdr = false;
        public boolean supports_raw = false;
    }

    public static class Size {
        public int width = 0;
        public int height = 0;

        public Size(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Size))
                return false;
            Size that = (Size) o;
            return this.width == that.width && this.height == that.height;
        }

        @Override
        public int hashCode() {
            return width * 31 + height;
        }
    }


    public static class Area {
        public Rect rect = null;
        public int weight = 0;

        public Area(Rect rect, int weight) {
            this.rect = rect;
            this.weight = weight;
        }
    }


    public interface PictureCallback {
        void onCompleted();

        void onPictureTaken(byte[] data);
    }

    public interface AutoFocusCallback {
        void onAutoFocus(boolean success);
    }

    public interface ContinuousFocusMoveCallback {
        void onContinuousFocusMove(boolean start);
    }

    public interface ErrorCallback {
        void onError();
    }

    public static class Face {
        public int score = 0;
        public Rect rect = null;

        Face(int score, Rect rect) {
            this.score = score;
            this.rect = rect;
        }
    }

    public static class SupportedValues {
        public List<String> values = null;
        public String selected_value = null;

        SupportedValues(List<String> values, String selected_value) {
            this.values = values;
            this.selected_value = selected_value;
        }
    }

    public abstract void release();

    public CameraController(int cameraId) {
        this.cameraId = cameraId;
    }

    public abstract String getAPI();

    public abstract CameraFeatures getCameraFeatures();

    public int getCameraId() {
        return cameraId;
    }

    public abstract SupportedValues setSceneMode(String value);

    public abstract String getSceneMode();

    public abstract SupportedValues setColorEffect(String value);

    public abstract String getColorEffect();

    public abstract SupportedValues setWhiteBalance(String value);

    public abstract String getWhiteBalance();

    public abstract SupportedValues setISO(String value);

    public abstract String getISOKey();

    public abstract int getISO();

    public abstract boolean setISO(int iso);

    public abstract long getExposureTime();

    public abstract boolean setExposureTime(long exposure_time);

    public abstract CameraController.Size getPictureSize();

    public abstract void setPictureSize(int width, int height);

    public abstract void setPreviewSize(int width, int height);

    public abstract void setVideoStabilization(boolean enabled);

    public abstract void setJpegQuality(int quality);

    public abstract int getZoom();

    public abstract void setZoom(int value);

    public abstract int getExposureCompensation();

    public abstract boolean setExposureCompensation(int new_exposure);

    public abstract void setPreviewFpsRange(int min, int max);

    public abstract List<int[]> getSupportedPreviewFpsRange();

    public String getDefaultSceneMode() {
        return "auto";
    }

    public String getDefaultColorEffect() {
        return "none"; // chosen to match Camera.Parameters.EFFECT_NONE, but we also use compatible values for Camera2 API
    }

    public String getDefaultWhiteBalance() {
        return "auto"; // chosen to match Camera.Parameters.WHITE_BALANCE_AUTO, but we also use compatible values for Camera2 API
    }

    public String getDefaultISO() {
        return "auto";
    }


    public abstract void setFocusValue(String focus_value);

    public abstract String getFocusValue();

    public abstract float getFocusDistance();

    public abstract boolean setFocusDistance(float focus_distance);

    public abstract void setFlashValue(String flash_value);

    public abstract String getFlashValue();

    public abstract void setRecordingHint(boolean hint);

    public abstract void setAutoExposureLock(boolean enabled);


    public abstract void setRotation(int rotation);

    public abstract void setLocationInfo(Location location);

    public abstract void removeLocationInfo();

    public abstract void enableShutterSound(boolean enabled);

    public abstract boolean setFocusAndMeteringArea(List<CameraController.Area> areas);

    public abstract void clearFocusAndMetering();

    public abstract boolean supportsAutoFocus();

    public abstract boolean focusIsContinuous();

    public abstract boolean focusIsVideo();

    public abstract void reconnect() throws CameraControllerException;

    public abstract void setPreviewDisplay(SurfaceHolder holder) throws CameraControllerException;

    public abstract void setPreviewTexture(SurfaceTexture texture) throws CameraControllerException;

    public abstract void startPreview() throws CameraControllerException;

    public abstract void stopPreview();

    public abstract void autoFocus(final CameraController.AutoFocusCallback cb);

    public abstract void cancelAutoFocus();

    public abstract void setContinuousFocusMoveCallback(ContinuousFocusMoveCallback cb);

    public abstract void takePicture(final CameraController.PictureCallback picture, final ErrorCallback error);

    public abstract void setDisplayOrientation(int degrees);

    public abstract int getDisplayOrientation();

    public abstract int getCameraOrientation();

    public abstract boolean isFrontFacing();

    public abstract void unlock();

    public abstract void initVideoRecorderPrePrepare(MediaRecorder video_recorder);

    public abstract void initVideoRecorderPostPrepare(MediaRecorder video_recorder) throws CameraControllerException;

    public boolean captureResultHasIso() {
        return false;
    }

    public int captureResultIso() {
        return 0;
    }

    public boolean captureResultHasExposureTime() {
        return false;
    }

    public long captureResultExposureTime() {
        return 0;
    }

    protected SupportedValues checkModeIsSupported(List<String> values, String value, String default_value) {
        if (values != null && values.size() > 1) {
            if (!values.contains(value)) {
                if (values.contains(default_value))
                    value = default_value;
                else
                    value = values.get(0);
            }
            return new SupportedValues(values, value);
        }
        return null;
    }
}
