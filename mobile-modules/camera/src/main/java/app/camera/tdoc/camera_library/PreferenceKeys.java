package app.camera.tdoc.camera_library;

public class PreferenceKeys {

    public static String getUseCamera2PreferenceKey() {
        return "preference_use_camera2";
    }

    public static String getFlashPreferenceKey(int cameraId) {
        return "flash_value_" + cameraId;
    }

    public static String getFocusPreferenceKey(int cameraId, boolean is_video) {
        return "focus_value_" + cameraId + "_" + is_video;
    }

    public static String getResolutionPreferenceKey(int cameraId) {
        return "camera_resolution_" + cameraId;
    }

    public static String getVideoQualityPreferenceKey(int cameraId) {
        return "video_quality_" + cameraId;
    }

    public static String getServiceCameraModeEnabledPreferenceKey() {
        return "is_service_camera_mode";
    }

    public static String getSessionPhotoCountPreferenceKey() {
        return "session_photo_count";
    }

    public static String getSyncEnabledPreferenceKey() {
        return "sync_enabled";
    }

    public static String getUserDeactivatedPreferenceKey() {
        return "user_deactivated";
    }

    public static String getIsVideoPreferenceKey() {
        return "is_video";
    }

    public static String getExposurePreferenceKey() {
        return "preference_exposure";
    }

    public static String getColorEffectPreferenceKey() {
        return "preference_color_effect";
    }

    public static String getSceneModePreferenceKey() {
        return "preference_scene_mode";
    }

    public static String getWhiteBalancePreferenceKey() {
        return "preference_white_balance";
    }

    public static String getISOPreferenceKey() {
        return "preference_iso";
    }

    public static String getExposureTimePreferenceKey() {
        return "preference_exposure_time";
    }

    public static String getQualityPreferenceKey() {
        return "preference_quality";
    }

    public static String getAutoStabilisePreferenceKey() {
        return "preference_auto_stabilise";
    }


    public static String getRequireLocationPreferenceKey() {
        return "preference_require_location";
    }

    public static String getStampPreferenceKey() {
        return "preference_stamp";
    }

    public static String getStampDateFormatPreferenceKey() {
        return "preference_stamp_dateformat";
    }

    public static String getStampTimeFormatPreferenceKey() {
        return "preference_stamp_timeformat";
    }

    public static String getStampGPSFormatPreferenceKey() {
        return "preference_stamp_gpsformat";
    }


    public static String getStampStyleKey() {
        return "preference_stamp_style";
    }

    public static String getBackgroundPhotoSavingPreferenceKey() {
        return "preference_background_photo_saving";
    }

    public static String getUIPlacementPreferenceKey() {
        return "preference_ui_placement";
    }

    public static String getTouchCapturePreferenceKey() {
        return "preference_touch_capture";
    }

    public static String getPausePreviewPreferenceKey() {
        return "preference_pause_preview";
    }


    public static String getTakePhotoBorderPreferenceKey() {
        return "preference_take_photo_border";
    }

    public static String getShowWhenLockedPreferenceKey() {
        return "preference_show_when_locked";
    }

    public static String getStartupFocusPreferenceKey() {
        return "preference_startup_focus";
    }

    public static String getKeepDisplayOnPreferenceKey() {
        return "preference_keep_display_on";
    }

    public static String getMaxBrightnessPreferenceKey() {
        return "preference_max_brightness";
    }

    public static String getUsingSAFPreferenceKey() {
        return "preference_using_saf";
    }

    public static String getSaveLocationPreferenceKey() {
        return "preference_save_location";
    }

    public static String getSaveLocationSAFPreferenceKey() {
        return "preference_save_location_saf";
    }

    public static String getSaveVideoPrefixPreferenceKey() {
        return "preference_save_video_prefix";
    }

    public static String getSaveZuluTimePreferenceKey() {
        return "preference_save_zulu_time";
    }


    public static String getShowZoomSliderControlsPreferenceKey() {
        return "preference_show_zoom_slider_controls";
    }

    public static String getShowZoomPreferenceKey() {
        return "preference_show_zoom";
    }

