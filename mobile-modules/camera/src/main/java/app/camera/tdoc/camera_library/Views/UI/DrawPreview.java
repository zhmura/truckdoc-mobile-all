package app.camera.tdoc.camera_library.Views.UI;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.preference.PreferenceManager;
import android.util.Pair;
import android.view.View;
import android.widget.ImageButton;

import app.camera.tdoc.camera_library.CamActivity;
import app.camera.tdoc.camera_library.Controllers.CameraController;
import app.camera.tdoc.camera_library.MyApplicationInterface;
import app.camera.tdoc.camera_library.PreferenceKeys;
import app.camera.tdoc.camera_library.R;
import app.camera.tdoc.camera_library.Views.Preview;


public class DrawPreview {

    private CamActivity main_activity = null;
    private MyApplicationInterface applicationInterface = null;

    private Paint p = new Paint();
    private int[] gui_location = new int[2];
    private float stroke_width = 0.0f;
    private Bitmap location_bitmap = null;
    private Bitmap location_off_bitmap = null;
    private Rect location_dest = new Rect();

    private Bitmap last_thumbnail = null;
    private boolean thumbnail_anim = false;
    private long thumbnail_anim_start_ms = -1;
    private RectF thumbnail_anim_src_rect = new RectF();
    private RectF thumbnail_anim_dst_rect = new RectF();
    private Matrix thumbnail_anim_matrix = new Matrix();

    private boolean taking_picture = false;

    private boolean continuous_focus_moving = false;
    private long continuous_focus_moving_ms = 0;

    public DrawPreview(CamActivity main_activity, MyApplicationInterface applicationInterface) {
        this.main_activity = main_activity;
        this.applicationInterface = applicationInterface;

        p.setAntiAlias(true);
        p.setStrokeCap(Paint.Cap.ROUND);
        final float scale = getContext().getResources().getDisplayMetrics().density;
        this.stroke_width = 0.5f * scale + 0.5f;
        p.setStrokeWidth(stroke_width);
    }

    private Context getContext() {
        return main_activity;
    }

    public void cameraInOperation(boolean in_operation) {
        taking_picture = in_operation && !main_activity.getPreview().isVideo();
    }

    public void onContinuousFocusMove(boolean start) {
        if (start) {
            if (!continuous_focus_moving) {
                continuous_focus_moving = true;
                continuous_focus_moving_ms = System.currentTimeMillis();
            }
        }
    }

    public void clearContinuousFocusMove() {
        continuous_focus_moving = false;
        continuous_focus_moving_ms = 0;
    }

