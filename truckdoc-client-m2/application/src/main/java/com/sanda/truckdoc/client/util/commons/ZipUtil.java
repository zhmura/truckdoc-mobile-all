package com.sanda.truckdoc.client.util.commons;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ch.qos.logback.core.util.CloseUtil;

public class ZipUtil {
    private static final int BUFFER = 2048;

    public static void zip(String[] files, String zipFile) {
        try {
            BufferedInputStream origin;
            FileOutputStream dest = new FileOutputStream(zipFile);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            byte[] data = new byte[BUFFER];

            for (String file : files) {
                Log.v("Compress", "Adding: " + file);
                FileInputStream fi = new FileInputStream(file);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(file.substring(file.lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                CloseUtil.closeQuietly(origin);
            }
            CloseUtil.closeQuietly(out);
        } catch (Exception e) {
            Log.e("Compress issue", e.getMessage());
        }

    }

} 