package app.camera.tdoc.camera_library.Views.UI;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import app.camera.tdoc.camera_library.CamActivity;
import app.camera.tdoc.camera_library.Controllers.CameraController;
import app.camera.tdoc.camera_library.PreferenceKeys;
import app.camera.tdoc.camera_library.R;
import app.camera.tdoc.camera_library.Views.Preview;

public class SettingsView extends LinearLayout {

    public static final float ALPHA_BUTTON_SELECTED = 1.0f;
    public static final float ALPHA_BUTTON = 0.6f;

    private int picture_size_index = -1;
    private int grid_index = -1;

    private Map<String, View> popup_buttons = new Hashtable<String, View>();

    public SettingsView(Context context) {
        super(context);

        this.setOrientation(LinearLayout.VERTICAL);

        final CamActivity main_activity = (CamActivity) this.getContext();
        final Preview preview = main_activity.getPreview();
        int layoutPadding = (int) (20 * getContext().getResources().getDisplayMetrics().density);
        this.setPadding(layoutPadding, layoutPadding, layoutPadding, layoutPadding);

        if (preview.isVideo() && preview.isTakingPhoto()) {

        } else {
            List<String> supported_focus_values = preview.getSupportedFocusValues();
            if (supported_focus_values != null) {
                supported_focus_values = new ArrayList<String>(supported_focus_values);
                if (preview.isVideo()) {
                    supported_focus_values.remove("focus_mode_continuous_picture");
                } else {
                    supported_focus_values.remove("focus_mode_continuous_video");
                }
            }


            List<String> supported_isos = preview.getSupportedISOs();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
            if (sharedPreferences.getBoolean(PreferenceKeys.getAutostabiliseKey(), true)) {
                if (main_activity.supportsAutoStabilise()) {
                    CheckBox checkBox = new CheckBox(main_activity);
                    checkBox.setText(getResources().getString(R.string.preference_auto_stabilise));
                    checkBox.setTextColor(Color.WHITE);

                    boolean auto_stabilise = sharedPreferences.getBoolean(PreferenceKeys.getAutoStabilisePreferenceKey(), false);
                    checkBox.setChecked(auto_stabilise);
                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        public void onCheckedChanged(CompoundButton buttonView,
                                                     boolean isChecked) {
                            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(PreferenceKeys.getAutoStabilisePreferenceKey(), isChecked);
                            editor.apply();
                            main_activity.closePopup();
                        }
                    });
                    checkBox.setButtonDrawable(R.drawable.checkbox_selector);

                    this.addView(checkBox);
                }
            }


