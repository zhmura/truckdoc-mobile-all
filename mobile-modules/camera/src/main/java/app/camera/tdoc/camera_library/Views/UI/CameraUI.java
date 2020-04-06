package app.camera.tdoc.camera_library.Views.UI;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import app.camera.tdoc.camera_library.CamActivity;
import app.camera.tdoc.camera_library.NotificationHelper;
import app.camera.tdoc.camera_library.PreferenceKeys;
import app.camera.tdoc.camera_library.R;

import static app.camera.tdoc.camera_library.GalleryActivity.DELETE_FILES_ACTION;
import static app.camera.tdoc.camera_library.GalleryActivity.SEND_FILES_ACTION;


public class CameraUI {

    private CamActivity main_activity = null;
    private boolean popup_view_is_open = false;
    private SettingsView popup_view = null;
    private int current_orientation = 0;
    private boolean ui_placement_right = true;


    public CameraUI(CamActivity main_activity) {
        this.main_activity = main_activity;

        this.setSeekbarColors();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setSeekbarColors() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ColorStateList progress_color = ColorStateList.valueOf(Color.argb(255, 240, 240, 240));
            ColorStateList thumb_color = ColorStateList.valueOf(Color.argb(255, 255, 255, 255));

            SeekBar seekBar = main_activity.findViewById(R.id.zoom_seekbar);
            seekBar.setProgressTintList(progress_color);
            seekBar.setThumbTintList(thumb_color);

            seekBar = main_activity.findViewById(R.id.focus_seekbar);
            seekBar.setProgressTintList(progress_color);
            seekBar.setThumbTintList(thumb_color);

            seekBar = main_activity.findViewById(R.id.exposure_seekbar);
            seekBar.setProgressTintList(progress_color);
            seekBar.setThumbTintList(thumb_color);

            seekBar = main_activity.findViewById(R.id.iso_seekbar);
            seekBar.setProgressTintList(progress_color);
            seekBar.setThumbTintList(thumb_color);

            seekBar = main_activity.findViewById(R.id.exposure_time_seekbar);
            seekBar.setProgressTintList(progress_color);
            seekBar.setThumbTintList(thumb_color);
        }
    }

    public void layoutUI() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
        String ui_placement = sharedPreferences.getString(PreferenceKeys.getUIPlacementPreferenceKey(), "ui_right");
        this.ui_placement_right = ui_placement.equals("ui_right");
        int ui_rotation = 0;
        int align_left = RelativeLayout.ALIGN_LEFT;
        int align_right = RelativeLayout.ALIGN_RIGHT;
        int left_of = RelativeLayout.LEFT_OF;
        int right_of = RelativeLayout.RIGHT_OF;
        int above = RelativeLayout.ABOVE;
        int below = RelativeLayout.BELOW;
        int align_parent_left = RelativeLayout.ALIGN_PARENT_LEFT;
        int align_parent_right = RelativeLayout.ALIGN_PARENT_RIGHT;
        int align_parent_top = RelativeLayout.ALIGN_PARENT_TOP;
        int align_parent_bottom = RelativeLayout.ALIGN_PARENT_BOTTOM;
        if (!ui_placement_right) {
            above = RelativeLayout.BELOW;
            below = RelativeLayout.ABOVE;
            align_parent_top = RelativeLayout.ALIGN_PARENT_BOTTOM;
            align_parent_bottom = RelativeLayout.ALIGN_PARENT_TOP;
        }
        {
            View view = main_activity.findViewById(R.id.gui_anchor);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            layoutParams.addRule(align_parent_left, 0);
            layoutParams.addRule(align_parent_right, RelativeLayout.TRUE);
            layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
            layoutParams.addRule(align_parent_bottom, 0);
            layoutParams.addRule(left_of, 0);
            layoutParams.addRule(right_of, 0);
            view.setLayoutParams(layoutParams);


            view = main_activity.findViewById(R.id.close);
            layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
            layoutParams.addRule(align_parent_bottom, 0);
            layoutParams.addRule(left_of, R.id.gui_anchor);
            layoutParams.addRule(right_of, 0);
            view.setLayoutParams(layoutParams);

            view = main_activity.findViewById(R.id.settings);
            layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
            layoutParams.addRule(align_parent_bottom, 0);
            layoutParams.addRule(left_of, R.id.close);
            layoutParams.addRule(right_of, 0);
            view.setLayoutParams(layoutParams);

            view = main_activity.findViewById(R.id.gallery);
            layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
            layoutParams.addRule(align_parent_bottom, 0);
            layoutParams.addRule(left_of, R.id.settings);
            layoutParams.addRule(right_of, 0);
            view.setLayoutParams(layoutParams);


            view = main_activity.findViewById(R.id.popup);
            layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
            layoutParams.addRule(align_parent_bottom, 0);
            layoutParams.addRule(left_of, R.id.gallery);
            layoutParams.addRule(right_of, 0);
            view.setLayoutParams(layoutParams);

            if (!sharedPreferences.getBoolean(PreferenceKeys.getSettingEnableKey(), true)) {
                view.setVisibility(View.GONE);
            }

            view = main_activity.findViewById(R.id.exposure);
            layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
            layoutParams.addRule(align_parent_bottom, 0);
            layoutParams.addRule(left_of, R.id.popup);
            layoutParams.addRule(right_of, 0);
            view.setLayoutParams(layoutParams);

            if (!sharedPreferences.getBoolean(PreferenceKeys.getVideoEnableKey(), false)) {
                view.setVisibility(View.GONE);
            }

/*            view = main_activity.findViewById(R.id.switch_video);
            layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
            layoutParams.addRule(align_parent_bottom, 0);
            layoutParams.addRule(left_of, R.id.exposure);
            layoutParams.addRule(right_of, 0);
            view.setLayoutParams(layoutParams);

            if (!sharedPreferences.getBoolean(PreferenceKeys.getVideoEnableKey(), true)) {
                view.setVisibility(View.GONE);
            }*/

            view = main_activity.findViewById(R.id.take_photo);
            layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            layoutParams.addRule(align_parent_left, 0);
            layoutParams.addRule(align_parent_right, RelativeLayout.TRUE);
            view.setLayoutParams(layoutParams);

            view = main_activity.findViewById(R.id.send_photo);
            layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
            layoutParams.addRule(align_parent_left, RelativeLayout.TRUE);
            layoutParams.addRule(align_parent_bottom, 0);
            layoutParams.addRule(left_of, 0);
            view.setLayoutParams(layoutParams);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(main_activity);
                    builder.setMessage(main_activity.getResources().getString(R.string.send_all_scans_dlg))
                            .setCancelable(false)
                            .setPositiveButton(main_activity.getResources().getString(R.string.question_answer_y), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = null;
                                    try {
                                        intent = new Intent(main_activity, Class.forName("com.sanda.truckdoc.client.receivers.FileActionIntentReceiver"));
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                    intent.setAction(SEND_FILES_ACTION);
                                    LocalBroadcastManager.getInstance(main_activity).sendBroadcast(intent);
                                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
                                    sharedPreferences.edit().putInt(PreferenceKeys.getSessionPhotoCountPreferenceKey(), 0).apply();
                                    NotificationHelper.showNotificationMessage("Идет отправка фото ...", main_activity);
                                    TextView sessionCount = main_activity.findViewById(R.id.sessionCount);
                                    sessionCount.setText("( 0 )");
                                }
                            })
                            .setNegativeButton(main_activity.getResources().getString(R.string.question_answer_n), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }

                            });
                    builder.create().show();
                }
            });

            if (!sharedPreferences.getBoolean(PreferenceKeys.getSendBtnEnableKey(),
                    true)) {
                view.setVisibility(View.GONE);
                main_activity.findViewById(R.id.sessionCount).setVisibility(View.GONE);
            }

            view = main_activity.findViewById(R.id.delete_images);
            layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            layoutParams.addRule(align_parent_bottom, RelativeLayout.TRUE);
            layoutParams.addRule(align_parent_left, RelativeLayout.TRUE);
            view.setLayoutParams(layoutParams);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(main_activity);
                    builder.setMessage(main_activity.getResources().getString(R.string.delete_all_scans_dlg))
                            .setCancelable(false)
                            .setPositiveButton(main_activity.getResources().getString(R.string.question_answer_y), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = null;
                                    try {
                                        intent = new Intent(main_activity, Class.forName("com.sanda.truckdoc.client.receivers.FileActionIntentReceiver"));
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                    intent.setAction(DELETE_FILES_ACTION);
                                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
                                    sharedPreferences.edit().putInt(PreferenceKeys.getSessionPhotoCountPreferenceKey(), 0).apply();
                                    main_activity.sendBroadcast(intent);
                                    TextView sessionCount = main_activity.findViewById(R.id.sessionCount);
                                    sessionCount.setText("( 0 )");
                                }
                            })
                            .setNegativeButton(main_activity.getResources().getString(R.string.question_answer_n), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }

                            });
                    builder.create().show();
                }
            });

            if (!sharedPreferences.getBoolean(PreferenceKeys.getTrashBtnEnableKey(), true)) {
                view.setVisibility(View.GONE);
            }

            view = main_activity.findViewById(R.id.zoom_seekbar);
            layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            layoutParams.addRule(align_parent_left, 0);
            layoutParams.addRule(align_parent_right, RelativeLayout.TRUE);
            layoutParams.addRule(align_parent_top, 0);
            layoutParams.addRule(align_parent_bottom, RelativeLayout.TRUE);
            layoutParams.addRule(align_left, 0);
            layoutParams.addRule(align_right, 0);
            layoutParams.addRule(above, 0);
            layoutParams.addRule(below, 0);
            view.setLayoutParams(layoutParams);

            view = main_activity.findViewById(R.id.focus_seekbar);
            layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            layoutParams.addRule(align_left, R.id.preview);
            layoutParams.addRule(align_right, 0);
            layoutParams.addRule(left_of, R.id.zoom_seekbar);
            layoutParams.addRule(right_of, 0);
            layoutParams.addRule(align_parent_top, 0);
            layoutParams.addRule(align_parent_bottom, RelativeLayout.TRUE);
            view.setLayoutParams(layoutParams);
        }
        {
            int width_dp = 300;
            int height_dp = 50;
            final float scale = main_activity.getResources().getDisplayMetrics().density;
            int width_pixels = (int) (width_dp * scale + 0.5f); // convert dps to pixels
            int height_pixels = (int) (height_dp * scale + 0.5f); // convert dps to pixels

            View view = main_activity.findViewById(R.id.exposure_seekbar);
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
            lp.width = width_pixels;
            lp.height = height_pixels;
            view.setLayoutParams(lp);


            view.setTranslationX(0);
            view.setTranslationY(height_pixels);

            view = main_activity.findViewById(R.id.iso_seekbar);
            lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
            lp.width = width_pixels;
            lp.height = height_pixels;
            view.setLayoutParams(lp);

            view = main_activity.findViewById(R.id.exposure_time_seekbar);
            lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
            lp.width = width_pixels;
            lp.height = height_pixels;
            view.setLayoutParams(lp);
            view.setTranslationX(0);
            view.setTranslationY(height_pixels);

        }

        {
            View view = main_activity.findViewById(R.id.popup_container);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            layoutParams.addRule(align_right, R.id.popup);
            layoutParams.addRule(below, R.id.popup);
            layoutParams.addRule(align_parent_bottom, RelativeLayout.TRUE);
            layoutParams.addRule(above, 0);
            layoutParams.addRule(align_parent_top, 0);
            view.setLayoutParams(layoutParams);

            view.setRotation(ui_rotation);
            view.setTranslationX(0.0f);
            view.setTranslationY(0.0f);
            view.setPivotX(view.getWidth() / 2.0f);
            view.setPivotY(view.getHeight() / 2.0f);
        }

        setTakePhotoIcon();

    }

    public void setTakePhotoIcon() {
        if (main_activity.getPreview() != null) {
            ImageButton view = main_activity.findViewById(R.id.take_photo);
            int resource = 0;
            int content_description = 0;
            int switch_video_content_description = 0;
            if (main_activity.getPreview().isVideo()) {
                resource = main_activity.getPreview().isVideoRecording() ? R.drawable.ic_take_video_rec : R.drawable.take_video_selector;
                content_description = main_activity.getPreview().isVideoRecording() ? R.string.stop_video : R.string.start_video;
                switch_video_content_description = R.string.switch_to_photo;
            } else {
                resource = R.drawable.take_photo_selector;
                content_description = R.string.take_photo;
                switch_video_content_description = R.string.switch_to_video;
            }
            view.setImageResource(resource);
            view.setContentDescription(main_activity.getResources().getString(content_description));
            view.setTag(resource);

/*            view = (ImageButton) main_activity.findViewById(R.id.switch_video);
            view.setContentDescription(main_activity.getResources().getString(switch_video_content_description));*/
        }
    }


    public boolean getUIPlacementRight() {
        return this.ui_placement_right;
    }

    public void onOrientationChanged(int orientation) {
        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN)
            return;
        int diff = Math.abs(orientation - current_orientation);
        if (diff > 180)
            diff = 360 - diff;
        if (diff > 60) {
            orientation = (orientation + 45) / 90 * 90;
            orientation = orientation % 360;
            if (orientation != current_orientation) {
                this.current_orientation = orientation;
                layoutUI();
            }
        }
    }


    public void showGUI(final boolean show) {
        main_activity.runOnUiThread(new Runnable() {
            public void run() {
                final int visibility = show ? View.VISIBLE : View.GONE;
/*
                View switchVideoButton = (View) main_activity.findViewById(R.id.switch_video);
*/
                View exposureButton = main_activity.findViewById(R.id.exposure);

/*                if (!main_activity.getPreview().isVideo())
                    switchVideoButton.setVisibility(visibility);*/
                //if (main_activity.supportsExposureButton() && !main_activity.getPreview().isVideo())
                exposureButton.setVisibility(View.GONE);
                if (!show) {
                    closePopup();
                }
/*                if (!main_activity.getPreview().isVideo() || !main_activity.getPreview().supportsFlash()) {
                    //todo show popup
//                    popupButton.setVisibility(visibility);
                }*/
            }
        });
    }

    public void toggleExposureUI() {
        closePopup();
        SeekBar exposure_seek_bar = main_activity.findViewById(R.id.exposure_seekbar);
        int exposure_visibility = exposure_seek_bar.getVisibility();
        SeekBar iso_seek_bar = main_activity.findViewById(R.id.iso_seekbar);
        int iso_visibility = iso_seek_bar.getVisibility();
        SeekBar exposure_time_seek_bar = main_activity.findViewById(R.id.exposure_time_seekbar);
        int exposure_time_visibility = iso_seek_bar.getVisibility();
        boolean is_open = exposure_visibility == View.VISIBLE || iso_visibility == View.VISIBLE || exposure_time_visibility == View.VISIBLE;
        if (is_open) {
            clearSeekBar();
        } else if (main_activity.getPreview().getCameraController() != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
            String value = sharedPreferences.getString(PreferenceKeys.getISOPreferenceKey(), main_activity.getPreview().getCameraController().getDefaultISO());
            if (main_activity.getPreview().usingCamera2API() && !value.equals(main_activity.getPreview().getCameraController().getDefaultISO())) {
                if (main_activity.getPreview().supportsISORange()) {
                    iso_seek_bar.setVisibility(View.VISIBLE);
                    if (main_activity.getPreview().supportsExposureTime()) {
                        exposure_time_seek_bar.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                if (main_activity.getPreview().supportsExposures()) {
                    exposure_seek_bar.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public void setSeekbarZoom() {
        SeekBar zoomSeekBar = main_activity.findViewById(R.id.zoom_seekbar);
        zoomSeekBar.setProgress(main_activity.getPreview().getMaxZoom() - main_activity.getPreview().getCameraController().getZoom());
    }

    public void changeSeekbar(int seekBarId, int change) {
        SeekBar seekBar = main_activity.findViewById(seekBarId);
        int value = seekBar.getProgress();
        int new_value = value + change;
        if (new_value < 0)
            new_value = 0;
        else if (new_value > seekBar.getMax())
            new_value = seekBar.getMax();
        if (new_value != value) {
            seekBar.setProgress(new_value);
        }
    }

    public void clearSeekBar() {
        View view = main_activity.findViewById(R.id.exposure_seekbar);
        view.setVisibility(View.GONE);
        view = main_activity.findViewById(R.id.iso_seekbar);
        view.setVisibility(View.GONE);
        view = main_activity.findViewById(R.id.exposure_time_seekbar);
        view.setVisibility(View.GONE);
    }

    public void setPopupIcon() {
        ImageButton popup = main_activity.findViewById(R.id.popup);
        String flash_value = null;

        if (flash_value != null && flash_value.equals("flash_off")) {
            popup.setImageResource(R.drawable.flash_off);
        } else if (flash_value != null && flash_value.equals("flash_torch")) {
            popup.setImageResource(R.drawable.flash_torch);
        } else if (flash_value != null && flash_value.equals("flash_auto")) {
            popup.setImageResource(R.drawable.flash_auto);
        } else if (flash_value != null && flash_value.equals("flash_on")) {
            popup.setImageResource(R.drawable.flash_on);
        } else if (flash_value != null && flash_value.equals("flash_red_eye")) {
            popup.setImageResource(R.drawable.flash_red_eye);
        } else {
            popup.setImageResource(R.drawable.ic_settings);
        }
    }

    public void closePopup() {
        if (popupIsOpen()) {
            ViewGroup popup_container = main_activity.findViewById(R.id.popup_container);
            popup_container.removeAllViews();
            popup_view_is_open = false;

            destroyPopup();
        }
    }

    public boolean popupIsOpen() {
        return popup_view_is_open;
    }

    public void destroyPopup() {
        if (popupIsOpen()) {
            closePopup();
        }
        popup_view = null;
    }

    public void togglePopupSettings() {
        final ViewGroup popup_container = main_activity.findViewById(R.id.popup_container);
        if (popupIsOpen()) {
            closePopup();
            return;
        }
        if (main_activity.getPreview().getCameraController() == null) {
            return;
        }


        clearSeekBar();
        popup_container.setBackgroundColor(Color.BLACK);
        popup_container.setAlpha(0.9f);

        if (popup_view == null) {
            popup_view = new SettingsView(main_activity);
        }
        popup_container.addView(popup_view);
        popup_view_is_open = true;

        popup_container.getViewTreeObserver().addOnGlobalLayoutListener(
                new OnGlobalLayoutListener() {
                    @SuppressWarnings("deprecation")
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onGlobalLayout() {
                        layoutUI();
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                            popup_container.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else {
                            popup_container.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }

                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
                        String ui_placement = sharedPreferences.getString(PreferenceKeys.getUIPlacementPreferenceKey(), "ui_right");
                        boolean ui_placement_right = ui_placement.equals("ui_right");
                        ScaleAnimation animation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, ui_placement_right ? 0.0f : 1.0f);
                        animation.setDuration(100);
                        popup_container.setAnimation(animation);
                    }
                }
        );
    }
}
