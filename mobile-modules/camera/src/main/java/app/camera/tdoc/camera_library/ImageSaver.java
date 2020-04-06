package app.camera.tdoc.camera_library;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.hardware.camera2.DngCreator;
import android.location.Location;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.os.Build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import app.camera.tdoc.camera_library.Controllers.CameraController;

public class ImageSaver extends Thread {

    private Paint p = new Paint();
    private DecimalFormat decimalFormat = new DecimalFormat("#0.0");

    private CamActivity main_activity = null;

    private int n_images_to_save = 0;
    private int image_session_count = 0;
    private BlockingQueue<Request> queue = new ArrayBlockingQueue<Request>(1);

    private static class Request {

        enum Type {
            JPEG,
            RAW,
            DUMMY
        }

        Type type = Type.JPEG;
        boolean save_expo = false; // for is_hdr
        List<byte[]> jpeg_images = null; // for jpeg
        DngCreator dngCreator = null; // for raw
        Image image = null; // for raw
        boolean image_capture_intent = false;
        Uri image_capture_intent_uri = null;
        boolean using_camera2 = false;
        int image_quality = 0;
        boolean do_auto_stabilise = false;
        double level_angle = 0.0;
        boolean is_front_facing = false;
        Date current_date = null;
        String pref_style = null;
        String preference_stamp_dateformat = null;
        String preference_stamp_timeformat = null;
        String preference_stamp_gpsformat = null;
        boolean store_location = false;
        Location location = null;
        boolean has_thumbnail_animation = false;
        boolean needStamp = true;

        Request(Type type,
                boolean save_expo,
                List<byte[]> jpeg_images,
                DngCreator dngCreator, Image image,
                boolean image_capture_intent, Uri image_capture_intent_uri,
                boolean using_camera2, int image_quality,
                boolean do_auto_stabilise, double level_angle,
                boolean is_front_facing,
                Date current_date,
                String pref_style,
                String preference_stamp_dateformat,
                String preference_stamp_timeformat,
                String preference_stamp_gpsformat,
                boolean store_location,
                Location location,
                boolean needStamp
        ) {
            this.type = type;
            this.save_expo = save_expo;
            this.jpeg_images = jpeg_images;
            this.dngCreator = dngCreator;
            this.image = image;
            this.image_capture_intent = image_capture_intent;
            this.image_capture_intent_uri = image_capture_intent_uri;
            this.using_camera2 = using_camera2;
            this.image_quality = image_quality;
            this.do_auto_stabilise = do_auto_stabilise;
            this.level_angle = level_angle;
            this.is_front_facing = is_front_facing;
            this.current_date = current_date;
            this.pref_style = pref_style;
            this.preference_stamp_dateformat = preference_stamp_dateformat;
            this.preference_stamp_timeformat = preference_stamp_timeformat;
            this.preference_stamp_gpsformat = preference_stamp_gpsformat;
            this.store_location = store_location;
            this.location = location;
            this.needStamp = needStamp;
        }
    }

    ImageSaver(CamActivity main_activity) {
        this.main_activity = main_activity;

        p.setAntiAlias(true);
    }

    protected void onDestroy() {
    }

    @Override

