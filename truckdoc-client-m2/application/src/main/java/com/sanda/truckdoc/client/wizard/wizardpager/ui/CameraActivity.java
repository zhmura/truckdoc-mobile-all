package com.sanda.truckdoc.client.wizard.wizardpager.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.sanda.truckdoc.client.Consts;
import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.ui.dgcam.CameraPreview;
import com.sanda.truckdoc.client.util.FileHelper;
import com.sanda.truckdoc.client.util.ImageHelper;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.joda.time.DateTime;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.annotation.NonNull;
import timber.log.Timber;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

@SuppressWarnings("ALL")
@EActivity(R.layout.activity_camera_wizard)
public class CameraActivity extends Activity {

    public static final String IMAGE = "image";

    private Camera camera;
    private CameraPreview cameraView;
    private File destination;

    @Extra
    String description;
    @ViewById(R.id.camera_preview)
    FrameLayout preview;
    @ViewById(R.id.previewImage)
    ImageView previewImage;
    @ViewById(R.id.button_capture)
    ImageButton captureButton;
    @ViewById(R.id.button_discard)
    ImageButton discardButton;
    @ViewById(R.id.button_save)
    ImageButton saveButton;
    @ViewById(android.R.id.title)
    TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @AfterViews
    protected void afterViews() {
        title.setText(description);
        // Create an instance of Camera
        camera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        cameraView = new CameraPreview(this, camera);

        preview.addView(cameraView, 0);
    }

    @Click(R.id.button_capture)
    void onTakePicture() {
        if (Build.MANUFACTURER.toLowerCase().contains("genymotion")) {
            camera.takePicture(null, null, pictureCallback);
        } else {
            camera.autoFocus(autofocusCallback);
        }
    }

    @Click(R.id.button_discard)
    void onDiscardPicture() {
        captureButton.setVisibility(VISIBLE);
        previewImage.setVisibility(GONE);
        saveButton.setVisibility(GONE);
        discardButton.setVisibility(GONE);
        preview.setVisibility(VISIBLE);
        title.setVisibility(VISIBLE);
        camera.startPreview();
        previewImage.setImageBitmap(null);
    }

    @Click(R.id.button_save)
    void onSavePicture() {

        if (destination != null && destination.exists()) {
            setResult(RESULT_OK, new Intent().putExtra(IMAGE, destination));
        } else {
            setResult(RESULT_CANCELED);
        }
        finish();
    }

    @NonNull
    private File saveBitmap(Bitmap bitmap) {
        File destination = new File(FileHelper.getOutcomeDirForAccidents(true),
                String.format("DTP_%s.jpg", DateTime.now().toString(Consts.DATE_TIME_FORMAT)));
        try {
            FileOutputStream out = new FileOutputStream(destination);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            Timber.e(e, "Error saving photo bitmap");
            destination.delete();
        }
        return destination;
    }

    private Camera.PictureCallback pictureCallback = (byte[] data, Camera camera) -> {
        final BitmapFactory.Options opts = new BitmapFactory.Options();
        //opts.inBitmap = bitmap; //as a buffer
        opts.inMutable = true; //to draw watermark

        Bitmap bitmap2 = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
        captureButton.setEnabled(true);
        captureButton.setVisibility(GONE);
        previewImage.setVisibility(VISIBLE);
        saveButton.setVisibility(VISIBLE);
        discardButton.setVisibility(VISIBLE);
        preview.setVisibility(View.GONE);
        title.setVisibility(GONE);

        ImageHelper.drawWaterMark(bitmap2, DateTime.now().toString(Consts.DATE_TIME_FORMAT));
        destination = saveBitmap(bitmap2);
        Picasso.with(this).load(destination).fit().centerCrop().into(previewImage);
        //bitmap = bitmap2;
    };

    private Bitmap scaleDownBitmapImage(Bitmap bitmap, int newWidth, int newHeight) {
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    Camera.AutoFocusCallback autofocusCallback = (boolean success, Camera camera) -> {
        captureButton.setEnabled(false);
        this.camera.takePicture(null, null, null, pictureCallback);
    };

    @Override
    protected void onPause() {
        super.onPause();
//        bitmap = null;
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.release();        // release the camera for other applications
            camera = null;
        }
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            Timber.e(e, "Error getting camera instance");
        }
        return c; // returns null if camera is unavailable
    }
}