            if (sharedPreferences.getBoolean(PreferenceKeys.getResolutionKey(), true)) {
                final List<CameraController.Size> picture_sizes = preview.getSupportedPictureSizes();
                picture_size_index = preview.getCurrentPictureSizeIndex();
                final List<String> picture_size_strings = new ArrayList<String>();
                String size_string = "";
                if (picture_sizes.get(0).width * picture_sizes.get(0).height <
                        picture_sizes.get(picture_sizes.size() - 1).width * picture_sizes.get(picture_sizes.size() - 1).height) {
                    for (int i = 0; i < picture_sizes.size(); i++) {
                        if (i <= picture_sizes.size() / 3) {
                            size_string = getResources().getString(R.string.small_size);
                        } else if (i > picture_sizes.size() / 3 && i <= picture_sizes.size() / 1.5) {
                            size_string = getResources().getString(R.string.normal_size);
                        } else {
                            size_string = getResources().getString(R.string.big_size);
                        }
                        size_string += " (" + Preview.getMPString(picture_sizes.get(i).width, picture_sizes.get(i).height) + ")";
                        picture_size_strings.add(size_string);
                    }
                } else {
                    for (int i = 0; i < picture_sizes.size(); i++) {
                        if (i <= picture_sizes.size() / 3) {
                            size_string = getResources().getString(R.string.big_size);
                        } else if (i > picture_sizes.size() / 3 && i <= picture_sizes.size() / 1.5) {
                            size_string = getResources().getString(R.string.normal_size);
                        } else {
                            size_string = getResources().getString(R.string.small_size);
                        }
                        size_string += " (" + Preview.getMPString(picture_sizes.get(i).width, picture_sizes.get(i).height) + ")";
                        picture_size_strings.add(size_string);
                    }
                }


                addArrayOptionsToPopup(picture_size_strings, getResources().getString(R.string.preference_resolution), false, picture_size_index, false, "PHOTO_RESOLUTIONS",
                        new ArrayOptionsPopupListener() {
                            final Handler handler = new Handler();
                            Runnable update_runnable = new Runnable() {
                                @Override
                                public void run() {
                                    main_activity.updateForSettings("");
                                }
                            };

                            private void update() {
                                if (picture_size_index == -1)
                                    return;
                                CameraController.Size new_size = picture_sizes.get(picture_size_index);
                                String resolution_string = new_size.width + " " + new_size.height;
                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(PreferenceKeys.getResolutionPreferenceKey(preview.getCameraId()), resolution_string);
                                editor.apply();

                                // make it easier to scroll through the list of resolutions without a pause each time
                                handler.removeCallbacks(update_runnable);
                                handler.postDelayed(update_runnable, 400);
                            }

                            @Override
                            public int onClickPrev() {
                                if (picture_size_index != -1 && picture_size_index > 0) {
                                    picture_size_index--;
                                    update();
                                    return picture_size_index;
                                }
                                return -1;
                            }

                            @Override
                            public int onClickNext() {
                                if (picture_size_index != -1 && picture_size_index < picture_sizes.size() - 1) {
                                    picture_size_index++;
                                    update();
                                    return picture_size_index;
                                }
                                return -1;
                            }
                        });
            }

            if (sharedPreferences.getBoolean(PreferenceKeys.getBordersKey(), true)) {
                final String[] grid_values = getResources().getStringArray(R.array.preference_grid_values);
                String[] grid_entries = getResources().getStringArray(R.array.preference_grid_entries);
                String grid_value = sharedPreferences.getString(PreferenceKeys.getShowGridPreferenceKey(), "preference_grid_none");
                grid_index = Arrays.asList(grid_values).indexOf(grid_value);
                if (grid_index == -1) {
                    grid_index = 0;
                }
                addArrayOptionsToPopup(Arrays.asList(grid_entries), getResources().getString(R.string.grid), false, grid_index, true, "GRID", new ArrayOptionsPopupListener() {
                    private void update() {
                        if (grid_index == -1)
                            return;
                        String new_grid_value = grid_values[grid_index];
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(PreferenceKeys.getShowGridPreferenceKey(), new_grid_value);
                        editor.apply();
                    }

                    @Override
                    public int onClickPrev() {
                        if (grid_index != -1) {
                            grid_index--;
                            if (grid_index < 0)
                                grid_index += grid_values.length;
                            update();
                            return grid_index;
                        }
                        return -1;
                    }

                    @Override
                    public int onClickNext() {
                        if (grid_index != -1) {
                            grid_index++;
                            if (grid_index >= grid_values.length)
                                grid_index -= grid_values.length;
                            update();
                            return grid_index;
                        }
                        return -1;
                    }
                });
            }