    private boolean getTakePhotoBorderPref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getBoolean(PreferenceKeys.getTakePhotoBorderPreferenceKey(), true);
    }


    private String getTimeStringFromSeconds(long time) {
        int secs = (int) (time % 60);
        time /= 60;
        int mins = (int) (time % 60);
        time /= 60;
        long hours = time;
        return hours + ":" + String.format("%02d", mins) + ":" + String.format("%02d", secs);
    }


    public void onDrawPreview(Canvas canvas) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        Preview preview = main_activity.getPreview();
        CameraController camera_controller = preview.getCameraController();
        int ui_rotation = preview.getUIRotation();
        boolean ui_placement_right = main_activity.getMainUI().getUIPlacementRight();

        final float scale = getContext().getResources().getDisplayMetrics().density;


        String preference_grid = sharedPreferences.getString(PreferenceKeys.getShowGridPreferenceKey(), "preference_grid_none");
        if (camera_controller != null && taking_picture && getTakePhotoBorderPref()) {
            p.setColor(Color.WHITE);
            p.setStyle(Paint.Style.STROKE);
            float this_stroke_width = 5.0f * scale + 0.5f; // convert dps to pixels
            p.setStrokeWidth(this_stroke_width);
            canvas.drawRect(0.0f, 0.0f, canvas.getWidth(), canvas.getHeight(), p);
            p.setStyle(Paint.Style.FILL); // reset
            p.setStrokeWidth(stroke_width); // reset
        }
        if (camera_controller != null && preference_grid.equals("preference_grid_3x3")) {
            p.setColor(Color.WHITE);
            canvas.drawLine(canvas.getWidth() / 3.0f, 0.0f, canvas.getWidth() / 3.0f, canvas.getHeight() - 1.0f, p);
            canvas.drawLine(2.0f * canvas.getWidth() / 3.0f, 0.0f, 2.0f * canvas.getWidth() / 3.0f, canvas.getHeight() - 1.0f, p);
            canvas.drawLine(0.0f, canvas.getHeight() / 3.0f, canvas.getWidth() - 1.0f, canvas.getHeight() / 3.0f, p);
            canvas.drawLine(0.0f, 2.0f * canvas.getHeight() / 3.0f, canvas.getWidth() - 1.0f, 2.0f * canvas.getHeight() / 3.0f, p);
        } else if (camera_controller != null && preference_grid.equals("preference_grid_4x2")) {
            p.setColor(Color.GRAY);
            canvas.drawLine(canvas.getWidth() / 4.0f, 0.0f, canvas.getWidth() / 4.0f, canvas.getHeight() - 1.0f, p);
            canvas.drawLine(canvas.getWidth() / 2.0f, 0.0f, canvas.getWidth() / 2.0f, canvas.getHeight() - 1.0f, p);
            canvas.drawLine(3.0f * canvas.getWidth() / 4.0f, 0.0f, 3.0f * canvas.getWidth() / 4.0f, canvas.getHeight() - 1.0f, p);
            canvas.drawLine(0.0f, canvas.getHeight() / 2.0f, canvas.getWidth() - 1.0f, canvas.getHeight() / 2.0f, p);
            p.setColor(Color.WHITE);
            int crosshairs_radius = (int) (20 * scale + 0.5f);
            canvas.drawLine(canvas.getWidth() / 2.0f, canvas.getHeight() / 2.0f - crosshairs_radius, canvas.getWidth() / 2.0f, canvas.getHeight() / 2.0f + crosshairs_radius, p);
            canvas.drawLine(canvas.getWidth() / 2.0f - crosshairs_radius, canvas.getHeight() / 2.0f, canvas.getWidth() / 2.0f + crosshairs_radius, canvas.getHeight() / 2.0f, p);
        } else if (camera_controller != null && preference_grid.equals("preference_grid_crosshair")) {
            p.setColor(Color.WHITE);
            canvas.drawLine(canvas.getWidth() / 2.0f, 0.0f, canvas.getWidth() / 2.0f, canvas.getHeight() - 1.0f, p);
            canvas.drawLine(0.0f, canvas.getHeight() / 2.0f, canvas.getWidth() - 1.0f, canvas.getHeight() / 2.0f, p);
        } else if (camera_controller != null && preference_grid.equals("preference_grid_a4")) {
            p.setColor(Color.WHITE);
            float padding = (canvas.getWidth() / 5);
            float height = ((canvas.getWidth() - 2 * padding) / 1.41488f);
            float wight = canvas.getWidth() - padding;
            float verticalPadding = (canvas.getHeight() - height) / 2;
            canvas.drawLine(padding, verticalPadding, wight, verticalPadding, p);
            canvas.drawLine(wight, verticalPadding, wight, height + verticalPadding, p);
            canvas.drawLine(padding, verticalPadding, padding, height + verticalPadding, p);
            canvas.drawLine(padding, height + verticalPadding, wight, height + verticalPadding, p);
        }
        if (preview.isVideo() || sharedPreferences.getString(PreferenceKeys.getPreviewSizePreferenceKey(), "preference_preview_size_wysiwyg").equals("preference_preview_size_wysiwyg")) {
            String preference_crop_guide = sharedPreferences.getString(PreferenceKeys.getShowCropGuidePreferenceKey(), "crop_guide_none");
            if (camera_controller != null && preview.getTargetRatio() > 0.0 && !preference_crop_guide.equals("crop_guide_none")) {
                p.setStyle(Paint.Style.STROKE);
                p.setColor(Color.rgb(255, 235, 59)); // Yellow 500
                double crop_ratio = -1.0;
                switch (preference_crop_guide) {
                    case "crop_guide_1":
                        crop_ratio = 1.0;
                        break;
                    case "crop_guide_1.25":
                        crop_ratio = 1.25;
                        break;
                    case "crop_guide_1.33":
                        crop_ratio = 1.33333333;
                        break;
                    case "crop_guide_1.4":
                        crop_ratio = 1.4;
                        break;
                    case "crop_guide_1.5":
                        crop_ratio = 1.5;
                        break;
                    case "crop_guide_1.78":
                        crop_ratio = 1.77777778;
                        break;
                    case "crop_guide_1.85":
                        crop_ratio = 1.85;
                        break;
                    case "crop_guide_2.33":
                        crop_ratio = 2.33333333;
                        break;
                    case "crop_guide_2.35":
                        crop_ratio = 2.35006120; // actually 1920:817
                        break;
                    case "crop_guide_2.4":
                        crop_ratio = 2.4;
                        break;
                }
                if (crop_ratio > 0.0 && Math.abs(preview.getTargetRatio() - crop_ratio) > 1.0e-5) {

                    int left = 1, top = 1, right = canvas.getWidth() - 1, bottom = canvas.getHeight() - 1;
                    if (crop_ratio > preview.getTargetRatio()) {
                        double new_hheight = ((double) canvas.getWidth()) / (2.0f * crop_ratio);
                        top = canvas.getHeight() / 2 - (int) new_hheight;
                        bottom = canvas.getHeight() / 2 + (int) new_hheight;
                    } else {
                        double new_hwidth = (((double) canvas.getHeight()) * crop_ratio) / 2.0f;
                        left = canvas.getWidth() / 2 - (int) new_hwidth;
                        right = canvas.getWidth() / 2 + (int) new_hwidth;
                    }
                    canvas.drawRect(left, top, right, bottom, p);
                }
                p.setStyle(Paint.Style.FILL);
            }
        }


        if (camera_controller != null && this.thumbnail_anim && last_thumbnail != null) {
            long time = System.currentTimeMillis() - this.thumbnail_anim_start_ms;
            final long duration = 500;
            if (time > duration) {
                this.thumbnail_anim = false;
            } else {
                thumbnail_anim_src_rect.left = 0;
                thumbnail_anim_src_rect.top = 0;
                thumbnail_anim_src_rect.right = last_thumbnail.getWidth();
                thumbnail_anim_src_rect.bottom = last_thumbnail.getHeight();
                View galleryButton = main_activity.findViewById(R.id.gallery);
                float alpha = ((float) time) / (float) duration;

                int st_x = canvas.getWidth() / 2;
                int st_y = canvas.getHeight() / 2;
                int nd_x = galleryButton.getLeft() + galleryButton.getWidth() / 2;
                int nd_y = galleryButton.getTop() + galleryButton.getHeight() / 2;
                int thumbnail_x = (int) ((1.0f - alpha) * st_x + alpha * nd_x);
                int thumbnail_y = (int) ((1.0f - alpha) * st_y + alpha * nd_y);

                float st_w = canvas.getWidth();
                float st_h = canvas.getHeight();
                float nd_w = galleryButton.getWidth();
                float nd_h = galleryButton.getHeight();
                float correction_w = st_w / nd_w - 1.0f;
                float correction_h = st_h / nd_h - 1.0f;
                int thumbnail_w = (int) (st_w / (1.0f + alpha * correction_w));
                int thumbnail_h = (int) (st_h / (1.0f + alpha * correction_h));
                thumbnail_anim_dst_rect.left = thumbnail_x - thumbnail_w / 2;
                thumbnail_anim_dst_rect.top = thumbnail_y - thumbnail_h / 2;
                thumbnail_anim_dst_rect.right = thumbnail_x + thumbnail_w / 2;
                thumbnail_anim_dst_rect.bottom = thumbnail_y + thumbnail_h / 2;
                thumbnail_anim_matrix.setRectToRect(thumbnail_anim_src_rect, thumbnail_anim_dst_rect, Matrix.ScaleToFit.FILL);
                if (ui_rotation == 90 || ui_rotation == 270) {
                    float ratio = ((float) last_thumbnail.getWidth()) / (float) last_thumbnail.getHeight();
                    thumbnail_anim_matrix.preScale(ratio, 1.0f / ratio, last_thumbnail.getWidth() / 2.0f, last_thumbnail.getHeight() / 2.0f);
                }
                thumbnail_anim_matrix.preRotate(ui_rotation, last_thumbnail.getWidth() / 2.0f, last_thumbnail.getHeight() / 2.0f);
                canvas.drawBitmap(last_thumbnail, thumbnail_anim_matrix, p);
            }
        }

        canvas.save();
        canvas.rotate(ui_rotation, canvas.getWidth() / 2.0f, canvas.getHeight() / 2.0f);

        int text_y = (int) (20 * scale + 0.5f); // convert dps to pixels
        // fine tuning to adjust placement of text with respect to the GUI, depending on orientation
        int text_base_y = 0;
        if (ui_rotation == (ui_placement_right ? 0 : 180)) {
            text_base_y = canvas.getHeight() - (int) (0.5 * text_y);
        } else if (ui_rotation == (ui_placement_right ? 180 : 0)) {
            text_base_y = canvas.getHeight() - (int) (2.5 * text_y); // leave room for GUI icons
        } else if (ui_rotation == 90 || ui_rotation == 270) {
            //text_base_y = canvas.getHeight() + (int)(0.5*text_y);
            ImageButton view = main_activity.findViewById(R.id.take_photo);
            // align with "top" of the take_photo button, but remember to take the rotation into account!
            view.getLocationOnScreen(gui_location);
            int view_left = gui_location[0];
            preview.getView().getLocationOnScreen(gui_location);
            int this_left = gui_location[0];
            int diff_x = view_left - (this_left + canvas.getWidth() / 2);

            int max_x = canvas.getWidth();
            if (ui_rotation == 90) {
                max_x -= (int) (2.5 * text_y);
            }
            if (canvas.getWidth() / 2 + diff_x > max_x) {
                diff_x = max_x - canvas.getWidth() / 2;
            }
            text_base_y = canvas.getHeight() / 2 + diff_x - (int) (0.5 * text_y);
        }
        final int top_y = (int) (5 * scale + 0.5f); // convert dps to pixels
        final int location_size = (int) (20 * scale + 0.5f); // convert dps to pixels

        final String ybounds_text = getContext().getResources().getString(R.string.zoom) + getContext().getResources().getString(R.string.angle) + getContext().getResources().getString(R.string.direction);
        final double close_angle = 1.0f;
        if (camera_controller != null && !preview.isPreviewPaused()) {
            if (preview.isOnTimer()) {
                long remaining_time = (preview.getTimerEndTime() - System.currentTimeMillis() + 999) / 1000;

                if (remaining_time > 0) {
                    p.setTextSize(42 * scale + 0.5f);
                    p.setTextAlign(Paint.Align.CENTER);
                    String time_s = "";
                    if (remaining_time < 60) {
                        time_s = "" + remaining_time;
                    } else {
                        time_s = getTimeStringFromSeconds(remaining_time);
                    }
                    applicationInterface.drawTextWithBackground(canvas, p, time_s, Color.rgb(244, 67, 54), Color.BLACK, canvas.getWidth() / 2, canvas.getHeight() / 2); // Red 500
                }
            } else if (preview.isVideoRecording()) {
                long video_time = preview.getVideoTime();
                String time_s = getTimeStringFromSeconds(video_time / 1000);
                p.setTextSize(14 * scale + 0.5f);
                p.setTextAlign(Paint.Align.CENTER);
                int pixels_offset_y = 3 * text_y;
                int color = Color.rgb(244, 67, 54);
                if (main_activity.isScreenLocked()) {
                    applicationInterface.drawTextWithBackground(canvas, p, getContext().getResources().getString(R.string.screen_lock_message_2), color, Color.BLACK, canvas.getWidth() / 2, text_base_y - pixels_offset_y);
                    pixels_offset_y += text_y;
                    applicationInterface.drawTextWithBackground(canvas, p, getContext().getResources().getString(R.string.screen_lock_message_1), color, Color.BLACK, canvas.getWidth() / 2, text_base_y - pixels_offset_y);
                    pixels_offset_y += text_y;
                }
                applicationInterface.drawTextWithBackground(canvas, p, time_s, color, Color.BLACK, canvas.getWidth() / 2, text_base_y - pixels_offset_y);
            }
        } else if (camera_controller == null) {
            p.setColor(Color.WHITE);
            p.setTextSize(14 * scale + 0.5f); // convert dps to pixels
            p.setTextAlign(Paint.Align.CENTER);
            int pixels_offset = (int) (20 * scale + 0.5f); // convert dps to pixels
            canvas.drawText(getContext().getResources().getString(R.string.failed_to_open_camera_1), canvas.getWidth() / 2.0f, canvas.getHeight() / 2.0f, p);
            canvas.drawText(getContext().getResources().getString(R.string.failed_to_open_camera_2), canvas.getWidth() / 2.0f, canvas.getHeight() / 2.0f + pixels_offset, p);
            canvas.drawText(getContext().getResources().getString(R.string.failed_to_open_camera_3), canvas.getWidth() / 2.0f, canvas.getHeight() / 2.0f + 2 * pixels_offset, p);
        }
        if (camera_controller != null && sharedPreferences.getBoolean(PreferenceKeys.getShowISOPreferenceKey(), true)) {
            p.setTextSize(14 * scale + 0.5f); // convert dps to pixels
            p.setTextAlign(Paint.Align.LEFT);
            int location_x = (int) (50 * scale + 0.5f);
            int location_y = top_y + (int) (32 * scale + 0.5f); // convert dps to pixels
            if (ui_rotation == 90 || ui_rotation == 270) {
                int diff = canvas.getWidth() - canvas.getHeight();
                location_x += diff / 2;
                location_y -= diff / 2;
            }
            if (ui_rotation == 90) {
                location_y = canvas.getHeight() - location_y - location_size;
            }
            if (ui_rotation == 180) {
                location_x = canvas.getWidth() - location_x;
                p.setTextAlign(Paint.Align.RIGHT);
            }
            String string = "";
            if (camera_controller.captureResultHasIso()) {
                int iso = camera_controller.captureResultIso();
                if (string.length() > 0)
                    string += " ";
                string += preview.getISOString(iso);
            }
            if (camera_controller.captureResultHasExposureTime()) {
                long exposure_time = camera_controller.captureResultExposureTime();
                if (string.length() > 0)
                    string += " ";
                string += preview.getExposureTimeString(exposure_time);
            }

            if (string.length() > 0) {
                applicationInterface.drawTextWithBackground(canvas, p, string, Color.rgb(255, 235, 59), Color.BLACK, location_x, location_y, true, ybounds_text, true); // Yellow 500
            }

        }
        if (preview.supportsZoom() && camera_controller != null && sharedPreferences.getBoolean(PreferenceKeys.getShowZoomPreferenceKey(), true)) {
            float zoom_ratio = preview.getZoomRatio();
            if (zoom_ratio > 1.0f + 1.0e-5f) {
                p.setTextSize(14 * scale + 0.5f);
                p.setTextAlign(Paint.Align.CENTER);
                applicationInterface.drawTextWithBackground(canvas, p, getContext().getResources().getString(R.string.zoom) + ": " + zoom_ratio + "x", Color.WHITE, Color.BLACK, canvas.getWidth() / 2, text_base_y - text_y, false, ybounds_text, true);
            }
        }

        canvas.restore();
        if (camera_controller != null && continuous_focus_moving) {
            long dt = System.currentTimeMillis() - continuous_focus_moving_ms;
            final long length = 1000;
            if (dt <= length) {
                float frac = ((float) dt) / (float) length;
                float pos_x = canvas.getWidth() / 2.0f;
                float pos_y = canvas.getHeight() / 2.0f;
                float min_radius = 40 * scale + 0.5f; // convert dps to pixels
                float max_radius = 60 * scale + 0.5f; // convert dps to pixels
                float radius = 0.0f;
                if (frac < 0.5f) {
                    float alpha = frac * 2.0f;
                    radius = (1.0f - alpha) * min_radius + alpha * max_radius;
                } else {
                    float alpha = (frac - 0.5f) * 2.0f;
                    radius = (1.0f - alpha) * max_radius + alpha * min_radius;
                }

                p.setStyle(Paint.Style.STROKE);
                canvas.drawCircle(pos_x, pos_y, radius, p);
                p.setStyle(Paint.Style.FILL); // reset
            } else {
                continuous_focus_moving = false;
            }
        }

        if (preview.isFocusWaiting() || preview.isFocusRecentSuccess() || preview.isFocusRecentFailure()) {
            long time_since_focus_started = preview.timeSinceStartedAutoFocus();
            float min_radius = 40 * scale + 0.5f; // convert dps to pixels
            float max_radius = 45 * scale + 0.5f; // convert dps to pixels
            float radius = min_radius;
            if (time_since_focus_started > 0) {
                final long length = 500;
                float frac = ((float) time_since_focus_started) / (float) length;
                if (frac > 1.0f)
                    frac = 1.0f;
                if (frac < 0.5f) {
                    float alpha = frac * 2.0f;
                    radius = (1.0f - alpha) * min_radius + alpha * max_radius;
                } else {
                    float alpha = (frac - 0.5f) * 2.0f;
                    radius = (1.0f - alpha) * max_radius + alpha * min_radius;
                }
            }
            int size = (int) radius;

            if (preview.isFocusRecentSuccess())
                p.setColor(Color.rgb(20, 231, 21)); // Green A400
            else if (preview.isFocusRecentFailure())
                p.setColor(Color.rgb(244, 67, 54)); // Red 500
            else
                p.setColor(Color.WHITE);
            p.setStyle(Paint.Style.STROKE);
            int pos_x = 0;
            int pos_y = 0;
            if (preview.hasFocusArea()) {
                Pair<Integer, Integer> focus_pos = preview.getFocusPos();
                pos_x = focus_pos.first;
                pos_y = focus_pos.second;
            } else {
                pos_x = canvas.getWidth() / 2;
                pos_y = canvas.getHeight() / 2;
            }
            float frac = 0.5f;
            canvas.drawLine(pos_x - size, pos_y - size, pos_x - frac * size, pos_y - size, p);
            canvas.drawLine(pos_x + frac * size, pos_y - size, pos_x + size, pos_y - size, p);
            canvas.drawLine(pos_x - size, pos_y + size, pos_x - frac * size, pos_y + size, p);
            canvas.drawLine(pos_x + frac * size, pos_y + size, pos_x + size, pos_y + size, p);
            canvas.drawLine(pos_x - size, pos_y - size, pos_x - size, pos_y - frac * size, p);
            canvas.drawLine(pos_x - size, pos_y + frac * size, pos_x - size, pos_y + size, p);
            canvas.drawLine(pos_x + size, pos_y - size, pos_x + size, pos_y - frac * size, p);
            canvas.drawLine(pos_x + size, pos_y + frac * size, pos_x + size, pos_y + size, p);
            p.setStyle(Paint.Style.FILL); // reset
        }
    }
}
