
package app.camera.tdoc.camera_library;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import app.camera.tdoc.camera_library.Views.GalleryViewPager;
import com.github.chrisbanes.photoview.PhotoView;


public class GalleryActivity extends Activity implements View.OnClickListener {

    public static final String SAVE_AND_UPLOAD_FILE_ACTION = "com.sanda.truckdoc.client.receivers.FileSaveReceiverIntent";
    public static final String SAVE_MNT_FILE_ACTION = "com.sanda.truckdoc.client.receivers.MntFileSaveReceiverIntent";
    public static final String DELETE_FILES_ACTION = "com.sanda.truckdoc.client.receivers.FilesDeleteReceiverIntent";
    public static final String SEND_FILES_ACTION = "com.sanda.truckdoc.client.receivers.FileSendReceiverIntent";

    public static final String EMPTY_LIST = "EMPTY_LIST";
    public static final String GALLERY_FOLDER = "Gallery";
    public static final String TRUCKDOC_BASE_FOLDER = "TruckDoc";
    public static boolean isTaskRunning = false;
    public static int rotateDegree = 0;
    private ViewPager mViewPager;
    private ArrayList<String> imageList;
    private RelativeLayout mProgressView;
    private TextView mLabel;
    private Spinner mSpinnerPrefix;
    private List<String> listPrefixsStrings;
    private List<String> listLabels;
    private SharedPreferences sharedPreferences;
    private boolean isOnePhotoMode;
    private boolean isServiceCameraModeEnabled;
    GalleryPagerAdapter pagerAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));
        setContentView(R.layout.activity_view_pager);
        mProgressView = findViewById(R.id.progress_view);
        ImageButton mRotateRightBtn = findViewById(R.id.rotate90right);
        ImageButton mRotateLeftBtn = findViewById(R.id.rotate90left);
        ImageButton mDeleteImageBtn = findViewById(R.id.delete_image);
        ImageButton mSaveImage = findViewById(R.id.save_image);
        mLabel = findViewById(R.id.label);
        mSaveImage.setOnClickListener(this);
        mRotateRightBtn.setOnClickListener(this);
        mRotateLeftBtn.setOnClickListener(this);
        mDeleteImageBtn.setOnClickListener(this);
        mViewPager = (GalleryViewPager) findViewById(R.id.view_pager);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        isOnePhotoMode = getIntent().getBooleanExtra(PreferenceKeys.getLastPhotoModeKey(), false);
        isServiceCameraModeEnabled = sharedPreferences.getBoolean(PreferenceKeys.getServiceCameraModeEnabledPreferenceKey(), false);
        loadGallery();
        setSpinnerr();
    }

    protected void loadGallery() {
        String folder_name = sharedPreferences.getString(PreferenceKeys.getGalleryFolderPathKey(), GALLERY_FOLDER);
        String baseFolderName = sharedPreferences.getString(PreferenceKeys.getBaseFolderPathKey(), TRUCKDOC_BASE_FOLDER);
        File file = StorageUtils.getImageFolder(folder_name, Environment.getExternalStoragePublicDirectory(baseFolderName));
        imageList = new ArrayList<>();

        final FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name != null && name.startsWith("_");
            }
        };

        if (file.exists() && file.isDirectory() && file.listFiles().length > 0) {
            File[] files = file.listFiles(filter);

            Arrays.sort(files, new Comparator() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                public int compare(Object o1, Object o2) {
                    return Long.compare(((File) o1).lastModified(), ((File) o2).lastModified());
                }
            });

            for (int i = files.length - 1; i >= 0; i--) {
                if (files[i].getAbsolutePath().contains("jpg")) {
                    imageList.add(files[i].getAbsolutePath());
                }
                if (isOnePhotoMode) {
                    break;
                }
            }
        }

        pagerAdapter = new GalleryPagerAdapter(imageList);
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isTaskRunning) {
                    return false;
                }
                return GalleryActivity.super.onTouchEvent(event);
            }
        });
    }

    protected void setSpinnerr() {
        mSpinnerPrefix = findViewById(R.id.prefix_spinner);
        Set<String> prefixSet = new HashSet<>();
        prefixSet.add(EMPTY_LIST);
        Set<String> prefixStrings = sharedPreferences.getStringSet(PreferenceKeys.getPrefixImageListKey(), prefixSet);

        if (!prefixStrings.contains(EMPTY_LIST) && prefixStrings.size() > 0 && sharedPreferences.getBoolean(PreferenceKeys.getPrefixEnable(), false)) {

            listPrefixsStrings = new ArrayList<>();
            listLabels = new ArrayList<>();
            for (String pl : prefixStrings) {
                String[] valueLabel = pl.split(",");
                if (valueLabel.length > 1) {
                    listPrefixsStrings.add(valueLabel[1]);
                    listLabels.add(valueLabel[0]);
                }
            }
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listPrefixsStrings);
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            mSpinnerPrefix.setAdapter(spinnerArrayAdapter);
            int selectedItem = sharedPreferences.getInt(PreferenceKeys.getPrefixSpinnerPositionKey(), 0);
            mSpinnerPrefix.setSelection(selectedItem);
            mLabel.setText(listLabels.get(selectedItem));
            mSpinnerPrefix.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    sharedPreferences.edit().putInt(PreferenceKeys.getPrefixSpinnerPositionKey(), position).apply();
                    mLabel.setText(listLabels.get(position));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });


        } else {
            mSpinnerPrefix.setVisibility(View.GONE);
        }
    }


    public void rotateInBackground(int degree) {
        rotateDegree = degree;
        if (!isTaskRunning) {
            new ChangeRotationTask().execute();
        }
    }

    @Override
    public void onClick(View v) {
        if (imageList.size() <= 0) {
            return;
        }
        int i = v.getId();
        if (i == R.id.rotate90left) {
            rotateInBackground(-90);
        } else if (i == R.id.rotate90right) {
            rotateInBackground(90);
        } else if (i == R.id.delete_image) {
            deleteCurrentImage();
            if (isOnePhotoMode) {
                finish();
            }
        } else if (i == R.id.save_image) {
            saveImageWithPreffix();
        }
    }

    @Override
    public void onBackPressed() {
        //this is only needed if you have specific things
        //that you want to do when the user presses the back button.
        /* your specific things...*/
        deleteCurrentImage();
        super.onBackPressed();
    }

    private void saveImageWithPreffix() {
        String path = imageList.get(mViewPager.getCurrentItem());
        String[] splitString = path.split("_");
        StringBuilder newFileName = new StringBuilder();
        if (splitString.length > 0 && mSpinnerPrefix.getVisibility() != View.GONE) {
            int lastindex = splitString[0].lastIndexOf("/");
            for (int i = 0; i <= lastindex; i++) {
                newFileName.append(splitString[0].charAt(i));
            }
            if (listPrefixsStrings != null) {
                newFileName.append(listPrefixsStrings.get(mSpinnerPrefix.getSelectedItemPosition()));
            }
            for (int i = 1; i < splitString.length; i++) {
                newFileName.append("_");
                newFileName.append(splitString[i]);
            }
            new File(path).renameTo(new File(newFileName.toString()));
            sendSaveFileIntent(newFileName.toString(), isServiceCameraModeEnabled);
            if (isServiceCameraModeEnabled) {
                showCreateAnotherPhotoDialog(this, newFileName.toString());
            } else {
                sendResult(newFileName.toString());
            }
        }

    }

    public void showCreateAnotherPhotoDialog(Context context, final String fileName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Сделать еще одно фото?")
                .setCancelable(true)
                .setPositiveButton(R.string.question_answer_y, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        GalleryActivity.this.createOneMorePhoto(fileName);
                    }
                })
                .setNegativeButton(R.string.question_answer_n, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        GalleryActivity.this.createNoMorePhotos();
                    }
                });
        builder.create().show();
    }

    private void createNoMorePhotos() {
        finish();

    }

    private void createOneMorePhoto(String fileName) {
        sendResult(fileName);
        if (isServiceCameraModeEnabled) {
            startToCameraActivity(app.camera.tdoc.camera_library.ImageType.SCENE_PHOTO, 0);
        }
    }

    public void startToCameraActivity(ImageType type, long recipientId) {
        ArrayList<PrefixList> prefixes = new ArrayList<>();
        prefixes.add(new PrefixList("Tехнический осмотр", "TO"));
        Intent i = CamActivity.newBuilder()
                .setFolderName("TruckDoc")
                .setGalleryFolderName("Maintenance")
                .setImagePrefixList(prefixes)
                .setPrefixEnable(true)
                .setRecipient(recipientId)
                .setImageType(type)
                .setServiceCameraModeEnabled(true)
                .setDeleteButtonVisibility(false)
                .setSendButtonVisibility(false)
                .setSettingButtonVisibility(true)
                .setVideoButtonVisibility(false)
                .setExposureEnable(true)
                .setWhiteBalanceEnable(true)
                .setColorEffectsEnable(true)
                .setBordersOptionEnable(true)
                .setResolutionOptionEnable(true)
                .setIsoOptionEnable(true)
                .setFocusOptionEnable(true)
                .setFlashOptionEnable(true)
                .setAutoStabiliseOptionEnable(true)
                .setTimeStampeEnable(true)
                .setLocationStampEnable(true)
                .build(this);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(i);
    }

    public void sendSaveFileIntent(String fileName, boolean isServiceCameraMode) {
        if (!isServiceCameraModeEnabled) {
            int photoCount = sharedPreferences.getInt(PreferenceKeys.getSessionPhotoCountPreferenceKey(), 0);
            photoCount++;
            sharedPreferences.edit().putInt(PreferenceKeys.getSessionPhotoCountPreferenceKey(), photoCount).apply();
        }
        Intent intent = null;
        try {
            intent = new Intent(this, Class.forName("com.sanda.truckdoc.client.receivers.FileActionIntentReceiver"));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        intent.setAction((isServiceCameraMode ? SAVE_MNT_FILE_ACTION : SAVE_AND_UPLOAD_FILE_ACTION));
        intent.putExtra(PreferenceKeys.getFileNameKey(), fileName);
        if (!isServiceCameraMode) {
            intent.putExtra(PreferenceKeys.getRecipientKey(), sharedPreferences.getLong(PreferenceKeys.getRecipientKey(), 1L));
            intent.putExtra(PreferenceKeys.getImageTypeKey(),
                    ImageType.valueOf(sharedPreferences.getString(PreferenceKeys.getImageTypeKey(), ImageType.SCENE_PHOTO.name())).isForDoc());
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendResult(String newFilePath) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(PreferenceKeys.getKeyForNewImagePath(), newFilePath);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }


    private void deleteCurrentImage() {
        int curImageIndex = mViewPager.getCurrentItem();
        String path = imageList.get(mViewPager.getCurrentItem());
        new File(path).getAbsoluteFile().delete();
        imageList.remove(curImageIndex);
        pagerAdapter.notifyDataSetChanged();
    }

    @SuppressLint("StaticFieldLeak")
    private class ChangeRotationTask extends AsyncTask<Void, Void, Void> {

        private int imageIndex;

        @Override
        protected Void doInBackground(Void... params) {

            String path = imageList.get(imageIndex);
            Bitmap bmp = BitmapFactory.decodeFile(path);

            Matrix matrix = new Matrix();
            matrix.postRotate(rotateDegree);
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

            FileOutputStream fOut;
            try {
                fOut = new FileOutputStream(path);
                bmp.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
                fOut.flush();
                fOut.close();

            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            mProgressView.setVisibility(View.VISIBLE);
            isTaskRunning = true;
            imageIndex = mViewPager.getCurrentItem();
            super.onPreExecute();

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            isTaskRunning = false;
            pagerAdapter.notifyDataSetChanged();
            mProgressView.setVisibility(View.GONE);
        }
    }


    public class GalleryPagerAdapter extends PagerAdapter {

        ArrayList<String> imageList;
        private PhotoView photo;

        public GalleryPagerAdapter(ArrayList<String> images) {
            imageList = images;
        }

        @Override
        public int getCount() {
            return imageList.size();
        }


        @NonNull
        @Override
        public View instantiateItem(ViewGroup container, int position) {
            photo = new PhotoView(container.getContext());
            File imageFile = new File(imageList.get(position));
            if (imageFile.exists()) {
                Glide.with(getApplicationContext())
                        .asBitmap()
                        .load(Uri.fromFile(imageFile))
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(photo);

                container.addView(photo, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            }
            return photo;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

}