          /*  final List<String> video_sizes = preview.getSupportedVideoQuality();
            video_size_index = preview.getCurrentVideoQualityIndex();
            final List<String> video_size_strings = new ArrayList<String>();
            for (String video_size : video_sizes) {
                String quality_string = preview.getCamcorderProfileDescriptionShort(video_size);
                video_size_strings.add(quality_string);
            }
            addArrayOptionsToPopup(video_size_strings, getResources().getString(R.string.video_quality), false, video_size_index, false, "VIDEO_RESOLUTIONS", new ArrayOptionsPopupListener() {
                final Handler handler = new Handler();
                Runnable update_runnable = new Runnable() {
                    @Override
                    public void run() {
                        main_activity.updateForSettings("");
                    }
                };

                private void update() {
                    if (video_size_index == -1)
                        return;
                    String quality = video_sizes.get(video_size_index);
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(PreferenceKeys.getVideoQualityPreferenceKey(preview.getCameraId()), quality);
                    editor.apply();

                    // make it easier to scroll through the list of resolutions without a pause each time
                    handler.removeCallbacks(update_runnable);
                    handler.postDelayed(update_runnable, 400);
                }

                @Override
                public int onClickPrev() {
                    if (video_size_index != -1 && video_size_index > 0) {
                        video_size_index--;
                        update();
                        return video_size_index;
                    }
                    return -1;
                }

                @Override
                public int onClickNext() {
                    if (video_size_index != -1 && video_size_index < video_sizes.size() - 1) {
                        video_size_index++;
                        update();
                        return video_size_index;
                    }
                    return -1;
                }
            });
*/

            if (sharedPreferences.getBoolean(PreferenceKeys.getFlashOptionKey(), true)) {
                List<String> supported_flash_values = preview.getSupportedFlashValues();
                addButtonOptionsToPopup(supported_flash_values, R.array.flash_icons, R.array.flash_values, getResources().getString(R.string.flash_mode), preview.getCurrentFlashValue(), "TEST_FLASH", new ButtonOptionsPopupListener() {
                    @Override
                    public void onClick(String option) {
                        preview.updateFlash(option);
                        main_activity.getMainUI().setPopupIcon();
                        main_activity.closePopup();
                    }
                });
            }

            if (sharedPreferences.getBoolean(PreferenceKeys.getFocusOptionKey(), true)) {
                addButtonOptionsToPopup(supported_focus_values, R.array.focus_mode_icons, R.array.focus_mode_values, getResources().getString(R.string.focus_mode), preview.getCurrentFocusValue(), "TEST_FOCUS", new ButtonOptionsPopupListener() {
                    @Override
                    public void onClick(String option) {
                        preview.updateFocus(option, false, true);
                        main_activity.closePopup();
                    }
                });
            }

            if (sharedPreferences.getBoolean(PreferenceKeys.getIsoKey(), true)) {
                String current_iso = sharedPreferences.getString(PreferenceKeys.getISOPreferenceKey(), "auto");
                addButtonOptionsToPopup(supported_isos, -1, -1, "ISO", current_iso, "TEST_ISO", new ButtonOptionsPopupListener() {
                    @Override
                    public void onClick(String option) {
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(PreferenceKeys.getISOPreferenceKey(), option);
                        if (option.equals("auto")) {
                            editor.putLong(PreferenceKeys.getExposureTimePreferenceKey(), CameraController.EXPOSURE_TIME_DEFAULT);
                        } else {
                            if (preview.usingCamera2API()) {
                                if (preview.getCameraController() != null && preview.getCameraController().captureResultHasExposureTime()) {
                                    long exposure_time = preview.getCameraController().captureResultExposureTime();
                                    editor.putLong(PreferenceKeys.getExposureTimePreferenceKey(), exposure_time);
                                }
                            }
                        }
                        editor.apply();

                        main_activity.updateForSettings("ISO: " + option);
                        main_activity.closePopup();
                    }
                });
            }


