package app.camera.tdoc.camera_library.Views.CameraSurface;

import android.graphics.Matrix;
import android.media.MediaRecorder;
import android.view.View;

import app.camera.tdoc.camera_library.Controllers.CameraController;


public interface CameraSurface {
    View getView();

    void setPreviewDisplay(CameraController camera_controller);

    void setVideoRecorder(MediaRecorder video_recorder);

    void setTransform(Matrix matrix);
}