    public void run() {
        while (true) {
            try {
                Request request = queue.take(); // if empty, take() blocks until non-empty
                boolean success = false;
                if (request.type == Request.Type.RAW) {
                    success = saveImageNowRaw(request.dngCreator, request.image, request.current_date);
                } else if (request.type == Request.Type.JPEG) {
                    success = saveImageNow(request);
                } else if (request.type == Request.Type.DUMMY) {
                    success = true;
                }
                synchronized (this) {
                    n_images_to_save--;
                    notifyAll();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean saveImageJpeg(boolean do_in_background,
                                 boolean save_expo,
                                 List<byte[]> images,
                                 boolean image_capture_intent,
                                 Uri image_capture_intent_uri,
                                 boolean using_camera2,
                                 int image_quality,
                                 boolean do_auto_stabilise,
                                 double level_angle,
                                 boolean is_front_facing,
                                 Date current_date,
                                 String pref_style,
                                 String preference_stamp_dateformat,
                                 String preference_stamp_timeformat,
                                 String preference_stamp_gpsformat,
                                 boolean store_location,
                                 Location location,
                                 boolean needStamp
    ) {
        return saveImage(do_in_background,
                false,
                save_expo,
                images,
                null, null,
                image_capture_intent, image_capture_intent_uri,
                using_camera2, image_quality,
                do_auto_stabilise, level_angle,
                is_front_facing,
                current_date,
                pref_style,
                preference_stamp_dateformat,
                preference_stamp_timeformat,
                preference_stamp_gpsformat,
                store_location, location,
                needStamp);
    }


    private boolean saveImage(boolean do_in_background,
                              boolean is_raw,
                              boolean save_expo,
                              List<byte[]> jpeg_images,
                              DngCreator dngCreator, Image image,
                              boolean image_capture_intent, Uri image_capture_intent_uri,
                              boolean using_camera2, int image_quality,
                              boolean do_auto_stabilise, double level_angle,
                              boolean is_front_facing,
                              Date current_date,
                              String pref_style,
                              String preference_stamp_dateformat,
                              String preference_stamp_timeformat,
                              String preference_stamp_gpsformat,
                              boolean store_location,
                              Location location,
                              boolean needStamp
    ) {
        boolean success = false;


        Request request = new Request(is_raw ? Request.Type.RAW : Request.Type.JPEG,
                save_expo,
                jpeg_images,
                dngCreator, image,
                image_capture_intent, image_capture_intent_uri,
                using_camera2, image_quality,
                do_auto_stabilise, level_angle,
                is_front_facing,
                current_date,
                pref_style, preference_stamp_dateformat, preference_stamp_timeformat, preference_stamp_gpsformat,
                store_location, location, needStamp);

        if (do_in_background) {
            addRequest(request);
            success = true;
        } else {
            waitUntilDone();
            if (is_raw) {
                success = saveImageNowRaw(request.dngCreator, request.image, request.current_date);
            } else {
                success = saveImageNow(request);
            }
        }
        return success;
    }

    private void addRequest(Request request) {
        boolean done = false;
        while (!done) {
            try {
                synchronized (this) {
                    n_images_to_save++;
                }
                queue.put(request);
                done = true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void waitUntilDone() {
        synchronized (this) {
            while (n_images_to_save > 0) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private boolean saveImageNow(final Request request) {

        if (request.type != Request.Type.JPEG) {
            throw new RuntimeException();
        } else if (request.jpeg_images.size() == 0) {
            throw new RuntimeException();
        }

        if (request.jpeg_images.size() > 1) {
            throw new RuntimeException();
        }

        return saveSingleImageNow(request, request.jpeg_images.get(0), null, "", true, true);
    }

    @SuppressLint("SimpleDateFormat")
    @SuppressWarnings("deprecation")
    private boolean saveSingleImageNow(final Request request, byte[] data, Bitmap bitmap, String filename_suffix, boolean update_thumbnail, boolean share_image) {

        if (request.type != Request.Type.JPEG) {
            throw new RuntimeException();
        } else if (data == null) {
            throw new RuntimeException();
        }

        boolean image_capture_intent = request.image_capture_intent;
        boolean using_camera2 = request.using_camera2;
        Date current_date = request.current_date;
        boolean store_location = request.store_location;
        boolean needStamp = request.needStamp;

        boolean success = false;
        final MyApplicationInterface applicationInterface = main_activity.getApplicationInterface();
        StorageUtils storageUtils = main_activity.getStorageUtils();

        main_activity.savingImage(true);

        if (request.do_auto_stabilise) {
            double level_angle = request.level_angle;
            while (level_angle < -90)
                level_angle += 180;
            while (level_angle > 90)
                level_angle -= 180;
            if (bitmap == null) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                //options.inMutable = true;
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                    // setting is ignored in Android 5 onwards
                    options.inPurgeable = true;
                }
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                if (bitmap == null) {
                    System.gc();
                }
            }
            if (bitmap != null) {
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                Matrix matrix = new Matrix();
                double level_angle_rad_abs = Math.abs(Math.toRadians(level_angle));
                int w1 = width, h1 = height;
                double w0 = (w1 * Math.cos(level_angle_rad_abs) + h1 * Math.sin(level_angle_rad_abs));
                double h0 = (w1 * Math.sin(level_angle_rad_abs) + h1 * Math.cos(level_angle_rad_abs));
                // apply a scale so that the overall image size isn't increased
                float orig_size = w1 * h1;
                float rotated_size = (float) (w0 * h0);
                float scale = (float) Math.sqrt(orig_size / rotated_size);
                if (main_activity.test_low_memory) {
                    scale *= 2.0f; // test 20MP on Galaxy Nexus or Nexus 7; 52MP on Nexus 6
                }
                matrix.postScale(scale, scale);
                w0 *= scale;
                h0 *= scale;
                w1 *= scale;
                h1 *= scale;
                if (request.is_front_facing) {
                    matrix.postRotate((float) -level_angle);
                } else {
                    matrix.postRotate((float) level_angle);
                }
                Bitmap new_bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
                if (new_bitmap != bitmap) {
                    bitmap.recycle();
                    bitmap = new_bitmap;
                }
                System.gc();
                double tan_theta = Math.tan(level_angle_rad_abs);
                double sin_theta = Math.sin(level_angle_rad_abs);
                double denom = h0 / w0 + tan_theta;
                double alt_denom = w0 / h0 + tan_theta;
                if (denom == 0.0 || denom < 1.0e-14) {
                } else if (alt_denom == 0.0 || alt_denom < 1.0e-14) {
                } else {
                    int w2 = (int) ((h0 + 2.0 * h1 * sin_theta * tan_theta - w0 * tan_theta) / denom);
                    int h2 = (int) (w2 * h0 / w0);
                    int alt_h2 = (int) ((w0 + 2.0 * w1 * sin_theta * tan_theta - h0 * tan_theta) / alt_denom);
                    int alt_w2 = (int) (alt_h2 * w0 / h0);
                    if (alt_w2 < w2) {
                        w2 = alt_w2;
                        h2 = alt_h2;
                    }
                    if (w2 <= 0)
                        w2 = 1;
                    else if (w2 >= bitmap.getWidth())
                        w2 = bitmap.getWidth() - 1;
                    if (h2 <= 0)
                        h2 = 1;
                    else if (h2 >= bitmap.getHeight())
                        h2 = bitmap.getHeight() - 1;
                    int x0 = (bitmap.getWidth() - w2) / 2;
                    int y0 = (bitmap.getHeight() - h2) / 2;
                    new_bitmap = Bitmap.createBitmap(bitmap, x0, y0, w2, h2);
                    if (new_bitmap != bitmap) {
                        bitmap.recycle();
                        bitmap = new_bitmap;
                    }
                    System.gc();
                }
            }
        }
        if (needStamp) {
            if (bitmap == null) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inMutable = true;
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                    // setting is ignored in Android 5 onwards
                    options.inPurgeable = true;
                }
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                if (bitmap == null) {
                    System.gc();
                }
            }
            if (bitmap != null) {


                String pref_style = request.pref_style;
                String preference_stamp_dateformat = request.preference_stamp_dateformat;
                String preference_stamp_timeformat = request.preference_stamp_timeformat;
                String preference_stamp_gpsformat = request.preference_stamp_gpsformat;
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                Canvas canvas = new Canvas(bitmap);
                p.setColor(Color.WHITE);
                int smallest_size = (width < height) ? width : height;
                float scale = ((float) smallest_size) / (72.0f * 4.0f);
                int font_size_pixel = (int) (12 * scale + 0.5f); // convert pt to pixels
                p.setTextSize(font_size_pixel);
                int offset_x = (int) (8 * scale + 0.5f); // convert pt to pixels
                int offset_y = (int) (8 * scale + 0.5f); // convert pt to pixels
                int diff_y = (int) ((24 + 4) * scale + 0.5f); // convert pt to pixels
                int ypos = height - offset_y;
                p.setTextAlign(Align.RIGHT);
                boolean draw_shadowed = false;
                if (pref_style.equals("preference_stamp_style_shadowed")) {
                    draw_shadowed = true;
                } else if (pref_style.equals("preference_stamp_style_plain")) {
                    draw_shadowed = false;
                }
                if (needStamp) {
                    String date_stamp = "", time_stamp = "";

                    date_stamp = DateFormat.getDateInstance().format(current_date);
                    time_stamp = DateFormat.getTimeInstance().format(current_date);

                    if (date_stamp.length() > 0 || time_stamp.length() > 0) {
                        String datetime_stamp = "";
                        if (date_stamp.length() > 0)
                            datetime_stamp += date_stamp;
                        if (time_stamp.length() > 0) {
                            if (date_stamp.length() > 0)
                                datetime_stamp += " ";
                            datetime_stamp += time_stamp;
                        }
                        applicationInterface.drawTextWithBackground(canvas, p, datetime_stamp, Color.WHITE, Color.BLACK, width - offset_x, ypos, false, null, draw_shadowed);
                    }
                    ypos -= diff_y;
                    String gps_stamp = "";
                    if (!preference_stamp_gpsformat.equals("preference_stamp_gpsformat_none")) {
                        if (store_location) {
                            Location location = request.location;
                            if (preference_stamp_gpsformat.equals("preference_stamp_gpsformat_dms")) {
                                gps_stamp += LocationSupplier.locationToDMS(location.getLatitude()) + ", " + LocationSupplier.locationToDMS(location.getLongitude());
                            } else {
                                gps_stamp += Location.convert(location.getLatitude(), Location.FORMAT_DEGREES) + ", " + Location.convert(location.getLongitude(), Location.FORMAT_DEGREES);
                            }
                        }
                    }
                    if (gps_stamp.length() > 0) {
                        applicationInterface.drawTextWithBackground(canvas, p, gps_stamp, Color.WHITE, Color.BLACK, width - offset_x, ypos, false, null, draw_shadowed);
                        ypos -= diff_y;
                    }
                }
            }
        }

        int exif_orientation_s = ExifInterface.ORIENTATION_UNDEFINED;
        File picFile = null;
        Uri saveUri = null; // if non-null, then picFile is a temporary file, which afterwards we should redirect to saveUri
        try {
            if (image_capture_intent) {
                if (request.image_capture_intent_uri != null) {
                    saveUri = request.image_capture_intent_uri;
                } else {
                    if (bitmap == null) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        //options.inMutable = true;
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                            // setting is ignored in Android 5 onwards
                            options.inPurgeable = true;
                        }
                        bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                    }
                    if (bitmap != null) {
                        int width = bitmap.getWidth();
                        int height = bitmap.getHeight();
                        final int small_size_c = 128;
                        if (width > small_size_c) {
                            float scale = ((float) small_size_c) / (float) width;
                            Matrix matrix = new Matrix();
                            matrix.postScale(scale, scale);
                            Bitmap new_bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
                            // careful, as new_bitmap is sometimes not a copy!
                            if (new_bitmap != bitmap) {
                                bitmap.recycle();
                                bitmap = new_bitmap;
                            }
                        }
                    }
                    if (bitmap != null)
                        main_activity.setResult(Activity.RESULT_OK, new Intent("inline-data").putExtra("data", bitmap));
                    main_activity.finish();
                }
            } else if (storageUtils.isUsingSAF()) {
                saveUri = storageUtils.createOutputMediaFileSAF(StorageUtils.MEDIA_TYPE_IMAGE, filename_suffix, "jpg", current_date);
            } else {
                picFile = storageUtils.createOutputMediaFile(StorageUtils.MEDIA_TYPE_IMAGE, filename_suffix, "jpg", current_date);
            }

            if (saveUri != null && picFile == null) {
                picFile = File.createTempFile("picFile", "jpg", main_activity.getCacheDir());
            }

            OutputStream outputStream = null;
            if (picFile != null) {
                outputStream = new FileOutputStream(picFile);
            }

            if (outputStream != null) {
                try {
                    if (bitmap != null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, request.image_quality, outputStream);
                    } else {
                        outputStream.write(data);
                    }
                } finally {
                    outputStream.close();
                }

                if (saveUri == null) { // if saveUri is non-null, then we haven't succeeded until we've copied to the saveUri
                    success = true;
                }
                if (picFile != null) {
                    if (bitmap != null) {
                        File tempFile = File.createTempFile("opencamera_exif", "");
                        OutputStream tempOutputStream = new FileOutputStream(tempFile);
                        try {
                            tempOutputStream.write(data);
                        } finally {
                            tempOutputStream.close();
                        }
                        ExifInterface exif = new ExifInterface(tempFile.getAbsolutePath());
                        String exif_aperture = exif.getAttribute(ExifInterface.TAG_APERTURE);
                        String exif_datetime = exif.getAttribute(ExifInterface.TAG_DATETIME);
                        String exif_exposure_time = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
                        String exif_flash = exif.getAttribute(ExifInterface.TAG_FLASH);
                        String exif_focal_length = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
                        String exif_gps_altitude = exif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE);
                        String exif_gps_altitude_ref = exif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF);
                        String exif_gps_datestamp = exif.getAttribute(ExifInterface.TAG_GPS_DATESTAMP);
                        String exif_gps_latitude = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
                        String exif_gps_latitude_ref = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
                        String exif_gps_longitude = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
                        String exif_gps_longitude_ref = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
                        String exif_gps_processing_method = exif.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD);
                        String exif_gps_timestamp = exif.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP);
                        // leave width/height, as this will have changed!
                        String exif_iso = exif.getAttribute(ExifInterface.TAG_ISO);
                        String exif_make = exif.getAttribute(ExifInterface.TAG_MAKE);
                        String exif_model = exif.getAttribute(ExifInterface.TAG_MODEL);
                        int exif_orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                        exif_orientation_s = exif_orientation; // store for later use (for the thumbnail, to save rereading it)
                        String exif_white_balance = exif.getAttribute(ExifInterface.TAG_WHITE_BALANCE);

                        if (!tempFile.delete()) {
                        }
                        ExifInterface exif_new = new ExifInterface(picFile.getAbsolutePath());
                        if (exif_aperture != null)
                            exif_new.setAttribute(ExifInterface.TAG_APERTURE, exif_aperture);
                        if (exif_datetime != null)
                            exif_new.setAttribute(ExifInterface.TAG_DATETIME, exif_datetime);
                        if (exif_exposure_time != null)
                            exif_new.setAttribute(ExifInterface.TAG_EXPOSURE_TIME, exif_exposure_time);
                        if (exif_flash != null)
                            exif_new.setAttribute(ExifInterface.TAG_FLASH, exif_flash);
                        if (exif_focal_length != null)
                            exif_new.setAttribute(ExifInterface.TAG_FOCAL_LENGTH, exif_focal_length);
                        if (exif_gps_altitude != null)
                            exif_new.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, exif_gps_altitude);
                        if (exif_gps_altitude_ref != null)
                            exif_new.setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, exif_gps_altitude_ref);
                        if (exif_gps_datestamp != null)
                            exif_new.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, exif_gps_datestamp);
                        if (exif_gps_latitude != null)
                            exif_new.setAttribute(ExifInterface.TAG_GPS_LATITUDE, exif_gps_latitude);
                        if (exif_gps_latitude_ref != null)
                            exif_new.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, exif_gps_latitude_ref);
                        if (exif_gps_longitude != null)
                            exif_new.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, exif_gps_longitude);
                        if (exif_gps_longitude_ref != null)
                            exif_new.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, exif_gps_longitude_ref);
                        if (exif_gps_processing_method != null)
                            exif_new.setAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD, exif_gps_processing_method);
                        if (exif_gps_timestamp != null)
                            exif_new.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, exif_gps_timestamp);
                        if (exif_iso != null)
                            exif_new.setAttribute(ExifInterface.TAG_ISO, exif_iso);
                        if (exif_make != null)
                            exif_new.setAttribute(ExifInterface.TAG_MAKE, exif_make);
                        if (exif_model != null)
                            exif_new.setAttribute(ExifInterface.TAG_MODEL, exif_model);
                        if (exif_orientation != ExifInterface.ORIENTATION_UNDEFINED)
                            exif_new.setAttribute(ExifInterface.TAG_ORIENTATION, "" + exif_orientation);
                        if (exif_white_balance != null)
                            exif_new.setAttribute(ExifInterface.TAG_WHITE_BALANCE, exif_white_balance);
                        setDateTimeExif(exif_new);
                        if (needGPSTimestampHack(using_camera2, store_location)) {
                            fixGPSTimestamp(exif_new);
                        }
                        exif_new.saveAttributes();
                    } else if (needGPSTimestampHack(using_camera2, store_location)) {
                        ExifInterface exif = new ExifInterface(picFile.getAbsolutePath());
                        fixGPSTimestamp(exif);
                        exif.saveAttributes();
                    }

                    if (saveUri == null) {
                        storageUtils.broadcastFile(picFile, true, false, update_thumbnail);
                        main_activity.test_last_saved_image = picFile.getAbsolutePath();
                    }
                }
                if (image_capture_intent) {
                    main_activity.setResult(Activity.RESULT_OK);
                    main_activity.finish();
                }
                if (storageUtils.isUsingSAF()) {
                    storageUtils.clearLastMediaScanned();
                }

                if (saveUri != null) {
                    copyFileToUri(main_activity, saveUri, picFile);
                    success = true;
                    File real_file = storageUtils.getFileFromDocumentUriSAF(saveUri);
                    if (real_file != null) {
                        storageUtils.broadcastFile(real_file, true, false, true);
                        main_activity.test_last_saved_image = real_file.getAbsolutePath();
                    } else if (!image_capture_intent) {
                        storageUtils.announceUri(saveUri, true, false);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (success && saveUri == null) {
            applicationInterface.addLastImage(picFile, share_image);
        } else if (success && storageUtils.isUsingSAF()) {
            applicationInterface.addLastImageSAF(saveUri, share_image);
        }

        if (success && main_activity.getPreview().getCameraController() != null && update_thumbnail) {
            CameraController.Size size = main_activity.getPreview().getCameraController().getPictureSize();
            int ratio = (int) Math.ceil((double) size.width / main_activity.getPreview().getView().getWidth());
            int sample_size = Integer.highestOneBit(ratio) * 4; // * 4 to increase performance, without noticeable loss in visual quality
            if (!request.has_thumbnail_animation) {
                sample_size *= 4;
            }
            Bitmap thumbnail = null;
            if (bitmap == null) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inMutable = false;
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                    options.inPurgeable = true;
                }
                options.inSampleSize = sample_size;
                thumbnail = BitmapFactory.decodeByteArray(data, 0, data.length, options);
            } else {
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                Matrix matrix = new Matrix();
                float scale = 1.0f / (float) sample_size;
                matrix.postScale(scale, scale);
                thumbnail = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
            }
            if (thumbnail == null) {
            } else {
                thumbnail = rotateForExif(thumbnail, exif_orientation_s, picFile.getAbsolutePath());

                final Bitmap thumbnail_f = thumbnail;
                main_activity.runOnUiThread(new Runnable() {
                    public void run() {
                        applicationInterface.updateThumbnail(thumbnail_f);
                    }
                });
            }
        }

        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }

        if (picFile != null && saveUri != null) {
            if (!picFile.delete()) {
            }
            picFile = null;
        }

        System.gc();

        main_activity.savingImage(false);

        return success;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean saveImageNowRaw(DngCreator dngCreator, Image image, Date current_date) {

        boolean success = false;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return success;
        }
        StorageUtils storageUtils = main_activity.getStorageUtils();

        main_activity.savingImage(true);

        OutputStream output = null;
        try {
            File picFile = null;
            Uri saveUri = null;

            if (storageUtils.isUsingSAF()) {
                saveUri = storageUtils.createOutputMediaFileSAF(StorageUtils.MEDIA_TYPE_IMAGE, "", "dng", current_date);
            } else {
                picFile = storageUtils.createOutputMediaFile(StorageUtils.MEDIA_TYPE_IMAGE, "", "dng", current_date);
            }

            if (picFile != null) {
                output = new FileOutputStream(picFile);
            } else {
                output = main_activity.getContentResolver().openOutputStream(saveUri);
            }
            dngCreator.writeImage(output, image);
            image.close();
            image = null;
            dngCreator.close();
            dngCreator = null;
            output.close();
            output = null;

            Location location = null;
            if (main_activity.getApplicationInterface().getGeotaggingPref()) {
                location = main_activity.getApplicationInterface().getLocation();
            }

            if (saveUri == null) {
                success = true;
                storageUtils.broadcastFile(picFile, true, false, false);
            } else {
                success = true;
                File real_file = storageUtils.getFileFromDocumentUriSAF(saveUri);
                if (real_file != null) {
                    storageUtils.broadcastFile(real_file, true, false, false);
                } else {
                    storageUtils.announceUri(saveUri, true, false);
                }
            }

            MyApplicationInterface applicationInterface = main_activity.getApplicationInterface();
            if (success && saveUri == null) {
                applicationInterface.addLastImage(picFile, false);
            } else if (success && storageUtils.isUsingSAF()) {
                applicationInterface.addLastImageSAF(saveUri, false);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (image != null) {
                image.close();
                image = null;
            }
            if (dngCreator != null) {
                dngCreator.close();
                dngCreator = null;
            }
        }


        System.gc();

        main_activity.savingImage(false);

        return success;
    }

    private Bitmap rotateForExif(Bitmap bitmap, int exif_orientation_s, String path) {
        try {
            if (exif_orientation_s == ExifInterface.ORIENTATION_UNDEFINED) {
                ExifInterface exif = new ExifInterface(path);
                exif_orientation_s = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            }
            boolean needs_tf = false;
            int exif_orientation = 0;
            if (exif_orientation_s == ExifInterface.ORIENTATION_UNDEFINED || exif_orientation_s == ExifInterface.ORIENTATION_NORMAL) {
            } else if (exif_orientation_s == ExifInterface.ORIENTATION_ROTATE_180) {
                needs_tf = true;
                exif_orientation = 180;
            } else if (exif_orientation_s == ExifInterface.ORIENTATION_ROTATE_90) {
                needs_tf = true;
                exif_orientation = 90;
            } else if (exif_orientation_s == ExifInterface.ORIENTATION_ROTATE_270) {
                needs_tf = true;
                exif_orientation = 270;
            }

            if (needs_tf) {
                Matrix m = new Matrix();
                m.setRotate(exif_orientation, bitmap.getWidth() * 0.5f, bitmap.getHeight() * 0.5f);
                Bitmap rotated_bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                if (rotated_bitmap != bitmap) {
                    bitmap.recycle();
                    bitmap = rotated_bitmap;
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return bitmap;
    }

    private void setDateTimeExif(ExifInterface exif) {
        String exif_datetime = exif.getAttribute(ExifInterface.TAG_DATETIME);
        if (exif_datetime != null) {
            exif.setAttribute("DateTimeOriginal", exif_datetime);
            exif.setAttribute("DateTimeDigitized", exif_datetime);
        }
    }

    private void fixGPSTimestamp(ExifInterface exif) {
        exif.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, Long.toString(System.currentTimeMillis()));
    }

    private boolean needGPSTimestampHack(boolean using_camera2, boolean store_location) {
        if (using_camera2) {
            return store_location;
        }
        return false;
    }

    private void copyFileToUri(Context context, Uri saveUri, File picFile) throws IOException {
        InputStream inputStream = null;
        OutputStream realOutputStream = null;
        try {
            inputStream = new FileInputStream(picFile);
            realOutputStream = context.getContentResolver().openOutputStream(saveUri);
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buffer)) > 0) {
                realOutputStream.write(buffer, 0, len);
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (realOutputStream != null) {
                realOutputStream.close();
            }
        }
    }
}
