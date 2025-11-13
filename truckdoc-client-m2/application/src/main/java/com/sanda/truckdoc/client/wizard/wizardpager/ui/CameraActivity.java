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

import com.sanda.truckdoc.client.Consts;
import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.databinding.ActivityCameraWizardBinding;
import com.sanda.truckdoc.client.ui.dgcam.CameraPreview;
import com.sanda.truckdoc.client.util.FileHelper;
import com.sanda.truckdoc.client.util.ImageHelper;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.annotation.NonNull;
import timber.log.Timber;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

@SuppressWarnings("ALL")
public class CameraActivity extends Activity {

    public static final String IMAGE = "image";
    public static final String DESCRIPTION = "description";

    private Camera camera;
    private CameraPreview cameraView;
    private File destination;
    private String description;
    private ActivityCameraWizardBinding binding;

    public static void start(Activity activity, String description) {
        Intent intent = new Intent(activity, CameraActivity.class);
        intent.putExtra(DESCRIPTION, description);
        activity.startActivityForResult(intent, ImagesFragment.REQUEST_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        binding = ActivityCameraWizardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        description = getIntent().getStringExtra(DESCRIPTION);
        setupViews();
    }

    private void setupViews() {
        binding.title.setText(description);
        // Create an instance of Camera
        camera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        cameraView = new CameraPreview(this, camera);

        binding.cameraPreview.addView(cameraView, 0);

        binding.buttonCapture.setOnClickListener(v -> onTakePicture());
        binding.buttonDiscard.setOnClickListener(v -> onDiscardPicture());
        binding.buttonSave.setOnClickListener(v -> onSavePicture());
    }

    private void onTakePicture() {
        if (Build.MANUFACTURER.toLowerCase().contains("genymotion")) {
            camera.takePicture(null, null, pictureCallback);
        } else {
            camera.autoFocus(autofocusCallback);
        }
    }

    private void onDiscardPicture() {
        binding.buttonCapture.setVisibility(VISIBLE);
        binding.previewImage.setVisibility(GONE);
        binding.buttonSave.setVisibility(GONE);
        binding.buttonDiscard.setVisibility(GONE);
        binding.cameraPreview.setVisibility(VISIBLE);
        binding.title.setVisibility(VISIBLE);
        camera.startPreview();
        binding.previewImage.setImageBitmap(null);
    }

    private void onSavePicture() {
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

    private Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            Timber.e(e, "Error opening camera");
        }
        return c;
    }

    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            if (bitmap != null) {
                destination = saveBitmap(bitmap);
                binding.previewImage.setImageBitmap(bitmap);
                binding.buttonCapture.setVisibility(GONE);
                binding.previewImage.setVisibility(VISIBLE);
                binding.buttonSave.setVisibility(VISIBLE);
                binding.buttonDiscard.setVisibility(VISIBLE);
                binding.cameraPreview.setVisibility(GONE);
                binding.title.setVisibility(GONE);
            }
        }
    };

    private Camera.AutoFocusCallback autofocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {
                camera.takePicture(null, null, pictureCallback);
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }
}