    public static String getShowISOPreferenceKey() {
        return "preference_show_iso";
    }

    public static String getShowGridPreferenceKey() {
        return "preference_grid";
    }

    public static String getShowCropGuidePreferenceKey() {
        return "preference_crop_guide";
    }


    public static String getVideoStabilizationPreferenceKey() {
        return "preference_video_stabilization";
    }

    public static String getVideoBitratePreferenceKey() {
        return "preference_video_bitrate";
    }

    public static String getVideoFPSPreferenceKey() {
        return "preference_video_fps";
    }

    public static String getVideoMaxDurationPreferenceKey() {
        return "preference_video_max_duration";
    }

    public static String getVideoRestartPreferenceKey() {
        return "preference_video_restart";
    }

    public static String getVideoMaxFileSizePreferenceKey() {
        return "preference_video_max_filesize";
    }

    public static String getVideoRestartMaxFileSizePreferenceKey() {
        return "preference_video_restart_max_filesize";
    }

    public static String getVideoFlashPreferenceKey() {
        return "preference_video_flash";
    }

    public static String getLockVideoPreferenceKey() {
        return "preference_lock_video";
    }

    public static String getPreviewSizePreferenceKey() {
        return "preference_preview_size";
    }

    public static String getRotatePreviewPreferenceKey() {
        return "preference_rotate_preview";
    }

    public static String getLockOrientationPreferenceKey() {
        return "preference_lock_orientation";
    }

    public static String getShutterSoundPreferenceKey() {
        return "preference_shutter_sound";
    }

    public static String getBaseFolderPathKey() {
        return "preference_main_folder_path_name";
    }

    public static String getImageTypeKey() {
        return "preference_imagetype_key";
    }

    public static String getRecipientKey() {
        return "preference_recipient_id";
    }

    public static String getGalleryFolderPathKey() {
        return "preference_gallery_folder_name";
    }

    public static String getTrashBtnEnableKey() {
        return "preference_trash_button";
    }

    public static String getSendBtnEnableKey() {
        return "preference_send_button";
    }


    public static String getSettingEnableKey() {
        return "preference_SETTING_ENABLE_PERMISSIOM";
    }

    public static String getWhiteBalanceKey() {
        return "preference_WHITE_BALANCE_PERMISSION";
    }

    public static String getColorEffectsKey() {
        return "preference_COLOR_EFFECTS_PERMISSION";
    }

    public static String getResolutionKey() {
        return "preference_RESOLUTION_PERMISSION";
    }

    public static String getBordersKey() {
        return "preference_BORDERS_PERMISSION";
    }

    public static String getIsoKey() {
        return "preference_ISO_PERMISSION";
    }

    public static String getFocusOptionKey() {
        return "preference_FOCUS_OPTIONS_PERMISSION";
    }

    public static String getFlashOptionKey() {
        return "preference_FLASH_OPTIONS_PERMISSION";
    }

    public static String getExposureEnableKey() {
        return "preference_EXPOSURE_PERMISSION";
    }

    public static String getAutostabiliseKey() {
        return "preference_AUTO_STABILISE_PERMISSION";
    }

    public static String getVideoEnableKey() {
        return "preference_VIDEO_ENABLE_PERMISSION";
    }

    public static String getTimeStampEnable() {
        return "preference_TIMESTAMP_PERMISSION";
    }

    public static String getLocationStampEnable() {
        return "preference_LOCATION_PERMISSION";
    }

    public static String getPrefixImageKey() {
        return "preference_Prefix_Image_key";
    }


    public static String getFileNameKey() {
        return "file_name";
    }

    public static String getKeyForNewImagePath() {
        return "NEW_IMAGE_FILE_PATH";
    }

    public static String getLastPhotoModeKey() {
        return "Last_Photo_Mode";
    }

    public static String getPrefixSpinnerPositionKey() {
        return "preference_Prefix__spiner_position_key";
    }

    public static String getPrefixImageListKey() {
        return "preference_Prefix_Image_list_key";
    }

    public static String getPrefixEnable() {
        return "preference_Prefix_enable";
    }

}
