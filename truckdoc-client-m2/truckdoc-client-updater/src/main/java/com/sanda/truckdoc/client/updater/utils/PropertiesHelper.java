package com.sanda.truckdoc.client.updater.utils;

import android.os.Environment;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import timber.log.Timber;

/**
 * Created by Sergey Zhmura on 9/13/2016.
 */

public class PropertiesHelper {

    private static final String PARENT_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath() + "/TruckDoc/";

    private class PropertiesHelperTaskParams {
        String key;
        Object value;

        PropertiesHelperTaskParams(String key, Object value) {
            this.key = key;
            this.value = value;
        }
    }

    public String getTargetPackageForUpdate() {
        FileInputStream fileIn = null;
        try {
            Properties properties = new Properties();
            File directory = new File(PARENT_DIRECTORY);
            File propertiesFile = new File(directory, ".shared.properties");
            if (propertiesFile.exists()) {
                fileIn = new FileInputStream(propertiesFile);
                properties.load(fileIn);
                return (String) properties.get("clientTargetPackage");
            } else {
                return null;
            }
        } catch (Exception e) {
            Timber.e(e, "Can not read client package");
            return null;
        } finally {
            IOUtils.closeQuietly(fileIn);
        }
    }
}
