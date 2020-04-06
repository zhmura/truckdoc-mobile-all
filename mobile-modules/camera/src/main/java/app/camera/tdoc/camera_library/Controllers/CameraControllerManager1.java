package app.camera.tdoc.camera_library.Controllers;

import android.hardware.Camera;


@SuppressWarnings("deprecation")
public class CameraControllerManager1 extends CameraControllerManager {
    private static final String TAG = "CameraControllerManager1";

    public int getNumberOfCameras() {
        return Camera.getNumberOfCameras();
    }

    public boolean isFrontFacing(int cameraId) {
        try {
            Camera.CameraInfo camera_info = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, camera_info);
            return (camera_info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }
    }
}