            if (preview.getCameraController() != null) {
                if (sharedPreferences.getBoolean(PreferenceKeys.getWhiteBalanceKey(), true)) {
                    List<String> supported_white_balances = preview.getSupportedWhiteBalances();
                    addRadioOptionsToPopup(supported_white_balances, getResources().getString(R.string.white_balance), PreferenceKeys.getWhiteBalancePreferenceKey(), preview.getCameraController().getDefaultWhiteBalance(), "TEST_WHITE_BALANCE");
                }
              /*  List<String> supported_scene_modes = preview.getSupportedSceneModes();
                addRadioOptionsToPopup(supported_scene_modes, getResources().getString(R.string.scene_mode), PreferenceKeys.getSceneModePreferenceKey(), preview.getCameraController().getDefaultSceneMode(), "TEST_SCENE_MODE");
                */
                if (sharedPreferences.getBoolean(PreferenceKeys.getColorEffectsKey(), true)) {
                    List<String> supported_color_effects = preview.getSupportedColorEffects();
                    addRadioOptionsToPopup(supported_color_effects, getResources().getString(R.string.color_effect), PreferenceKeys.getColorEffectPreferenceKey(), preview.getCameraController().getDefaultColorEffect(), "TEST_COLOR_EFFECT");
                }
            }


        }
    }

    private abstract class ButtonOptionsPopupListener {
        public abstract void onClick(String option);
    }

    private void addButtonOptionsToPopup(List<String> supported_options, int icons_id, int values_id, String prefix_string, String current_value, String test_key, final ButtonOptionsPopupListener listener) {
        if (supported_options != null) {
            LinearLayout ll2 = new LinearLayout(this.getContext());
            ll2.setOrientation(LinearLayout.HORIZONTAL);
            String[] icons = icons_id != -1 ? getResources().getStringArray(icons_id) : null;
            String[] values = values_id != -1 ? getResources().getStringArray(values_id) : null;

            final float scale = getResources().getDisplayMetrics().density;
            int total_width = 280;
            {
                Activity activity = (Activity) this.getContext();
                Display display = activity.getWindowManager().getDefaultDisplay();
                DisplayMetrics outMetrics = new DisplayMetrics();
                display.getMetrics(outMetrics);

                // the height should limit the width, due to when held in portrait
                int dpHeight = (int) (outMetrics.heightPixels / scale);
                dpHeight -= 50; // allow space for the icons at top/right of screen
                if (total_width > dpHeight)
                    total_width = dpHeight;
            }
            int button_width_dp = total_width / supported_options.size();
            boolean use_scrollview = false;
            if (button_width_dp < 40) {
                button_width_dp = 40;
                use_scrollview = true;
            }
            View current_view = null;

            for (final String supported_option : supported_options) {
                int resource = -1;
                if (icons != null && values != null) {
                    int index = -1;
                    for (int i = 0; i < values.length && index == -1; i++) {
                        if (values[i].equals(supported_option))
                            index = i;
                    }
                    if (index != -1) {
                        resource = getResources().getIdentifier(icons[index], null, this.getContext().getApplicationContext().getPackageName());
                    }
                }

                String button_string = "";
                // hacks for ISO mode ISO_HJR (e.g., on Samsung S5)
                // also some devices report e.g. "ISO100" etc
                if (prefix_string.length() == 0) {
                    button_string = supported_option;
                } else if (prefix_string.equalsIgnoreCase("ISO") && supported_option.length() >= 4 && supported_option.substring(0, 4).equalsIgnoreCase("ISO_")) {
                    button_string = prefix_string + "\n" + supported_option.substring(4);
                } else if (prefix_string.equalsIgnoreCase("ISO") && supported_option.length() >= 3 && supported_option.substring(0, 3).equalsIgnoreCase("ISO")) {
                    button_string = prefix_string + "\n" + supported_option.substring(3);
                } else {
                    button_string = prefix_string + "\n" + supported_option;
                }
                View view = null;
                if (resource != -1) {
                    ImageButton image_button = new ImageButton(this.getContext());
                    view = image_button;
                    ll2.addView(view);

                    image_button.setImageResource(resource);
                    final CamActivity main_activity = (CamActivity) this.getContext();
                    Bitmap bm = main_activity.getPreloadedBitmap(resource);
                    if (bm != null)
                        image_button.setImageBitmap(bm);
                    image_button.setScaleType(ScaleType.FIT_CENTER);
                    final int padding = (int) (10 * scale + 0.5f); // convert dps to pixels
                    view.setPadding(padding, padding, padding, padding);
                } else {
                    Button button = new Button(this.getContext());
                    button.setBackgroundColor(Color.TRANSPARENT); // workaround for Android 6 crash!
                    view = button;
                    ll2.addView(view);

                    button.setText(button_string);
                    button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.0f);
                    button.setTextColor(Color.WHITE);
                    // need 0 padding so we have enough room to display text for ISO buttons, when there are 6 ISO settings
                    final int padding = (int) (0 * scale + 0.5f); // convert dps to pixels
                    view.setPadding(padding, padding, padding, padding);
                }

                ViewGroup.LayoutParams params = view.getLayoutParams();
                params.width = (int) (button_width_dp * scale + 0.5f); // convert dps to pixels
                params.height = (int) (50 * scale + 0.5f); // convert dps to pixels
                view.setLayoutParams(params);

                view.setContentDescription(button_string);
                if (supported_option.equals(current_value)) {
                    view.setAlpha(ALPHA_BUTTON_SELECTED);
                    current_view = view;
                } else {
                    view.setAlpha(ALPHA_BUTTON);
                }
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onClick(supported_option);
                    }
                });
                this.popup_buttons.put(test_key + "_" + supported_option, view);
            }
            if (use_scrollview) {
                final HorizontalScrollView scroll = new HorizontalScrollView(this.getContext());
                scroll.addView(ll2);
                {
                    ViewGroup.LayoutParams params = new LayoutParams(
                            (int) (total_width * scale + 0.5f), // convert dps to pixels
                            LayoutParams.WRAP_CONTENT);
                    scroll.setLayoutParams(params);
                }
                this.addView(scroll);
                if (current_view != null) {
                    final View final_current_view = current_view;
                    this.getViewTreeObserver().addOnGlobalLayoutListener(
                            new OnGlobalLayoutListener() {
                                @Override
                                public void onGlobalLayout() {
                                    scroll.scrollTo(final_current_view.getLeft(), 0);
                                }
                            }
                    );
                }
            } else {
                this.addView(ll2);
            }
        }
    }

    private void addTitleToPopup(final String title) {
        TextView text_view = new TextView(this.getContext());
        text_view.setText(title);
        text_view.setTextColor(Color.WHITE);
        text_view.setGravity(Gravity.CENTER);
        text_view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 8.0f);
        this.addView(text_view);
    }

    private void addRadioOptionsToPopup(List<String> supported_options, final String title, final String preference_key, final String default_option, final String test_key) {
        if (supported_options != null) {
            final CamActivity main_activity = (CamActivity) this.getContext();

            addTitleToPopup(title);

            RadioGroup rg = new RadioGroup(this.getContext());
            rg.setOrientation(RadioGroup.VERTICAL);
            this.popup_buttons.put(test_key, rg);

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
            String current_option = sharedPreferences.getString(preference_key, default_option);
            for (final String supported_option : supported_options) {
                RadioButton button = new RadioButton(this.getContext());
                button.setText(supported_option);
                button.setTextColor(Color.WHITE);
                if (supported_option.equals(current_option)) {
                    button.setChecked(true);
                } else {
                    button.setChecked(false);
                }
                rg.addView(button);
                button.setContentDescription(supported_option);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(preference_key, supported_option);
                        editor.apply();

                        main_activity.updateForSettings(title + ": " + supported_option);
                        main_activity.closePopup();
                    }
                });
                this.popup_buttons.put(test_key + "_" + supported_option, button);
                button.setButtonDrawable(R.drawable.radio_style);
            }
            this.addView(rg);
        }
    }

    private abstract class ArrayOptionsPopupListener {
        public abstract int onClickPrev();

        public abstract int onClickNext();
    }

    private void addArrayOptionsToPopup(final List<String> supported_options, final String title, final boolean title_in_options, final int current_index, final boolean cyclic, final String test_key, final ArrayOptionsPopupListener listener) {
        if (supported_options != null && current_index != -1) {
            if (!title_in_options) {
                TextView text_view = new TextView(this.getContext());
                text_view.setText(title);
                text_view.setTextColor(Color.WHITE);
                text_view.setGravity(Gravity.CENTER);
                text_view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 8.0f);
                this.addView(text_view);
            }

            LinearLayout ll2 = new LinearLayout(this.getContext());
            ll2.setOrientation(LinearLayout.HORIZONTAL);
            ll2.setVerticalGravity(Gravity.CENTER_VERTICAL);

            final TextView resolution_text_view = new TextView(this.getContext());
            if (title_in_options)
                resolution_text_view.setText(title + ": " + supported_options.get(current_index));
            else
                resolution_text_view.setText(supported_options.get(current_index));
            resolution_text_view.setTextColor(Color.WHITE);
            resolution_text_view.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                    (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            resolution_text_view.setLayoutParams(params);

            final float scale = getResources().getDisplayMetrics().density;
            final ImageView prev_button = new ImageView(this.getContext());
            prev_button.setBackgroundColor(Color.TRANSPARENT);
            ll2.addView(prev_button);
//            final int padding = (int) (0 * scale + 0.5f);
            ViewGroup.LayoutParams vg_params = prev_button.getLayoutParams();
            prev_button.setImageDrawable(getResources().getDrawable(R.drawable.ic_left));
            prev_button.setLayoutParams(new LinearLayout.LayoutParams
                    (LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

            prev_button.setLayoutParams(vg_params);
            int pdng = (int) scale * 10;
            prev_button.setPadding(pdng, pdng, pdng, pdng);
            prev_button.setVisibility((cyclic || current_index > 0) ? View.VISIBLE : View.INVISIBLE);

            this.popup_buttons.put(test_key + "_PREV", prev_button);

            ll2.addView(resolution_text_view);
            this.popup_buttons.put(test_key, resolution_text_view);

            final ImageView next_button = new ImageView(this.getContext());
            next_button.setBackgroundColor(Color.TRANSPARENT); // workaround for Android 6 crash!
            ll2.addView(next_button);
            next_button.setImageDrawable(getResources().getDrawable(R.drawable.ic_right));
            next_button.setPadding(pdng, pdng, pdng, pdng);
            next_button.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

            next_button.setVisibility((cyclic || current_index < supported_options.size() - 1) ? View.VISIBLE : View.INVISIBLE);
            this.popup_buttons.put(test_key + "_NEXT", next_button);

            prev_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int new_index = listener.onClickPrev();
                    if (new_index != -1) {
                        if (title_in_options)
                            resolution_text_view.setText(title + ": " + supported_options.get(new_index));
                        else
                            resolution_text_view.setText(supported_options.get(new_index));
                        prev_button.setVisibility((cyclic || new_index > 0) ? View.VISIBLE : View.INVISIBLE);
                        next_button.setVisibility((cyclic || new_index < supported_options.size() - 1) ? View.VISIBLE : View.INVISIBLE);
                    }
                }
            });
            next_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int new_index = listener.onClickNext();
                    if (new_index != -1) {
                        if (title_in_options)
                            resolution_text_view.setText(title + ": " + supported_options.get(new_index));
                        else
                            resolution_text_view.setText(supported_options.get(new_index));
                        prev_button.setVisibility((cyclic || new_index > 0) ? View.VISIBLE : View.INVISIBLE);
                        next_button.setVisibility((cyclic || new_index < supported_options.size() - 1) ? View.VISIBLE : View.INVISIBLE);
                    }
                }
            });
            this.addView(ll2);
        }
    }
}
