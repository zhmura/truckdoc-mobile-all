package com.sanda.truckdoc.client.util;

import android.content.ContextWrapper;
import android.os.AsyncTask;

import com.sanda.truckdoc.client.util.commons.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import timber.log.Timber;

public class UpdateClientInfoHelper {
    private static class PropertiesHelperTaskParams {
        String key;
        Object value;

        PropertiesHelperTaskParams(String key, Object value) {
            this.key = key;
            this.value = value;
        }
    }

    private class PropertiesWriteHelperTask extends AsyncTask<PropertiesHelperTaskParams, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(PropertiesHelperTaskParams... params) {
            FileOutputStream fileOut = null;
            FileInputStream fileIn = null;
            try {
                Properties properties = new Properties();
                File directory = FileHelper.getParentDirectory();
                File propertiesFile = new File(directory, ".shared.properties");
                if (propertiesFile.exists()) {
                    fileIn = new FileInputStream(propertiesFile);
                    properties.load(fileIn);
                } else {
                    if (!propertiesFile.createNewFile()) {
                        Timber.e("Failed to create properties file");
                        return false;
                    }
                }
                properties.put(params[0].key, params[0].value);
                fileOut = new FileOutputStream(propertiesFile);
                properties.store(fileOut, "Properties updated");
            } catch (Exception e) {
                Timber.e(e, "Can not set update client info flag");
                return false;
            } finally {
                IOUtils.closeQuietly(fileIn);
                IOUtils.closeQuietly(fileOut);
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
        }
    }

    public boolean setTargetPackageForUpdate(ContextWrapper context) {
        PropertiesHelperTaskParams params = new PropertiesHelperTaskParams("clientTargetPackage", context.getPackageName());
        PropertiesWriteHelperTask task = new PropertiesWriteHelperTask();
        try {
            return task.execute(params).get();
        } catch (Exception e) {
            return false;
        }
    }
}