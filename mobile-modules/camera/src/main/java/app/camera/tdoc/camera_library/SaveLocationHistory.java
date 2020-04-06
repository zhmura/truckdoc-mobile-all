package app.camera.tdoc.camera_library;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;

public class SaveLocationHistory {

    private CamActivity main_activity = null;
    private String pref_base = null;
    private ArrayList<String> save_location_history = new ArrayList<String>();


    SaveLocationHistory(CamActivity main_activity, String pref_base, String folder_name) {
        this.main_activity = main_activity;
        this.pref_base = pref_base;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);

        save_location_history.clear();
        int save_location_history_size = sharedPreferences.getInt(pref_base + "_size", 0);
        for (int i = 0; i < save_location_history_size; i++) {
            String string = sharedPreferences.getString(pref_base + "_" + i, null);
            if (string != null) {
                save_location_history.add(string);
            }
        }
        updateFolderHistory(folder_name, false);
    }

    void updateFolderHistory(String folder_name, boolean update_icon) {
        updateFolderHistory(folder_name);
        if (update_icon) {
            main_activity.updateGalleryIcon();
        }
    }

    private void updateFolderHistory(String folder_name) {
        while (save_location_history.remove(folder_name)) {
        }
        save_location_history.add(folder_name);
        while (save_location_history.size() > 6) {
            save_location_history.remove(0);
        }
        writeSaveLocations();
    }

    public void clearFolderHistory(String folder_name) {
        save_location_history.clear();
        updateFolderHistory(folder_name, true);
    }

    private void writeSaveLocations() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(pref_base + "_size", save_location_history.size());
        for (int i = 0; i < save_location_history.size(); i++) {
            String string = save_location_history.get(i);
            editor.putString(pref_base + "_" + i, string);
        }
        editor.apply();
    }

    public int size() {
        return save_location_history.size();
    }

    public String get(int index) {
        return save_location_history.get(index);
    }

}
